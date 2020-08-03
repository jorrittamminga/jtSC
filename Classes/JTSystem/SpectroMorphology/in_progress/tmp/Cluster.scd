/*
0	add the new node to the the head of the group specified by the add target ID.
1	add the new node to the the tail of the group specified by the add target ID.
2	add the new node just before the node specified by the add target ID.
3	add the new node just after the node specified by the add target ID.
4	the new node replaces the node specified by the add target ID. The target node is freed.
*/
/*
ClusterJT {
	var <>path, <>type;
	var <>ratios, <>startFrame, <>endFrame;
	var <>controlSpecs, <>values, <>function, <buffer, <>numChannels, <>server, <synthDef;
	var <synthDefFunc, <synthDefName;
	var <>sampleRate, <>headerFormat, <>sampleFormat;
	var <>normalize, <score, <renderFlag;
	var <>addActionNr, <>targetID;
	var <>duration, <id, <synthDefSF, <synthDefFuncSF, <synthDefNameSF;
	var <synth, <oscFuncs, <>dirname, <>fileName, <clusterNr, <>normalizeFlag;

	*new {arg buffer, ratios, render, addActionNr, targetID, startFrame, endFrame, values
		, synthDef, function, type=\nothing;
		^super.new.init(buffer, ratios, render, addActionNr, targetID, startFrame, endFrame
			, values, synthDef, function, type)
	}

	init {arg argbuffer, argratios, argrender, argaddActionNr, argtargetID, argstartFrame
		, argendFrame, argvalues, argsynthDef, argfunction, argtype;
		buffer=argbuffer;
		id=UniqueID.next;
		this.defaults;
		ratios=argratios??{ratios};
		renderFlag=argrender??{renderFlag};
		addActionNr=argaddActionNr??{addActionNr};
		targetID=argtargetID??{targetID};
		startFrame=argstartFrame??{startFrame};
		endFrame=argendFrame??{endFrame};
		values=argvalues??{values};
		synthDef=argsynthDef??{synthDef};
		function=argfunction??{function};
		type=argtype??{\nothing};
		this.run;
	}

	*play{arg buffer;

	}

	*renderPlay{

	}

	*render{

	}

	initRun {
		score=nil;
		this.prepareScore;
		function.value;
		this.endScore;
	}

	run {arg typ;
		type=typ??{type};
		switch(type, \nothing, {

		},\play, {
			this.play;
		}, \render, {
			this.render;
		}, \renderPlay, {
			this.renderPlay;
		});
	}

	playRT {
		renderFlag=false;
		this.initRun;
		Score.play(score, server);
	}

	playSF {arg path, deleteAfterPlay=true;
		{
			var bufferSF;
			bufferSF=Buffer.cueSoundFile(server, path, 0, numChannels, 262144);
			server.sync;
			synth=Synth(synthDefNameSF, [
				\bufnum, bufferSF
				//\outBus, values[\outBus], \amp, values[\amp]
			], targetID, addActionNr).register;
			server.sync;
			oscFuncs.add(
				OSCFunc({arg msg;
					bufferSF.close;
					bufferSF.free;
					if (deleteAfterPlay, {
						File.delete(path)
					});
				}, '/n_end', server.addr, nil, [synth.nodeID]).oneShot;
			);
		}.fork
	}

	renderPlay {arg path, normalize, deleteAfterPlay=true;
		renderFlag=true;
		path=path??{this.makePath};
		this.initRun;
		this.render(path, normalize, {this.playSF(path, deleteAfterPlay)});
	}

	render {arg path, normalize, action;
		renderFlag=true;
		if (score==nil, {this.initRun});
		path=path??{this.makePath};
		normalizeFlag=normalize??{normalizeFlag};
		Score.render(score, path, numChannels, sampleRate, headerFormat, sampleFormat
			, action, false, normalizeFlag);
		score=nil;
	}

	makePath {
		clusterNr=clusterNr+1;
		path=dirname++"cluster_"++id++"_"++clusterNr++"."++headerFormat.toLower;
		^path
	}

	buffer_ {arg buf;
		buffer=buf;
		server=buffer.server;
	}

	makeGUI {}

	prepareScore {
		score=List[];
		if (renderFlag, {
			//score.add([0.0, ['/d_recv', synthDef.asBytes]]);
			score.add([0.0, [\b_allocRead, buffer.bufnum, buffer.path]]);
		});
	}
	endScore {
		score.add([duration, [\c_set, 0, 0]]);
	}

	free {
		oscFuncs.do{|o| o.free};
	}

	defaults {
		server=buffer.server;
		oscFuncs=List[];
		clusterNr=0;
		renderFlag=false;
		normalizeFlag=false;
		duration=1.0;
		ratios=(1..16);
		startFrame=0;
		endFrame= -1;
		controlSpecs=();
		addActionNr=0;
		targetID=1;
		numChannels=2;
		sampleRate=server.sampleRate;
		headerFormat="AIFF";
		sampleFormat="int24";
		dirname=thisProcess.platform.recordingsDir++"/";
		fileName="cluster.aiff";
		path=dirname++fileName;

		values=(
			sync:0.5
			, ampPow: 0.0
			, durPow: -1.0
			, attackTime: 0.5
			, releaseTime: 0.5
			, layers: 1
			, az: [-0.5, 0.5]
			, transpose: 1.0
			, startTimeDev: 0.0
			, detune: 1.0
			, startPos: [0, 0.0]
			, dur: -1
			, durDev: 1.0
			, cAttack: 2.0
			, cRelease: -4.0
			, outBus: 0
		);

		function={
			var bufferDuration, startTimes, events, sustainTime;
			var durs, tmpRatios, maxDur, minRatio, numberOfEvents, ampScale;
			if (endFrame<=0, {endFrame=buffer.numFrames});
			bufferDuration=(endFrame-startFrame).abs/buffer.sampleRate;
			tmpRatios=(
				ratios.deepCopy*.t(values[\transpose].asArray)
			).flat.sort.dup(values[\layers]).flat.collect{|ratio, i|
				exprand(values[\detune].reciprocal, values[\detune])*ratio
			}.flat.sort;
			minRatio=tmpRatios.minItem;
			if (values[\dur]<=0, {
				durs=tmpRatios.pow(values[\durPow]).collect{|ratio,i|
					ratio*exprand(values[\durDev].reciprocal, 1)
				}*bufferDuration;
			},{
				durs=tmpRatios.collect{|ratio,i|
					ratio.pow(values[\durPow])
					*
					exprand(values[\durDev].reciprocal, values[\durDev])
				};
				durs=(durs/durs.maxItem) * values[\dur];
			});
			maxDur=durs.maxItem;
			startTimes=durs.collect{|dur,i|
				(maxDur-dur)*values[\sync]+(values[\startTimeDev].rand)
			};
			duration=(startTimes+durs).maxItem;
			numberOfEvents=startTimes.size;
			ampScale=numberOfEvents.reciprocal.sqrt;

			values[\attackTime]=values[\attackTime].min(1-values[\releaseTime]);
			sustainTime=(1-values[\attackTime]-values[\releaseTime]).clip(0,1);

			startTimes.do{|startTime,i|
				score.add([startTime, [\s_new, synthDefName, -1, addActionNr, targetID
					, \attackTime, values[\attackTime]*durs[i]
					, \sustainTime, sustainTime*durs[i]
					, \releaseTime, values[\releaseTime]*durs[i]
					, \bufnum, buffer.bufnum
					, \rate, tmpRatios[i]
					, \amp, (minRatio/tmpRatios[i]).pow(values[\ampPow])*ampScale
					, \az, rrand(values[\az][0],values[\az][1])
					, \startFrame, rrand(values[\startPos][0], values[\startPos][1])
					*buffer.numFrames
					, \outBus, 0//values[\outBus]
					, \cAttack, values[\cAttack]
					, \cRelease, values[\cRelease]
				]]);
			};
		};

		synthDefFunc={arg bufnum, rate=1.0, attackTime=0.0, sustainTime=1.0
			, releaseTime=1.0, amp=1.0, az=0.0, startFrame=0.0, outBus=0, cAttack= 4.0
			, cRelease= -4.0;
			var env=EnvGen.kr(Env.linen(attackTime, sustainTime, releaseTime, 1.0
				, [cAttack, 1, cRelease]), doneAction:2);
			var out;
			out=PlayBuf.ar(1, bufnum, rate, 1, startFrame, 1)*env;
			Out.ar(outBus, PanAz.ar(numChannels, out, az, amp))
		};
		synthDefName=(\ClusterJT++id).asSymbol;
		synthDef=SynthDef(synthDefName, synthDefFunc);

		synthDefFuncSF={arg bufnum, amp=1.0, gate=1.0, fadeIn=0.0, fadeOut=3.0, outBus=0;
			var env=EnvGen.ar(Env.asr(fadeIn, 1, fadeOut), gate, doneAction:2);
			var out;
			out=VDiskIn.ar(numChannels, bufnum, BufRateScale.ir(bufnum));
			FreeSelfWhenDone.kr(out);
			Out.ar(outBus, out*amp.lag(0.1))
		};
		synthDefNameSF=(\ClusterSFJT++id).asSymbol;
		synthDefSF=SynthDef(synthDefNameSF, synthDefFuncSF);

		synthDef.add;
		synthDef.load(server);
		synthDefSF.add;
	}

	synthDefFunc_ {arg func;
		synthDefFunc=func;
		synthDef=SynthDef(synthDefName, synthDefFunc);
	}
}
*/