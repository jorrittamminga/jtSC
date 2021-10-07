/*
valPreset=object.removeAllWithoutActions(valPreset);//WARNING: THIS IS A NEW LINE!! COULD BREAK THIS
*/
CueFunctionsJT : CuesJT {
	//var <objectDefault;
	//var <neuralNet, <blender;
	//var <cueListJT;
	//	var <keysNoTransition;
	var <>enviroment;
	*new {arg enviroment, cueName=\action;
		/*
		^if (object.class==PresetsJT, {
		CuesPresetsJT(object, cueName)
		},{
		*/
		^super.basicNew(enviroment, cueName)
		//})
	}
	//--------------------------------------------------------------------------------- INITS
	initObject {
		removeKeyWhenSave=[];
		controlSpecs=();
		value={};
		enviroment=object;
		/*
		object.sortedKeysValuesDo{|key,obj|
			var cs;//=ControlSpec(0.0, 1.0);
			value[key]=obj.value;
			cs=obj.controlSpec;
			if (cs!=nil, {
				if (cs.step<0.01, {cs=cs.warp});
				controlSpecs[key]=cs;
			})
		}
		*/
	}
	initGetAction {
		getAction={ value }

	}//get values from object
	initSetAction {
		var actionFunc={arg val;
			val
		};
		action=action.addFunc(actionFunc)
	}//set values to object
	//initEntriesAction {}
	value_ {arg val;
		value=val
	}
	//----------------------------------------------------------------------------- FILE SYSTEM inits
	/*
	initPathName { //analyze the content/class of the pathname
		basename=pathName.deepCopy;
	}
*/
	prRestore {
		var preset=array[index];
		actionArray[index].value(enviroment, this);
		value=preset;
		funcs[\restore].value(index);
		^preset
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CueFunctionsGUIJT(this, parent, bounds)}.defer;
	}
	/*
	keysNoTransition_ {
	keysNoTransition=keys;
	//this.initSetAction
	}
	*/
	/*
	initPathName {
	basename=pathName.deepCopy;
	}
	initSetAction {
	var actionFunc;
	//keysNoTransition=keysNoTransition??{[]};
	actionFunc={arg val;
	var valObject=(), valPreset=(), index;//oid
	var actionObject, actionPreset, extras=val[\extras].deepCopy??{()};
	var presetsObject;
	if (presetsJT.class==PresetsJT, {
	presetsObject=presetsJT.object;
	if (val[\basename]!=nil, {
	index=presetsJT.keys.indexOfEqual(val[\basename]);
	if (index!=nil, {
	valPreset=presetsJT.array[index];
	valPreset=presetsObject.removeAllWithoutActions(valPreset);
	})
	});
	valObject=object.removeAllWithoutActions(val);
	actionObject={
	presetsObject[\routinesJT].do(_.stop);
	presetsJT.index=index;
	presetsJT.funcs[\index].value(presetsJT.index, presetsJT);
	valObject.keysValuesDo{|key,val|
	object[key].action.value(val);
	{object[key].value_(val)}.defer;
	};
	};
	actionPreset={
	valPreset.sortedKeysValuesDo{|key,val|
	presetsObject[key].action.value(val);
	{presetsObject[key].value_(val)}.defer;
	}
	};
	}, {
	presetsObject=object;
	valPreset=val.deepCopy;
	[\method, \durations, \extras].do{|key|
	valObject[key]=val[key];
	valPreset.removeAt(key)
	};
	actionObject={
	presetsObject[\routinesJT].do(_.stop);
	valObject.keysValuesDo{|key,val|
	if (presetsObject[key]==nil, {

	},{
	presetsObject[key].action.value(val);
	{presetsObject[key].value_(val)}.defer;
	})
	};
	};

	valPreset=object.removeAllWithoutActions(valPreset);//WARNING: THIS IS A NEW LINE!! COULD BREAK THIS

	actionPreset={
	valPreset.sortedKeysValuesDo{|key,val|
	object[key].action.value(val);
	{object[key].value_(val)}.defer;
	}
	};
	});
	if (val[\method]!=nil, {
	if (val[\method]>0, {
	if (val[\durations]!=nil, {
	if (val[\durations]>0.0, {
	if (val[\extras]!=nil, {
	if (extras[\durations]!=nil, {
	extras[\durations][\common]=(val[\durations]??{0});
	},{
	extras[\durations]=val[\durations]
	});
	},{
	extras=(durations: val[\durations]);
	});
	//if (keysNoTransition.size>0, {},{});
	actionPreset={
	/*
	valPreset.sortedKeysValuesDo{|key,val|
	object[key].action.value(val);
	{object[key].value_(val)}.defer;
	}
	*/
	presetsObject.valuesActionsTransition(valPreset, extras[\durations], extras[\curves]
	, extras[\delayTimes]
	, extras[\specs], extras[\actions], extras[\resolution]??{10})
	}
	})
	})
	})
	});
	if (val[\extras]==nil, {
	{
	actionObject.value;
	actionPreset.value;
	};
	},{
	{
	actionObject.value;
	{
	extras[\preAction].value(val, enviroment);
	actionPreset.value;
	extras[\postAction].value(val, enviroment);
	}.fork(AppClock)
	}
	});
	};
	action=action.addFunc(actionFunc);
	}
	addToCueList {arg cueList;
	pathName=basename.deepCopy;
	cueList.addCue(this);
	cueListJT=cueList;
	}
	removeFromCueList {arg cueList;
	cueList.removeCue(this);
	}
	cueName {^basename}
	addNN{neuralNet=PresetsNNJT(this)}
	addBlender{blender=PresetsBlenderJT(this)}
	makeGui {arg parent, bounds=350@20;
	{gui=CuesGUIJT(this, parent, bounds)}.defer;
	}
	*/
}

