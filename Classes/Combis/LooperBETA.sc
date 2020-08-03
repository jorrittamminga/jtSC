/*
LOOPER CLASS

TODO:
- GUI: for small sizes change also the flowlayout and ezsliders (gap and margin, labellength)

- RecordBuf is able to overdub and delete-first-run (and overlap after)
- make only as many soundin's as inbusses!
- after recording wait for buffer sync to start playback (otherwise you hear a click)
- add windowsize warp to the expert window
- make it possible to create a big library of recordings (copy buffers into a big array)
- make a selector which recording to play (for instance playback the same recording with all voices)
mode: Metrosynced or free (tempo=1.0) (rec and play)
- twee soorten record en playback (met interne en externe 'impulse')
autoRec: waitForThreshold or manually
tempo: basedOnRecording, BeatTracker, tap or manually
recMode: replace or overdub

Looper.new(inBus, target, addAction, outBus, voices, window, bounds)
var <>resolution, <>tempo, <busClock, <busOut

bij meerdere voices gaat het niet goed met playbuf buffer!
*/

LooperBETA {
	var timesPlay;
	var <ready;
	var <>info, <>synth, <bus, <buf, <server, maxRecTime, <voices, preDelay, postDelay, tempo, makeMixer;
	var inBus, target, addAction, outBus, sync, <parent, bounds, mode;
	var <w, <>guis, <>controlSpecs;
	var <numChans, <group, h, <>parameters;
	var waitSynth;
	var windows, repetitionTimes, repetitionSpeed;

	*new {arg inBus=0, target, addAction=\addAfter, outBus=0, voices=1, sync=false, mode=0, maxRecTime=20, parameters, parent, bounds=400@20, controlSpecs=(
		preDelay: ControlSpec(0.01, 5.0, \exp), lagTime: ControlSpec(0.01, 5.0, \exp)
		, fadeIn: ControlSpec(0.01, 30.0, \exp), fadeOut: ControlSpec(0.01, 30.0, \exp)
		, ws: ControlSpec(0.05, 1.0, \exp), tDev: ControlSpec(0.0001, 1.0, \exp)
		, wsDev: ControlSpec(0.0, 1.0), overlap: ControlSpec(0.1, 8, \exp)
		, rate: ControlSpec(0.1, 10.0, \exp), freqScale: ControlSpec(0.125, 8.0, \exp), amp: \amp.asSpec
		, az: \bipolar.asSpec);
	^super.new.init(inBus, target, addAction, outBus, voices, sync, mode, maxRecTime, parameters, parent, bounds, controlSpecs)
	}

	init {arg arginBus, argtarget, argaddAction, argoutBus, argvoices, argsync, argmode, argmaxRecTime, argparameters, argparent, argbounds, argcontrolSpecs;
		var azs, tmpcs;
		ready=false;
		inBus=arginBus;
		target=argtarget;
		addAction=argaddAction;
		numChans=argoutBus.asArray.size.max(2);
		outBus=argoutBus.asArray.minItem;
		voices=argvoices;
		sync=argsync;
		maxRecTime=if (voices>1, {
			argmaxRecTime.asArray.resamp0(voices);//argMaxRecTime
		},{
			argmaxRecTime.asArray
		});
		parent=argparent;
		bounds=argbounds;
		mode=argmode.asArray;
		parameters=argparameters;
		controlSpecs=argcontrolSpecs;

		repetitionSpeed=0.1;
		repetitionTimes={Main.elapsedTime}!voices;

		if (target==nil, {
			server=Server.default;
		},{
			if (target.class==Server, {
				server=target
			},{
				server=target.server;
			})
		});


		preDelay=0.25;//2*lagtime playbufcf at least?
		postDelay=1.0;
		tempo=1.0;

		info=();
		synth=();
		bus=();
		timesPlay={Main.elapsedTime}!voices;
		windows=();
		guis=();
		makeMixer=outBus.isNil.not;

		{
			this.synthDefs; server.sync;
			synth[\playback]={Synth(\Dummy).register}!voices;
			synth[\record]={Synth(\Dummy).register}!voices;
			buf={|i| {Buffer.alloc(server, server.sampleRate*maxRecTime[i])}!2}!voices;//maxRecTime[i]
			server.sync;
			group=Group.new(target, addAction);
			server.sync;
			//[0,1].asBus;
			if (inBus.class==Bus, {inBus=inBus.indices});
			bus.in=inBus??{[0]};
			bus.in=bus.in.asArray;
			bus.delayIn=Bus.audio(server, voices);
			bus.out=Bus.audio(server, voices);
			server.sync;
			bus.out={|i| bus.out.index+i}!voices;
			bus.delayIn={|i| bus.delayIn.index+i}!voices;
			synth[\in]={|voice|
				var synthDef=\DelayIn;
				if (bus[\in].wrapAt(voice)<server.options.numOutputBusChannels, {synthDef=\DelaySoundIn});
				Synth.head(group, synthDef, [\inBus, bus[\in].wrapAt(voice), \outBus, bus.delayIn.wrapAt(voice)
					, \preDelay, preDelay])
			}!voices;
			server.sync;
			azs=if (voices>1, {
				[(1.0-(voices.reciprocal)).neg, (1.0-(voices.reciprocal))].resamp1(voices)
			},{
				[0]
			});
			info[\mix]={|voice| (amp: voices.reciprocal.sqrt, az: azs[voice])}!voices;
			if (makeMixer, {
				synth[\mix]={|voice| Synth.after(synth[\in][voice], \Mix, [
					\inBus, bus.out[voice], \outBus, outBus, \az, info[\mix][voice][\az]
					, \amp, info[\mix][voice][\amp]
				]).register}!voices;
				server.sync;
			});



			info[\record]={|voice| (
				overDub: 0, eraseFirst:1, loop:0,
				postDelay: postDelay, preDelay: preDelay, sync: 0, inBus: bus[\delayIn].wrapAt(voice))
			}!voices;

			info[\playback]={|voice| (freqScale:1, rate:1, lagTime: 0.1, fadeIn: 0.1
				, fadeOut: 1.0, preDelay: preDelay, sync: 0
				//, ws: 0.1
				, mode: mode.wrapAt(voice), outBus: bus[\out].asArray.wrapAt(voice))}!voices;

			if (parameters!=nil, {parameters.keysValuesDo({|key,val|
				var value=val.asArray;
				voices.do{|voice|
					info[\playback][voice][key]=value.wrapAt(voice)
				}
			})});

			info[\buf]={(tempo: 1.0, start: 0.0, end:1.0)}!voices;

			voices.do{|voice|
				info[\buf][voice][\bufnums]=buf[voice];
				info[\buf][voice][\bufnumRecord]=buf[voice][0].bufnum;
				info[\buf][voice][\bufnumPlayback]=buf[voice][1].bufnum;
				info[\buf][voice][\voices]=voice.asArray;
				info[\playback][voice][\bufnum]=info[\buf][voice][\bufnumPlayback];
				info[\record][voice][\bufnum]=info[\buf][voice][\bufnumRecord];
			};

			tmpcs=controlSpecs;
			controlSpecs=(preDelay: ControlSpec(0.01, 5.0, \exp), lagTime: ControlSpec(0.01, 5.0, \exp)
				, fadeIn: ControlSpec(0.01, 30.0, \exp), fadeOut: ControlSpec(0.01, 30.0, \exp)
				, ws: ControlSpec(0.05, 1.0, \exp), tDev: ControlSpec(0.0001, 1.0, \exp)
				, wsDev: ControlSpec(0.0, 1.0), overlap: ControlSpec(0.1, 8, \exp)
				, rate: ControlSpec(0.1, 10.0, \exp), freqScale: ControlSpec(0.125, 8.0, \exp)
				, amp: \amp.asSpec, az: \bipolar.asSpec);

			if (tmpcs!=nil, {tmpcs.keysValuesDo({|key,val|
				controlSpecs[key]=val
			})});

			{
				h=bounds.y;
				this.gui;
				ready=true;
			}.defer;
		}.fork;
	}


	play {arg gate=1, voice=0, sync, mode;//mode 0=classic, 1=timewarp
		var synthDef=\PlayBufFree, deltaTime, flag=true;

		//		info[\playback][voice][\gate]=gate;
		sync=sync??{info[\playback][voice][\sync]};
		mode=mode??{info[\playback][voice][\mode]};
		info[\playback][voice][\sync]=sync;
		info[\playback][voice][\mode]=mode;

		if (gate==1, {
			//	if (info[\playback][voice][\gate]==1, {
			//		synth[\playback][voice].set(\gate, -1);
			//	});

			if (info[\playback][voice][\gate]==1, {
				//if (synth[\playback][voice].isPlaying, {//according to Looper, NOT synth.isPlaying
				synth[\playback][voice].get(\gate, {|val|
					if (val==1, {
						synth[\playback][voice].set(\gate, -0.001);
					},{
					});
				});
			});

			synthDef=[\PlayBufFree, \TimeWarpFree][mode];
			info[\playback][voice][\gate]=gate;
			timesPlay[voice]=Main.elapsedTime;
			synth[\playback][voice]=Synth.head(group, synthDef, ([\preDelay, preDelay, \tempo, tempo]++info[\playback][voice].asKeyValuePairs)).register;

		},{
			synth[\playback][voice].set(\gate, -0.001);
			server.sendMsg(\n_set, synth[\playback][voice].nodeID, \gate, 0);
			if (synth[\playback][voice].isRunning, {
				//	synth[\playback][voice].free
			});
			/*
			if (synth[\playback][voice].isPlaying, {
			synth[\playback][voice].get(\gate, {|val|
			if (val==1, {
			synth[\playback][voice].set(\gate, 0)
			})
			})
			});
			*/
		});
		info[\playback][voice][\gate]=gate;
	}


	replay {arg voice, sync, mode;
		info[\buf][voice][\voices].do{|v|
			var syn=synth[\playback][v];
			info[\buf][voice].keysValuesDo{|key,value|
				info[\playback][v][key]=value;
			};
			{
				[\start, \end].do{|par|
					guis[voice][par].controlSpec_(ControlSpec(0.0, info[\buf][voice].end));
					guis[voice][par].value_(info[\buf][voice][par])
				}
			}.defer;
			info[\playback][v][\bufnum]=info[\buf][voice][\bufnumPlayback];

			if (info[\playback][voice][\gate]==1, {
				//if (syn.isPlaying, {//if the synth is playing acoording to Looper, not syn.isPlaying
				syn.set(\gate, -0.001);//this.play(0,v)
				this.play(1, v);

				//syn.get(\gate, {|val|
				//	if (val==1, {
				//
				//					this.play(1, v);
				//	});
				//});
			})
		};
	}


	record {arg gate=1, voice=0, sync=0;//sync: 0=free, 1=clocksynced
		var tmpBuf, func;

		if (gate==1, {
			if (sync==0, {
				synth[\record][voice]=Synth.after(synth[\in][voice], \RecordBufFree, ([
					\tempo, tempo, \preDelay, preDelay
					//, \overDub, 1, \eraseFirst, 1
				]++info[\record][voice].asKeyValuePairs)).register;
				info[\buf][voice][\start]=Main.elapsedTime;
			},{
				nil//OSCFunc
			});
		},{
			tmpBuf=info[\buf][voice][\bufnumRecord].copy;
			info[\buf][voice][\bufnumRecord]=info[\buf][voice][\bufnumPlayback].copy;
			info[\buf][voice][\bufnumPlayback]=tmpBuf.copy;
			info[\record][voice][\bufnum]=info[\buf][voice][\bufnumRecord];

			synth[\record][voice].set(\gate, -0.001);

			if (sync==0, {
				info[\buf][voice][\end]=(
					Main.elapsedTime-info[\buf][voice][\start]+(2*preDelay)).clip(0, maxRecTime[voice]);
				info[\buf][voice][\start]=0;
				this.replay(voice);
			},{
				nil//OSCFunc
			});
		})
	}


	tempo_ {arg val; tempo=val; }
	preDelay_ {arg val; preDelay=val; }//set the DelayIn synths


	synthDefs {
		/*
		SynthDef(\MasterClock, {arg outBus=0, t_trig=1, tempo=2;
		var impulse;
		impulse=TDuty.ar(tempo.reciprocal, t_trig);
		Out.ar(outBus, impulse)
		}).add;
		*/

		SynthDef(\Mix, {arg inBus, outBus, amp=1.0, az=0.0;
			Out.ar(outBus, PanAz.ar(numChans, In.ar(inBus), az.lag(1.0), amp.lag(1.0), numChans.pow(0.6).max(2)))
		}).add;

		SynthDef(\Dummy, {var test=Line.kr(0,1,0.001, doneAction:2); Out.ar(0, test*WhiteNoise.ar(0.0000000001))}).add;

		SynthDef(\DelaySoundIn, {arg inBus, outBus, preDelay=0.1;
			var in, out;
			in=SoundIn.ar(inBus);
			out=DelayN.ar(in, (preDelay*2).max(1.0), preDelay);
			Out.ar(outBus, out)
		}).add;

		SynthDef(\DelayIn, {arg inBus, outBus, preDelay=0.1;
			var in, out;
			in=In.ar(inBus);
			out=DelayN.ar(in, (preDelay*2).max(1.0), preDelay);
			Out.ar(outBus, out)
		}).add;

		SynthDef(\RecordBufFree, {arg inBus, bufnum, gate=1.0, preDelay=0.1, postDelay=1.0
			, overDub=0, eraseFirst=1, loop=0, fadeOut=1.0;
			var in=In.ar(inBus);
			var env2;
			var env=EnvGen.kr(Env.asr(0, 1, postDelay), gate, doneAction:2);
			loop=(overDub+loop).min(1);
			env2=EnvGen.kr(Env.linen(0,BufDur.ir(bufnum)-fadeOut,fadeOut), 1, 1-loop, loop);
			//eraseFirst=EnvGen.kr(Env.dadsr(eraseFirst*BufDur.ir(bufnum),0.1, 1, 1), gate);
			eraseFirst=EnvGen.kr(Env.new([0,0,1,1],[eraseFirst*BufDur.ir(bufnum),0.1,1], 'lin', 2),gate);
			//(eraseFirst*overDub).poll(1);
			//[env2, eraseFirst*overDub].poll(10);
			RecordBuf.ar(in*env2, bufnum, 0, 1, eraseFirst*overDub, loop: loop, doneAction:2);
			//RecordBuf.ar(in, bufnum, 0, 1, loop:0, doneAction:2);
		}).add;

		SynthDef(\PlayBufFree, {arg outBus, bufnum, rate=1, start=0.0, end=1.0, lagTime=0.1, gate=1.0, fadeIn=0.1, fadeOut=0.1, pitch=0.0;
			var out, trigger, tmp;
			var env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut), gate, doneAction:2);
			rate=rate.lag(0.1);
			trigger=Impulse.kr((end-start).abs.max(lagTime*2).reciprocal*rate);
			//rate=((end-start)>0)*2-1*rate;
			rate=Latch.kr( ((end-start)<0).neg*2+1, trigger)*rate;
			out=PlayBufCF.ar(1, bufnum, rate, trigger, start*BufSampleRate.kr(bufnum), 1, lagTime);
			Out.ar(outBus, out*env)//outBus
		}).add;


		SynthDef(\TimeWarpFree, {arg outBus, bufnum, rate=1, start=0.0, end=1.0, preDelay=0.1, gate=1.0, fadeIn=0.1, fadeOut=0.1, freqScale=1.0, ws=0.1, tDev=0.005, wsDev=0.0, overlap=4, lagTime=0.1;
			var out, trigger;
			var env;
			var pointer, durR=BufDur.kr(bufnum).reciprocal, ampl;

			ws=ws.lag(lagTime);

			env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut,[\sine,-4.0]),gate,doneAction:2);
			overlap=(rate.abs.reciprocal*overlap).clip(overlap, 50);
			overlap=if (rate<1.0, overlap, 3);
			ampl=overlap.pow(-0.5).min(1.0)*2.0;

			rate=((end-start)>0)*2-1*rate;

			pointer=Phasor.ar(0.0, BufFrames.kr(bufnum).reciprocal*rate, start*durR, end*durR, start*durR);
			pointer=pointer+WhiteNoise.ar(durR*tDev);
			//freqScale=freqScale.lag(0.1);
			ws=(freqScale.reciprocal*ws).clip(0.04, ws);
			out=Warp1.ar(1, bufnum, pointer, freqScale, ws.lag3(1.0), -1, overlap, wsDev, 4, ampl.lag(0.1));
			Out.ar(outBus, out*env)//outBus
		}).add;
	}


	free {
		group.free;
		buf.flat.do{|b| b.free};
		bus.do{|b|
			b.do{|i|
				if (i>=server.options.numOutputBusChannels, {
					i.asBus(\audio).free
				})
			};
		};
	}

	expertWindow {arg voice, parent=nil;
		var n=info[\playback][voice].keys.sect(controlSpecs.keys).size;
		var u;
		var font=Font("Helvetica",h*0.7);
		var e;

		ready=false;
		//guis[voice]
		if (windows[voice]==nil, {
			if (parent==nil, {
				u=Window(voice.asString, Rect(100,100,(17*h+8),n*(h+4)+8)).front;
				u.addFlowLayout; u.onClose_{windows.removeAt(voice)};
				windows[voice]=u;
			},{
				u=CompositeView(parent, (17*h)@(h*(n+1))); u.addFlowLayout(0@0,0@0);
				//u=parent;
				windows[voice]=u;
				StaticText(u, (17*h)@h).string_("expert voice " ++ voice)
				.font_(font).stringColor_(Color.black);//.background_(Color.black);
			});
			info[\playback][voice].keysValuesDo{|key,val|
				if (controlSpecs[key]!=nil, {
					e=EZSlider(u, (17*h)@h, key, controlSpecs[key], {|ez|
						info[\playback][voice][key]=ez.value;
						if (synth[\playback][voice].isPlaying, {
							synth[\playback][voice].set(key, ez.value)});
					}).round2_(0.000001).font_(font);//.setColors(Color.black, Color.white, nil, Color.black, Color.white, Color.white, Color.red(1.5));
					e.value_(val);
					guis[voice][key]=e;
				})
			};
		},{
			windows[voice].front

		});
		ready=true;
	}


	gui {
		var v, controls, mixer;
		var font=Font("Helvetica",h*0.7);
		if (parent==nil, {
			w=Window("Looper", Rect(100,400,(37*h+20+(makeMixer.binaryValue*h*16+20)),((h+8)*voices+16 ))).front; w.addFlowLayout(4@4, 0@0); w.alwaysOnTop_(true);
			w.onClose_{ windows.do{|w| w.close}; this.free};
			parent=w;
		},{
			w=parent;
			parent.onClose=(parent.onClose.addFunc({ windows.do{|w| w.close}; this.free}));
		});

		controls=CompositeView(w, ((37*(h))+20)@((h+8)*voices+8)); controls.addFlowLayout(4@4, 0@0);
		//controls.background_(Color.blue(0.4));
		if (makeMixer, {
			mixer=CompositeView(w, (16*(h)+12)@((h+8)*voices+8)); mixer.addFlowLayout(2@8, 2@8);
			//mixer.background_(Color.green(0.2));
		});
		voices.do{|voice|
			var v;
			var values=();
			guis[voice]=();
			v=CompositeView(controls, (controls.bounds.width-4)@(h+8)); v.addFlowLayout(4@4, 4@4);
			//v.background_(Color.gray(0.2));
			values[\recB]=0;
			guis[voice][\recB]=Button(v,h@h).states_([ [\r, Color.red(0.2)],[\r, Color.white, Color.red] ]).font_(font).action_{|b|
				if (b.value!=values[\recB], {
					this.record(b.value, voice, info[\record][voice][\sync]);
				});
				values[\recB]=b.value;
			}.value_(values[\recB]);
			guis[voice][\recordSync]=Button(v,(h/2)@h).states_([ [\f],[\s, Color.black, Color.yellow] ]).font_(font).action_{|b| info[\record][voice][\sync]=b.value};

			values[\playB]=0;
			guis[voice][\playB]=Button(v,h@h).states_([ [\p, Color.green(0.2)],[\p, Color.white, Color.green] ]).font_(font).action_{|b|
				if (b.value!=values[\playB], {
					this.play(b.value, voice, info[\record][voice][\sync] , info[\record][voice][\mode]);
				});
				values[\playB]=b.value;
			}.value_(values[\playB]);
			guis[voice][\playbackSync]=Button(v,(h/2)@h).font_(font).states_([ [\f],[\s, Color.black, Color.yellow] ]).action_{|b| info[\playback][voice][\sync]=b.value}.value_(info[\playback][voice][\sync]);
			guis[voice][\playbackMode]=Button(v,(h/2)@h).font_(font).states_([ [\p],[\w, Color.black, Color.blue(2.0)] ]).action_{|b| info[\playback][voice][\mode]=b.value}.value_(info[\playback][voice][\mode]);

			[\start, \end].do{arg par; guis[voice][par]=EZSlider(v, (9*h)@h, par, ControlSpec(0, 1), {|ez|
				info[\playback][voice][par]=ez.value;
				if (synth[\playback][voice].isPlaying, {synth[\playback][voice].set(par, ez.value)});
			}, info[\playback][voice][par], false, h*1.5, gap:0@0, margin: 0@0).round2_(0.001)//.setColors(Color.black,Color.white, Color.black,Color.black,Color.white, Color.white, Color.yellow)
			.font_(font);
			};

			guis[voice][\rate]=EZNumber(v, (h*3.5)@h, \rate, controlSpecs[\rate], {|ez|
				info[\playback][voice][\rate]=ez.value;
				if (synth[\playback][voice].isPlaying, {synth[\playback][voice].set(\rate, ez.value)});
			}, info[\playback][voice][\rate], false, h*1.5, gap:0@0, margin: 0@0).round2_(0.001)//.setColors(Color.black,Color.white, Color.black,Color.black,Color.white, Color.white, Color.black)
			.font_(font);
			guis[voice][\freqScale]=EZNumber(v, (h*5)@h, \freqScale, controlSpecs[\freqScale], {|ez|
				info[\playback][voice][\freqScale]=ez.value;
				if (synth[\playback][voice].isPlaying, {synth[\playback][voice].set(\freqScale, ez.value)});
			}, info[\playback][voice][\freqScale], false, h*3, gap:0@0, margin: 0@0).round2_(0.001)//.setColors(Color.black,Color.white, Color.black,Color.black,Color.white, Color.white, Color.black)
			.font_(font);
			Button(v,(h*4)@h).states_([ [\expert, Color.white, Color.black] ]).font_(font).action_{
				this.expertWindow(voice)
			};
			if (makeMixer, {
				guis[voice][\amp]=EZSlider(mixer, h*8@h, \amp, controlSpecs[\amp], {|ez| synth[\mix][voice].set(\amp, ez.value)}, info[\mix][voice][\amp], false, h*1.5).font_(font)
				//.setColors(Color.black, Color.white, Color.black, Color.black, Color.white, Color.white, Color.red, Color.blue, Color.black)
				;
				guis[voice][\az]=EZSlider(mixer, h*8@h, \az, controlSpecs[\az], {|ez| synth[\mix][voice].set(\az, ez.value)}, info[\mix][voice][\az], false, h*1.5).font_(font)
				//.setColors(Color.black, Color.white, Color.black, Color.black, Color.white, Color.white, Color.red, Color.blue, Color.black)
				;
			});
		};
	}
}