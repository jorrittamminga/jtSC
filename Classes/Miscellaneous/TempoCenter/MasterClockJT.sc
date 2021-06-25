/*
Server.killAll
s.waitForBoot{m=MasterClockJT.new}
TempoClock
x={arg t_trig=1; SinOsc.ar(1000, 0,InTrig.kr(m.busTrigger).lag(0.0, 0.1))!2}.play(s, 1)
*/
MasterClockJT {
	var <clock, <synth, <>synthParameters, metronome, metronomeFunc, visualFunc, visual, flashTime=0.05, <server, <queue, tmpTempo=2.0, tmpBeat=1.0;
	var <cBus;
	var <parent, window, compositeview, views;
	var taptime=0, times;

	*new {arg server, clock, parent, bounds=350@20;
		^super.new.init(server, clock, parent, bounds)
	}

	init {arg argserver, argclock, argparent, argbounds;
		queue=[];
		synthParameters=(amp:0.0, freq:1000, outBus:0, pan:0.0);
		server=argserver??{Server.default};
		clock=argclock??{TempoClock.new(2.0, 2.0*server.latency, 0, 1024)};

		cBus=Bus.control(server, 1).set(clock.tempo);//tempoBus
		//tBus=Bus.control(server, 1);//triggerBus
		this.initMetronome;
		this.makeGui(argparent, argbounds);
	}
	initMetronome {
		SynthDef(\clickMasterClockJT, { |outBus=0, freq=1000, amp=0.2, pan=0.0|
			var sig = SinOsc.ar(freq, 0.5pi) *
			Env.perc(0.0,0.06).ar(Done.freeSelf, levelScale: amp);
			OffsetOut.ar(outBus, Pan2.ar(sig, pan))
		}).add;
		metronomeFunc={
			inf.do{arg i;
				server.sendBundle(server.latency, [\s_new, \clickMasterClockJT, -1, 1, 1]++synthParameters.asKeyValuePairs);
				if (server.latency!=nil, {
					AppClock.sched(server.latency, {compositeview.background_(Color.yellow)});
					AppClock.sched(server.latency+flashTime, {compositeview.background_(Color.grey)});
				});
				1.0.wait;
			};
		};
	}
	updateQueue {arg excludeMetronome=true;
		forBy(1, clock.queue.size-1, 3) {|i|
			//clock.queue[i+1].removedFromScheduler(true)
			var obj=clock.queue[i+1];
			if (obj.class==Function, {

			},{
				if (queue.includesEqual(obj).not, {
					queue=queue.add(obj)
				})
			})
		};
		if (excludeMetronome, {
			queue.remove(metronome);
		});
	}
	free {
		{
			this.updateQueue(false).do(_.stop);
			clock.beatDur.wait;
			clock.clear;
			clock.stop;
		}.fork;
	}
	pSync {
		var latency=server.latency??{0}, latencyBeats=latency*clock.tempo, beats=clock.beats, beatsi=beats.asInteger, beatsr=beats.round(0.5).asInteger, beatsrl=beatsr+latencyBeats;
		var tempo=clock.tempo;
		if (beatsrl-beats<latency, {
			clock.beats_(beatsrl);
			AppClock.sched(0, {compositeview.background_(Color.yellow)});
			AppClock.sched(flashTime, {compositeview.background_(Color.grey)});
		},{
			//"te groooot".postln;
		});
	}
	start {arg quant=1.0;
		this.updateQueue;
		{
			queue.do(_.play(quant:1))
		}.fork(clock)
	}
	restart {arg quant=1.0;
		this.updateQueue;
		{
			queue.do(_.stop); //1.0.wait;
			queue.do(_.reset);
			(1-clock.beats.frac).wait;
			queue.do(_.play(quant:1))
		}.fork(clock)
	}
	stop {arg quant=1.0;
		this.updateQueue;
		{
			(clock.nextTimeOnGrid(quant)-clock.beats).wait;
			queue.do(_.stop); //1.0.wait;
		}.fork(clock)
	}
	tap {arg taps=4, weight=0.0;
		var ttime=Main.elapsedTime;
		var delta=ttime-taptime, tempo;
		server.sendBundle(nil, [\s_new, \clickMasterClockJT, -1, 1, 1]++synthParameters.asKeyValuePairs);
		if (delta<1.5, {
			if (delta>0.1, {
				times=times.add(delta).keep(taps.neg);
				if (times.size>1, {
					tempo=times.meanWeighted( (1..times.size).pow(weight) ).reciprocal;
					//					clock.tempo_(tempo);
					clock.tempo_(tempo);
					this.pSync;
					{views[\bpm].value_(tempo*60)}.defer;
				},{
					this.pSync;
				})
			},{

			})
		},{
			this.pSync;
			times=[];
		});
		taptime=ttime;
	}
	startMetronome {arg tempo, startBeat=0.0;
		metronome=Routine(metronomeFunc).play(clock, startBeat, quant:1.0);
	}
	stopMetronome {
		metronome.stop;
	}
	//------------------------------------------------------------------------------------------ GUI
	makeGui {arg argparent, argbounds;
		var buttonWidth, font, fontSmall, fontBig;
		var bounds;
		bounds=argbounds;
		font=Font("Monoca", bounds.y*0.75);
		fontSmall=Font("Monaco", bounds.y*0.5);
		fontBig=Font("Monaco", bounds.y*1.25);
		if (argparent==nil, {
			parent=Window("MasterClock", Rect(400,400,bounds.x+8,(bounds.y+4*3)+12)).front;
			parent.addFlowLayout;
			parent.alwaysOnTop_(true);
			parent.onClose_{this.free};
			window=parent;
		},{
			parent=argparent;
			if (parent.class!=Window, {
				window=parent.findWindow;
			});
			parent.onClose=parent.onClose.addFunc({this.free});
		});
		compositeview=CompositeView(parent, bounds.x@(bounds.y+4*3+4));
		compositeview.addFlowLayout(4@4, 4@4);
		compositeview.background_(Color.grey);
		views=();
		buttonWidth=(bounds.x-8/5).floor-4;
		bounds=(bounds.x-8)@bounds.y;
		views[\tap]=Button(compositeview, buttonWidth@bounds.y).states_([ [\tap] ]).action_{
			this.tap
		}.font_(font);
		views[\bpm]=EZSlider(compositeview, (bounds.x-buttonWidth-4)@bounds.y, \bpm, ControlSpec(40, 200, \exp), {|ez| clock.tempo=(ez.value/60)}, 120, false, bounds.y*2).font_(font);
		compositeview.decorator.nextLine;
		views[\click]=Button(compositeview, buttonWidth@bounds.y).states_([ [\click], [\click, Color.black,Color.green] ]).action_{|b|
			if (b.value==1, {
				this.startMetronome;
			},{
				this.stopMetronome;
			});
		}.canFocus_(false).font_(font);
		views[\amp]=EZSlider(compositeview, (bounds.x-buttonWidth-4)@bounds.y, \amp, \amp, {|ez| synthParameters[\amp]=ez.value}, synthParameters[\amp], false, bounds.y*2).font_(font);
		views[\restart]=Button(compositeview, buttonWidth@bounds.y).states_([ [\restart] ]).action_{
			this.restart(views[\restartQuant].value)
		}.font_(font);
		views[\restartQuant]=EZNumber(compositeview, (buttonWidth*0.5)@bounds.y, nil, [0.0, 1.0], {|ez| }, 1.0, false, 0);
		views[\start]=Button(compositeview, buttonWidth@bounds.y).states_([ [\continue] ]).action_{
			this.start(views[\startQuant].value)
		}.font_(font);
		views[\startQuant]=EZNumber(compositeview, (buttonWidth*0.5)@bounds.y, nil, [0.0, 1.0], {|ez| }, 1.0, false, 0);
		views[\stop]=Button(compositeview, buttonWidth@bounds.y).states_([ [\stop] ]).action_{
			this.stop(views[\stopQuant].value)
		}.font_(font);
		views[\stopQuant]=EZNumber(compositeview, (buttonWidth*0.5)@bounds.y, nil, [0.0, 1.0], {|ez| }, 0.0, false, 0);
		compositeview.decorator.nextLine;
		/*
		views[\beat]=EZNumber(compositeview, bounds.x*0.2@bounds.y, \beats, ControlSpec(1, 100, 0, 1), {|ez| beats=ez.value}, beats, false, bounds.y*2).font_(fontSmall);
		views[\division]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,4,8,16,32]).action_{|l| division=l.items[l.value]}.value_(2).font_(fontSmall);
		views[\subdivision]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,3,4,5,6,7]).action_{|l| subdivision=l.items[l.value]}.value_(3).font_(fontSmall);
		views[\amp]=EZSlider(compositeview, bounds, \amp, \amp, {|ez| synth.set(\amp, ez.value)}, 0.0, false, bounds.y*2).font_(font);
		views[\time]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(time.asTimeString.copyToEnd(1)).font_(fontBig).align_(\left).stringColor_(Color.red(1.5))
		.background_(Color.black);
		views[\beats]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(beat.asBeatsString(beats, division, subdivision, resolution))
		.font_(fontBig).align_(\left).stringColor_(Color.blue(1.5))
		.background_(Color.black);
		*/
	}

	/*
	*new {arg parent, bounds=350@20, outBus=0, target=Server.default, addAction=\addToHead;
	^super.new.init(parent, bounds, outBus, target, addAction)
	}

	init {arg argparent, argbounds, argoutBus, argtarget, argaddAction;
	this.makeGui(argparent, argbounds);
	target=argtarget.asTarget;
	addAction=argaddAction;
	server=target.server;
	//appClock=AppClock;
	if (server==nil, {server=Server.default});
	if (argoutBus.class==Bus, {outBus=argoutBus; if (server==nil, {server=outBus.server})},{
	if (server==nil, {server=Server.default});
	outBus=argoutBus.asBus('audio', 1, server);
	});
	this.beatDur_(nil);
	this.resolution_(nil);
	busBeatDur=Bus.control(server, 1).set(beatDur);//bus with the beatDur in seconds
	busTrigger=Bus.control(server, 1);//bus with the t_trig on the beat
	func={
	ticks=0;
	AppClock.clear;
	1.do{
	var timeDefer=time, beatDefer=beat;
	server.sendBundle(nil, [\n_set, synth.nodeID, \t_trig, 1],[\c_set, busTrigger.index, 1]);
	{
	views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
	views[\beats].string_(beatDefer.asBeatsString(beats, division, subdivision, resolution));
	{compositeview.background_(Color.yellow); flashTime.wait; compositeview.background_(Color.grey)}.fork(AppClock)
	}.defer;
	(beatDur-latency).wait;
	ticks=0;
	time=time+beatDur;
	beat=beat+1;
	};
	inf.do{arg i;
	var timeDefer, beatDefer;
	timeDefer=time;
	beatDefer=beat;
	if (ticks%resolution==0, {
	server.sendBundle(latency,[\c_set, busTrigger.index, 1], [\n_set, synth.nodeID, \t_trig, 1]);
	beatFunc.value(this, beatDefer, timeDefer);
	AppClock.sched(latency, {
	compositeview.background_(Color.yellow);
	views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
	views[\beats].string_(beatDefer.asBeatsString(beats, division, subdivision, resolution));
	});
	AppClock.sched(latency+flashTime, {
	compositeview.background_(Color.grey)
	});
	},{
	AppClock.sched(latency, {
	views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
	views[\beats].string_(beatDefer.asBeatsString(beats, division, subdivision, resolution));
	});
	});
	time=time+beatDurR;
	beat=beat+resolutionR;
	ticks=ticks+1;
	beatDurR.wait;
	}
	};
	if (server.serverRunning, {
	this.makeSynth
	},{
	server.waitForBoot{
	this.makeSynth
	}
	});
	}
	outBus_ {arg bus;
	outBus=bus??{outBus};
	synth.set(\outBus, outBus);
	}
	resolution_ {arg res;
	resolution=res??{resolution};
	resolutionR=resolution.reciprocal;
	beatDurR=beatDur*resolutionR;
	}
	free {
	fork.stop;
	busBeatDur.free;
	busTrigger.free;
	synth.free;
	}

	start {
	if (fork.isPlaying, {this.resync},{fork=Routine(func).play});
	}
	reset {
	time=0.0;
	beat=0;
	ticks=0;
	{
	views[\time].string_(time.asTimeString);
	views[\beats].string_(beat.asBeatsString(beats, division, subdivision, resolution));
	}.defer
	}
	stop {
	fork.stop
	}
	makeSynth {
	synth={arg freq=1000, t_trig, dur=0.05, amp=0.0, outBus, az=0.0;
	//Out.kr(busTrigger.index, t_trig);
	OffsetOut.ar(outBus, Pan2.ar(SinOsc.ar(freq, 0, t_trig.lag(0.0, dur)*amp.lag(0.1)), az))
	}
	.play(target, 0, 0.02, addAction, [\outBus, outBus.index]);
	}
	bps_ {arg beatspersecond;

	}
	addFunc {arg func;
	beatFunc=beatFunc.addFunc(func)
	}
	removeFunc {arg func;
	beatFunc=beatFunc.removeFunc(func)
	}
	oneShot {arg func;
	var func2={arg time; func.value(time); beatFunc=beatFunc.removeFunc(func2) };
	this.addFunc(func2);
	}
	setbpm {arg beatsperminute;
	bpm=beatsperminute??{bpm};
	bps=bpm/60;
	beatDur=60/bpm;
	busBeatDur.set(beatDur);
	beatDurR=beatDur*resolutionR;
	}
	bpm_ {arg beatsperminute;
	this.setbpm(beatsperminute);
	{views[\bpm].value_(bpm)}.defer;
	}
	beatDur_ {arg time;
	beatDur=time??{beatDur};
	bps=beatDur.reciprocal;
	bpm=bps*60;
	busBeatDur.set(beatDur);
	beatDurR=beatDur*resolutionR;
	{views[\bpm].value_(bpm)}.defer;
	}
	tap {
	var ttime=Main.elapsedTime;
	var delta=ttime-taptime;
	this.resync;
	if (delta<2.0, {
	times.add(delta);
	this.beatDur_(times.mean);
	},{
	//beatDur=5.0;
	times=List[];
	});
	taptime=ttime;
	}
	resync {
	fork.stop;
	beat=beat.ceil;
	fork=Routine(func).play;
	if (views[\start].value==0, { {views[\start].value_(1)}.defer});
	}
	//--------------------------------------------------------------------------------------------------- MIDI
	/*
	p[\midi]=();
	p[\midi][\smpte]=0!8; p[\midi][\logicTime]; p[\midi][\time]={Main.elapsedTime}!8;

	MIDIIn.smpte={arg ...args; var prev=p[\part].deepCopy, color;
	p[\midi][\smpte][7-args[1]]=args[2];
	if (args[1]==7, {p[\midi][\smpte][0]=0;
	p[\midi][\logicTime]=p[\midi][\smpte].clump(2).collect{|i| i.sum};
	p[\time]=(p[\midi][\logicTime]*[3600,60,1,1/25]).sum;
	p[\part]=p[\parts][([0, 120, 900, 1560].indexInBetween(p[\time]+p[\preDelay]).asInteger)];
	if (p[\part]!=prev, {
	(p[\path]++"settings/"++p[\part]++".scd").load.value(p);
	{
	p[\gui][\main][\part].string_(p[\part].asString);
	color=(prologue: Color.black, blue: Color.blue, red: Color.red, epilogue: Color.black)[p[\part]];
	"background color ".post; color.postln;
	w.background_(color);
	v[\main].background_(color);
	v[\io].background_(color);
	"in midi_setup.scd".postln;
	//p[\gui][\main][\update].valueAction_(1);
	}.defer;
	});
	{
	p[\gui][\midi][\time].string= p[\time].asTimeString;
	p[\gui][\midi][\progress].value_(p[\time]/p[\totalTime]);
	}.defer;
	});
	};

	p[\midi][\isPlaying]=false;
	p[\midi][\isResetted]=false;

	MIDIIn.sysrt={arg ...args;
	switch(args[1], 10, {
	p[\midi][\isPlaying]=true;
	p[\midi][\isResetted]=false;
	{
	p[\gui][\midi][\play].valueAction_(1);
	"gui midi play 10".postln;
	}.defer;
	}, 11, {
	{
	}.defer;
	{
	p[\gui][\midi][\pause].valueAction_(0); p[\gui][\midi][\play].valueAction_(1);
	"gui midi play 11".postln;
	}.defer;
	}, 12, {
	p[\midi][\isPlaying]=false;
	{p[\gui][\midi][\pause].valueAction_(1);}.defer;
	//p[\gui][\midi][\play].valueAction_(0);
	"gui midi play 12".postln;
	},2, {
	if ( p[\midi][\isPlaying].not && (args[2]==0) && (p[\midi][\isResetted].not), {
	{p[\gui][\midi][\pause].valueAction_(0);p[\gui][\midi][\play].valueAction_(0);}.defer;
	p[\midi][\isResetted]=true
	});
	"gui midi play 2".postln;
	})
	};
	*/
	//--------------------------------------------------------------------------------------------------- GUI
	makeGui {arg argparent, argbounds;
	var buttonWidth, font, fontSmall, fontBig;
	bounds=argbounds;
	font=Font("Monoca", bounds.y*0.75);
	fontSmall=Font("Monaco", bounds.y*0.5);
	fontBig=Font("Monaco", bounds.y*1.25);
	if (argparent==nil, {
	parent=Window("Metronome", Rect(400,400,bounds.x+8,(bounds.y+4*5)+12)).front;
	parent.addFlowLayout;
	parent.alwaysOnTop_(true);
	parent.onClose_{this.free};
	window=parent;
	},{
	parent=argparent;
	window=parent.findWindow;
	parent.onClose=parent.onClose.addFunc({this.free});
	});
	compositeview=CompositeView(parent, bounds.x@(bounds.y+4*5+4));
	compositeview.addFlowLayout(4@4, 4@4);
	compositeview.background_(Color.grey);
	views=();
	buttonWidth=(bounds.x-8/5).floor-4;
	bounds=(bounds.x-8)@bounds.y;
	views[\start]=Button(compositeview, buttonWidth@bounds.y).states_([ [\start], [\stop, Color.black,Color.green] ]).action_{|b|
	if (b.value==1, {
	this.start;
	},{
	this.stop;
	});
	}.canFocus_(false).font_(font);
	views[\reset]=Button(compositeview, buttonWidth@bounds.y).states_([ [\reset] ]).action_{
	this.reset;
	}.font_(font);
	views[\tap]=Button(compositeview, buttonWidth@bounds.y).states_([ [\tap] ]).action_{
	this.tap;
	}.font_(font);
	views[\resync]=Button(compositeview, buttonWidth@bounds.y).states_([ [\resync] ]).action_{
	this.resync;
	}.font_(font);
	views[\bpm]=EZNumber(compositeview, bounds.x*0.35@bounds.y, \bpm, ControlSpec(40, 200, \exp), {|ez| this.setbpm(ez.value)}, bpm, false, bounds.y*2).font_(font);
	views[\beat]=EZNumber(compositeview, bounds.x*0.2@bounds.y, \beats, ControlSpec(1, 100, 0, 1), {|ez| beats=ez.value}, beats, false, bounds.y*2).font_(fontSmall);
	views[\division]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,4,8,16,32]).action_{|l| division=l.items[l.value]}.value_(2).font_(fontSmall);
	views[\subdivision]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,3,4,5,6,7]).action_{|l| subdivision=l.items[l.value]}.value_(3).font_(fontSmall);
	views[\amp]=EZSlider(compositeview, bounds, \amp, \amp, {|ez| synth.set(\amp, ez.value)}, 0.0, false, bounds.y*2).font_(font);
	views[\time]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(time.asTimeString.copyToEnd(1)).font_(fontBig).align_(\left).stringColor_(Color.red(1.5))
	.background_(Color.black);
	views[\beats]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(beat.asBeatsString(beats, division, subdivision, resolution))
	.font_(fontBig).align_(\left).stringColor_(Color.blue(1.5))
	.background_(Color.black);
	}
	*/
}