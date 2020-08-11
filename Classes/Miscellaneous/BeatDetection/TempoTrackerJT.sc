/*
*/
TempoTrackerJT {
	var <tempo, <bpm, <tempi;
	var <buffer, server, target, addAction;
	var <synth, <oscFunc, oscpath, args;
	var <cmdName, <cmdNameTempo, <onsets, <>gui, <id, tapTime, tapIndex;
	var lastTime;
	var <>action, prevIndex, index;


	*new {arg inBus=0, target, addAction=\addAfter, args=();
		^super.new.init(inBus, target, addAction, args)
	}

	init {arg arginBus, argtarget, argaddAction, argargs;
		var defaultArgs=(tempo:0.25, tempoDrift:0.95, tr:0.5, amp:0
			, freq:1000, odftype: \complex, duration:15.0, outBusMetronome:0
			, tempoLagTime: 0.1, fftSize:512, refreshTime: 2.5, method: \last
			, tempiSize:4
		);
		target = argtarget.asTarget;
		server = target.server;
		addAction=argaddAction;
		args=argargs??{()};
		defaultArgs.keysValuesDo{|key,val| if (args[key]==nil, {args[key]=val})};
		args[\inBus]=arginBus;
		id=UniqueID.next;

		action={};
		oscFunc=();
		buffer=();
		cmdName=();

		cmdName[\getFrame]=('/frame'++id).asSymbol;
		cmdName[\flash]=('/flash'++id).asSymbol;
		cmdName[\beat]=('/beat'++id).asSymbol;

		tapIndex=0;
		tempo=0.5;
		bpm=60/tempo;
		tapTime=Main.elapsedTime;
		lastTime=Main.elapsedTime;

		{
			buffer[\writeBuffer]=Buffer.alloc(server, server.sampleRate*30);
			server.sync;
			buffer[\tmp]=Buffer.alloc(server, server.sampleRate*args[\duration]);
			server.sync;
			args[\bufnum]=buffer[\writeBuffer].bufnum;
			this.makeSynthDef;
			server.sync;
			//
			this.makeSynth;
		}.fork
	}

	free {
		if (synth.isPlaying, {synth.free});
		oscFunc.do{|osc| osc.free};
		if (gui.class==TempoTrackerJTGUI, {gui.close})
	}
	close { this.free }

	reset {
		tempi=[];
	}

	sync {
		if (synth.isPlaying, {
			synth.set(\t_reset, 1)
		})
	}

	tap {
		var time=Main.elapsedTime;
		if ((time-tapTime<2.0)&&(time-tapTime>0.25), {
			if (tempi.size<tapIndex, {
				tempi[tapIndex]=time-tapTime;
			},{
				tempi=tempi.add(time-tapTime)
			});
			tapIndex=tapIndex+1%args[\tempiSize];
			tempo=tempi.mean;
			args[\tempo]=tempo;
			action.value(tempo);
			if (synth.isPlaying, {synth.set(\tempo, tempo, \t_reset, 1)});
			//synth.free;
			//this.makeSynth;
		},{
			if (synth.isPlaying, {synth.set(\t_reset, 1)});
		});
		tapTime=time
	}

	set { arg ... aargs;
		aargs.clump(2).do{arg x;
			args[x[0]]=x[1]
		};
		if (synth.isPlaying, {
			synth.set(*aargs);
		})
	}

	start {
		if (oscFunc[\getFrame]==nil, {
			this.makeOSCFuncs;
			{
				//this.makeSynth;
				args[\refreshTime].wait;
				if (synth.isPlaying, {
					synth.set(\t_trig, 1);
				});
			}.fork
		},{
			"TempoTracker already started".postln;
		})
	}

	stop {
		oscFunc[\getFrame].free;
		oscFunc[\getFrame]=nil;
	}

	makeOSCFuncs {
		var tmpbpm;
		oscFunc[\getFrame]=OSCFunc({arg msg;
			{
				buffer[\writeBuffer].copyDataWrap(buffer[\tmp], 0
					, (msg[3]-buffer[\tmp].numFrames)%buffer[\writeBuffer].numFrames
					, buffer[\tmp].numFrames);

				tmpbpm=buffer[\tmp].tempoTracker;
				tmpbpm=tmpbpm??{bpm};
				bpm=if (tmpbpm>160, {tmpbpm/2},{tmpbpm});
				//bpm=tmpbpm;//check ff of het niet veeeeeel te hoog is
				tempo=60/bpm;
				synth.set(\tempo, tempo);
				action.value(tempo);

				args[\refreshTime].wait;
				if (synth.isPlaying, {
					synth.set(\t_trig, 1);
				});
			}.fork
		}, cmdName[\getFrame]);
	}

	makeSynthDef {
		SynthDef(\TempoTracker, {arg t_trig, tempo=0.5, t_reset, decayTime=5.0, dur=0.0001
			, drift=0.95, inBus, bufnum, metronomeDrift=0.1, outBusMetronome, amp=0;
			var metronome, fft, onsets, trigger, resetTrigger, time;
			var in=In.ar(inBus);//PlayBuf.ar(1, b.bufnum, loop:1);
			var phase=Phasor.ar(1, 1, 0, buffer[\writeBuffer].numFrames);
			BufWr.ar(in, bufnum, phase);
			SendReply.kr(t_trig, cmdName[\getFrame], phase);
			fft=FFT(LocalBuf(512), in);
			onsets=Onsets.kr(fft);
			resetTrigger=LocalIn.kr(1);
			metronome=TDuty.kr(tempo, t_reset+resetTrigger, 1);
			onsets=Trig1.kr(onsets, SampleDur.ir);
			trigger=metronome+Trig1.kr(onsets, tempo*drift);
			time=Timer.kr(trigger);
			LocalOut.kr( Trig1.kr( time<metronomeDrift, 0.01));
			SendReply.kr(Trig1.kr(metronome, tempo*0.5), cmdName[\flash], tempo);
			metronome=Trig1.kr(Trig1.kr(metronome, tempo*0.5), SampleDur.ir);
			Out.ar(outBusMetronome, K2A.ar(metronome)*amp)
		}).send(server);
	}

	makeSynth {
		synth=Synth(\TempoTracker, args.asKeyValuePairs, target, addAction).register;
	}

	makeGUI {arg parent, bounds=350@40;
		gui=TempoTrackerJTGUI(this, parent, bounds);
		^gui
	}
}



