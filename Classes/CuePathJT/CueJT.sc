/*
cue -> (index:0, method: \restore, args:[], preAction:{}, postAction: {}, preWait: 0, postWait: 30, continue: false)
maar een cue kan misschien ipv een index:0 ook een presetnaam hebben? dus b.v. index: "greatreverb" oid
targets[index].doMethod(method)(*args)
targets=(delay: [PresetJT, PresetCollection], reverb: PresetJT)
CueJT( (000)
PresetJT
cueList = [(000master: (), grain: ()), (000master: (), grain: ()), etc]
cueNames = ["/0000_Section1/0000_Part1/0000_start/","/0000_Section1/0000_Part1/0001_continue/" ,"/0000_Section1/0000_Part1/0002_stop/"]
cueFunctionLists = [FunctionList, FunctionList, FunctionList]
cueNamesNested = ()
*/
CueJT {
	var <cueJT;
	var target, <value, key, name, <gui;
	var path, <cueID=0;

	cueID_ {arg index;
		cueID=index;
	}
	value_ {arg v;
		value=v;
	}
	store {
		var file, pathName;
		pathName=path++name++key++".scd";
		file=File(pathName, "w");
		file.write(value.asCompileString);
		file.close;

		cueJT.cueList[cueID][key]=value;

	}
	restore {}
	delete {}

	add {
		this.store;
	}
	remove {
		this.delete;
	}

	deactivateCue {}
	activateCue {}
	deactivate {}
	activate {}
}

CueJTMaster : CueJT {
	var <cues, targets, <cueList, <cueNames, cueFuncs, <cueStructure;

	*new {arg path, targets;
		^super.new.init(path, targets)
	}
	init {arg argpath, argtargets;
		path=argpath;
		targets=argtargets??{()};
		this.loadCues;
		//this.analyzeStructure;
	}

	add {
		this.store;
	}
	remove {
		this.delete;
	}
	addBefore {}
	addAfter {}
	addGroupBefore {}
	addGroupAfter {}
	removeGroup {}

	addTarget {arg target, key;
		if (targets.keys.asArray.includesEqual(key).not, {
			targets[key]=target;
			//cues.deepDo({});!!!
		},{
			"Warning: key ".post; key.post; " is already used!".postln;
		});
	}
	loadCues {
		//var path=thisProcess.nowExecutingPath.dirname++"/cues/";
		var objects=();
		var functions=[], functionLists=[];
		var name="", cue=0;
		cueNames=[];
		cueList=[];
		PathName(path).deepFiles.do{arg pathname;
			var fullPath, fileName, pathOnly, folders, out;
			var key, func;
			out=pathname.fullPath.load;
			pathname=PathName(pathname.fullPath.replace(path, "/"));
			fullPath=pathname.fullPath;
			pathOnly=pathname.pathOnly;
			fileName=pathname.fileNameWithoutExtension;
			//folders=pathname.allFolders;
			if (pathOnly!=name, {
				cueNames=cueNames.add(pathOnly);
				name=pathOnly;
				cueList=cueList.add( () );
				functions=functions.add( () );
			},{

			});
			cue=cueList.size-1;
			key=fileName.asSymbol;
			cueList[cue][key]=out;
			//actions[cue][objectKey]=values;
			//func=this.getFunc(objects[objectName], values);
			//of this.putFunc(cueName, cue, objectName, values);
			//cueFunc[cue]=cueFunc[cue].addFunc(func);
		};
		this.analyzeStructure;
	}
	loadFunc {
		^{arg pathname, key, event;
			var object, values, index;
			if (targets[key]==nil, {
				pathname.fullPath.load??{()}
			},{
				values=pathname.fullPath.load??{()};
				index=values[\index]??{0};
				if (targets[key]!=nil, {
					if (targets[key].class==Array, {
						object=targets[key][index];
					});
				});
				this.makeFunc(object, values)
			})
		}
	}
	makeFunc {arg object, values;
		var func, args, method;
		args=values[\args]??{[]};
		func=switch(object.class, PresetJT, {
			switch(method, \restore, {
				var index=args[0]??{0};
				{object.restore(index)}
			}, \transitionTo, {
				var index=args[0], durations=args[1], curves=args[2], delayTimes=args[3];
				{
					object.objects.transitionTo(object.array[index], durations, curves, delayTimes)
				}
			});
		}, PresetJTCollection, {
			{}
		}, PresetJTCollectionBlender, {
			{}
		}, PresetJTNeuralNet, {
			{}
		});
		^func
	}
	analyzeStructure {
		var cue=0;
		cueStructure=();
		cueNames.do{|cueName|
			var folders=PathName(cueName).allFolders;
			var event=cueStructure;
			folders.do{|folderName, depth|
				if (event[folderName.asSymbol]==nil, {
					event[folderName.asSymbol]=()
				});
				if (depth==(folders.size-1), {
					event[folderName.asSymbol]=cue
				});
				event=event[folderName.asSymbol];
			};
			cue=cue+1;
		};
		^cueStructure
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CueJTMasterGUI(this, parent, bounds)}.defer
	}
}

