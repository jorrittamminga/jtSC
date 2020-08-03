/*
- en maak dus een 'autofollow' functie waarmee je een bounce van bounces kunt maken. bijvoorbeeld een 'éénmalige' actie-reactie (of een aantal, met een keuze uit verschillende 'methods')
- send level
*/
BouncerJT : JT {
	var <numChannelsBuffer, <out, <>parameters, <buffer, <noParameters, <score;
	var <>nrt, <>normalize, <>includeFirst;
	var addActionID, targetID;

	*new {arg buffer, args=[], target, addAction=\addToHead;
		^super.new.init(buffer, args, target, addAction)
	}

	init {arg argbuffer, argargs, argtarget, argaddAction;
		var func;
		buffer=argbuffer;
		server=buffer.server;
		target=argtarget.asTarget;
		targetID=target.nodeID;
		addAction=argaddAction;
		addActionID = Node.addActions[addAction];

		out=();
		noParameters=[\bounces, \numChannels, \fadeIn, \fadeOut];
		nrt=false;
		normalize=true;
		includeFirst=true;
		server.latency=nil;

		func={
			numChannelsBuffer=buffer.numChannels;
			args=this.initArgs(argargs.asEvent);
			this.initSynthDef;
			synthDef.send(server);
		};

		if (this.isThreaded, {func.value},{{func.value}.fork})
	}

	buffer_ {arg buf;
		var func={
			buffer=buf;
			server=buffer.server;
			server.sync;
			numChannelsBuffer=buffer.numChannels;
			this.initSynthDef;
			synthDef.send(server);
		};
		if (this.isThreaded, {func.value},{{func.value}.fork})
	}

	numChannels_ {arg ch;
		numChannels=ch;
	}

	synthDef_ {arg def;
		synthDef=def;
		synthDef.send(server);
	}

	bounceMirror {arg newArgs, path;
		var mirrorArgs=newArgs.deepCopy;
		var startTime=0;
		score=nil;
		this.bounce(newArgs, path, 0, nil, false);
		mirrorArgs.postln;
		startTime=score.last[0];
		score.removeAt(score.size-1);

		mirrorArgs.keysDo{|key|
			switch(mirrorArgs[key].class, Array, {
				mirrorArgs[key]=if (mirrorArgs[key].rank>1, {
					mirrorArgs[key].collect{|i| i.asArray.reverse.flat}
				},{
					[Array.geom(mirrorArgs[\bounces]
						,mirrorArgs[key][0],mirrorArgs[key][1]).last
						, mirrorArgs[key][1].reciprocal];
				});
			});
		};
		mirrorArgs.postln;
		this.bounce(mirrorArgs, path, startTime, 1, true);
		^score
	}

	bounce {arg newArgs, path, startTime=0, sc, last=true;
		var bounces, deltaTimes, keys, time, latency;
		time=Main.elapsedTime;
		if (newArgs!=nil, {this.updateArgs(newArgs)});
		bounces=args[\bounces];
		parameters.do{|key|
			out[key]=switch(args[key].class, Array, {
				if (args[key].rank>1, {
					args[key].asEnv.discretize2(bounces)
				},{
					Array.geom(bounces, args[key][0], args[key][1]);
				});
			},
			/*Event, {
			var type=args[key].keys.asArray[0];
			var val=args[key][type];
			switch(type, \geom, {
			Array.geom()
			}, \env, {

			});
			},*/
			Function, { {|i| args[key].value(i)}!bounces }
			, args[key]!bounces
			);
		};

		if (args[\dur].isKindOf(SimpleNumber), {
			if (args[\dur]<=0, {
				out[\dur]=(1-out[\startPos])*buffer.duration*(out[\rate].reciprocal);
			});
		});

		out[\at]=(out[\at]*out[\dur]).max(args[\fadeIn]);
		out[\rt]=(out[\rt]*out[\dur]).max(args[\fadeOut]);
		out[\st]=(out[\dur]-out[\at]-out[\rt]).max(0);
		out[\startPos]=out[\startPos]*buffer.numFrames;
		keys=out.keys.asArray;
		out[\az]=if (numChannels>2, {
			//out[\outBus]={args[\outBus].copy}!out[\at].size;
			out[\az].collect{|az, i|
				var bus, azi;
				#bus,azi=az.azToBusAndPan2(numChannels, out[\outBus][i]);
				out[\outBus][i]=out[\outBus][i]+bus;
				azi
			};
		},{
			out[\az].fold(-0.5, 0.5)*2
		});
		out[\outBus1]=out[\outBus].copy;
		out[\outBus2]=(out[\outBus]+1).wrap(args[\outBus], args[\outBus]+numChannels-1);
		//make score
		if (sc==nil, {
			score=List[];
			if (nrt, {
				score.add([0.0, [\b_allocRead, 1, buffer.path]]);
				score.add([0.0, ['/d_recv', synthDef.asBytes]]);
				out[\bufnum]=1!bounces
			},{
				out[\bufnum]=buffer.bufnum!bounces;
			});
		});

		keys=keys.add(\bufnum);
		deltaTimes=out[\deltaTime].integrate;//-latency
		keys.remove(\deltaTime);
		if (normalize, {
			out[\amp]=out[\amp]/out[\amp].maxItem;
		});
		if (includeFirst, {
			deltaTimes=deltaTimes-deltaTimes[0];
		});
		bounces.do{|i|
			score.add([deltaTimes[i]+startTime, [\s_new, synthDef.name, -1
				, addActionID, targetID
			] ++
			keys.collect{|key| [key,out[key][i]]}.flat
			]);
		};
		if (last, {
			latency=time-Main.elapsedTime;
			score.do{|sc| sc[0]=(sc[0]-latency).max(0.0)};
			if (nrt, {
				score.add([deltaTimes.last+(out[\at].last+out[\rt].last+out[\st].last)
					, [\c_set, 0, 0]]);
				Score.render(score, path, numChannels, server.sampleRate, "AIFF", "int24")
			},{
				Score.play(score, server);
			});
		});
		^score
	}


	set {arg ... settings;
		this.updateArgs(settings)
	}

	updateArgs {arg newArgs=[];
		newArgs.asEvent.keysValuesDo{|key,val|
			args[key]=val;
			if ((parameters.includesEqual(key).not)
				&&(noParameters.includesEqual(key).not),{
					parameters=parameters.add(key)
				},{
					if (key==\numChannels, {
						numChannels=val;
					});
			});
		};
	}

	initArgs {arg tmpArgs;
		var args=(
			bounces: 16,

			amp: 1.0,
			deltaTime: [0.01, 1.1],
			az: [[-1.0, 1.0],[1]],
			rate: 1.0,
			dur: -1.0,
			at: 0.0,
			rt: 0.0,
			overlap: -1.0,
			startPos: 0.0,
			fadeIn:0.0,
			fadeOut:0.0,

			outBus: 0,
			numChannels: 2
		);
		args.keysValuesDo{|key,val| if (tmpArgs[key]==nil, {tmpArgs[key]=val}) };
		parameters=[];
		tmpArgs.keys.asArray.do{|key|
			parameters=parameters.add(key);
		};

		numChannels=tmpArgs[\numChannels];
		parameters.remove(\bounces);
		parameters.remove(\numChannels);
		parameters.remove(\fadeIn);
		parameters.remove(\fadeOut);
		^tmpArgs
	}

	initSynthDef {
		synthDef=SynthDef(\BouncyJT, {arg bufnum, startPos=0, amp=0.1, az=0.0, at=0.0
			, st=0.0
			, rt=1.0, curveA=0, curveR= -4.0, rate=1.0
			, loop=0, outBus1=0, outBus2=1;
			var env=EnvGen.ar(Env.linen(at, st, rt, amp, [curveA, 0, curveR])
				, doneAction:2);
			var out;
			out=PlayBuf.ar(numChannelsBuffer, bufnum, BufRateScale.ir(bufnum)*rate, 1
				, startPos, loop)*env;
			//maak ook andere pannings methods zoals ambisonics en PanOut
			out=Pan2.ar(out, az);
			Out.ar(outBus1, out[0]);
			Out.ar(outBus2, out[1]);
		});
	}
}