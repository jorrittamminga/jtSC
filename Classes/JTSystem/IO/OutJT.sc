/*
zorg ervoor dat er automatisch een masterfader wordt toegevoegd als er een plugin zoals Compressor wordt toegevoegd
*/
OutJT : IOJT {
	var <amp, <db;
	//	var <busForMeter, <synthForMeter, <groupForMeter;

	*new {arg inBus, server, label, addAction=\addAfter;
		^super.new.init(inBus, server, label, addAction);
	}

	init {arg arginBus, argtarget, arglabel, argaddAction;
		amp=1.0; db=0;
		mute=false;
		this.isThreaded;
		if (threaded, {
			this.initFunc(arginBus, argtarget, arglabel, argaddAction, false)
		},{ {
			this.initFunc(arginBus, argtarget, arglabel, argaddAction, false)
		}.fork
		})
	}

	mute_ {arg value=true;
		mute=value;
		synth.do{|syn| if (mute, {syn.set(\amp, 0)},{syn.set(\amp, amp)})};
	}

	amp_ {arg value=1.0;
		amp=value;
		db=amp.ampdb;
		synth.do{|syn| syn.set(\amp, value)};
	}

	db_ {arg value=0.0;
		db=value;
		this.amp_(value.dbamp)
	}

	makeSynth {
		^servers.collect{|server, serverIndex|
			var synth, synthDef=(\OutJT++serverIndex).asSymbol;
			synth=SynthDef(synthDef, {arg amp=1.0, lagTime=0.1;
				var in=In.ar(busIndexPerServer[serverIndex]);
				/*
				var amps={|i| NamedControl.kr((\amp++i).asSymbol, 1, 0.1)}
				!inBussesPerServerIndex[i].size;
				in=inBussesPerServerIndex[i].collect{|i| In.ar(i)*amps[i] };
				in=in*amp.lag(0.1);
				*/
				in=in*amp.lag(lagTime);
				busIndexPerServer[serverIndex].collect{|bus,i|
					ReplaceOut.ar(bus, in[i])
				};
			}).play(group[serverIndex], [], \addToTail);
			server.sync;
			synth
		};
	}
	/*
	optimizeSynthAndBusForMeter {
	busForMeter=busPerFlatIndex.asArray.collect{|b| b.asArray[0]};
	synthForMeter=synthPerFlatIndex.asArray.collect{|syn| syn.asArray[0]};
	groupForMeter=synthForMeter.collect{|syn| syn.group};
	}
	*/
	addPlugin {arg type=\Meter, args=[];
		var plugIn, func;
		if (plugins==nil, {plugins=()});
		func=switch(type
			, \Meter, {{arg target, updateFreq=20;
				//this.optimizeSynthAndBusForMeter;
				target=target??{group};
				MeterJT(busIndexPerServer, target, updateFreq);
			}}
			, \MasterFader, {{
				true
			}}
			, \EQ, {{arg type=\fiveBand, settings=();
				if (plugins[\MasterFader]==nil, {this.addPlugin(\MasterFader)});
				EQJT(bus, synth, type, settings)
			}}
			, \Splay, {{arg outBus=0, run=false;
				if (plugins[\MasterFader]==nil, {this.addPlugin(\MasterFader)});
				SplayJT(bus, synth, outBus, run)
			}}
			, \Compressor, {{arg uGen=\CompanderC, settings=(thresh: -10, slopeAbove: 0.5
				, clampTime:0.01, relaxTime:0.1, limiter:false, sanitize:false);
			//replaceSynth=true;
			if (plugins[\MasterFader]==nil, {this.addPlugin(\MasterFader)});
			CompressorJT(bus, synth, uGen, settings)
			}}
		);
		this.isThreaded;
		//if ((type==\MasterFader) && (plugins[\MasterFader]!=nil, {},{});
		if (threaded.not, {
			{
				plugIn=func.value(*args);
				this.addPlugins(type, plugIn);
				//^plugIn
			}.fork;
		},{
			plugIn=func.value(*args);
			this.addPlugins(type, plugIn);
			//^plugIn
		});
	}

	makeGUI {arg parent, bounds=30@20, margin=2@2, gap=0@0, parentMargin=4@4
		, parentGap=4@2, meterHeight;
		gui=OutJTGUI(this, parent, bounds, margin, gap, parentMargin, parentGap, meterHeight);
	}
}

OutJTGUI : IOJTGUI { }