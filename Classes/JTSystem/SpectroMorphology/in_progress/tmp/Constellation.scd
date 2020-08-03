Constellation : JT {

	classvar addActions, initArgsEvent;
	var argsEvent, argsEventArray, <voices, <lagTime=0.1;
	var routines;

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
		routines={()}!voices;
		this.initSynthDef;
		if (this.isThreaded, {
		server.sync;
		});
		initArgsEvent.keysValuesDo{arg key, val;
			if (argsEvent[key]==nil, {
				argsEvent[key]=if ( (val.isKindOf(SimpleNumber)) || (val.isKindOf(Array))
					, {val.asArray.lace(voices)}, {val!voices})
			})
		};

		this.eventToArray;
		^this
	}

	lagTime_ {arg value;
		lagTime=value;
		synth.do{|syn| syn.set(\lagTime, 2*lagTime)};
	}

	play {
		synth=voices.collect{arg i;
			Synth(argsEvent[\synthDef][i]
				, argsEventArray[i].asKeyValuePairs++[\boost, voices.reciprocal.sqrt, \lagTime, 2*lagTime]
				, target, addAction)
		}
	}

	eventToArray {
		var keys;
		keys=argsEvent.keys;
		argsEventArray=voices.collect{arg i;
			var event=();
			keys.do{|key| event[key]=argsEvent[key].asArray.wrapAt(i)};
			//synth[i].set( event.asArray );
			event;
		};
	}

	set { arg ... args;
		var event=args.asEvent, keys=event.keys, array;
		args.asEvent.keysValuesDo{|key,val| argsEvent[key]=val};

		array=voices.collect{|i|
			var e=();
			keys.do{arg key;
				e[key]=event[key].wrapAt(i);
				argsEventArray[i][key]=e[key];
			};
			e
		};
		voices.do{|i|
			synth[i].set( *array[i].asKeyValuePairs );
		};
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

			curves=if (curves==nil, {(\sin)!voices}, {curves.asArray.lace(voices)});
			delayTimes=if (delayTimes==nil, {0!voices}, {delayTimes.asArray.lace(voices)});

			voices.do{|i|
				var waitTime=lagTime;
				var steps=times[i]*(waitTime.reciprocal);
				var env=Env([argsEvent[key][i], values[i]], [1.0]
					, curves[i]).discretize2(steps);
				if (routines[i][key].class==Routine, {routines[i][key].stop});
				routines[i][key]={
					delayTimes[i].wait;
					env.do{arg value;
						argsEvent[key][i]=value;
						synth[i].set(key, value);
						waitTime.wait;
					};
				}.fork
			};
		}
	}

	*initClass {

		addActions = (addToHead: 0, addToTail: 1, addBefore: 2, addAfter: 3, addReplace: 4,
			h: 0, t: 1, 0: 0, 1: 1, 2: 2, 3: 3, 4: 4);

		initArgsEvent=(
			voices: 8
			, outBus: 0
			, rate: (1..8)
			, amp: 8.reciprocal.sqrt!8
			, az: 8.asAzimuthArray/pi
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
		SynthDef(\ConstellationJT, {arg bufnum, fadeIn=1.0, fadeOut=3.0, gate=1
			, xfadeTime=1.0, az=0.0, amp=1.0, rate=1.0, startPos=0, outBus, azDev=0.0
			, rateDev=0.0, ampDev=0.0, tDev=0.0, lagTime=0.1, boost=1.0;
			var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut),gate, doneAction:2);
			var out, trigger;
			rate=rate.lag(lagTime);
			amp=amp.lag(lagTime);
			az=az.lag(lagTime);
			trigger=Dust.kr( BufDur.ir(bufnum).reciprocal*rate, tDev );
			out=PlayBufCF.ar(1, bufnum, BufRateScale.ir(bufnum)*rate, trigger, startPos
				, 1, xfadeTime)*env*boost;
			Out.ar(outBus, PanAz.ar(numChannels, out, az, amp))
		}).add;


	}

}