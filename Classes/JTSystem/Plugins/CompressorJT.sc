CompressorJT : PluginJT {
	var <defaultSettings, <uGen;
	var <limiter, <sanitize, synthdefsettings;

	*new {arg inBus, target, uGen=\CompanderC, settings=(thresh: -10, slopeAbove: 0.5
		, clampTime:0.01, relaxTime:0.1, limiter:false, sanitize: false);
	^super.new.init(inBus, target, uGen, settings);
	}

	init {arg arginBus, argtarget, arguGen, argsettings;
		var extraControlSpecs;
		bus=arginBus;
		target=argtarget;
		uGen=arguGen;
		settings=argsettings??{()};
		synthdefsettings=();
		id=UniqueID.next;
		//this.initializeVars;
		defaultSettings=(
			CompanderC: (thresh: -10, slopeAbove: 0.5, clampTime:0.01, relaxTime:0.1
				, boost:1, makeUpGain: 0, limiter:false, sanitize:false),
			//			CompanderD: (thresh: -10, slopeAbove: 0.5, clampTime:0.01, relaxTime:0.1
			//				, boost:1),
			//			Compander: (thresh: -10, slopeAbove: 0.5, clampTime:0.01, relaxTime:0.1
			//				, boost: 1.0),
			SoftKneeCompressor: (thresh: -10, slopeAbove: 1, knee: 6, clampTime: 0
				, relaxTime: 0.05, makeUpGain: 0, limiter:false, sanitize:false
				//, makeUp: 0, rms: 0
			)
		)[uGen];
		defaultSettings.keysValuesDo{|key,val|
			if (settings[key]==nil, {settings[key]=val})
		};
		[\limiter, \sanitize].do{|key|
			synthdefsettings[key]=settings[key].copy??{false};
			settings.removeAt(key);
		};
		//if (settings[\knee]==nil, {settings[\knee]=0});
		//if (settings[\makeUp]==nil, {settings[\makeUp]=6});
		//if (settings[\rms]==nil, {settings[\rms]=0});
		//if (settings[\boost]==nil, {settings[\boost]=1});

		controlSpecs=if (uGen==\SoftKneeCompressor, {
			(thresh: \db.asSpec, slopeAbove: ControlSpec(1.0, 1/20)
				, knee: ControlSpec(0, 60, 4.0)
				, clampTime: ControlSpec(0.0, 1.0, 4.0)
				, relaxTime: ControlSpec(0.0, 1.0, 4.0)
				, makeUpGain: ControlSpec(-20, 20, 0, 1)
				//, makeUp: ControlSpec(-20, 20)
				//, rms: ControlSpec(0, 8192, 4.0, 1)
			);
		},{
			(thresh: \db.asSpec, slopeAbove: ControlSpec(1.0, 1/20, -2.0)
				, clampTime: ControlSpec(0.0, 1.0, 4.0)
				, relaxTime: ControlSpec(0.0, 1.0, 4.0)
				, boost: ControlSpec(0.25, 8.0, \exp)
				, makeUpGain: ControlSpec(-20, 20, 0, 1)
			);
		});

		//----------------------------------------------------- bypass settings
		controlSpecs[\run]=ControlSpec(0.0, 1.0, 0, 1);
		bypass=if (settings[\run]==nil, {
			settings[\run]=1.0;
			false
		},{
			settings[\run]<1.0
		});
		bypassFunc={};
		//-----------------------------------------------------
		this.isThreaded;
		if (threaded, {
			this.makeSynth;
		},{
			{
				this.makeSynth;
			}.fork
		});
	}

	makeSynth {
		synth=bus.asArray.collect{|bus, i|
			var synth, synthDef, defname=(\Compressor++id).asSymbol;
			if (bus.class!=Bus, {bus=bus.asBus});
			synth=switch(uGen, \CompanderC, {
				SynthDef(defname, {arg thresh= 0, slopeAbove=0.5, clampTime=0.01
					, relaxTime=0.1, makeUpGain=0;
					var in, out;
					in=In.ar(bus.index, bus.numChannels);
					if (synthdefsettings[\sanitize], {in=Sanitize.ar(in)});
					out=CompanderC.ar(
						//DelayN.ar(in, 0.1, clampTime)
						in
						, in, thresh.dbamp.lag(0.1), 1.0
						, slopeAbove.lag(0.1), clampTime.lag(0.1), relaxTime.lag(0.1)
						, makeUpGain.dbamp.lag(0.1));
					if (synthdefsettings[\limiter]
						, {out=Limiter.ar(out, 0.99999999, clampTime)});
					ReplaceOut.ar(bus.index, out)
				})
			}, \SoftKneeCompressor, {
				SynthDef(defname, {arg thresh= 0, slopeAbove=0.5, clampTime=0.01
					, relaxTime=0.1, makeUpGain=0, knee=6;
					var in, out;
					in=In.ar(bus.index, bus.numChannels);
					//[thresh, slopeAbove, knee, makeUpGain].poll(1);

					out=SoftKneeCompressor.ar(
						//DelayN.ar(in, 0.1, clampTime)
						in
						, in, thresh.lag(0.1)
						, slopeAbove.lag(0.1), knee.lag(0.1), clampTime.lag(0.1)
						, relaxTime, 1)*makeUpGain.dbamp.lag(0.1);
					if (synthdefsettings[\sanitize], {out=Sanitize.ar(out)});
					if (synthdefsettings[\limiter], {
						out=Limiter.ar(out, 0.99999999, clampTime.max(0.01))
					});
					ReplaceOut.ar(bus.index, out)
				})
			}).add.play(
				target.asArray[i]
				, settings.asKeyValuePairs
				,\addAfter).register;
			target.asArray[i].server.sync;
			synth
		};
		id=synth.collect(_.nodeID);
		if (synth.size==1, {synth=synth[0]; id=id[0]});
		if (bypass, {this.bypass_(bypass)});
	}

	makeGUI {arg parent, bounds=350@20, updateFreq=20, frontFlag=true;
		gui=CompressorJTGUI(this, parent, bounds, updateFreq, frontFlag);
		^gui
	}

}

