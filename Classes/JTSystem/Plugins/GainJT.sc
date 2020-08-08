GainJT : PluginJT {
	var <defaultSettings, <type, <order;
	var showScope, <flags, <argz;

	*new {arg inBus, target, settings=(), addAction=\addAfter;
		^super.new.init(inBus, target, settings, addAction);
	}

	init {arg arginBus, argtarget, argsettings, argaddAction;
		var extraControlSpecs, types;
		bus=arginBus;
		target=argtarget;
		settings=argsettings??{()};
		addAction=argaddAction;
		id=UniqueID.next;
		//this.initializeVars;

		defaultSettings=(boost:0, lagTime: 0.1);
		controlSpecs=(boost: ControlSpec(-20, 60, 0, 1), lagTime: ControlSpec(0, 30, 4));

		defaultSettings.keysValuesDo{|key,val|
			if (settings[key]==nil, {settings[key]=val})
		};
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
			this.boost(settings[\boost]);
		},{
			{
				this.makeSynth;
			}.fork
		});
	}

	boost {arg b=0;
		settings[\boost]=b;
		settings[\amp]=b.dbamp;
		if (synth.isRunning, {synth.asArray.do{|syn| syn.set(\amp, settings[\amp])}});
	}

	makeSynth {
		synth=bus.asArray.collect{|bus, i|
			var synth;
			if (bus.class!=Bus, {bus=bus.asBus});
			synth=SynthDef((\GainJT++id).asSymbol, {arg amp=1, lagTime=0.1;
				var in;
				in=In.ar(bus.index, bus.numChannels)*amp.lag(lagTime);
				ReplaceOut.ar(bus.index, in)
			}, metadata: (specs: controlSpecs)
			).add.play(target.asArray[i], settings.asKeyValuePairs, addAction).register;
			target.asArray[i].server.sync;
			synth
		};
		id=synth.collect(_.nodeID);
		if (synth.size==1, {synth=synth[0]; id=id[0]});
		if (bypass, {this.bypass_(bypass)});
	}

	makeGUI {arg parent, bounds=100@20, onClose=false;
		gui=GainGUIJT(this, parent, bounds, onClose);
		^gui
		//this.initAll;
	}

	addPresetSystem {arg path, folderName="master", index=0;
		var func={arg preset;
			if (preset[\run]!=nil, {
				this.bypass_(preset[\run]<1.0)
			});
			if (preset[\boost]!=nil, {
				this.boost(preset[\boost])
			});
		};
		this.metaAddPresetSystem(path, folderName, index, func)
	}

}


GainGUIJT : GUIJT {
	var <gain, <showScope, <scope, <controlSpecs, <settings;
	var <width2, <height2, boundsKnob;
	var <maxHeight;

	*new {arg gain, parent, bounds, onClose=false;
		^super.new.init(gain, parent, bounds, onClose);
	}

	init {arg arggain, argparent, argbounds, argonClose;
		gain=arggain;
		parent=argparent;
		bounds=argbounds;
		classJT=arggain;

		controlSpecs=gain.controlSpecs;
		settings=gain.settings;
		//of settings=synth.getAll; server.sync;
		freeOnClose=argonClose;

		this.initAll;

		viewsPreset[\run]=Button(parent, bounds)
		.states_([[\bypassed],[\ON, Color.black, Color.green]]).action_{|b|
			classJT.bypass_(b.value<1);
			classJT.settings[\run]=b.value;
		}.value_(classJT.bypass.not.binaryValue);

		viewsPreset[\boost]=EZKnob(parent, bounds.x@bounds.x, \boost, controlSpecs[\boost], {|ez|
			classJT.boost(ez.value)
		}, settings[\boost], false, 60);
		parent.rebounds;

		this.postInitAll;

		if (hasWindow, {window.rebounds});
	}
}