AnalyzerJTGUI : GUIJT {
	var <hasGater, <gaterJT;
	var <analyzer, <thresholds;
	var <oscGUI, <controlSpecs, <descriptors, <hasOnsets, <descriptorsWithoutOnsets;
	var <thresholds, <thresholdFuncs, threadedFunc;

	*new {arg analyzer, parent, descriptors, bounds=350@20, freeOnClose=false
		, margin=4@4, gap=4@4, font, thresholds=()
		, thresholdFuncs=(), gaterJT;
		^super.new.init(analyzer, parent, descriptors, bounds, freeOnClose
			, margin, gap, font, thresholds, thresholdFuncs, gaterJT);
	}

	init {arg arganalyzer, argparent, argdescriptors, argbounds, freeOnClose=false
		, margin, gap, argfont, argthresholds, argthresholdFuncs, arggaterJT;
		var maxChars;
		//=============================================================== INITS
		analyzer=arganalyzer;
		classJT=analyzer;
		bounds=argbounds.copy;
		parent=argparent;
		descriptors=argdescriptors??{analyzer.descriptors};
		oscGUI=();
		hasOnsets=descriptors.includes(\onsets);
		descriptorsWithoutOnsets=descriptors.deepCopy;
		if (hasOnsets, {descriptorsWithoutOnsets.remove(\onsets)});
		gaterJT=arggaterJT;
		thresholds=argthresholds;
		thresholdFuncs=argthresholdFuncs;

		if (gaterJT.class==GaterJT, {
			hasGater=true;
			this.makeThresholds;
		});

		this.initAll;

		controlSpecs=();
		descriptorsWithoutOnsets.do{|key|
			controlSpecs[key]=analyzer.controlSpecs[key].copy};
		//=============================================================== make GUI
		/*
		StaticText(parent, (bounds.x*0.5).floor@bounds.y)
		.string_(analyzer.synthDef).font_(font)
		.background_(Color.black).stringColor_(Color.white);
		parent.decorator.nextLine;

		views[\pauseButton]=Button(parent, (bounds.x*0.5-gap.x).floor@bounds.y)
		.states_([ ["pause",Color.green,Color.black],["resume",Color.yellow,Color.black]])
		.font_(font).action_{|b|
			if (b.value==1, {
				this.removeOSCFuncs;//this.pause;
			},{
				this.addOSCFuncs;//this.resume;
			})
		}.canFocus_(false);
		*/
		maxChars=descriptorsWithoutOnsets.asArray.collect{|key| key.asString.size}.maxItem;
		maxChars=maxChars*((bounds.y/2).floor)*0.73;

		descriptorsWithoutOnsets.do{|key|
			var gui;
			views[key]=this.makeEZGUI(bounds.copy, key, controlSpecs[key], {}
				, ({controlSpecs[key].minval} !
					analyzer.outBusperDescriptor[key].numChannels).unbubble
				, labelWidth: maxChars//(bounds.copy.x*0.4).postln
			);
			if (thresholds[key]!=nil, {
				var slider=EZSlider, argument=(\threshold_++key).asSymbol;
				var func=thresholdFuncs[key]??{|ez|

				};
				views[argument]=this.makeEZGUI(bounds.x@(bounds.y/2).floor, "threshold"
					, controlSpecs[key], func, thresholds[key], false
					, views[key].labelView.bounds.width
					, equalLength:true);
			});
		};
		if (hasOnsets, {
			views[\onsets]=Button(parent, bounds).states_([[\onsets]
				,[\onsets,Color.white, Color.green]]).font_(font).canFocus_(false);
		});
		parent.onClose_(parent.onClose.addFunc{
			if (freeOnClose, {analyzer.free});
			this.close
		});
		if (hasWindow&&freeOnClose, {
			//window.userCanClose_(false);
		});
		this.addOSCFuncs;

		threadedFunc={
			var synthGUI;
			synthGUI=analyzer.synth.makeGUI(parent, bounds, canFocus:true, willHang:true
				, returnButtons:true);
			synthGUI.keysValuesDo{|key,gui| views[key]=gui};
			//{
				parent.rebounds;
				if (window!=nil, {window.rebounds});
			//}.defer;
		};

		if (threaded, {
			threadedFunc.value
		},{{
			threadedFunc.value
		}.fork(AppClock)});
	}

	makeThresholds {
		gaterJT.descriptors.do{|key|
			var key2=(\threshold_ ++ key).asSymbol;
			thresholds[key]=thresholds[key]??{gaterJT.settings[\thresholds][key]};
			thresholdFuncs[key]=thresholdFuncs[key]??{{|ez|
				if (gaterJT.gui!=nil, {
					{gaterJT.gui.views[key2].value_(ez.value)}.defer;
				});
				gaterJT.synth.set(key2, ez.value)
			}}
		};
	}
}