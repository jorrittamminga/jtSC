/*
OSCPlayerJT — efficient player for recorded OSC streams
========================================================

Rewrite goals (vs. the old version):
  * Instant, exact seeking. Instead of estimating a byte position from a
    time/stdev model and then linearly scanning, we build a per-line INDEX
    once at load: two FloatArrays holding (relativeTime, byteOffset) for
    every line. Seeking is then a binary search on the time array followed
    by a single f.pos_(). O(log n) + one disk seek, exact to the line.
  * One clean playback Routine instead of the multi-Condition / double-fork
    scheduler. State lives in instance vars so the GUI and seek can poke it
    live.
  * Forward AND backward playback, variable speed (via a private TempoClock),
    a loop region, pause/resume, and jog (skip) buttons.

File format (one OSC message per line):
    <absTime>,/osc/address,<f1>,<f2>,...,<fN>
  - absTime is an absolute seconds stamp (e.g. from Main.elapsedTime),
    monotonically increasing across the file.
  - "nan" tokens are coerced to 0.0; spaces are stripped.

Usage:
    o = OSCPlayerJT.new("/path/to/recording.txt").gui;
    o.play;                 // start (forward, from current position)
    o.pause; o.resume;      // or o.togglePlay
    o.seek(12.5);           // jump to 12.5 s  (exact, instant)
    o.speed_(0.5);          // half speed  (2.0 = double, etc.)
    o.direction_(\backward);// reverse playback   (\forward to restore)
    o.setLoopRegion(8, 20); // loop between 8 s and 20 s
    o.loopOSC_(true);
    o.stop;

Index build is asynchronous (forked) so the GUI stays responsive on long
files; transport waits for `ready`.
*/

