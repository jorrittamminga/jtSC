//check makeCompositeView! gebruik eventueel bounds.y
GUIJT {
	var <bounds, <parent, <font, <window, <compositeView, <labelWidth, <hasWindow;
	var <views, <>classJT, <viewsPreset;
	var <oscGUI, <>name;
	var <margin, <gap, <background;
	var pparent, <>freeOnClose, <presetSystem, <presets, <threaded;
	var <>path, <>folderName;
	var <frontFlag, <userCanClose;
	var <parentMargin, <parentGap, <windowMargin, <windowGap, <parentAtInit;

	isThreaded {
		threaded=(thisProcess.mainThread.state>3);
		^threaded
	}

	initVars {
		var nameString;
		margin=margin??{4@4};
		gap=gap??{4@4};
		background=background??{Color.grey};
		font=font??{Font("Monaco", 10)};
		freeOnClose=freeOnClose??{false};
		if (parent.class==String, {nameString=parent; parent=nil});
		if (classJT!=nil, {
			if (classJT.class.topclass==JT, {
				name=name??{classJT.name}
			},{
				name=name??{classJT.class.asString}
			})
		});
		name=name??{this.makeName(nameString)};
		pparent=[];
		hasWindow=false;
		oscGUI=();
		frontFlag=frontFlag??{true};
		userCanClose=userCanClose??{true};//dit was default false
		threaded=this.isThreaded; //thisProcess.mainThread.state>3;
		views=();
		viewsPreset=();
	}

	initGUI {arg parentMargin, parentGap, windowMargin, windowGap;
		var tmpBounds;
		//hieronder is van een andere orde (kan in een {}.defer b.v.)

		if (parent==nil, {
			hasWindow=true;
			this.makeWindow(400, 400, windowMargin??{4@0}, windowGap??{4@2}, 4);
			pparent=pparent.add(window);
		},{
			window=if (parent.class==Window, {
				parent
			},{
				parent.findWindow;
			});
			pparent=pparent.add(window);
			pparent=pparent.add(parent);
			tmpBounds=window.bounds;
			tmpBounds.height=20;
			//window.view.decorator.top=5000;
			//window.bounds_(tmpBounds);
		});
		if (parent.class==CompositeView, {
			var tmpBounds=parent.bounds;
			tmpBounds.height=20;
			//parent.decorator.top=5000;
			//parent.bounds_(tmpBounds);
		});
		this.initFont;
		this.makeCompositeView(
			parentMargin??{if (hasWindow, {4@4},{margin})}
			, parentGap??{if (hasWindow, {4@2},{gap})}
		);

		if (window.onClose==nil, {window.onClose={}});
		if (classJT!=nil, {
			if (hasWindow, {
				if (classJT.windowBounds!=nil, {
					window.bounds_(classJT.windowBounds)
				});
			});
			window.onClose=window.onClose.addFunc(
				{
					classJT.gui=nil;
			});
		});

		//dit alleen bij JT classes, moet ook bij andere dingen kunnen
		if (freeOnClose, {
			window.onClose=window.onClose.addFunc({
				classJT.free
			});
		});
	}

	initAll {arg parentMargin, parentGap, windowMargin, windowGap;
		parentAtInit=parent.copy;
		this.initVars;
		this.initGUI(parentMargin, parentGap, windowMargin, windowGap);
	}

	postInitAll {
		if (classJT!=nil, {
			if (classJT.hasPresetSystem==true, {
				this.addPresetSystem(
					nil,
					classJT.presetSystem??{classJT.presetPath},
					classJT.presetFolder,
					classJT.presetType,
					classJT.presetIndex,
					2,
					false);
			});
			if (classJT.hasPresets==true, {
				this.addPresets(
					nil,
					classJT.presetsPath);
			});
		})
	}

	makeName {arg string;
		^(string??{classJT.class.asString});
		//^(string??{classJT.class.asString ++ "_" ++ classJT.id});
	}

	makeWindow {arg left=400, top=400, margin=4@0, gap=4@2, marginAdd=4;
		var w;
		w=Window(name, Rect(left,top
			,bounds.x+((margin.x+marginAdd)*2)
			,bounds.y+((margin.y+marginAdd)*2)
		));
		windowMargin=margin;
		windowGap=gap;
		w.userCanClose_(userCanClose);
		window=w;
		parent=w;
		w.addFlowLayout(margin, gap);
		w.alwaysOnTop_(true);
		if (frontFlag, {w.front});
		hasWindow=true;
	}

	makeCompositeView {arg margin=0@0, gap=0@4, backgroundC=Color.grey(0.5);
		compositeView=CompositeView(parent
			, (bounds.x+(2*margin.x))
			@
			((2*margin.y)+bounds.y)
			//20//zou ook (2*margin.y)+bounds.y kunnen zijn!
		);
		parentMargin=margin;
		parentGap=gap;
		compositeView.addFlowLayout(margin, gap);
		compositeView.background_(backgroundC??{background});
		//compositeView.background_(Color.yellow);
		parent=compositeView;
		pparent=pparent.add(parent);
	}

	initFont {//arg name, bold = false, italic = false, usePointSize = false;
		//name=name??{Font.defaultMonoFace};
		font.size_(bounds.y*0.6);
	}

	addPresets {arg guiObjects, path, folderName, type, index, guiType=2
		, argguiflag=false, rebounds=true, interpolationTime, parentPS, preLoad;
		var ps, windowReboundsFlag=true;
		var parentt=parentPS??{parent};
		guiObjects=switch(guiObjects.class, Array, {
			var tmp=();
			guiObjects.do{|key| tmp[key]=views[key]}; tmp
		},Event, {
			guiObjects
		},Nil, {
			if (viewsPreset.size>0, {viewsPreset},{views})
		});
		presets=PresetsJT(guiObjects, path);
		presets.makeGui(parentt, bounds.x@bounds.y);
		parentt.rebounds;
		^presets
	}

	addPresetSystem {arg guiObjects, path, folderName, type, index, guiType=2
		, argguiflag=false, rebounds=true, interpolationTime, parentPS, preLoad;
		var ps, windowReboundsFlag=true;
		var parentt=parentPS??{parent};
		guiObjects=switch(guiObjects.class, Array, {
			var tmp=();
			guiObjects.do{|key| tmp[key]=views[key]}; tmp
		},Event, {
			guiObjects
		},Nil, {
			if (viewsPreset.size>0, {viewsPreset},{views})
		});
		path=path??{var tmpPath;
			tmpPath=if (classJT!=nil, {
				if (classJT.presetPath!=nil, {
					if (classJT.presetSystem!=nil, {ps=classJT.presetSystem});
					if (type==nil, {type=classJT.presetType});
					if (index==nil, {index=classJT.presetIndex});
					if ((classJT.presetFolder!=nil)&&(folderName==nil), {
						folderName=classJT.presetFolder
					});
					classJT.presetPath
				},{
					nil
				})
			},{nil});
			tmpPath??{thisProcess.nowExecutingPath.dirname++"/presets/"}
		};
		if (path.class==PresetSystem, {
			if (type==nil, {
				type=\slave;
				if (folderName==nil, {folderName=type.asString});
			});
			ps=path;
			path=path.path;
		});
		folderName=folderName??{name};
		presetSystem=if (ps==nil, {
			ps=PresetSystem(guiObjects, path, folderName, preLoad: (preLoad??{true}) );
			ps.index_(index??{0});
			ps
		},{
			ps.addSlave(guiObjects, folderName,preload:preLoad)
		});
		if (interpolationTime!=nil, {
			presetSystem.addInterpolation(interpolationTime)
		});
		presetSystem.gui(parentt, bounds.x@(bounds.y), guiType,argguiflag: argguiflag);
		parentt.rebounds;
		if (rebounds, {
			if (parentt!=window, {
				//force rebounds.....
				//vergelijking hieronder klopt niet!
				//windowReboundsFlag
				window.bounds_(
					Rect(window.bounds.left, window.bounds.top
						, window.bounds.width.max(parentt.bounds.width
							+(2*window.view.decorator.margin.x)
						)
						, window.bounds.height.max(parentt.bounds.height
							+(2*window.view.decorator.margin.y)
						)
					)
				);
				/*
				window.bounds_(
				Rect(window.bounds.left, window.bounds.top
				, window.bounds.left.max(parent.bounds.width+window.view.decorator.margin.x)
				, window.bounds.height.max(parent.bounds.height+window.view.decorator.margin.y)
				));

				window.rebounds(false);
				if ((window.bounds.width<parent.bounds.width)
				|| (window.bounds.height<parent.bounds.height)
				, {
				window.bounds_(
				Rect(window.bounds.left, window.bounds.top
				, parent.bounds.width+window.view.decorator.margin.x
				, parent.bounds.height+window.view.decorator.margin.y
				));
				});
				*/
			});
		});
		^presetSystem
	}

	makeEZGUI {arg argbounds=350@20, label, controlSpec, action, value,
		initAction=false, labelWidth, numberWidth, unitWidth=0, labelHeight
		,  layout=\horz, arggap=0@0, argmargin=0@0, equalLength=true, defaultRound=0.0001;
		var boundz=argbounds.copy??{bounds.copy}, type, round=0, guiOut;
		//font=Font(fontName??{Font.defaultMonoFace}, boundz.y*0.6);//Font.defaultMonoFace
		labelWidth=labelWidth??{boundz.y*0.6 * 8;};
		numberWidth=numberWidth??{boundz.x*0.15};
		^if (controlSpec==nil, {
			EZText(parent, boundz, label, action, value, false, layout: argmargin, gap:arggap).font_(font)
		},{
		if (controlSpec.class==Array, {
			if ((controlSpec[0].class==String)||(controlSpec[0].class==Symbol), {
				type=EZPopUpMenu;
				//action=action.addFunc({|ez| [ez, ez.value].postln})
			})
		});
		type=type??{if (value!=nil, {
			switch(value.asArray.size, 1, {EZSlider}, 2, {
				EZRanger
			}, {
				//LET OP, hier iets doen! [value, controlSpec, value.class].postln;
				boundz.y=boundz.x*0.25;
				EZMultiSlider
			})
		},{EZSlider})};

		if ((type==EZRanger) && (equalLength), {
			labelWidth=labelWidth-numberWidth-4;
		});
		if ((type==EZMultiSlider) && ((boundz.x/boundz.y)>8)
			, {boundz.y=boundz.x*0.125});
		if (type==EZPopUpMenu, {

		},{
			round=controlSpec.step;
			if (round<0.00000001, {round=defaultRound.copy});
		});
		//action=action.addFunc({|ez| [ez, ez.value].postln});
		//EZText(parent, bounds, label, action, value, false, layout: layout, margin: argmargin, gap: arggap).font_(font);
		type.new(parent, boundz, label, controlSpec, action
			, value, false, labelWidth, numberWidth
			, layout: layout
			, margin: argmargin, gap: arggap).font_(font).round2_(round, value)
		});
	}

	reboundsAll {
		pparent.reverse.do{|p| p.rebounds}
	}

	initClose {
		if (hasWindow, {
			window.close
		},{
			compositeView.remove
		});
		classJT.gui=nil;
	}

	front {
		frontFlag=true;
		window.front;
	}

	close {
		this.initClose
	}
}