TempoTrackerJTGUI {
	var beatDetectorJT, parent, bounds, <views, bbounds;
	var <>action, <>oscFunc;
	var <compositeView;

	*new {arg beatDetectorJT, parent, bounds=350@20;
		^super.new.init(beatDetectorJT, parent, bounds)
	}

	init {arg argbeatDetectorJT, argparent, argbounds;
		beatDetectorJT=argbeatDetectorJT;
		parent=argparent;
		bounds=argbounds;
		bbounds=(bounds.x/5).floor@(bounds.y/2);
		views=();
		if (parent==nil, {
			parent=Window("TempoTracker", Rect(200,200,bounds.x+8, bounds.y+8));
			parent.addFlowLayout(4@4, 0@0);
			parent.alwaysOnTop_(true);
			parent.front;
			parent.onClose_({});
		});
		compositeView=CompositeView(parent, bounds.x@(bounds.y*2));
		compositeView.addFlowLayout(0@0, 0@0);
		views[\start]=Button(compositeView, bbounds)
		.states_([ [\analyze],[\analyzing,Color.black,Color.green] ]).action_{|b|
			if (b.value==1, {
				beatDetectorJT.start
			},{
				beatDetectorJT.stop
			})
		};
		views[\tap]=Button(compositeView, bbounds).states_([ [\tap] ]).action_{
			beatDetectorJT.tap
		};
		views[\flash]=Button(compositeView, bbounds)
		.states_([ [\bpm ],[\bpm,Color.black,Color.yellow ] ]);
		views[\sync]=Button(compositeView, bbounds).states_([ [\resync] ]).action_{
			beatDetectorJT.sync
		};
		views[\reset]=Button(compositeView, bbounds).states_([ [\reset] ]).action_{
			beatDetectorJT.reset
		};
		views[\amp]=Slider(compositeView, bounds.x@bbounds.y).action_{arg sl;
			beatDetectorJT.set(\amp, sl.value)
		};
		views.do{|v| v.canFocus_(false)};
		oscFunc=OSCFunc({arg msg;
			{
				views[\flash].value_(1);
				0.1.wait;
				views[\flash].value_(0);
			}.fork(AppClock)
		}, beatDetectorJT.cmdName[\flash]);

		parent.onClose=parent.onClose.addFunc({
			oscFunc.free;
			beatDetectorJT.action=beatDetectorJT.action.removeFunc(action);
			beatDetectorJT.gui=nil;
			beatDetectorJT.free;
		});
		action={arg tempo;
			var bpm=(60.0/tempo).round(0.1);
			//[tempo, bpm].postln;
			{views[\flash].states_([ [bpm.asString],[bpm.asString, Color.black,Color.yellow]])}.defer;
		};

		beatDetectorJT.action=beatDetectorJT.action.addFunc(action);
	}
}