CueJTSlave : CueJT { }

CueJTMasterGUI {
	classvar cueJT;
	var parent, bounds;
	var cMain, views, cListViews, numberOfColumns, indices;
	*new {arg cueSystem, parent, bounds;
		cueJT=cueSystem;
		^super.new.init(parent, bounds)
	}
	makeListViews {arg cueName;
		var folders=PathName(cueName).allFolders;
		var width;
		var numberOfColums=folders.size;
		var event=cueJT.cueStructure;

		cListViews.removeAll;
		cListViews.decorator.reset;
		width=(bounds.x/numberOfColums).floor.asInteger;

		views[\currentCue].string_(PathName(cueJT.cueNames.clipAt(cueJT.cueID)).allFolders.collect{|i| i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\previous].string_(PathName(cueJT.cueNames.clipAt(cueJT.cueID-1)).allFolders.collect{|i| i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\upcoming].string_(PathName(cueJT.cueNames.clipAt(cueJT.cueID+1)).allFolders.collect{|i| i.asString.split($_).copyToEnd(1).join($_)}.join($/));

		folders.collect{|folderName, i|
			var views=(), folderNames;
			var buttonWidth=(width/6).floor.asInteger;
			var c=CompositeView(cListViews, width@cListViews.decorator.bounds.height);
			var keys=event.keys.asArray.sort;
			var index, currentEvent=event;
			index=keys.indexOfEqual(folderName.asSymbol);
			folderNames=keys.collect{|i|
				i.asString.split($_).copyToEnd(1).join($_)
			};
			c.addFlowLayout(0@0,0@0);
			c.background_(Color.rand);
			views[\listView]=ListView(c, width@(cListViews.decorator.bounds.height-60)).items_(folderNames).canFocus_(false).action_{arg l;
				var cEvent;
				cEvent=currentEvent[keys[l.value]];
				while ({cEvent.class==Event}, {
					cEvent=cEvent[cEvent.keys.asArray.sort[0]];
				});
				cueJT.cueID_(cEvent);
				this.makeListViews(cueJT.cueNames[cEvent]);
			}.value_(index);
			views[\textField]=TextField(c, width@20).string_(folderName).action_{arg t;
				//f[\changeName].value(folderNames[indices[i]], t.string, currentEvent)
			};
			Button(c, buttonWidth@20).states_([ ["+v"] ]).canFocus_(false).action_{ };//addAfter
			Button(c, buttonWidth@20).states_([ ["+^"] ]).canFocus_(false).action_{ };//addBefore
			Button(c, buttonWidth@20).states_([ ["-"] ]).canFocus_(false).action_{ };//remove
			Button(c, buttonWidth@20).states_([ ["+<"] ]).canFocus_(false).action_{ };//addFolderBefore
			Button(c, buttonWidth@20).states_([ ["+>"] ]).canFocus_(false).action_{ };//addFolderAfter
			Button(c, buttonWidth@20).states_([ ["--"] ]).canFocus_(false).action_{ };//removeFolder
			views[\compositeView]=c;
			event=event[folderName.asSymbol];
			views
		};
	}

	init {arg argparent, argbounds;
		views=();
		indices=[0];
		bounds=350@240;
		parent=Window("cues", Rect(400,400,400,400)).front.alwaysOnTop_(true);
		parent.addFlowLayout(4@4, 0@0);
		cMain=CompositeView(parent, bounds); cMain.addFlowLayout(0@0, 0@0); cMain.background_(Color.rand);
		views[\currentCue]=StaticText(cMain, bounds.x@20).string_("").align_(\center);
		views[\prevB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\PREV] ]).action_{
			if (cueJT.cueID>0, {
				cueJT.cueID_(cueJT.cueID-1);
				this.makeListViews(cueJT.cueNames[cueJT.cueID])
			});
		};
		views[\nextB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\NEXT] ]).action_{
			if (cueJT.cueID<(cueJT.cueNames.size-1), {
				cueJT.cueID_(cueJT.cueID+1);
				this.makeListViews(cueJT.cueNames[cueJT.cueID])
			});
		};
		//StaticText(cMain, (bounds.x*0.2).floor.asInteger@20).string_("upcoming: ");
		views[\previous]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\left);
		views[\upcoming]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\right);
		cListViews=CompositeView(cMain, bounds.x@(bounds.y-40)); cListViews.addFlowLayout(0@0, 0@0); cListViews.background_(Color.rand);
		//f[\makeListViews].value(folders=f[\getAllListViews].value(deepFoldersAsEvent, 0))
		this.makeListViews(cueJT.cueNames[cueJT.cueID])
	}
}