+ String {

	findOnsets {arg ws=512, tr=0.5, relaxtime=1.0, s, fileDelete=false, action;
		var path="~/";
		var resultbuf, resultpath, oscpath, score, dur, sf, cond, size, data, o;
		var deleteTmp=false;
		//var path=thisProcess.nowExecutingPath.dirname++"/";

		s=s??{Server.default};

		if (this=="tmp", {

		},{
		sf = SoundFile.openRead(this);
		dur = sf.duration;
		sf.close;
		});
		resultpath = (path++ "tmp.aiff").absolutePath;
		oscpath = (path++ "tmp.osc").absolutePath;

		score = Score([
			[0, (resultbuf = Buffer.new(s, 1000, 1, 0)).allocMsg],
			[0, [\d_recv, SynthDef(\onsets, {
				var sig = SoundIn.ar(0), // will come from NRT input file
				sig2=BufCombN.ar(LocalBuf(dur*SampleRate.ir), sig, dur, 100000, 1, sig),
				//sig2=sig,
				fft = FFT(LocalBuf(ws, 1), sig2),
				//	fft1024=FFT(LocalBuf(1024), sig2),
				trig = Onsets.kr(fft, tr, 'rcomplex', 0.01),
				i = PulseCount.kr(trig),
				timer = Sweep.ar(1);
				//	var tempo;
				//	tempo=BeatTrack.kr(fft1024)[3];
				// 'i' must be audio-rate for BufWr.ar
				//	trig=Changed.kr(tempo);
				i=PulseCount.kr(trig);
				BufWr.ar(timer, resultbuf, K2A.ar(i), loop: 0);
				BufWr.kr(i, resultbuf, DC.kr(0), 0);  // # of points in index 0
			}).asBytes]],
			[0, Synth.basicNew(\onsets, s, 1000).newMsg],
			[dur, resultbuf.writeMsg(resultpath, headerFormat: "AIFF", sampleFormat: "float")]
		]);

		cond = Condition.new;

		o=ServerOptions.new;
		o.memSize = 2**18;
		o.maxSynthDefs = 2**18;

		// osc file path, output path, input path - input is soundfile to analyze
		score.recordNRT(oscpath, "/dev/null", sf.path, sampleRate: sf.sampleRate,
			options: o
			.verbosity_(-1)
			.numInputBusChannels_(sf.numChannels)
			.numOutputBusChannels_(sf.numChannels)
			.sampleRate_(sf.sampleRate),
			action: { cond.unhang }  // this re-awakens the process after NRT is finished
		);
		cond.hang;  // wait for completion
		sf = SoundFile.openRead(resultpath);
		sf.readData(size = FloatArray.newClear(1));
		size = size[0];
		sf.readData(data = FloatArray.newClear(size));
		sf.close;
		File.delete(oscpath);
		File.delete(resultpath);
		if (fileDelete, {File.delete(this)});
		//		data=data.collect{|d| if (d>2.5, {d/2},{d})};
		//		data.mean.postln;
		//		data.stdev.postln;
		action.value;
		^(data-(ws/2/sf.sampleRate))
	}

	analyseRhythm {arg s, minDuration=15, path="~/";
		var resultbuf, resultpath, oscpath, score, dur, sf, cond, size, data;
		//var path=thisProcess.nowExecutingPath.dirname++"/";

		s=s??{Server.default};

		sf = SoundFile.openRead(this);
		dur = sf.duration;
		sf.close;
		resultpath = (path++ "tmp.aiff").absolutePath;
		oscpath = (path++ "tmp.osc").absolutePath;

		score = Score([
			[0, (resultbuf = Buffer.new(s, 1000, 1, 0)).allocMsg],
			[0, [\d_recv, SynthDef(\onsets, {
				var sig = SoundIn.ar(0), // will come from NRT input file
				sig2=BufCombN.ar(LocalBuf(dur*SampleRate.ir), sig, dur, 100000, 1, sig),
				fft = FFT(LocalBuf(512, 1), sig2),
				fft1024=FFT(LocalBuf(1024), sig2),
				trig = Onsets.kr(fft),
				i = PulseCount.kr(trig),
				timer = Sweep.ar(1);
				var tempo;
				tempo=BeatTrack.kr(fft1024)[3];
				// 'i' must be audio-rate for BufWr.ar
				trig=Changed.kr(tempo);
				i=PulseCount.kr(trig);
				BufWr.ar(K2A.ar(tempo), resultbuf, K2A.ar(i), loop: 0);
				BufWr.kr(i, resultbuf, DC.kr(0), 0);  // # of points in index 0
			}).asBytes]],
			[0, Synth.basicNew(\onsets, s, 1000).newMsg],
			[dur.max(minDuration), resultbuf.writeMsg(resultpath, headerFormat: "AIFF", sampleFormat: "float")]
		]);

		cond = Condition.new;

		// osc file path, output path, input path - input is soundfile to analyze
		score.recordNRT(oscpath, "/dev/null", sf.path, sampleRate: sf.sampleRate,
			options: ServerOptions.new
			.verbosity_(-1)
			.numInputBusChannels_(sf.numChannels)
			.numOutputBusChannels_(sf.numChannels)
			.sampleRate_(sf.sampleRate),
			action: { cond.unhang }  // this re-awakens the process after NRT is finished
		);
		cond.hang;  // wait for completion
		sf = SoundFile.openRead(resultpath);
		sf.readData(size = FloatArray.newClear(1));
		size = size[0];
		sf.readData(data = FloatArray.newClear(size));
		sf.close;
		File.delete(oscpath); File.delete(resultpath);

		data=data.collect{|d| if (d>2.5, {d/2},{d})};
		//		data.mean.postln;
		//		data.stdev.postln;
		^data
	}
}