CueFunctionsGUIJT {
	var <presets;
	var <views, <parent, <bounds;
	var <prevIndex=0, <document;
	*new {arg presets, parent, bounds;
		^super.newCopyArgs(presets).init(parent, bounds)
	}
	init {arg argparent, argbounds;
		var boundsName=(argbounds.x/3).floor@argbounds.y;
		var boundsButton=(boundsName.x/7).floor@argbounds.y;
		var c;
		var initName="nil";
		bounds=argbounds??{350@20};
		views=();
		c=CompositeView(argparent, bounds.x@(bounds.y));
		c.addFlowLayout(0@0, 0@0);
		views[\addBefore]=Button(c, boundsButton).states_([ ["±"] ])
		.action_{
			if (views[\basename].stringColor==Color.red, {
				views[\addAfter].states_([ ["cp"] ]);
				views[\basename].stringColor_(Color.black);
				presets.add(presets.basename, \addToHead, presets.directory)
			},{
				//copy?
				presets.store
			});
		};
		views[\addAfter]=Button(c, boundsButton).states_([ ["+"] ])
		.action_{
			var currentPath, currentCueListPath, index;
			if (views[\basename].stringColor==Color.red, {
				views[\addAfter].states_([ ["cp"] ]);
				views[\basename].stringColor_(Color.black);
				presets.add(presets.basename, \addToTail, presets.directory)
			},{
				currentCueListPath=presets.cueListJT.pathNameNumberedManager.currentFolder;
				currentPath=(presets.entries[presets.index].fullPath.dirname++"/");
				if (currentPath!=currentCueListPath, {
					index=presets.entries.collect{|path| path.fullPath.dirname++"/"}.indexOfEqual(currentCueListPath);
					presets.put(index, presets.array[presets.index].deepCopy);
					views[\presets].value_(index);
					presets.index_(index);
				},{
					presets.store
				});
			});
		};
		views[\delete]=Button(c, boundsButton).states_([ ["-"] ]).action_{
			presets.delete;
		};
		if (presets.directory!=nil, {
			presets.directory.postln;
			presets.directory.allFolders.postln;
			presets.directory.allFolders.last.postln;
			initName="/"++presets.directory.allFolders.last.removeNumbersFromNumberedPath++"/";
		});
		views[\basename]=StaticText(c, boundsName).string_(
			initName
			//presets.basename
			//"/"++presets.cueListJT.pathNameNumberedManager.currentPathName.folderName.removeNumbersFromNumberedPath++"/"
		).font_(Font("Monaco", boundsName.y*0.45)).stringColor_(
			if (presets.entries.size>0, {
				Color.black
			},{
				Color.red
			})
		);
		views[\store]=Button(c, boundsButton).states_([ ["o"] ]).action_{
			var path;
			document=Document.open(path=presets.entriesFullPath[presets.index]);
			document.onClose_{presets.value_(path.load); presets.store};
		};
		views[\restore]=Button(c, boundsButton).states_([ ["r"] ]).action_{ presets.restore };
		views[\prev]=Button(c, boundsButton).states_([ ["<"] ]).action_{ presets.prev };
		views[\presets]=PopUpMenu(c, boundsName)
		.items_(if (presets.array.size>0, {presets.entriesFullPath},{["(empty)"]}))
		.action_{|p|
			prevIndex=presets.index;
			presets.restore(p.value);
		};
		views[\next]=Button(c, boundsButton).states_([ [">"] ]).action_{ presets.next };
		/*
		presets.object[\method]=PopUpMenu(c, (bounds.x*0.1).floor@bounds.y).items_(
		[\restore, \valuesActionsTransition]
		).action_{|p|

		};
		presets.object[\durations]=EZNumber(c, (bounds.x*0.1).floor@bounds.y, nil, ControlSpec(0.0, 60.0), {|ez|
		if (ez.value<=0.0, {
		{presets.object[\method].valueAction_(0)}.defer
		});
		}, 0.0, false, 0).round2_(0.01);
		presets.object[\extras]=TextField(c, (bounds.x*0.8).floor@bounds.y).string_("").action_{arg t;
		};
		*/
		//------------------------------------------------------------------------------- FUNCTIONS
		[\directory].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({arg deepFoldersRelative, exists=true;
				{views[\basename].string_(
					deepFoldersRelative.removeNumbersFromNumberedPath
				).stringColor_(if (exists, {
					views[\addAfter].states_([ ["cp"] ]);
					Color.black
				},{
					views[\addAfter].states_([ ["+"] ]);
					Color.red
				}))}.defer;
			});
		};
		[\update].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					//views[\basename].string_( presets.directory );
					views[\presets].items_(
						presets.entries.collect{|p| p.pathOnly.replace(presets.rootPath, "").removeNumbersFromNumberedPath}
					).value_(presets.index);
				}.defer
			});
		};
		[\index].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					//views[\basename].string_( presets.fileNamesWithoutNumbers[presets.index] );
					views[\presets].value_(presets.index);
				}.defer
			});
		};
		presets.update;
/*
		if (presets.cueListJT!=nil, {
			//"cueList ".post; presets.cueListJT.postln;
			presets.cueListJT.pathNameNumberedManager.folderID_(presets.cueListJT.pathNameNumberedManager.folderID)//BRUTE FORCE!
		});
*/
	}
}
