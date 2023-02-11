/*
- voor PlayerJT moet nog een rerouter (en mixbus) gemaakt worden voor complexere routings
*/
InJT : IOJT {
	var <busForMeter, <synthForMeter, <groupForMeter;
	//var <soundInOffset;

	*new {arg inBus, server, label, addAction=\addBefore, hasInputGains=false;//hasInputGains=false
		^super.new.init(inBus, server, label, addAction, hasInputGains);
	}

	init {arg arginBus, argtarget, arglabel, argaddAction, arghasInputGains;
		hasInputGains=arghasInputGains;
		this.isThreaded;//dit moet je dus sws doen! eigenlijk bij alle JT class
		if (threaded, {
			this.initFunc(arginBus, argtarget, arglabel, argaddAction)
		},{ {
			this.initFunc(arginBus, argtarget, arglabel, argaddAction)
		}.fork
		})
	}
	makeSynth {//arg hasInputGains=false;
		if (hasInputGains, {gains=Array.fill(labels.flatten.size, {1.0})},{gains=nil});
		^servers.collect{|server, serverIndex|
			var synth, synthDef=(\InJT++serverIndex).asSymbol, indices;
			indices=busIndexPerServer[serverIndex].deepCopy.unbubble.collect{|index,i|
				i//inBus[serverIndex].deepCopy.unbubble.indexOf(index)
			};
			synth=SynthDef(synthDef, {
				var in, gains;
				if (hasInputGains&&(indices!=nil), {
					gains=indices.collect{|i|
						NamedControl.kr((\gain_++labels[i]).asSymbol, 1.0, 0.1)
					};
					in=SoundIn.ar(busIndexPerServer[serverIndex].unbubble, gains);
				},{
					in=SoundIn.ar(busIndexPerServer[serverIndex].unbubble);
				});
				//in=SoundIn.ar(busIndexPerServer[serverIndex].unbubble);
				Out.ar(busPerServer[serverIndex].index, in)
			}).play(group[serverIndex], [], \addToHead);
			server.sync;
			synth
		};
	}

	optimizeSynthAndBusForMeter {
		busForMeter=busPerFlatIndex.asArray.collect{|b| b.asArray[0]};
		synthForMeter=synthPerFlatIndex.asArray.collect{|syn| syn.asArray[0]};
		groupForMeter=synthForMeter.collect{|syn| syn.group};
	}
	/*
	free {
	synth.asArray.do(_.free);
	group.asArray.do(_.free);
	if (gui!=nil, {gui.close});
	//plugins.keysValuesDo{|key,val| val.asArray.flat.do{|class| class.free}};
	}
	*/
	/*
	addInputGains {
	synth.do(_.free);
	gains=Array.fill(labels.flatten.size, {1.0});
	this.makeSynth(true);
	}
	*/
	addPlugin {arg type=\Meter, args=[];
		var plugIn, func;
		func=switch(type
			, \Meter, {{arg target, updateFreq=20;
				this.optimizeSynthAndBusForMeter;
				target=target??{groupForMeter};
				MeterJT(busForMeter, target, updateFreq, 3.0, this);
			}}
			, \Player, {{arg path, monitorBus=0, monitorChannels=2, monitorServerID=0;
				var buz=bus.copy, player;

				player=PlayerJT(buz, synth, path);
				player.addMonitor(monitorBus, monitorChannels, 0, monitorServerID);
				player.startPlayingFunc=player.startPlayingFunc.addFunc({
					synth.asArray.do{|syn| syn.run(false)};
					player.monitor.run(true);
				});
				player.stopPlayingFunc=player.stopPlayingFunc.addFunc({
					synth.asArray.do{|syn| syn.run(true)};
					player.monitor.run(false);
				});
				/*
				hier een bus rerouter per server maken, als dat moet
				en een mixbus voor één server om te monitoren
				*/
				player
			}}
			, \Recorder, {{arg path, sampleFormat="int24", headerFormat="AIFF";
				var ser=servers.asArray[0];
				RecorderJT(ser.options.numOutputBusChannels+inBusFlat
					, ser, path, sampleFormat, headerFormat)
			}}
		);
		this.isThreaded;
		if (threaded.not, {
			{
				plugIn=func.value(*args);
				this.addPlugins(type, plugIn);
			}.fork
		},{
			plugIn=func.value(*args);
			this.addPlugins(type, plugIn);

		});
		^plugIn
		//^this
	}

	makeGUI {arg parent, bounds=30@20, margin=2@2, gap=0@0, parentMargin=4@4, parentGap=4@2, meterHeight;
		gui=InJTGUI(this, parent, bounds, margin, gap, parentMargin, parentGap, meterHeight);
	}
	makeGui {arg parent, bounds=30@20, margin=2@2, gap=0@0, parentMargin=4@4, parentGap=4@2, meterHeight;
		gui=InJTGUI(this, parent, bounds, margin, gap, parentMargin, parentGap, meterHeight);
	}
}

InJTGUI : IOJTGUI {}