OSCPlayerJT2 {
	var <path, <>netaddr, <>latency, <>bufferSize;

	// ---- index (built once at load) ----
	var <indexTimes;      // FloatArray: per-line time, relative to file start
	var <indexPos;        // FloatArray: per-line byte offset in the file
	var <numLines = 0, <t0 = 0, <duration = 0;
	var <ready = false;

	// ---- playback state (read live by the Routine) ----
	var file, routine, <clock, pauseCond;
	var <playing = false, paused = false;
	var seekPending = false, seekTarget = 0;
	var <playIndex = 0, prevTime = 0, playGen = 0;
	var <direction = 1;          // 1 = forward, -1 = backward
	var <>loopOSC = true;
	var <loopStart = 0, <loopEnd = 0;
	var <speed = 1;
	var snapshotSpan = 48;       // lines to (re)send when scrubbing while stopped

	// ---- message cache: one contiguous window of parsed messages ----
	var cache, cacheStart = -1;

	// ---- gui ----
	var window, views, guiRoutine, guiOpen = false, scrubbing = false, lastScrub = 0;

	*new { arg path, netaddr, bufferSize = 64, latency = 0.2;
		^super.newCopyArgs(
			path.asString,
			netaddr ?? { NetAddr("localhost", 57120) },
			latency,
			bufferSize
		).init
	}

	init {
		clock = TempoClock.new(speed);
		pauseCond = Condition.new;
		this.buildIndex;
	}

	// =============================================================== index
	buildIndex {
		Routine {
			var f, p, line, comma, t, first = true, times, poss, count = 0, len;
			ready = false;
			// note: no `^` here — this runs later, after buildIndex has returned
			if (File.exists(path)) {
				times = FloatArray.new(8192);
				poss  = FloatArray.new(8192);
				f = File(path, "r");
				len = f.length;
				p = 0;
				while { p < len } {
					f.pos_(p);
					line = f.getLine(65536);
					if (line.notNil and: { line.size > 2 }) {
						comma = line.indexOf($,);
						if (comma.notNil and: { comma > 0 }) {
							t = line.copyRange(0, comma - 1).asFloat;
							if (first) { t0 = t; first = false };
							times = times.add(t - t0);
							poss  = poss.add(p);
							count = count + 1;
						};
					};
					p = f.pos;
					if ((count % 8192) == 0) {
						{ this.prUpdateStatus("indexing… " ++ (p / len * 100).round(1) ++ "%") }.defer;
						0.yield;   // let the GUI breathe on long files
					};
				};
				f.close;
				indexTimes = times;
				indexPos   = poss;
				numLines   = count;
				duration   = if (count > 0) { times[count - 1] } { 0 };
				loopStart  = 0;
				loopEnd    = (count - 1).max(0);
				ready = true;
				{ this.prOnReady }.defer;
			} {
				"OSCPlayerJT: file not found: %".format(path).warn;
				{ this.prUpdateStatus("file not found") }.defer;
			};
		}.play(AppClock);
	}

	// first index whose time is >= t (binary search; array is sorted)
	indexForTime { arg t;
		var lo = 0, hi = numLines - 1, mid;
		if (numLines == 0) { ^0 };
		t = t.clip(indexTimes[0], indexTimes[numLines - 1]);
		while { lo < hi } {
			mid = (lo + hi).div(2);
			if (indexTimes[mid] < t) { lo = mid + 1 } { hi = mid };
		};
		^lo
	}

	timeAt { arg i; ^indexTimes[i.clip(0, numLines - 1)] }

	// ============================================================= reading
	prEnsureOpen {
		if (file.isNil or: { file.isOpen.not }) { file = File(path, "r") };
	}

	// parse and cache a contiguous window [start .. start+len-1]
	prFillCache { arg start, len;
		var msgs;
		this.prEnsureOpen;
		start = start.clip(0, numLines - 1);
		len   = len.clip(1, numLines - start);
		msgs  = Array.new(len);
		//file.pos_(indexPos[start]);
		file.pos_(indexPos[start].asInteger);
		len.do {
			var raw = file.getLine(65536), parts;
			if (raw.notNil) {
				parts = raw.replace(" ", "").replace("nan", "0.0").split($,);
				// [address] ++ float values ; drop the leading timestamp (we
				// already have exact times from the index)
				msgs = msgs.add([parts[1]] ++ parts[2..].collect(_.asFloat));
			} {
				msgs = msgs.add(nil);
			};
		};
		cache = msgs;
		cacheStart = start;
	}

	// message at absolute line index i, refilling the cache in play direction
	prMessageAt { arg i;
		var local;
		if (cacheStart < 0 or: { i < cacheStart } or: { i >= (cacheStart + cache.size) }) {
			if (direction > 0) {
				this.prFillCache(i, bufferSize);
			} {
				this.prFillCache((i - bufferSize + 1).max(0), bufferSize);
			};
		};
		local = i - cacheStart;
		^if (local >= 0 and: { local < cache.size }) { cache[local] } { nil }
	}

	// resend the ~last frame ending at i so every channel updates when
	// scrubbing/seeking while not playing
	prSendSnapshot { arg i;
		var s = (i - snapshotSpan + 1).max(0);
		this.prFillCache(s, i - s + 1);
		(s .. i).do { |k|
			var m = cache[k - cacheStart];
			if (m.notNil) { netaddr.sendBundle(latency, m) };
		};
		cacheStart = -1;   // force a fresh fill when live playback resumes
	}

	// =========================================================== transport
	play {
		if (ready.not) { "OSCPlayerJT: still indexing…".warn; ^this };
		if (numLines == 0) { ^this };
		if (playing) { if (paused) { this.resume }; ^this };
		playing = true;
		paused  = false;
		playGen = playGen + 1;   // supersede any not-yet-exited routine
		playIndex = playIndex.clip(loopStart, loopEnd);
		prevTime  = indexTimes[playIndex];
		cacheStart = -1;
		this.prEnsureOpen;
		this.prSetPlayButton(true);

		routine = Routine {
			var nextIndex, msg, dt, t, wrapped, myGen = playGen;
			while { playing and: { myGen == playGen } } {

				// pause: hang here until resumed (or stopped)
				if (paused and: { playing }) {
					pauseCond.hang;
					prevTime = indexTimes[playIndex];  // avoid a catch-up wait
				};

				if (playing and: { myGen == playGen }) {
					// honour a pending seek before doing anything else
					if (seekPending) {
						playIndex  = seekTarget.clip(loopStart, loopEnd);
						prevTime   = indexTimes[playIndex];
						cacheStart = -1;
						seekPending = false;
					};

					// send the message at the current line
					msg = this.prMessageAt(playIndex);
					if (msg.notNil) { netaddr.sendBundle(latency, msg) };
					t = indexTimes[playIndex];
					prevTime = t;

					// choose the next line, honouring loop region + direction
					nextIndex = playIndex + direction;
					wrapped = false;
					if (nextIndex > loopEnd) {
						if (loopOSC) { nextIndex = loopStart; wrapped = true } { playing = false };
					};
					if (nextIndex < loopStart) {
						if (loopOSC) { nextIndex = loopEnd; wrapped = true } { playing = false };
					};

					if (playing) {
						dt = if (wrapped) {
							if (loopEnd > loopStart) { 0 } { 0.05 }  // 0.05 guards a 1-line loop
						} {
							(indexTimes[nextIndex] - t).abs
						};
						playIndex = nextIndex;      // advance before waiting so a
						// live seek can override it
						if (dt > 0) { dt.wait };
					};
				};
			};
			// exited: leave the file handle open for a quick restart.
			// only touch the button if we weren't superseded by a newer play.
			if (myGen == playGen) { this.prSetPlayButton(false) };
		}.play(clock);
	}

	pause {
		paused = true;
		this.prSetPlayButton(false);
	}

	resume {
		if (playing.not) { ^this.play };
		paused = false;
		pauseCond.unhang;
		this.prSetPlayButton(true);
	}

	togglePlay {
		if (playing.not) { ^this.play };
		if (paused) { this.resume } { this.pause }
	}

	stop {
		playing = false;
		paused  = false;
		pauseCond.unhang;      // let a hung routine fall through and exit
		this.prSetPlayButton(false);
	}

	seek { arg timeSecs;
		var i = this.indexForTime(timeSecs).clip(loopStart, loopEnd);
		playIndex  = i;
		seekTarget = i;
		seekPending = true;
		if (playing.not or: { paused }) {
			prevTime = indexTimes[i];
			cacheStart = -1;
			this.prSendSnapshot(i);   // feedback while stopped/paused
		};
		this.prUpdateTimeUI(i);
	}

	seekFrac { arg frac; this.seek(frac.clip(0, 1) * duration) }

	reset { this.seek(indexTimes[loopStart]) }

	// jog by a number of lines (works while playing or stopped)
	jog { arg deltaLines;
		var i = (playIndex + deltaLines).clip(loopStart, loopEnd);
		playIndex  = i;
		seekTarget = i;
		seekPending = true;
		if (playing.not or: { paused }) {
			prevTime = indexTimes[i];
			cacheStart = -1;
			this.prSendSnapshot(i);
		};
		this.prUpdateTimeUI(i);
	}

	direction_ { arg dir;
		direction = case
		{ dir == \forward }  { 1 }
		{ dir == \backward } { -1 }
		{ dir == \reverse }  { direction.neg }
		{ dir.isNumber }     { dir.sign }
		{ 1 };
		cacheStart = -1;   // cache window is direction-oriented
		this.prUpdateReverseUI;
	}

	forward  { this.direction_(1);  this.prResumeOrPlay }
	backward { this.direction_(-1); this.prResumeOrPlay }

	prResumeOrPlay {
		if (playing.not) { this.play } { if (paused) { this.resume } };
	}

	speed_ { arg x;
		speed = x.max(0.0001);
		clock.tempo_(speed);
		this.prUpdateSpeedUI;
	}

	setLoopRegion { arg startSecs, endSecs;
		var a = this.indexForTime(startSecs), b = this.indexForTime(endSecs);
		if (a > b) { var tmp = a; a = b; b = tmp };
		loopStart = a.clip(0, numLines - 1);
		loopEnd   = b.clip(0, numLines - 1);
		playIndex = playIndex.clip(loopStart, loopEnd);
		this.prUpdateLoopUI;
	}

	setLoopFrac { arg loFrac, hiFrac;
		this.setLoopRegion(loFrac.clip(0, 1) * duration, hiFrac.clip(0, 1) * duration)
	}

	free {
		this.stop;
		if (file.notNil and: { file.isOpen }) { file.close };
		guiOpen = false;
		if (window.notNil) { window.close };
	}

	// ================================================================= gui
	gui {
		{
			var w, width = 320, pad = 4;
			w = Window("OSCPlayer — " ++ PathName(path).fileNameWithoutExtension,
				Rect(300, 300, width + (2 * pad), 300)).front;
			window = w;
			w.addFlowLayout;
			w.alwaysOnTop_(true);
			w.onClose_{
				guiOpen = false;
				this.stop;
				if (file.notNil and: { file.isOpen }) { file.close };
			};
			views = ();

			views[\filename] = StaticText(w, width @ 18).align_(\left)
			.string_(PathName(path).fileNameWithoutExtension);

			views[\time] = StaticText(w, 150 @ 20).align_(\left)
			.stringColor_(Color.blue).string_(0.asTimeString);
			views[\duration] = StaticText(w, (width - 154) @ 20).align_(\right)
			.string_(duration.asTimeString);

			// scrub / progress
			views[\progress] = Slider(w, width @ 20)
			.background_(Color.white).knobColor_(Color.blue)
			.mouseDownAction_{ scrubbing = true }
			.action_{ |sl|
				var now = Main.elapsedTime;
				// live scrub, throttled to ~25 Hz for the snapshot sends
				if ((now - lastScrub) > 0.04) {
					lastScrub = now;
					this.seekFrac(sl.value);
				};
			}
			.mouseUpAction_{ |sl|
				scrubbing = false;
				this.seekFrac(sl.value);
			};

			// transport row
			views[\play] = Button(w, 70 @ 22)
			.states_([
				["play",  Color.black, Color.green(0.8)],
				["pause", Color.black, Color.red(1, 0.5)]
			])
			//.action_{ |b| if (b.value == 1) { this.play } { this.pause } };
			.action_{ this.togglePlay };
			views[\stop] = Button(w, 60 @ 22)
			.states_([["stop"]])
			.action_{ this.stop; this.reset };

			views[\reverse] = Button(w, 70 @ 22)
			.states_([
				["fwd >"],
				["< rev", Color.black, Color.yellow]
			])
			.action_{ |b| this.direction_(if (b.value == 1) { \backward } { \forward }) };

			views[\loop] = Button(w, 60 @ 22)
			.states_([
				["loop"],
				["LOOP", Color.black, Color.yellow]
			])
			.value_(loopOSC.binaryValue)
			.action_{ |b| loopOSC = (b.value > 0) };

			// speed
			views[\speed] = EZSlider(w, width @ 20, "speed",
				ControlSpec(0.05, 4, \exp, 0, 1),
				{ |ez| this.speed_(ez.value) }, 1);

			// loop region
			views[\range] = RangeSlider(w, width @ 20).knobColor_(Color.green(0.7))
			.lo_(0).hi_(1)
			.action_{ |sl|
				views[\startTxt].string_((sl.lo * duration).asTimeString);
				views[\endTxt].string_((sl.hi * duration).asTimeString);
			}
			.mouseUpAction_{ |sl| this.setLoopFrac(sl.lo, sl.hi) };

			views[\startTxt] = StaticText(w, (width / 2 - 2) @ 18).align_(\left)
			.string_(0.asTimeString);
			views[\endTxt] = StaticText(w, (width / 2 - 2) @ 18).align_(\right)
			.string_(duration.asTimeString);

			// jog row
			views[\skipBack] = Button(w, 60 @ 22).states_([["<<"]])
			.action_{ this.jog(views[\skipSize].value.asInteger.neg) };
			views[\skipFwd] = Button(w, 60 @ 22).states_([[">>"]])
			.action_{ this.jog(views[\skipSize].value.asInteger) };
			views[\skipSize] = EZNumber(w, 120 @ 20, "jog lines",
				ControlSpec(1, 5000, \lin, 1), nil, 128);

			views[\status] = StaticText(w, width @ 18).align_(\left)
			.stringColor_(Color.gray).string_(if (ready) { "ready" } { "indexing…" });

			guiOpen = true;
			this.prStartGuiRoutine;
			if (ready) { this.prOnReady };
		}.defer;
		^this
	}

	prStartGuiRoutine {
		guiRoutine = Routine {
			while { guiOpen } {
				if (scrubbing.not and: { views.notNil and: { ready } }) {
					var i = playIndex;
					views[\time].string_(indexTimes[i].asTimeString);
					views[\progress].value_(if (duration > 0) { indexTimes[i] / duration } { 0 });
				};
				(1 / 30).wait;
			};
		}.play(AppClock);
	}

	// ---- deferred UI helpers (safe to call from any thread) ----
	prOnReady {
		if (views.isNil) { ^this };
		views[\duration].string_(duration.asTimeString);
		views[\endTxt].string_(duration.asTimeString);
		views[\status].string_("ready — % lines, %".format(numLines, duration.asTimeString));
	}

	prUpdateStatus { arg str;
		if (views.notNil) { views[\status].string_(str) };
	}

	prUpdateTimeUI { arg i;
		{
			if (views.notNil and: { ready }) {
				views[\time].string_(indexTimes[i].asTimeString);
				if (scrubbing.not) {
					views[\progress].value_(if (duration > 0) { indexTimes[i] / duration } { 0 });
				};
			};
		}.defer;
	}

	prSetPlayButton { arg isPlaying;
		{ if (views.notNil) { views[\play].value_(isPlaying.binaryValue) } }.defer;
	}

	prUpdateReverseUI {
		{ if (views.notNil) { views[\reverse].value_((direction < 0).binaryValue) } }.defer;
	}

	prUpdateSpeedUI {
		{ if (views.notNil) { views[\speed].value_(speed) } }.defer;
	}

	prUpdateLoopUI {
		{
			if (views.notNil and: { duration > 0 }) {
				views[\range].lo_(indexTimes[loopStart] / duration)
				.hi_(indexTimes[loopEnd] / duration);
				views[\startTxt].string_(indexTimes[loopStart].asTimeString);
				views[\endTxt].string_(indexTimes[loopEnd].asTimeString);
			};
		}.defer;
	}
}