CompressorJTGUI : GUIJT {
	var <synth, <>dBLow, <updateFreq, <flag;

	*new {arg compressor, parent, bounds, updateFreq=20, frontFlag;
		^super.new.init(compressor, parent, bounds, updateFreq, frontFlag);
	}

	init {arg argcompressor, argparent, argbounds, argupdateFreq, argfrontFlag;
		var keys;
		classJT=argcompressor; parent=argparent; bounds=argbounds;
		frontFlag=argfrontFlag;

		synth=();
		this.initAll;
		keys=classJT.settings.keys;
		keys.remove(\thresh);
		keys.remove(\run);
		oscGUI=();
		updateFreq=argupdateFreq;
		dBLow= -80;

		viewsPreset[\run]=Button(parent, bounds)
		.states_([["bypassed"],["ON", Color.black, Color.green]]).action_{|b|
			var run=b.value.binaryValue;
			classJT.bypass_(b.value<1);
			classJT.settings[\run]=b.value;
			[\meterBefore, \meterAfter].do{|key|
				if (synth[key]!=nil, {synth[key].run(run)})
			};
		}.value_(classJT.bypass.not.binaryValue);

		views[\meterBefore]=LevelIndicator(parent, bounds)
		.warning_(0.9).critical_(1.0).drawsPeak_(true);
		viewsPreset[\thresh]=EZSlider(parent, bounds, "", ControlSpec(-80, 0), {|sl|
			//var val=sl.value.linlin(0, 1.0, -80, 0.0);
			classJT.synth.asArray.do(_.set(\thresh, sl.value));
			classJT.settings[\thresh]=sl.value;
		}, classJT.settings[\thresh], false, 0, 0, 0, gap: 0@0, margin: 0@0);

		//.value_(classJT.settings[\thresh].linlin(-80,0,0,1.0));
		/*
		viewsPreset[\thresh]=Slider(parent, bounds).action_{|sl|
		var val=sl.value.linlin(0, 1.0, -80, 0.0);
		classJT.synth.asArray.do(_.set(\thresh, val));
		classJT.settings[\thresh]=val;
		}.value_(classJT.settings[\thresh].linlin(-80,0,0,1.0));
		*/
		views[\meterAfter]=LevelIndicator(parent, bounds)
		.warning_(0.9).critical_(1.0).drawsPeak_(true);

		[\meterBefore,\meterAfter].do{|key,i|
			var cmdName=("/compressor" ++ classJT.synth.asArray[0].nodeID ++ key).asString;
			var run=classJT.bypass.not??{true};
			{
				synth[key]=SynthDef(cmdName, {
					var in=In.ar(classJT.bus.asArray[0].index);
					SendPeakRMS.kr(in, updateFreq, 3.0, cmdName);
					//Out.ar(0, SinOsc.ar(440,0,0))
				}).add.play(classJT.synth.asArray[0], addAction: [\addBefore,\addAfter][i]).register;
				synth[key].server.sync;
				if (run==false, {
					synth[key].run(false)});
			}.fork;

			oscGUI[key]=OSCFunc({|msg|
				var db=msg[3+1].ampdb;
				var value=db.linlin(dBLow, 0, 0, 1);
				var peak=msg[3].ampdb.linlin(dBLow, 0, 0, 1, \min);
				{
					views[key].value_(value);
					views[key].peakLevel_(peak);
					//views[1].value_(db);
				}.defer
			}, cmdName);
		};
		keys.do{|label|
			var val=classJT.settings[label];
			viewsPreset[label]=this.makeEZGUI(bounds, label, classJT.controlSpecs[label]
				, {|ez|
					classJT.synth.asArray.do(_.set(label, ez.value));
					classJT.settings[label]=ez.value;
			}, val);
		};
		//this.addPresetSystem(viewsPreset
		this.postInitAll;
		this.reboundsAll;
		//window.userCanClose_(false);
		window.onClose=window.onClose.addFunc({
			oscGUI.do(_.free);
			synth.do{|synth| if (synth.isPlaying, {synth.free})};
		});
	}
}

/*
/*
out=(CompanderC: CompanderC.ar(in, in, p[\thresh].dbamp, 1.0
, p[\slopeAbove], p[\clampTime], p[\relaxTime], p[\boost]),
CompanderD: CompanderD.ar(in, p[\thresh].dbamp, 1.0
, p[\slopeAbove].lag(0.1), p[\clampTime], p[\relaxTime], p[\boost]),
Compander: Compander.ar(in, in, p[\thresh].dbamp, 1.0
, p[\slopeAbove], p[\clampTime], p[\relaxTime], p[\boost])

, SoftKneeCompressor: SoftKneeCompressor.ar(
in, in
, p[\thresh]
, p[\slopeAbove]
, p[\knee]
, p[\clampTime]
, p[\relaxTime]
//, p[\makeUp]
//, p[\rms]
)

)[uGen];
*/
*/