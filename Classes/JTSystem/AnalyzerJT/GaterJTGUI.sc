GaterJTGUI : GUIJT {
	var <includesGate, <bounds, <parent, <font, <window, <hasWindow;
	var <gater, <thresholds, <controlSpecs;
	var <oscGUI, <>funcs;
	var <hasAnalyzer, <analyzerJT, threadedFunc;

	*new {arg gater, parent, bounds=350@20, funcs, freeOnClose=false
		, makeCompositeView=true, margin=4@4, gap=4@4, font, analyzerJT;
		^super.new.init(gater, parent, bounds, funcs, freeOnClose
			, makeCompositeView, margin, gap, font, analyzerJT);
	}

	init {arg arggater, argparent, argbounds, argfuncs, freeOnClose=false
		, makeCompositeView, margin, gap, argfont, arganalyzerJT;
		//=============================================================== INITS
		gater=arggater;
		classJT=gater;
		bounds=argbounds.copy;
		parent=argparent;
		funcs=argfuncs??{()};
		this.initAll;
		oscGUI=();
		controlSpecs=();
		analyzerJT=arganalyzerJT;

		if (analyzerJT.class==AnalyzerJT, {
			hasAnalyzer=true;
			//this.makeThresholds;
		},{
			hasAnalyzer=false;
		});

		//=============================================================== make GUI
		StaticText(parent, (bounds.x*0.5).floor@bounds.y)
		.string_(gater.synthDef).font_(font)
		.background_(Color.black).stringColor_(Color.white);
		views[\pauseButton]=Button(parent, (bounds.x*0.5-gap.x)@bounds.y)
		.states_([ ["pause",Color.green,Color.black],["resume",Color.yellow,Color.black]])
		.font_(font).action_{|b|
			if (b.value==1, {
				this.removeOSCFuncs;//this.pause;
			},{
				this.addOSCFuncs;//this.resume;
			})
		}.canFocus_(false);

		views[\gate]=Button(parent, bounds)
		.states_([ [\gate], [\gate,Color.white,Color.green]]).action_{
		}.font_(font);

		parent.onClose_(parent.onClose.addFunc{
			if (freeOnClose, {gater.free});
			this.close
		});
		this.addOSCFuncs;
		threadedFunc={
			var synthGUI;
			synthGUI=gater.synth.makeGUI(parent, bounds, canFocus:true, willHang:true
				, returnButtons:true);
			synthGUI.keysValuesDo{|key,gui|
				views[key]=gui;
				if (hasAnalyzer, {
					if (key.asString.contains("threshold_"), {
						if (analyzerJT.gui!=nil, {
							if (analyzerJT.gui.views[key]!=nil, {
								gui.action=gui.action.addFunc({|ez|
									{
									analyzerJT.gui.views[key].value_(ez.value)
									}.defer
								});
							})
						})
					})
				});
			};
			{
				parent.rebounds;
				if (window!=nil, {window.rebounds});
			}.defer;
		};
		if (threaded, {
			threadedFunc.value
		},{{
			threadedFunc.value
		}.fork(AppClock)});

	}

}