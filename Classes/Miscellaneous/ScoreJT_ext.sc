+ ScoreJT {
	addTransition {arg time=0.0, target, from=(), to=()
		//, durations, curves, delayTimes
		, specs, busses;
		var durations, curves=\sin, delayTimes=0;
		var durationsPerKey=(), duration=1.0, curve=\sin, delayTime=0, maxDuration=0, timesPerKey=()
		, synthDef=\generate_a_name, keysWithTransition=[], keysWithoutTransition=[]
		, synthDefTarget, numChannels=0, setMsg=[\n_set], mapMsg=[], tmpBusses=[], b, specsSynthDef, bus=(), map=true
		, extras=();
		//----------------------------------------------------------------------------------------
		durations=to[\durations_CuesJT]??{1.0};
		if (to[\extras_CuesJT]!=nil, {
			curves=to[\extras_CuesJT][\curves]??{\sin};
			delayTimes=to[\extras_CuesJT][\delayTimes]??{0};
		},{

		});
		//----------------------------------------------------------------------------------------
		if (durations.class!=Event, {
			duration=durations.deepCopy;
			durations=();
		},{
			duration=durations.values.flat.maxItem
		});
		if (specs==nil, {specs=()});
		if (delayTimes.class!=Event, {delayTime=delayTimes??{delayTime}; delayTimes=()});
		if (curves.class!=Event, {curve=curves??{curve}; curves=()});

		if (to[\extras_CuesJT]!=nil, {
			if (to[\extras_CuesJT][\durations]!=nil, { to[\extras_CuesJT][\durations].keysValuesDo{|key,val|
				durations[key]=val} });
			if (to[\extras_CuesJT][\curves]!=nil, { to[\extras_CuesJT][\curves].keysValuesDo{|key,val| curves[key]=val} });
			if (to[\extras_CuesJT][\delayTimes]!=nil, { to[\extras_CuesJT][\delayTimes].keysValuesDo{|key,val|
				delayTimes[key]=val} });
		});
		if (busses==nil, {busses=()},{map=false});
		if (target.class!=Integer, {
			if (map, {setMsg=setMsg++target.nodeID});
			synthDefTarget=target.defName;
			if (synthDefTarget!=nil, {
				specsSynthDef=SynthDescLib.global.at(synthDefTarget).specs.deepCopy;
				specsSynthDef.keysValuesDo{|key,cs| if (specs[key]==nil, {specs[key]=cs.asSpec})};
			});
		},{
			if (map, {setMsg=setMsg++target});
		});
		specs=specs.deepCopy;
		to=to.deepCopy;
		[\durations_CuesJT, \method_CuesJT, \extras_CuesJT].do{|key| to.removeAt(key)};
		//----------------------------------------------------------------------------------------
		to.sortedKeysValuesDo{|key,val|
			if (from[key]==nil, {
				durationsPerKey[key]=0;
				keysWithoutTransition=keysWithoutTransition.add(key);
				setMsg=setMsg++[key, val];
			},{
				if ((from[key]-val).abs<0.00001, {

					durationsPerKey[key]=0;
					keysWithoutTransition=keysWithoutTransition.add(key);
					setMsg=setMsg++[key, val];
				},{
					durations[key]=durations[key]??{duration};
					if (durations[key]<0.00001, {
						keysWithoutTransition=keysWithoutTransition.add(key);
						setMsg=setMsg++[key, val];
					},{
						keysWithTransition=keysWithTransition.add(key);
						curves[key]=curves[key]??{curve};
						delayTimes[key]=delayTimes[key]??{delayTime};
						bus[key]=bus[key]??{b=Bus.control(server, val.asArray.size); tmpBusses=tmpBusses.add(b); b};
						mapMsg=mapMsg++[key, bus[key]];
						if (specs[key]==nil, {
							specs[key]=[from[key],val].asSpec;
						},{
							specs[key].minval=from[key];
							specs[key].maxval=val;
						});
						maxDuration=(delayTimes[key]+durations[key]).max(maxDuration);
					});
				});
			});
		};

		if (map, {
			if (setMsg.size>2, {
				this.add([time, setMsg])
			});
		},{
			keysWithoutTransition.keysValuesDo{|key, val|
				if (busses[key]!=nil, {
					this.add([time, busses[key].setMsg(val)])
				});
			};
		});
		if (keysWithTransition.size>0, {
			synthDef=(\ScoreTransitionJT++server.nextNodeID).asSymbol;
			SynthDef(synthDef, {
				var env, envelope, out;
				env=Line.kr(0, 1, maxDuration, doneAction:2);
				envelope=Env([0.0, 0.0, 1.0], [delayTime, duration], curve).kr(0);
				out=keysWithTransition.sort.collect{|key|
					var morph;
					if ( (curves[key]!=curve)||(delayTimes[key]!=delayTime)||(durations[key]!=duration), {
						morph=Env([0.0, 0.0, 1.0], [delayTimes[key], durations[key]], curve[key]).kr(0);
					},{
						morph=envelope;
					});
					specs[key].map(morph)
				}.flat;
				Out.kr(bus[bus.keys.asArray.sort[0]].index, out)
			}).store; server.sync;
			this.add([time, [\s_new, synthDef, server.nextNodeID, 2, target.nodeID] ]);
			if (map, {
				this.add([time, target.mapMsg( *mapMsg )])});
		});
	}
}