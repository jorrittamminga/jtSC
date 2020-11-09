PathSystemJT {
	var <>path, pathRelative, <>rootPath, fileName, extension="scd", numDigits=4;
	var <paths, <pathsRelative;
	var <pathStructure, <pathID=0, <gui;
	var <>slaveAction;
	//var <>funcs;

	*new {arg pathName;
		^super.new.init(pathName)
	}
	init {arg argPath;
		rootPath=argPath;
		//funcs=();
		this.updatePaths;
	}
	updatePaths {
		paths=PathName(rootPath).deepFolders.collect(_.fullPath);
		pathsRelative=PathName(rootPath).deepFoldersRelative.collect(_.fullPath);
		path=paths.first;
		this.analyzeStructure;
	}
	analyzeStructure {
		var index=0;
		pathStructure=();
		pathsRelative.do{|cueName|
			var folders=PathName(cueName).allFolders;
			var event=pathStructure;
			folders.do{|folderName, depth|
				if (event[folderName.asSymbol]==nil, {
					event[folderName.asSymbol]=()
				});
				if (depth==(folders.size-1), {
					event[folderName.asSymbol]=index
				});
				event=event[folderName.asSymbol];
			};
			index=index+1;
		};
		if (gui!=nil, {
			{this.gui.makeListViews(pathsRelative[0])}.defer
		});
		^pathStructure
	}
	pathID_ {arg index;
		pathID=index;
		path=paths[pathID];
		slaveAction.value(index, this);
	}
	//---------------------------------------------------------
	delete {arg argpath, action;
		var index;
		if (argpath!=nil, {
			index=paths.indexOf(argpath.asString);
			if (index!=nil, {
				pathID=index;
			});
		},{
			index=pathID
		});
		if (index!=nil, {
			{
				File.deleteNumbered(argpath??{path}, numDigits);
				paths.removeAt(pathID);
				pathsRelative.removeAt(pathID);
				//cueList.removeAt(pathID);
				path=paths[pathID];
				this.updatePaths;//brute force, this must be more efficient!
				action.value(this);
			}.fork
		});
	}
	add {arg pathName="untitled", addAction=\addAfter, argpath, index, action;
		{
			path=pathName.mkdirNumbered(argpath??{path}, addAction, numDigits);
			pathID=index??{pathID};
			paths=paths.insert(pathID, path);
			//pathsRelative=pathsRelative.insert(pathID,  path.replace(rootPath, "/"));
			//cueList=cueList.insert(pathID, ());
			//this.store;
			this.updatePaths;//brute force, this must be more efficient!
			action.value(this);
		}.fork
	}
	addBefore {arg pathName="untitled", argpath, index, action;
		this.add(pathName, \addBefore, argpath, index, action)
	}
	addAfter {arg pathName="untitled", argpath, index, action;
		this.add(pathName, \addAfter, argpath, index, action)
	}
	addFolder {arg pathName="untitled", addAction=\addAfter, argpath, action;
		var cond=Condition.new;
		var entries=PathName(argpath??{path}).entries;
		var newPath=(argpath??{path})++(0.asDigits(10, numDigits).join)++"_"++pathName++"/";
		{
			("mkdir " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			("mv " ++ path ++ "*." ++ extension ++ " " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			this.updatePaths;//brute force, this must be more efficient!
			action.value(this);
		}.fork;
	}
	group {arg pathName="untitled", addAction=\addBefore, argpath, argpaths, index, action;
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
			action.value(this);
		}.fork;
	}
	deleteFolder {arg argpath, action;
		var cond=Condition.new;
		var newPath=PathName(argpath??{path}).moveDir(1).fullPath;
		{
			("mv " ++ (argpath??{path}) ++ "*." ++ extension ++ " " ++ newPath).unixCmd({cond.unhang});
			cond.hang;
			("rm -r " ++ (argpath??{path})).unixCmd({cond.unhang});
			cond.hang;
			this.updatePaths;//brute force, this must be more efficient!
			action.value(this);
		}.fork;
	}
	rename {arg pathName, argpath, action;
		{
			PathName(argpath??{path}).renameNumbered(pathName, numDigits).fullPath;
			//paths[pathID]=path;
			//pathsRelative[pathID]=path.replace(rootPath, "/");
			this.updatePaths;//brute force, this must be more efficient!
			action.value(this);
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
	//-----------------------------------------
	makeGui {arg parent, bounds=350@20;
		{gui=PathSystemJTGUI(this, parent, bounds)}.defer
	}
}

PathSystemJTGUI {
	classvar pathSystemJT;
	var parent, bounds;
	var cMain, views, cListViews, numberOfColumns, indices;
	*new {arg pathSystem, parent, bounds;
		pathSystemJT=pathSystem;
		^super.new.init(parent, bounds)
	}
	makeListViews {arg cueName;
		var folders=PathName(cueName).allFolders;
		var width;
		var numberOfColums=folders.size;
		var event=pathSystemJT.pathStructure, kkeys=[];

		cListViews.removeAll;
		cListViews.decorator.reset;
		width=(bounds.x/numberOfColums).floor.asInteger;
		indices=[];

		views[\currentCue].string_(PathName(pathSystemJT.pathsRelative.clipAt(pathSystemJT.pathID)).allFolders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\previous].string_(PathName(pathSystemJT.pathsRelative.clipAt(pathSystemJT.pathID-1)).allFolders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)}.join($/));
		views[\upcoming].string_(PathName(pathSystemJT.pathsRelative.clipAt(pathSystemJT.pathID+1)).allFolders.collect{|i|
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
					pathSystemJT.pathID_(cEvent);
					//--------------------------------------------------------
					this.makeListViews(pathSystemJT.pathsRelative[cEvent]);
				});
			}.value_(index).selectionMode_(\extended);
			views[\textField]=TextField(c, width@20).string_(folderNames[index]).action_{arg t;
				var path;
				path=(pathSystemJT.rootPath++currentFolders.join($/)++"/");
				pathSystemJT.rename(t.string, path)
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
				path=(pathSystemJT.rootPath++currentFolders.join($/)++"/");
				//--------------------------------------------------------
				pathSystemJT.add("untitled", \addBefore, path, id);
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
				path=(pathSystemJT.rootPath++currentFolders.join($/)++"/");
				pathSystemJT.add("untitled", \addAfter, path, id+1);
				//pathSystemJT.addAfter("untitled", path, index)
			};//addAfter
			Button(c, buttonWidth@20).states_([ ["-"] ]).canFocus_(false).action_{ pathSystemJT.delete };//remove
			Button(c, buttonWidth@20).states_([ ["+<"] ]).canFocus_(false).action_{
				var path;
				path=(pathSystemJT.rootPath++currentFolders.copyRange(0, (currentFolders.size-2).max(0)).join($/)++"/");
				pathSystemJT.group("untitled", \addBefore, path, views[\listView].selection.collect{|i|
					(path++keys[i]++"/");
				}, index);
			};//addFolderBefore/group
			Button(c, buttonWidth@20).states_([ ["+>"] ]).canFocus_(false).action_{
				var path;
				path=(pathSystemJT.rootPath++currentFolders.join($/)++"/");
				pathSystemJT.addFolder("untitled", \addAfter, path);
			};//addFolderAfter
			Button(c, buttonWidth@20).states_([ ["--"] ]).canFocus_(false).action_{
				var path;
				path=(pathSystemJT.rootPath++currentFolders.join($/)++"/");
				pathSystemJT.deleteFolder(path)
			};//removeFolder
			Button(c, buttonWidth@20).states_([ ["<>"] ]).canFocus_(false).action_{
				var path, paths;
				if (views[\listView].selection.size==2, {
					path=(pathSystemJT.rootPath++currentFolders.copyRange(0, (currentFolders.size-2).max(0)).join($/)++"/");
					paths=views[\listView].selection.collect{|i|
						(path++keys[i]++"/");
					}.postln;
					pathSystemJT.swap(paths[0], paths[1]);
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
		parent=argparent??{
			var w=Window("cues", Rect(400,400,400,400)).front.alwaysOnTop_(true);
			w.addFlowLayout(4@4, 0@0);
			w
		};
		parent=CompositeView(parent, bounds);
		parent.background_(Color.green);
		parent.addFlowLayout(0@0, 0@0);

		cMain=CompositeView(parent, bounds); cMain.addFlowLayout(0@0, 0@0); cMain.background_(Color.rand);
		views[\currentCue]=StaticText(cMain, bounds.x@20).string_("").align_(\center);
		views[\prevB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\PREV] ]).action_{
			if (pathSystemJT.pathID>0, {
				pathSystemJT.pathID_(pathSystemJT.pathID-1);
				this.makeListViews(pathSystemJT.pathsRelative[pathSystemJT.pathID])
			});
		};
		views[\nextB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\NEXT] ]).action_{
			if (pathSystemJT.pathID<(pathSystemJT.pathsRelative.size-1), {
				pathSystemJT.pathID_(pathSystemJT.pathID+1);
				this.makeListViews(pathSystemJT.pathsRelative[pathSystemJT.pathID])
			});
		};
		//StaticText(cMain, (bounds.x*0.2).floor.asInteger@20).string_("upcoming: ");
		views[\previous]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\left);
		views[\upcoming]=StaticText(cMain, (bounds.x*0.5).floor.asInteger@20).string_("").align_(\right);
		cListViews=CompositeView(cMain, bounds.x@(bounds.y-40)); cListViews.addFlowLayout(0@0, 0@0); cListViews.background_(Color.rand);
		//f[\makeListViews].value(folders=f[\getAllListViews].value(deepFoldersAsEvent, 0))
		this.makeListViews(pathSystemJT.pathsRelative[pathSystemJT.pathID])
	}
}