/*
- maak de array waarin je wegschrijft twee keer zolang als de duration (de lengte van de analyse)
-
*/
BeatDetectorJT {
	var <tempo, <bpm, <tempi;
	var buffer, server, target, addAction;
	var <synth, <oscFunc, oscpath, args;
	var <cmdName, <cmdNameTempo, <onsets, <>gui, <id, tapTime, tapIndex;
	var lastTime;
	var <>action, prevIndex, index, indexDelta, indexTime;

	*new {arg inBus=0, target, addAction=\addAfter, args=();
		^super.new.init(inBus, target, addAction, args)
	}

	init {arg arginBus, argtarget, argaddAction, argargs;
		var defaultArgs=(tempo:0.25, tempoDrift:0.95, tr:0.5, overlap:4, amp:0
			, freq:1000, odftype: \complex, duration:15.0, tempiSize:4, outBusMetronome:0
			, tempoLagTime: 0.1, fftSize:512, highestDivision:5, divisionThresh:2.5
			, clusterThresh:1.1, refreshTime: 2.0
		);
		target = argtarget.asTarget;
		server = target.server;
		addAction=argaddAction;
		args=argargs??{()};
		defaultArgs.keysValuesDo{|key,val| if (args[key]==nil, {args[key]=val})};
		args[\inBus]=arginBus;
		id=UniqueID.next;
		cmdName=('/t_trig'++id).asSymbol;
		cmdNameTempo=('/tempo'++id).asSymbol;
		index=1.neg;
		prevIndex=0;
		tapIndex=0;
		tempo=1.0;
		indexDelta=0;
		indexTime=0;

		onsets=tempo!1024;
		tapTime=Main.elapsedTime;
		lastTime=Main.elapsedTime;
		action={};
		{
			this.makeSynthDef;
			server.sync;
			this.makeOSCFunc;
			//this.makeSynth;
		}.fork
	}

	free {
		synth.free;
		oscFunc.free;
		onsets=[];
		index=1.neg;
		prevIndex=0;
		if (gui.class==BeatDetectorJTGUI, {gui.close})
	}
	close { this.free }

	reset {
		tempo=1.0;
		tempi=[];
		onsets=tempo!1024;
		index=1.neg;
		prevIndex=0;
	}

	sync {
		synth.set(\t_trig, 1)
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
			//synth.free;
			//this.makeSynth;
		});
		tapTime=time
	}

	set { arg ... aargs;
		aargs.clump(2).do{arg x;
			args[x[0]]=x[1]
		};
		synth.set(*aargs);
	}

	start {
		this.makeSynth;
	}

	stop {
		synth.free;
	}

	makeOSCFunc {
		var tmpTempo, drift, tempiClusters;
		var tmponsets;
		var deltaTime=0, dur=0, i=0, t;
		oscFunc=OSCFunc({arg msg;

			index=index+1%onsets.size;
			onsets[index]=msg[3];
			deltaTime=deltaTime+msg[3];
			//tmponsets=onsets.copyRangeWrap(prevIndex, index);

			if (deltaTime>=args[\refreshTime], {
				deltaTime=0;
				i=index;
				dur=0;
				while({dur<args[\duration]},{
					dur=dur+onsets.wrapAt(i);
					i=i-1;
				});
				/*
				prevIndex=tmponsets.integrate.indexOfGreaterThan(
				args[\refreshTime])+prevIndex%onsets.size;
				*/
				tmponsets=onsets.copyRangeWrap(i, index);
				tmponsets.postcs;

				/*
				t=(tmponsets.integrate/tmponsets.sum).collect{|i|
					i.linlin(0.0, 1.0, 1, 4).round(1.0).asInteger};
				tmponsets=tmponsets.collect{arg v,i; v.dup(t[i])}.flat;
				*/

				tempo=tmponsets
				.tempo(tempo, args[\clusterThresh], args[\divisionThresh]
					, args[\highestDivision], 0.0, true);
				/*
				if (tempi.size<args[\tempiSize], {
				tempo=tmpTempo;
				tempi=tempi.add(tempo);
				},{
				tempiClusters=tempi.copy.clumpsClusters(1.1);
				drift=[tempiClusters[0].mean, tmpTempo].sort;
				"tmpTempo ".post; [tmpTempo, 60/tmpTempo].postln;
				//"drift: ".post; (drift[0]/drift[1]).postln;
				if (drift[0]/drift[1]>=args[\tempoDrift], {
				//"dus verander het tempo!".postln;
				tempo=tmpTempo;
				tempi=(tempi.copy.copyToEnd(1))++tempo;
				});
				});
				*/
				bpm=60/tempo;
				args[\tempo]=tempo;
				"tempo send is ".post; tempo.postln; "".postln;
				//"set the tempo at ".post; tempo.postln;
				action.value(tempo);
				synth.set(\t_gate, 1, \tempo, tempo);
			});
		}, cmdName);
	}

	makeSynthDef {
		SynthDef(\BeatDetector, {arg inBus, outBus, outBusMetronome=0
			, bufnum=10, tempo=0.25, t_trig=1, tempoDrift=0.95, tr=0.5
			, overlap=4, amp=1, freq=1000, t_gate, tempoLagTime=1, metronomeDrift=0.1;
			var fft, onsets;
			var in, metronome, pc, fb, time, resetTrigger, trigger;
			in=In.ar(inBus);
			fft=FFT(LocalBuf(args[\fftSize]), in);
			onsets=Onsets.kr(fft, tr, odftype:args[\odftype]);
			time=Timer.kr(onsets);
			SendReply.kr(onsets, cmdName, time);
			resetTrigger=LocalIn.kr(1);
			metronome=TDuty.kr(tempo, t_trig+resetTrigger, 1);
			onsets=Trig1.kr(onsets, SampleDur.ir);
			trigger=metronome+Trig1.kr(onsets, tempo*tempoDrift);
			time=Timer.kr(trigger);
			LocalOut.kr( Trig1.kr( time<metronomeDrift, 0.01));
			Out.kr(outBus, metronome);
			Out.ar(outBusMetronome, SinOsc.ar(freq, 0, metronome)*amp)
		}).send(server)
	}

	makeSynth {
		synth=Synth(\BeatDetector, args.asKeyValuePairs, target, addAction).register;
	}

	makeGUI {arg parent, bounds=350@40;
		gui=BeatDetectorJTGUI(this, parent, bounds)
	}
}

