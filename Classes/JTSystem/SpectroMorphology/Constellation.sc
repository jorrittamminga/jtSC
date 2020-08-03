/*
Server.killAll
(
s.waitForBoot{
	var voices=64;
	x={arg revtime=5, amp=0.1; NHHall.ar(In.ar(0, 2), revtime.lag(1.0), 1.0)*amp.lag(1.0)}.play;
	//b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/lacrimaeneuten.aiff");

b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/VoetstappenOostkappele.aif", 0, -1, 0);
	s.sync;

	c=Constellation([\voices, voices, \buffer, b], x).play

}
)
c.set(\tDev, 1.0, \xfadeTime, 4.0)
c.set(\rate, {exprand(0.25, 4.0)}!c.voices)

c.voices
c.setI( [\rate, {exprand(0.99, 1.01)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 0.2501)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 4.0)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 0.5)}!c.voices, 4.0] )

c.setI( [\rate, {(1..8).choose*0.125+0.001.rand2}!c.voices, 4.0] )

c.setI( [\rate, ({(1..8).choose*0.125+0.001.rand2}!c.voices).neg, 4.0] )
c.setI( [\az, {rrand(-0.5,0.5)}!c.voices, 4.0] )


c.setI( [\rate, {exprand(0.25, 1.0)}!c.voices, {exprand(1.0, 6.0)}!c.voices, 0, {rrand(0, 5.0)}!c.voices] )
*/
Constellation : JT {

	classvar addActions, initArgsEvent;
	var <argsEvent, <voices, <lagTime=0.1;
	var <routines;

	*new {arg args=[], target, addAction=\addBefore;
		^super.new.init(args, target, addAction)
	}


	init {arg argargs, argtarget, argaddAction=\addBefore;

		target=argtarget.asTarget;
		addAction=argaddAction;
		args=argargs;
		argsEvent=args.asEvent;
		server=target.server;
		/*
		//------------------------------------------------------- find server
		if (target==nil, {
		if (argsEvent[\buffer].class==Buffer, {
		target=argsEvent[\buffer].server
		},{
		target=Server.default
		})
		});
		server=if (target.class==Server, {target},{target.server});
		target=target.asTarget;
		*/
		//------------------------------------------------------------
		numChannels=argsEvent[\numChannels]??{2};
		voices=argsEvent[\voices]??{8};
		argsEvent.removeAt(\voices);
		argsEvent[\boost]=voices.reciprocal.sqrt;
		routines=();
		this.initSynthDef;
		if (this.isThreaded, {
			"wait for sync....".postln;
			server.sync;
			"synced!".postln;
		});
		initArgsEvent.keysValuesDo{arg key, val;
			if (argsEvent[key]==nil, {
				if (val.size>1, {
					argsEvent[key]=val.lace(voices);
				},{
					argsEvent[key]=val;
				})
			})
		};

		^this
	}

	lagTime_ {arg value;
		lagTime=value;
		synth.set(\lagTime, 2*lagTime);
	}

	play {
		"start the synth".postln;
		synth=Synth(argsEvent[\synthDef]
			, argsEvent.asKeyValuePairs++[\lagTime, 2*lagTime]
			, target, addAction)
	}

	get {arg key;
		^argsEvent[key]
	}

	set { arg ... args;
		var event=args.asEvent, keys=event.keys, array;
		args.asEvent.keysValuesDo{|key,val|
			if (routines[key].class==Routine, {routines[key].stop});
			argsEvent[key]=val
		};
		/*
		array=voices.collect{|i|
		var e=();
		keys.do{arg key;
		e[key]=event[key].wrapAt(i);
		argsEventArray[i][key]=e[key];
		};
		e
		};
		*/
		synth.set( *args );
	}

	//([parameter, values, times, curves, delayTimes], ....)
	setI {arg ... args;
		var array=args;

		array.do{arg p;
			var key=p[0];
			var values=p[1].asArray.lace(voices);
			var times=p[2].asArray.lace(voices);
			var curves=p[3];
			var delayTimes=p[4];
			var env, totalTime, waitTime=lagTime, steps;

			curves=if (curves==nil, {(\sin)!voices}, {curves.asArray.lace(voices)});
			delayTimes=if (delayTimes==nil, {0!voices}, {delayTimes.asArray.lace(voices)});
			totalTime=delayTimes.maxItem+times.maxItem;
			steps=totalTime*(waitTime.reciprocal);

			env=voices.collect{|i|
				var env=Env(
					[argsEvent[key][i], argsEvent[key][i], values[i], values[i]]
					, [delayTimes[i], times[i], totalTime-delayTimes[i]-times[i]].normalizeSum
					, curves[i]);
				env.discretize2(steps)
			};

			if (routines[key].class==Routine, {routines[key].stop});

			routines[key]={
				var values;
				steps.do{|index|
					values=env.collect{|e|
						e[index];
					};
					argsEvent[key]=values;
					synth.set(key, values);
					waitTime.wait;
				};
			}.fork

		};

	}


	*initClass {

		addActions = (addToHead: 0, addToTail: 1, addBefore: 2, addAfter: 3, addReplace: 4,
			h: 0, t: 1, 0: 0, 1: 1, 2: 2, 3: 3, 4: 4);

		initArgsEvent=(
			voices: 8
			, outBus: 0
			, rate: Array.rand(1024, 0.5, 2.0)
			, amp: Array.rand(1024, 1.0, 1.0)
			, az: Array.rand(1024, -1.0, 1.0)
			, fadeIn: 1
			, fadeOut: 3.0
			, gate: 1
			, xfadeTime: 1.0
			, startPos: 0
			, tDev: 0.0
			, synthDef: \ConstellationJT
			, numChannels: 2
		);
	}


	initSynthDef {
		SynthDef(\ConstellationJT, {arg buffer, fadeIn=1.0, fadeOut=3.0, gate=1
			, xfadeTime=1.0, startPos=0, outBus, lagTime=0.1, boost=1.0;
			var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut),gate, doneAction:2);
			var out, trigger;
			var azs=NamedControl.kr(\az, Array.rand(voices, -1.0, 1.0), lagTime);
			var amps=NamedControl.kr(\amp, Array.rand(voices, 0.1, 1.0), lagTime);
			var rates=NamedControl.kr(\rate, Array.rand(voices, 0.5, 2.0), lagTime);
			//trigger=Dust.kr( BufDur.ir(bufnum).reciprocal*rate, tDev );
			out=voices.collect{arg i;
				PanAz.ar(numChannels,
					PlayBufCF.ar(1, buffer, BufRateScale.ir(buffer)*rates[i], 1
						, startPos, 1, xfadeTime), azs[i], amps[i])
			}.sum*env*boost;
			Out.ar(outBus, out)
		}).add;
		/*
		SynthDef(\ConstellationJT, {arg bufnum, fadeIn=1.0, fadeOut=3.0, gate=1
			, xfadeTime=1.0, startPos=0, outBus, lagTime=0.1, boost=1.0, interp=4, tDev=0.01;
			var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut),gate, doneAction:2);
			var out, trigger;
			var azs=NamedControl.kr(\az, Array.rand(voices, -1.0, 1.0), lagTime);
			var amps=NamedControl.kr(\amp, Array.rand(voices, 0.1, 1.0), lagTime);
			var rates=NamedControl.kr(\rate, Array.rand(voices, 0.5, 2.0), lagTime);
			var cPos=A2K.kr(Phasor.ar(1, 1, 0, BufFrames.ir(bufnum)))/BufSampleRate.ir(bufnum);
			//trigger=Dust.kr( BufDur.ir(bufnum).reciprocal*rate, tDev );
			trigger=Impulse.kr(xfadeTime.reciprocal*2);
			out=voices.collect{arg i;
				var centerPos;
				centerPos=cPos+WhiteNoise.kr(tDev);
				TGrains.ar(numChannels, trigger, bufnum, rates[i], centerPos, xfadeTime
					, azs[i], amps[i], interp);
			}.sum*boost*env;
			Out.ar(outBus, out)
		}).add;
		*/
	}

}