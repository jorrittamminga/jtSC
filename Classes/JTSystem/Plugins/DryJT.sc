DryJT : PluginJT {
	var <defaultSettings;
	var <limiter, <sanitize, synthdefsettings;

	*new {arg inBus, target, addAction=\addToTail, settings=(amp:0.0, az:0);
		^super.new.init(inBus, target, addAction, settings);
	}

	init {arg arginBus, argtarget, argaddAction, argsettings;
		var extraControlSpecs;
		bus=arginBus;
		target=argtarget;
		addAction=argaddAction;
		if (bus.class!=Bus, {bus=bus.asBus(\audio, server:target.server)});

		settings=argsettings??{()};
		synthdefsettings=();
		bypass=false;
		id=UniqueID.next;
		//this.initializeVars;
		defaultSettings=(amp:0.0, az:0.0, outBus: 0.asBus(\audio,2, target.server));
		defaultSettings.keysValuesDo{|key,val|
			if (settings[key]==nil, {settings[key]=val})
		};

		if (settings[\outBus].class!=Bus, {
			settings[\outBus]=settings[\outBus].asBus(\audio, server:target.server)});

		controlSpecs=(amp: \amp.asSpec, az: \bipolar.asSpec);
		bypassFunc={};

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
		var defname=(\DryJT++id).asSymbol;
		synth=SynthDef(defname, {arg amp=0.0, az=0.0;
			var in, out;
			in=In.ar(bus.index, bus.numChannels);
			Out.ar(settings[\outBus].index
				, PanAz.ar(settings[\outBus].numChannels, in, az, amp))
		}).add.play(
			target
			, settings.asKeyValuePairs
			,addAction).register;
		target.server.sync;
		id=synth.nodeID;
	}

	makeGUI {arg parent, bounds=350@20;
		//gui=synth.makeGui(parent, bounds, false, true);
		gui=DryJTGUI(this, parent, bounds)
		^gui
	}

}

DryJTGUI : GUIJT {
	var <dry, <controlSpecs, <settings;
	var <width2, <height2, boundsKnob;
	var <maxHeight;

	*new {arg dry, parent, bounds;
		^super.new.init(dry, parent, bounds);
	}

	init {arg argdry, argparent, argbounds;
		var buz, active=1;
		dry=argdry;
		parent=argparent;
		bounds=argbounds;
		classJT=argdry;

		controlSpecs=dry.controlSpecs;
		settings=dry.settings;
		this.initAll;
		views[\bypass]=Button(parent, bounds)
		.states_([[\bypassed],[\ON, Color.black, Color.green]]).action_{|b|
			classJT.bypass_(b.value<1)
		}.value_(classJT.bypass.not.binaryValue);

		controlSpecs.sortedKeysValuesDo{|key,cs|
			EZSlider(parent, bounds, key, cs, {|ez|
				dry.synth.set(key, ez.value);
				settings[key]=ez.value;
			}, settings[key])
		};

		parent.rebounds;

		this.postInitAll;

		if (hasWindow, {window.rebounds});
	}
}