JTSingle : JT {

	var <muteSynth, <index;

	*new {arg inBus, target, label, index;
		^super.new.init(inBus, target, label, index);
	}

	init {arg arginBus, argtarget, arglabel, argindex;
		inBus=arginBus;
		target=argtarget;
		label=arglabel;
		index=argindex;

		synth=[]; group=[]; server=[];

		target.asArray.do{|target, i|
			switch(target.class, Synth, {
				synth=synth.add(target);
				group=group.add(target.group);
				server=server.add(target.server);
			}, Server, {
				server=server.add(target);
			}, Group, {
				group=group.add(target);
				server=server.add(group.target);
			});
		};
		servers=server;

		bus=inBus.unbubble;
		server=server.unbubble;
		group=group.unbubble;
		synth=synth.unbubble;
	}
	gain_ {arg gain=1.0;
		synth.do{|syn|
			syn.set((\gain_++label).asSymbol, gain)
		};
		//gains[index]=gain;
	}
	mute_ {arg value=true;
		var synthDef=(\mute++label).asSymbol;
		mute=value;

		if (mute, {
			muteSynth=server.asArray.collect{|server, serverIndex|
				SynthDef((synthDef++serverIndex).asSymbol, {
					ReplaceOut.ar(bus.asArray[serverIndex], DC.ar(0.0))})
				.play(synth.asArray[serverIndex], [], \addAfter).register
			}.unbubble;
		},{
			muteSynth.asArray.do({arg synth;
				if (synth.isPlaying, {synth.free})
			});
		})
	}

	addPlugin {arg type=\Compressor, args=[];
		var plugIn, func, replaceSynth=false;
		func=switch(type
			, \Compressor, {{arg uGen=\CompanderC, settings=(thresh: -10, slopeAbove: 0.5
				, clampTime:0.01, relaxTime:0.1);
			replaceSynth=true;
			CompressorJT(bus, synth, uGen, settings)
			}}
			, \EQ, {{arg type=\fiveBand, settings=();
				replaceSynth=true;
				EQJT(bus, synth, type, settings)
			}}
			, \Gain, {{arg settings=();
				replaceSynth=true;
				GainJT(bus, synth, settings)
			}}
			, \BufWr, {{
				BufWrJT(bus.asArray[0], synth.asArray[0]
					, 60, false, false, true, true, true, false)
			}}
			, \Player, {{arg path, monitorBus=0, monitorChannels=2, monitorServerID=0;
				var player;
				replaceSynth=false;
				player=PlayerJT(bus.asArray[0], group.asArray[0], path, \addToHead);
				player.addMonitor(monitorBus, monitorChannels, 0, monitorServerID);
				player.startPlayingFunc=player.startPlayingFunc.addFunc({
					synth.asArray.do{|syn| syn.run(false)};
					player.monitor.run(true);
				});
				player.stopPlayingFunc=player.stopPlayingFunc.addFunc({
					synth.asArray.do{|syn| syn.run(true)};
					player.monitor.run(false);
				});
				player
			}}
			, \Dry, {{arg settings=(outBus: Bus.new(\audio, 0, 2, Server.default), amp:0
				, az:0), serverID=0;
				DryJT(bus.asArray[serverID], synth.asArray[0].group, \addToTail, settings);
			}}

			, \Analyzer, {{arg descriptors=[\onsets, \loudness], settings=()
			, metadataSpecs=(), outFlag=true, sendreplyFlag=true, outFFTFlag=true
			, fftsizes=(), hopsizes=(), updateFreq, normalized=false;
				var analyzer;
				analyzer=AnalyzerJT(bus, synth, descriptors, settings, metadataSpecs
					, outFlag, sendreplyFlag, outFFTFlag, fftsizes, hopsizes, updateFreq
					, normalized);
				analyzer
			}}

		);

		this.isThreaded;
		if (threaded.not, {
			{
				plugIn=func.value(*args);
				plugIn.name=(type++"_"++label).asString;
				this.addPlugins(type, plugIn, replaceSynth);
			}.fork
		},{
			plugIn=func.value(*args);
				plugIn.name=(type++"-"++label).asString;
			this.addPlugins(type, plugIn, replaceSynth);
		});
		^plugIn
	}

}