BeatDetectorJTGUI {
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
			parent=Window("BeatDetector", Rect(200,200,bounds.x+8, bounds.y+8));
			parent.addFlowLayout(4@4, 0@0);
			parent.alwaysOnTop_(true);
			parent.front;
			parent.onClose_({});
		});
		compositeView=CompositeView(parent, bounds.x@(bounds.y*2));
		compositeView.addFlowLayout(0@0, 0@0);
		views[\start]=Button(compositeView, bbounds).states_([ [\start],[\start,Color.black,Color.green] ]).action_{|b|
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
		views[\sync]=Button(compositeView, bbounds).states_([ [\sync] ]).action_{
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
		}, beatDetectorJT.cmdNameTempo);

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
/*
Server.killAll
b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:0);
b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/DeepPurple1M.aiff", bufnum:0)
b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/1-01 Perfect Day.aif", 0, -1, 1, bufnum:0)

{b.beatRoot.postln}.fork

c=Buffer.alloc(s, 48, bufnum:10);//128

(
x={arg tempo=0.25, bufnum=10, t_trig=1, tempoDrift=0.9, tr=0.5, overlap=4;
var fft, onsets;
var in, metronome, pc, fb;

in=PlayBuf.ar(1, b.bufnum, BufRateScale.ir(b.bufnum), 1, 0, 1);

fft=FFT(LocalBuf(512), in);
onsets=Onsets.kr(fft, tr, odftype:\complex);
pc=PulseCount.kr(onsets);
BufWr.kr(Timer.kr(onsets), bufnum, pc%BufFrames.ir(bufnum), 1);
SendReply.kr(pc%(BufFrames.ir(bufnum)/4)<0.001, '/bpmRequest');

fb=LocalIn.kr(2);
//tempo=tempo, LocalIn.kr
metronome=TDuty.kr(tempo
, Trig.kr(Trig1.kr(onsets+fb[0], tempo*tempoDrift), 0.0001)+t_trig);

//Timer.kr(metronome).poll(metronome);
LocalOut.kr([metronome,tempo]);

SinOsc.ar(1000, 0, metronome)+in!2
}.play
)

x.free
s.recChannels_(2);
s.recSampleFormat_("int24");
x.set(\overlap, 16)
x.set(\tempoDrift, 0.95)
x.set(\tr, 0.5)
x.set(\t_trig, 1)

//===============================
(
var tempo=0.5, prevTempo, tmpdrift, drift=0;
~drift=0.0;
OSCFunc({arg msg;
c.getn(0, c.numFrames, {|b|
prevTempo=tempo;
tempo=f.value(b, tempo, 1.1, 3, ~drift).postln;
x.set(\tempo, tempo);
tmpdrift=(prevTempo/tempo);
if (tmpdrift>1, {tmpdrift=tmpdrift.reciprocal});
if (tmpdrift==1.0, {""},{drift=tmpdrift});

});
}, '/bpmRequest')
)

~drift=0.95;

t.stop
//===============================
*/
+ Array {
	tempo { arg prevTempo=0.5, clusterThresh=1.1, divisionThresh=3, highestDivision=5
		, drift=0.0, post=false;

		var deltaTimes=this;
		var tempo, bpm, ratio;
		var a=[], tempi=[], size;
		prevTempo=prevTempo??{0.5};
		deltaTimes.removeAllSuchThat({arg i; i==0});
		deltaTimes=deltaTimes.sort;
		(deltaTimes.size-1).do{|i|
			if (deltaTimes[i+1]/deltaTimes[i]>clusterThresh, {
				a=a.add(i+1);
		})};
		a=a.add(deltaTimes.size);
		a=a.differentiate;
		deltaTimes=deltaTimes.clumps(a);
		//deltaTimes=deltaTimes.clumpsClusters(clusterTresh);
		a=deltaTimes.collect{|i| i.size}.sort.reverse;
		size=a.size-1;
		a=(a.size-1).collect{|i| a[0]/a[i+1]}.indexOfGreaterThan(divisionThresh);//
		a=a??{size};
		a=deltaTimes.collect{|i| i.size}.order.reverse.copyRange(0,a).sort;
		tempo=if (a.size==0, {prevTempo},{
			if (a.size==1, {a=[a[0],a[0]]});
			a.powersetn(2).collect{|set|
				var frac, out;
				set=set.sort;
				//frac=(deltaTimes[set[0]].mean/deltaTimes[set[1]].mean);
				frac=(deltaTimes[set[0]].cpsmidi.mean.midicps
					/deltaTimes[set[1]].cpsmidi.mean.midicps);
				frac=frac.asFraction(4,false);

				if (frac.sum>highestDivision, {""}, {
					//out=(frac[0]/frac[1])*deltaTimes[set[1]].mean;
					out=(frac[0]/frac[1])*deltaTimes[set[1]].cpsmidi.mean.midicps;
					tempi=tempi.add(2.pow((out.log/2.log).floor.neg-1)*out)
				});
			};
			if (tempi.size==0, {tempi=[prevTempo]});
			tempi.cpsmidi.mean.midicps
		});
		ratio=[tempo, prevTempo].sort;
		//tempo=tempo/2;
		if (tempo==0, {tempo=prevTempo});
		if (ratio[0]/ratio[1]<drift, {tempo=prevTempo});

		bpm=60/tempo;
		if (post, {
			("tempo is " ++ tempo).postln;
			("bpm is " ++ bpm).postln;
		});
		^tempo
	}
}