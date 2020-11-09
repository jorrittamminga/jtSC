CueJT {
	var <cueJT;
	var target, <value, name, <gui;
	var path, <rootPath, <cueID=0, fileName, extension="scd", numDigits=4;
	var <paths, <pathsRelative;

	cueID_ {arg index;
		cueID=index;
		path=paths[cueID];
	}
	value_ {arg v;
		value=v;
	}
	store {
		var file, pathName;
		pathName=path++fileName.asString++"."++extension;
		file=File(pathName, "w");
		file.write(value.asCompileString);
		file.close;
		this.storeAction;
	}
	storeAction {
		//cueJT.cueList[cueID][fileName.asSymbol]=value;
	}
	restore {}
	delete {
		//File.deleteNumbered(path, numDigits);
	}
	add {
		this.store;
	}
	deactivateCue {}
	activateCue {}
	deactivate {}
	activate {}
}

CueJTMaster : CueJT {
	var <cues, targets, <cueList, cueFuncs, <cueStructure;

	*new {arg path, targets;
		^super.new.init(path, targets)
	}
	init {arg argpath, argtargets;
		rootPath=argpath;
		targets=argtargets??{()};
		fileName="0000_master";
		value=();
		this.loadCues;
		this.cueID_(0);
		//this.analyzeStructure;
	}
	storeAction {
		//cueList[cueID][fileName.asSymbol]=value;
	}
	delete {arg argpath;
		{
			File.deleteNumbered(argpath??{path}, numDigits);
			paths.removeAt(cueID);
			pathsRelative.removeAt(cueID);
			//cueList.removeAt(cueID);
			path=paths[cueID];
			this.updatePaths;//brute force, this must be more efficient!
		}.fork
	}
	add {arg pathName="untitled", addAction=\addAfter, argpath, index;
		{
			path=pathName.mkdirNumbered(argpath??{path}, addAction, numDigits);
			cueID=index??{cueID};
			paths=paths.insert(cueID, path);
			//pathsRelative=pathsRelative.insert(cueID,  path.replace(rootPath, "/"));
			//cueList=cueList.insert(cueID, ());
			this.store;
			this.updatePaths;//brute force, this must be more efficient!
		}.fork
	}
	addBefore {arg pathName="untitled", argpath, index;
		this.add(pathName, \addBefore, argpath, index)
	}
	addAfter {arg pathName="untitled", argpath, index;
		this.add(pathName, \addAfter, argpath, index)
	}
	addFolder {arg pathName="untitled", addAction=\addAfter, argpath;
		var cond=Condition.new;
		var entries=PathName(argpath??{path}).entries;
		var newPath=(argpath??{path})++(0.asDigits(10, numDigits).join)++"_"++pathName++"/";
		{
			("mkdir " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			("mv " ++ path ++ "*." ++ extension ++ " " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			this.updatePaths;//brute force, this must be more efficient!
		}.fork;
	}
	group {arg pathName="untitled", addAction=\addBefore, argpath, argpaths, index;
		var cond=Condition.new;
		var newPath=(argpath??{path})++((index??{0}).asDigits(10, numDigits).join)++"_"++pathName++"/";
		{
			("mkdir " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			argpaths.do{arg path;
				var folderName=PathName(path).folderName;
				("mv " ++ path ++ " " ++ newPath ++ folderName).unixCmd({cond.unhang});
				cond.hang;
			};
			//File.renumberEntries(argpath, index+1, 1, numDigits);
			File.renumberEntries(argpath, 0, 0, numDigits);
			File.renumberEntries(newPath, 0, 0, numDigits);
			this.updatePaths;//brute force, this must be more efficient!
		}.fork;
	}
	deleteFolder {arg argpath;
		var cond=Condition.new;
		var newPath=PathName(argpath??{path}).moveDir(1).fullPath;
		{
			("mv " ++ (argpath??{path}) ++ "*." ++ extension ++ " " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			("rm -r " ++ (argpath??{path})).unixCmd({cond.unhang});
			cond.hang;
			this.updatePaths;//brute force, this must be more efficient!
		}.fork;
	}
	rename {arg pathName, argpath;
		{
			PathName(argpath??{path}).renameNumbered(pathName, numDigits).fullPath;
			//paths[cueID]=path;
			//pathsRelative[cueID]=path.replace(rootPath, "/");
			this.updatePaths;//brute force, this must be more efficient!
		}.fork
	}
	swap {arg path1, path2;
		var pre1, pre2, path1New, path2New, cond=Condition.new;
		path1.postln;
		path2.postln;
		pre1=PathName(path1).folderName.split($_)[0];
		pre2=PathName(path2).folderName.split($_)[0];

		path1New=PathName(path1).allFolders;
		path1New[path1New.size-1]=pre2++"_"++path1New.last.split($_).copyToEnd(1).join($_);
		path2New=PathName(path2).allFolders;
		path2New[path2New.size-1]=pre1++"_"++path2New.last.split($_).copyToEnd(1).join($_);

		path1New="/"++path1New.join($/)++"/";
		path2New="/"++path2New.join($/)++"/";

		{
			("mv "++ path1 ++ " " ++ path1New).unixCmd({cond.unhang}); cond.hang;
			("mv "++ path2 ++ " " ++ path2New).unixCmd({cond.unhang}); cond.hang;
			this.updatePaths;//brute force, this must be more efficient!
		}.fork
	}
	//------------------------------------------ GROUPING
	ungroup {}
	addGroupBefore {}
	addGroupAfter {}
	removeGroup {}
	//------------------------------------------
	addTarget {arg target, key;
		if (targets.keys.asArray.includesEqual(key).not, {
			targets[key]=target;
			//cues.deepDo({});!!!
		},{
			"Warning: key ".post; key.post; " is already used!".postln;
		});
	}
	updatePaths {
		paths=PathName(rootPath).deepFolders.collect(_.fullPath);
		pathsRelative=PathName(rootPath).deepFoldersRelative.collect(_.fullPath);
		path=paths[cueID];
		this.analyzeStructure;
	}
	loadCues {
		this.updatePaths;
		paths.collect{|path|


		}
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
		pathsRelative.do{|cueName|
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
		if (gui!=nil, {
			{this.gui.makeListViews(pathsRelative[cueID])}.defer
		});
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
		var event=cueJT.cueStructure, kkeys=[];

		cListViews.removeAll;
		cListViews.decorator.reset;
		width=(bounds.x/numberOfColums).floor.asInteger;
		indices=[];

		views[\currentCue].string_(PathName(cueJT.pathsRelative.clipAt(cueJT.cueID)).allFolders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\previous].string_(PathName(cueJT.pathsRelative.clipAt(cueJT.cueID-1)).allFolders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\upcoming].string_(PathName(cueJT.pathsRelative.clipAt(cueJT.cueID+1)).allFolders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)}.join($/));

		folders.collect{|folderName, i|
			var views=(), folderNames;
			var buttonWidth=(width/7).floor.asInteger;
			var c=CompositeView(cListViews, width@cListViews.decorator.bounds.height);
			var keys=event.keys.asArray.sort;
			var index, currentEvent=event, currentFolders, currentIndices;

			kkeys=kkeys.add(folderName);
			currentFolders=kkeys.deepCopy;

			index=keys.indexOfEqual(folderName.asSymbol);
			indices=indices.add(index);
			currentIndices=indices;

			folderNames=keys.collect{|i|
				i.asString.split($_).copyToEnd(1).join($_)
			};
			c.addFlowLayout(0@0,0@0);
			c.background_(Color.rand);
			views[\listView]=ListView(c, width@(cListViews.decorator.bounds.height-60))
			.items_(folderNames).canFocus_(false).action_{arg l;
				var cEvent;
				if (l.selection.size==1, {
					//--------------------------------------------------------
					currentFolders[currentFolders.size-1]=keys[l.value];
					index=l.value;
					cEvent=currentEvent[keys[l.value]];
					while ({cEvent.class==Event}, {
						cEvent=cEvent[cEvent.keys.asArray.sort[0]];
					});
					cueJT.cueID_(cEvent);
					//--------------------------------------------------------
					this.makeListViews(cueJT.pathsRelative[cEvent]);
				});
			}.value_(index).selectionMode_(\extended);
			views[\textField]=TextField(c, width@20).string_(folderNames[index]).action_{arg t;
				var path;
				path=(cueJT.rootPath++currentFolders.join($/)++"/");
				cueJT.rename(t.string, path)
			};
			Button(c, buttonWidth@20).states_([ ["+^"] ]).canFocus_(false).action_{
				var cEvent, path, id;
				//--------------------------------------------------------
				currentFolders[currentFolders.size-1]=keys[index];//is this line necesary?
				cEvent=currentEvent[keys[index]];
				while ({cEvent.class==Event}, {
					cEvent=cEvent[cEvent.keys.asArray.sort.first];
				});
				id=cEvent;
				path=(cueJT.rootPath++currentFolders.join($/)++"/");
				//--------------------------------------------------------
				cueJT.add("untitled", \addBefore, path, id);
			};//addBefore
			Button(c, buttonWidth@20).states_([ ["+v"] ]).canFocus_(false).action_{
				var cEvent, path, id;
				//--------------------------------------------------------
				currentFolders[currentFolders.size-1]=keys[index];//is this line necesary?
				cEvent=currentEvent[keys[index]];
				while ({cEvent.class==Event}, {
					cEvent=cEvent[cEvent.keys.asArray.sort.last];
				});
				id=cEvent;
				path=(cueJT.rootPath++currentFolders.join($/)++"/");
				cueJT.add("untitled", \addAfter, path, id+1);
				//cueJT.addAfter("untitled", path, index)
			};//addAfter
			Button(c, buttonWidth@20).states_([ ["-"] ]).canFocus_(false).action_{ cueJT.delete };//remove
			Button(c, buttonWidth@20).states_([ ["+<"] ]).canFocus_(false).action_{
				var path;
				path=(cueJT.rootPath++currentFolders.copyRange(0, (currentFolders.size-2).max(0)).join($/)++"/");
				cueJT.group("untitled", \addBefore, path, views[\listView].selection.collect{|i|
					(path++keys[i]++"/");
				}, index);
			};//addFolderBefore/group
			Button(c, buttonWidth@20).states_([ ["+>"] ]).canFocus_(false).action_{
				var path;
				path=(cueJT.rootPath++currentFolders.join($/)++"/");
				cueJT.addFolder("untitled", \addAfter, path);
			};//addFolderAfter
			Button(c, buttonWidth@20).states_([ ["--"] ]).canFocus_(false).action_{
				var path;
				path=(cueJT.rootPath++currentFolders.join($/)++"/");
				cueJT.deleteFolder(path)
			};//removeFolder
			Button(c, buttonWidth@20).states_([ ["<>"] ]).canFocus_(false).action_{
				var path, paths;
				if (views[\listView].selection.size==2, {
					path=(cueJT.rootPath++currentFolders.copyRange(0, (currentFolders.size-2).max(0)).join($/)++"/");
					paths=views[\listView].selection.collect{|i|
						(path++keys[i]++"/");
					}.postln;
					cueJT.swap(paths[0], paths[1]);
				});
			};//removeFolder
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
				this.makeListViews(cueJT.pathsRelative[cueJT.cueID])
			});
		};
		views[\nextB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\NEXT] ]).action_{
			if (cueJT.cueID<(cueJT.pathsRelative.size-1), {
				cueJT.cueID_(cueJT.cueID+1);
				this.makeListViews(cueJT.pathsRelative[cueJT.cueID])
			});
		};
		//StaticText(cMain, (bounds.x*0.2).floor.asInteger@20).string_("upcoming: ");
		views[\previous]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\left);
		views[\upcoming]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\right);
		cListViews=CompositeView(cMain, bounds.x@(bounds.y-40)); cListViews.addFlowLayout(0@0, 0@0); cListViews.background_(Color.rand);
		//f[\makeListViews].value(folders=f[\getAllListViews].value(deepFoldersAsEvent, 0))
		this.makeListViews(cueJT.pathsRelative[cueJT.cueID])
	}
}