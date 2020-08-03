/*
- sync options: free/toFirstRecording/bpmInternal/bpmExternal
- zorg dat de metronome via task gaat en NIET via een synthdef (schijnt betere clock te hebben)
*/
LooperJT : JT {
	var <metroBus, <buffer;
	var <>argsVoices;
	var <routines, <metro, <resolution, <resolutionTime, <metroFunc, <bps, <bpm;

	*new {arg args=[], target=Server.default, addAction=\addToTail;
		^super.new.init(args, target, addAction)
	}

	init {arg argargs, argtarget, argaddAction;
		var initFunc;
		var defaultArgs=(voices:1, autoPlay: true, includeMixer: true, bps:1
			, warp: \classic, resolution: 4, xfadeFrames: 8192, maxRecTime: 30);
		args=if (args.class==Event, {argargs},{argargs.asKeyValuePairs});
		target=argtarget.asTarget;
		addAction=argaddAction;
		server=target.server;
		defaultArgs.keysValuesDo{|key,val| if (args[key]==nil, {args[key]=val})};
		[\warp].do{|key|
			if (args[key].size<args[\voices], {
				args[key]=args[key].asArray.lace(args[\voices])
			})
		};
		argsVoices={(startPhase:0, endPhase:1, bufNr:0, numberOfBeats:1)}!args[\voices];
		id=UniqueID.next;
		initFunc={
			this.synthDefs; server.sync;
			group=Group(target, addAction); server.sync;
			outBus=Bus.audio(server, args[\voices]); server.sync;
			metroBus=Bus.audio(server, 3); server.sync;
			buffer=Buffer.alloc(server, server.sampleRate*args[\maxRecTime]); server.sync;
			argsVoices.do{|argsvoice|
				argsvoice[\buffer]={Buffer.alloc(server, 1024, 1)}!2;
			};
			synth[\recorder]=Synth.head(group, \RecLooperJT, [
				\inBus, args[\inBus], \bufnum, buffer, \inBusT, metroBus]); server.sync;
		};
		synth=();
		routines=();
		if (this.isThreaded, {
			initFunc.value
		},{
			{initFunc.value}.fork
		})
	}

	resolution_ {arg res;
		resolution=res;
		resolutionTime=resolution.reciprocal*bps.reciprocal;
	}

	bpm_ {arg bp;
		bpm=bp;
		bps=bpm/60;
		resolutionTime=resolution.reciprocal*bps.reciprocal;
	}

	bps_ {arg bp;
		bps=bp;
		resolutionTime=resolution.reciprocal*bps.reciprocal;
	}

	makeMetro {arg stackSize=512;
		metro=Routine({
			var count;
			inf.do{arg i;
				count=i%resolution;
				metroFunc.value(i, count);
				resolutionTime.wait;
			}
		}, stackSize)
	}

	startMetro {arg resolution, quant, stackSize;//nil, 512
		metro.play;
	}
	stopMetro {
		metro.stop;
	}

	play {

	}

	stop {

	}

	startRecording {arg voice;
		var time=Main.elapsedTime;
		OSCFunc({arg msg;
			argsVoices[voice][\startPhase]=msg[3];
			"delta is ".post; time=(Main.elapsedTime-time).postln;

			argsVoices[voice][\startPhase]=
			argsVoices[voice][\startPhase]-args[\xfadeFrames] % buffer.numFrames;

		}, '/buffer_frame').oneShot;
	}

	stopRecording {arg voice;
		var time=Main.elapsedTime;
		//start een eenmalige synth die éénmalig de opname speelt, vanaf de 'oer' buffer
		//hierna start de nieuwe buffer met spelen
		OSCFunc({arg msg;
			var numFrames;
			argsVoices[voice][\endPhase]=msg[3];

			argsVoices[voice][\endPhase]=
			argsVoices[voice][\endPhase]-args[\xfadeFrames] % buffer.numFrames;

			numFrames=argsVoices[voice][\endPhase]-argsVoices[voice][\startPhase]
			% buffer.numFrames;

			argsVoices[voice][\bufNr]=argsVoices[voice][\bufNr]+1%2;
			{
				argsVoices[voice][\buffer][argsVoices[voice][\bufNr]]=Buffer.alloc(server
					, numFrames, 1
					, bufnum: argsVoices[voice][\buffer][argsVoices[voice][\bufNr]].bufnum);
				buffer.copyDataWrap(argsVoices[voice][\buffer][argsVoices[voice][\bufNr]],
					0, argsVoices[voice][\startPhase], numFrames);
			}.fork;
		}, '/buffer_frame').oneShot;
	}

	synthDefs {//add ID!
		//inputs.do{|i|
		SynthDef(\RecLooperJT, {arg inBus, inBusT, bufnum, outBus, t_trig;
			var inT, in, phase;
			in=In.ar(inBus, 1);
			inT=In.ar(inBusT, 1);
			phase=Phasor.ar(0, 1, 0, BufFrames.ir(bufnum), 0);
			BufWr.ar(in, bufnum, phase);
			SendReply.ar(inT, '/buffer_frame', phase);
			//Out.ar(outBus, Latch.ar(phase, t_trig))
		}).add;
		//};
		SynthDef(\PlayClassicLooperJT, {arg bufnum, rate=1, startPos=0, amp=0, outBus=0
			, inBus, duration=1, t_reset=1, length=1, xfadeTime=0.1;
			var inT=In.ar(inBus), trig;
			var counter;
			counter=PulseCount.ar(inT);
			trig=Trig1.ar(counter>length);
			Out.ar(outBus, PlayBufCF.ar(1, bufnum, rate, trig, startPos, 0
				, xfadeTime)*amp.lag(0.1))
		}).add;
		SynthDef(\PlayLooperJT, {arg bufnum, rate=1, startPos=0, outBus=0
			, gate=1, fadeIn=0.1, fadeOut=0.1;
			var env=EnvGen.ar(Env.asr(fadeIn, 1, fadeOut, -4.0), gate, doneAction:2);
			Out.ar(outBus, PlayBuf.ar(1, bufnum, rate, 1, startPos)*env)
		}).add;
		SynthDef(\PlayTLooperJT, {arg bufnum, rate=1, startPos=0, amp=0, outBus=0
			, inBus, gate=1, length=1;
			var inT=In.ar(inBus)*gate;
			Out.ar(outBus, PlayBuf.ar(1, bufnum, rate, inT, startPos)*amp.lag(0.1))
		}).add;

		SynthDef(\PlayGrainLooperJT);
		SynthDef(\PlayPvocLooperJT);
		SynthDef(\MixerLooperJT);
	}

	free {}
	close {}
	makeGUI {}
}


LooperJTGUI : GUIJT {}