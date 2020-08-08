SplayJT : PluginJT {
	var <runn, outBus;

	*new {arg inBus, target, outBus, settings=(run:0);
		^super.new.init(inBus, target, outBus, settings);
	}

	init {arg arginBus, argtarget, argoutBus, argsettings;
		bus=arginBus;
		target=argtarget;
		outBus=argoutBus??{bus.copy};
		if (outBus.class!=Bus, {outBus=outBus.asBus});
		settings=argsettings??{(run:0)};
		if (settings[\run]!=nil, {settings[\run]=0});
		bypass=settings[\run]<1.0;
		id=UniqueID.next;
		//this.initializeVars;
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
			var synth, synthDef=(\Splay++id).asSymbol, out;
			if (bus.class!=Bus, {bus=bus.asBus});
			SynthDef((\Splay++id).asSymbol, {
				var in;
				in=In.ar(bus.index, bus.numChannels);
				ReplaceOut.ar(outBus.index, Splay.ar(in)
					//++ ({DC.ar(0)}!(bus.numChannels-2).max(0))
				)
			}).add;
			target.asArray[i].server.sync;
			synth=if (bypass, {
				Synth.newPaused(synthDef, [], target.asArray[i], \addAfter).register;
			},{
				Synth(synthDef, [], target.asArray[i], \addAfter).register;
			});
			target.asArray[i].server.sync;
			synth
		};
		id=synth.collect(_.nodeID);
		if (synth.size==1, {synth=synth[0]; id=id[0]});
	}

	makeGUI {arg parent, bounds=400@20, onClose=false;
		gui=SplayGUIJT(this, parent, bounds, onClose);
		^gui
		//this.initAll;
	}
}


SplayGUIJT : GUIJT {
	var <width2, <height2, boundsKnob, <splay;

	*new {arg splay, parent, bounds, onClose=false;
		^super.new.init(splay, parent, bounds, onClose);
	}

	init {arg argsplay, argparent, argbounds, argonClose;
		splay=argsplay;
		parent=argparent;
		bounds=argbounds;
		classJT=argsplay;

		freeOnClose=argonClose;
		this.initAll;

		viewsPreset[\run]=Button(parent, bounds)
		.states_([[\bypassed],[\ON, Color.black, Color.green]]).action_{|b|
			classJT.bypass_(b.value<1);
			classJT.settings[\run]=b.value;
		}.value_(classJT.bypass.not.binaryValue);
		parent.rebounds;
		if (hasWindow, {window.rebounds});
	}
}