EQJT : PluginJT {
	var <defaultSettings, <type, <order;
	var showScope, <flags, <argz;

	*new {arg inBus, target, type=\fiveBand, settings=(), addAction=\addAfter;
		^super.new.init(inBus, target, type, settings, addAction);
	}

	init {arg arginBus, argtarget, argType, argsettings, argaddAction;
		var extraControlSpecs, types;
		bus=arginBus;
		target=argtarget;
		type=argType;
		settings=argsettings??{()};
		addAction=argaddAction;
		bypass=false;
		bypassFunc={};
		id=UniqueID.next;
		//this.initializeVars;

		showScope=settings[\showScope]??{false};
		settings.removeAt(\showScope);

		types=(fiveBand: [], threeBand: [\ls, \hs], HP: [\lpf, \ls, \hs, \mid]
			, LPHP: [\ls, \hs, \mid]);
		order=[\hpf, \ls, \mid, \hs, \lpf];
		flags=(lpf:true, hpf:true, ls:true, hs: true, mid: true);
		argz=(lpf: [\lpFreq], hpf: [\hpFreq], ls: [\lFreq, \lrs, \ldb]
			, hs: [\hFreq, \hrs, \hdb], mid: [\mFreq, \mrq, \mdb]);

		defaultSettings=(hpFreq:20, lFreq:200, lrs:1, ldb:0, mFreq:1000, mrq:1, mdb:0
			, hFreq:3000, hrs:1, hdb:0, lpFreq:20000);
		controlSpecs=(
			\hpFreq: \freq.asSpec
			, lFreq: \freq.asSpec, lrs: ControlSpec(0.1, 10, \exp, 0.1)
			, ldb: ControlSpec(-36, 36, 0, 1)
			, mFreq: \freq.asSpec, mrq: ControlSpec(0.01, 10, \exp)
			, mdb: ControlSpec(-36, 36, 0, 1)
			, hFreq: \freq.asSpec, hrs: ControlSpec(0.1, 10, \exp, 0.1)
			, hdb: ControlSpec(-36, 36, 0, 1)
			, lpFreq: \freq.asSpec
		);

		types[type].do{|k|
			flags[k]=false;
			order.remove(k);
			argz[k].do{|k| defaultSettings.removeAt(k); controlSpecs.removeAt(k)};
		};

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
		},{
			{
				this.makeSynth;
			}.fork
		});
	}

	makeSynth {
		synth=bus.asArray.collect{|bus, i|
			var synth;
			if (bus.class!=Bus, {bus=bus.asBus});
			synth=SynthDef((type++id).asSymbol, {
				arg lpFreq=20000, hpFreq=20
				, lFreq=20, lrs=1, ldb=0
				, mFreq=1000, mrq=1, mdb=0
				, hFreq=20000, hrs=1, hdb=0
				;
				var in;
				in=In.ar(bus.index, bus.numChannels);
				if (flags[\hpf], {in=BHiPass4.ar(in, hpFreq.lag(0.1) );});
				if (flags[\ls], {in=BLowShelf.ar(in, lFreq.lag(0.1), lrs.lag(0.1)
					, ldb.lag(0.1))});
				if (flags[\mid], {
					in=BPeakEQ.ar(in, mFreq.lag(0.1), mrq.lag(0.1), mdb.lag(0.1));});
				if (flags[\hs], {in=BHiShelf.ar(in, hFreq.lag(0.1), hrs.lag(0.1)
					, hdb.lag(0.1));});
				if (flags[\hpf], {in=BLowPass.ar(in, lpFreq.lag(0.1));});
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

	makeGUI {arg parent, bounds=400@20, showscope, onClose=false;
		gui=EQGUIJT(this, parent, bounds, showscope??{showScope}, onClose);
		^gui
		//this.initAll;
	}
}


EQGUIJT : GUIJT {
	var <eq, <showScope, <scope, <controlSpecs, <settings;
	var <width2, <height2, boundsKnob;
	var <maxHeight;

	*new {arg eq, parent, bounds, showScope=false, onClose=false;
		^super.new.init(eq, parent, bounds, showScope, onClose);
	}

	init {arg argeq, argparent, argbounds, argshowScope, argonClose;
		var buz, active=1;
		eq=argeq;
		parent=argparent;
		bounds=argbounds;
		showScope=argshowScope;
		classJT=argeq;

		controlSpecs=eq.controlSpecs;
		settings=eq.settings;
		//of settings=synth.getAll; server.sync;
		freeOnClose=argonClose;

		this.initAll;
		maxHeight=eq.order.collect{arg e; (hpf:1, ls:3, mid:3, hs:3, lpf:1)[e]}.maxItem;
		width2=(bounds.x/eq.order.size).floor-parent.decorator.gap.x;

		//parent.resize_(5);
		//if (hasWindow, {window.resize_(5)});
		viewsPreset[\run]=Button(parent, bounds)
		.states_([[\bypassed],[\ON, Color.black, Color.green]]).action_{|b|
			//eq.synth.asArray.do{|syn| syn.run(b.value>0)}
			classJT.bypass_(b.value<1);
			classJT.settings[\run]=b.value;
		}.value_(classJT.bypass.not.binaryValue);

		eq.order.do{|type, i|
			var c, boundsKnob, shift;
			boundsKnob=(bounds.y*3)@(bounds.y*3+(bounds.y));
			c=CompositeView(parent, width2@(boundsKnob.y+2*maxHeight+2));//*3
			c.addFlowLayout(0@0, 2@2);
			//c.background_(Color.rand);
			//c.resize_(5);
			shift=((width2/2).floor-(boundsKnob.x/2).floor).max(0);
			eq.argz[type].do{|par, j|
				var e;
				c.decorator.shift(shift,0);
				e=EZKnob(c, boundsKnob, par, controlSpecs[par], {|ez|
					eq.synth.asArray.do{|syn| syn.set(par, ez.value)};
					eq.settings[par]=ez.value;
				}, settings[par], false
				, bounds.y/2, nil, 0, bounds.y/2, 'vert2', 0@0, 0@0)
				.font_(Font(font.name, bounds.y*0.4));
				e.numberView.align_(\center);
				//e.knobView.resize_(5);
				viewsPreset[par]=e;
				c.decorator.nextLine;
			}
		};

		if (showScope, {
			scope=FreqScopeView(parent, bounds.x@(bounds.x*0.5)
				, eq.synth.asArray[0].server);
			buz=eq.bus.asArray[0];
			if (buz.class==Bus, {buz=buz.index});
			scope.inBus=buz;
			scope.freqMode_(1);
			scope.showFreqs;
			scope.active_(true);
			scope.mouseDownAction={active=active+1%2;
				scope.active_(active>0) };
			parent.onClose=parent.onClose.addFunc({scope.kill});
			/*
			parent.rebounds;
			if (hasWindow, {window.reboundsTo(
			parent.bounds.width+window.view.decorator.margin.x
			, parent.bounds.height)
			});
			*/
		});
		parent.rebounds;

		this.postInitAll;

		if (hasWindow, {window.rebounds});
	}
}