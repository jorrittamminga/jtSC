ConcatJTPlayer : ConcatJTSystem {

	var <>concatJTrecorder;

	*new {arg inBusA, outBus, target, path, synthDef, specs;
		^super.new.init(inBusA, outBus, target, path, synthDef, specs);
	}

	init {arg arginBus, argoutBus, argtarget, argpath, argsynthDef, argspecs;
		var threadedFunc;
		id=UniqueID.next;
		inBusA=arginBus;
		outBus=argoutBus;
		target=argtarget;
		server=target.server;
		path=argpath;
		synthDef=argsynthDef;
		specs=argspecs;

		this.initSettings;
		this.getFileNames;
		if (outBus.class!=Bus, {outBus=outBus.asBus(\audio, outBus.asArray.size, server)});

		threadedFunc={
			this.readKDTree(fileName);
			this.makeSynthDef; server.sync;
			this.makeSynth;
		};
		if (threaded, {threadedFunc.value},{{threadedFunc.value}.fork});
	}

	free {
		synth.free;
		bufKDTree.free;
		bufRecording.free;
	}

	close { this.free }

	makeSynth {}

	makeSynthDef {
		synthDef=(\PlayerConcat_++id).asSymbol;
		SynthDef(synthDef, {arg outBus, bufnumTree, bufnum, gate=1.0
			, grainDur=#[0.15,0.15], overLap=2, amp=0, tDev=0.001, dry=0, gateN=1
			, azDev=1.0, rate=1.0, rateDev=0.0;
			var in, fft, analysis, centerPos, out, env;
			env=EnvGen.kr(Env.asr(0.01, 1, 0.01), gate, doneAction:1);

			//analysis=inBusA.collect{|bus| In.kr(bus.index,bus.numChannels)}.flat;
			analysis=inBusA.collect{|bus| In.kr(bus)};

			centerPos=NearestN.kr(bufnumTree, analysis, gateN)[2]
			/SampleRate.ir+WhiteNoise.kr(tDev);
			grainDur=WhiteNoise.kr(1.0).exprange(grainDur[0],grainDur[1]);
			out=TGrains.ar(outBus.numChannels, Impulse.ar(grainDur.reciprocal*overLap)
				, bufnum
				, BufRateScale.kr(bufnum)*WhiteNoise.kr(rateDev*rate, rate)
				, centerPos, grainDur, WhiteNoise.kr(azDev), overLap.pow(-0.333));
			out=in*dry+out;
			out=out*env;
			Out.ar(outBus, amp.dbamp.lag(0.1)*out)
		}, metadata: (specs: (
			grainDur: ControlSpec(0.01, 1.0, \exp), overLap: ControlSpec(2, 16, \exp)
			, amp: \db.asSpec, tDev: ControlSpec(0.0, 2.0, 8.0), dry: \amp.asSpec
			, gateN: ControlSpec(0,1,0,1), azDev: \unipolar.asSpec
			, rate: ControlSpec(1/16, 16, \exp), rateDev: ControlSpec(0.0, 1.0, 4.0)
		))).add;
	}

	makeSynthDefExt {arg ugenGraphFunc, specs;
		synthDef=(\PlayerConcat_++id).asSymbol;
		SynthDef(synthDef, ugenGraphFunc.value(inBusA), metadata: (specs: specs)).add
	}

	startPlaying {
		if (synth==nil, {
			synth=Synth(synthDef, [\outBus, outBus, \bufnum, bufRecording
				, \bufnumTree, bufKDTree],
			target, if (target.class==Synth, {\addAfter},{\addToTail})).register;
		},{
			synth.run(true);
			synth.set(\gate, 1);
		})
	}

	stopPlaying {
		synth.set(\gate, 0);
	}
	pausePlaying {
		this.stopPlaying//synth.set(\gate, 0);
	}
	resumePlaying {
		this.startPlaying
	}

	readKDTree {arg name="test";
		var nn=path++"NN/"++name++".aiff", file=path++name++".aiff";
		var isRunning=synth.isRunning;
		if (isRunning, {
			synthPlayer.run(false);
		});

		if( File.exists(file) && File.exists(nn), {
			fileName=name;
			{
				bufKDTree=if (bufKDTree==nil, {
					Buffer.read(server, nn);
				},{
					Buffer.read(server, nn, bufnum:bufKDTree.bufnum);
				});
				server.sync;
				bufRecording=if (bufRecording==nil, {
					Buffer.read(server, file);
				},{
					Buffer.read(server, file, bufnum:bufRecording.bufnum);
				});
				server.sync;
				if (synth!=nil, {
					synth.set(\bufnum, bufRecording, \bufnumTree, bufKDTree);
				});
				if (isRunning, {synth.run(true);});
			}.fork
		},{
			"files do not exist".postln;
		});
	}

	makeGUI {arg parent, bounds=350@20;
		{
			gui=ConcatJTPlayerGUI(this, parent, bounds)
		}.defer
	}
}


ConcatJTPlayerGUI {
	var <guis, <window, <concatJTPlayer, parent, bounds, cv;

	*new {arg concatJTPlayer, parent, bounds;
		^super.new.init(concatJTPlayer, parent, bounds);
	}

	init {arg argconcatJTPlayer, argparent, argbounds;
		concatJTPlayer=argconcatJTPlayer;
		parent=argparent;
		bounds=argbounds;
		if (parent==nil, {
			window=Window("ConcatJTPlayer", (bounds.x+8)@(bounds.y+8)).front;
			window.alwaysOnTop_(true);
			window.addFlowLayout;
		});
		cv=CompositeView(parent, bounds);
		cv.addFlowLayout(0@0, 4@4);
		guis=();
		guis[\playB]=Button(cv, (bounds.x*(1/6)-4).flat@bounds.y)
		.states_([ [\play],[\play,Color.black, Color.green]]).action_{|b|
			if (b.value==1, {
				concatJTPlayer.startPlaying;
			},{
				concatJTPlayer.stopPlaying;
			})
		};
		guis[\fileNames]=PopUpMenu(cv, (bounds.x*(1/6)-4)@bounds.y)
		.items_(concatJTPlayer.fileNameArray)
		.action_{|l|
			concatJTPlayer.readKDTree(concatJTPlayer.fileNameArray[l.value])
		}.valueAction_(concatJTPlayer.fileNameArray.indexOf(concatJTPlayer.fileName));
		if (window!=nil, {
			window.onClose_{concatJTPlayer.close}
		});
	}
}