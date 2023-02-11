/*
folderID_ is nog wat te lomp, voert alle actions nog eens een keertje uit, lijkt me wat overdreven....
maak voor de gui ook een listView met de deepFolderNamesWithoutNumbers
of misschien drie type views: \nested, \listview, \combi (nested én listview)
maak een onderscheid (hahaha, onder schijt) tussen Files (deepFiles) en Folders (deepFolders)
betere naam verzinnen (PathNavigator? PathOrganizer? )
subclass of PathName?
*/
PathNameNumberedManager : Numbered {
	var <deepFiles, <deepKeys, <deepFilesPathName, <deepFolders, <deepFoldersPathName, <deepFoldersRelative
	, <currentFolder, <currentFile, <currentPathName;
	var <folderStructure, <folderID, <deepFolderNamesWithoutNumbers;
	var <>action, <>actionList, <>updateAction, <>parent;

	*new {arg pathName, updateAction, numDigits=4;
		^super.new.init(pathName, updateAction, numDigits)
	}
	init {arg argPath, argupdateAction, argnumDigits;
		var pathName;
		rootPathName=argPath.asPathName;
		rootPath=rootPathName.fullPath;
		if ((rootPathName.isFolder) && (rootPath.last!=$/), {rootPath=rootPath++"/"});
		numDigits=argnumDigits;//??{4};
		if (File.exists(rootPath).not, {File.mkdir(rootPath)});
		if (rootPathName.entries.size==0, {
			File.mkdir( rootPath++(0.asDigits(10, numDigits).join++"_"++"Init/"))
		});
		numDigits=numDigits??{argPath.asPathName.getNumDigits.asArray.maxItem.unbubble};
		updateAction=argupdateAction;
		this.updatePaths;
	}
	numDigits_ {arg digits=4;
		numDigits=digits;
		//en renumberen qua aantal digits
	}
	rootPath_ {arg path;
		this.init(path)
	}
	//--------------------------------------------------------- FILE/FOLDER ANALYSIS
	updatePaths {arg index, actionArgs, updateActionArgs, method;
		deepFoldersPathName=rootPathName.deepFolders;
		deepFolders=deepFoldersPathName.collect(_.fullPath);
		deepFoldersRelative=deepFolders.collect{|p| p.replace(rootPath, "/")};
		deepFilesPathName=deepFoldersPathName.collect{|p| p.entries};
		deepFiles=deepFilesPathName.collect{|p| p.collect(_.fullPath)};
		deepKeys=deepFilesPathName.collect{|p| p.collect{|p|
			p.fileNameWithoutExtension.split($_).copyToEnd(1).join($_).asSymbol}};
		this.analyzeFolderStructure;
		updateAction.value(this, updateActionArgs);
		if (index.class==String, {
			this.folderIDfromPath_(index, actionArgs, method)
		},{
			this.folderID_(index??{folderID??{0}}, actionArgs, method)
		});
	}
	analyzeFolderStructure {
		var index=0;
		folderStructure=();
		deepFoldersRelative.do{|cueName|
			var folders=PathName(cueName).allFolders;
			var event=folderStructure;
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
		deepFolderNamesWithoutNumbers=deepFoldersRelative.collect{arg path,i;
			var event=folderStructure.copy;
			PathName(path).allFolders.collect{|name,i|
				var keys, index, folderIDs, extras;
				name=name.asSymbol;
				keys=event.keys.asArray.sort;
				extras={""}!keys.size;
				index=keys.indexOf(name);
				folderIDs=keys.collect{|key,i|
					var event2=event.copy[key];
					while({
						event2.class==Event
					}, {
						extras[i]="/";
						key=event2.keys.asArray.sort[0];
						event2=event2[key];
					});
					event2
				};
				event=event[name.asSymbol];
				[
					keys.collect{|key,i|
						//key.asString.split($_).copyToEnd(1).join($_)
						key
					}
					, index, folderIDs, extras];
			};
		};
		^folderStructure
	}
	//--------------------------------------------------------- NAVIGATION
	folderIDfromPath_ {arg path, actionArgs;
		var index;
		index=deepFolders.indexOfEqual(path);
		if (index!=nil, {this.folderID_(index, actionArgs)});
	}
	folderID_ {arg index, actionArgs, method;
		if (index<deepFolders.size, {
			folderID=index;
			currentFolder=deepFolders[folderID];
			currentPathName=deepFoldersPathName[folderID];
			action.value(index, this, actionArgs, method);
		});
	}
	prev {arg action;
		if (folderID>0, {
			this.folderID_(this.folderID-1);
			action.value(folderID, this)
		});
	}
	next {arg action;
		if (folderID<(deepFolders.size-1), {
			this.folderID_(this.folderID+1);
			action.value(folderID, this);
		});
	}
	//--------------------------------------------------------- FILE/FOLDER MANAGEMENT
	delete {arg path, action;
		var pathname;
		if (path.class!=Array, {path=[path]});
		path.do{|path|
			pathname=if (path==nil, {currentFolder.asPathName},{path.asPathName});
			if (pathname.isFile, {
				{
					NumberedFile.delete(pathname);
					//this.prDeleteFile(pathname, action)
					this.updatePaths;
					action.value(this);
				}.fork
			},{
				//this.prDeleteFolder(pathname, action)
				{
					NumberedFolder.delete(pathname);
					this.updatePaths;
					action.value(this);
				}.fork
			});
		}
	}
	addFolder {arg folderName, target, addAction=\addAfter, action;
		folderName=folderName??{Date.localtime.stamp};
		target=target??{currentFolder};
		{
			var folder;
			folder=NumberedFolder(folderName, target.asPathName, addAction, numDigits, \allFiles);
			this.updatePaths(folder.pathName);
			action.value(this);
		}.fork;
	}
	groupFolder {arg folderName, targets, addAction=\addBefore, action;
		folderName=folderName??{Date.localtime.stamp};
		{
			NumberedFolder.groupNumbered(folderName, targets, addAction, numDigits);
			this.updatePaths;
			action.value(this);
		}.fork;
	}
	prAddFile {}
	renameFolder {arg folderName="test", pathName, action;
		{
			NumberedFolder.rename(folderName, pathName);
			this.updatePaths(method: \renameFolder);
			action.value(this);
		}.fork
	}
	moveUp {

	}
	moveDown {

	}
	/*
	swap {arg path1, path2;
	var pre1, pre2, path1New, path2New, cond=Condition.new;
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
	*/
	//-----------------------------------------
	makeGui {arg parent, bounds=350@250;
		{gui=PathNameNumberedGUI(this, parent, bounds)}.defer
	}
}
PathNameNumberedGUI {
	classvar pathNameNumbered;
	var parent, bounds;
	var cMain, <views, cListViews, numberOfColumns, indices;
	var <pathNameGuis, font;
	var <hiliteColor;

	*new {arg pathSystem, parent, bounds;
		pathNameNumbered=pathSystem;
		^super.new.init(parent, bounds)
	}
	makePathNameWithoutNumbers {arg pathName;
		^PathName(pathName).allFolders.collect{|i| this.removeNumber(i) }.join($/)
	}
	makeDeepestPathNameWithoutNumbers {arg pathName;
		^PathName(pathName).allFolders.last.asString.copy.split($_).copyToEnd(1).join($_)
	}
	removeNumber {arg name;
		^name.asString.copy.split($_).copyToEnd(1).join($_)
	}
	getCurrentFolder {arg depth=0;
		^if (depth>=0, {
			(pathNameNumbered.rootPath++PathName("/"
				++ (pathNameNumbered.currentFolder.copy.replace(pathNameNumbered.rootPath, "")))
			.allFolders.copyRange(0, depth).join($/)++ "/")
		},{
			pathNameNumbered.rootPath.copy
		})
	}
	makePathNameGUI {arg parent, bounds, entries, action, value, depth, extras;
		var c, bb, fontButton, fontList, views, minHeight=20;
		var entriesWithoutNumbers=entries.collect{|i,k| this.removeNumber(i)++extras[k]};
		bb=(bounds.x/8).floor;
		views=();
		fontList=Font("Monaco", 20);
		fontButton=Font("Monaco", 10);
		c=CompositeView(parent, bounds); c.addFlowLayout(0@0, 0@0);
		action=action.addFunc({arg l;
			{views[\name].string_(entriesWithoutNumbers[l.value])}.defer

		});
		views[\listView]=ListView(c, bounds.x@(bounds.y- (2*bb.min(20)) )).items_(
			entriesWithoutNumbers
		).action_(action).font_(fontList).value_(value).selectionMode_(\extended)
		.background_(Color.black).hiliteColor_(hiliteColor).stringColor_(Color.grey(0.5))
		.selectedStringColor_(Color.white);
		views[\name]=TextField(c, bounds.x@(bb.min(minHeight))).string_(entriesWithoutNumbers[value]).action_{arg t;
			pathNameNumbered.renameFolder( t.string, this.getCurrentFolder(depth));
		}.font_(fontButton);
		bb=bb@(bb.min(minHeight));
		[[\addBefore,"±"],[\addAfter,"+"],[\addToHead,"<"],[\addToTail,">"]].do{arg i;
			var key=i[0], icon=i[1];
			views[key]=Button(c, bb).states_([[ icon ]]).font_(fontButton).action_{
				pathNameNumbered.addFolder(nil, this.getCurrentFolder(depth), key )
			}.canFocus_(false);
		};
		views[\delete]=Button(c, bb).states_([ ["-"] ]).font_(fontButton).action_{
			var selection=views[\listView].selection.sort;
			if (views[\listView].selection.size>1, {
				var folder=this.getCurrentFolder(depth-1);
				pathNameNumbered.delete( selection.collect{|i| folder++entries[i]++"/"};)
			},{
				pathNameNumbered.delete( this.getCurrentFolder(depth) )
			})
		}.canFocus_(false);
		views[\group]=Button(c, bb).states_([ ["g"] ]).font_(fontButton).action_{
			var targets=views[\listView].selection.sort.collect{|i|
				this.getCurrentFolder(depth-1)++entries[i]++"/"
			};
			pathNameNumbered.groupFolder(nil, targets, \addBefore)
		}.canFocus_(false);
		views[\moveUp]=Button(c, bb).states_([ ["^"] ]).font_(fontButton).canFocus_(false);
		views[\movDown]=Button(c, bb).states_([ ["v"] ]).font_(fontButton).canFocus_(false);
		^views[\listView]
	}
	init {arg argparent, argbounds;
		hiliteColor=Color.green(0.4);
		font=Font("Monaco", 10);
		views=();
		indices=[0];
		bounds=argbounds.copy;
		parent=argparent??{
			var w=Window("cues", Rect(400,400,bounds.x+8,bounds.y+8)).front.alwaysOnTop_(true);
			w.addFlowLayout(4@4, 0@0);
			w
		};
		parent=CompositeView(parent, bounds);
		parent.background_(Color.grey);
		parent.addFlowLayout(0@0, 0@0);
		cMain=CompositeView(parent, bounds); cMain.addFlowLayout(0@0, 0@0); cMain.background_(Color.grey);
		views[\prevB]=Button(cMain, (font.size*2.75).floor@(font.size*2.75).floor).states_([ ["<"] ]).action_{
			pathNameNumbered.prev;
		}.font_(font).canFocus_(false);
		views[\currentPath]=StaticText(cMain, (bounds.x-(font.size*5.5))@(font.size*2.75))
		.string_(

			pathNameNumbered.deepFoldersRelative[pathNameNumbered.folderID]

		).align_(\center)
		.font_(Font(font.name, font.size*2)).stringColor_(Color.white).background_(hiliteColor);
		views[\nextB]=Button(cMain, (font.size*2.75).floor@(font.size*2.75).floor).states_([ [">"] ]).action_{
			pathNameNumbered.next
		}.font_(font).canFocus_(false);
		views[\previous]=StaticText(cMain, (bounds.x*0.5-10).floor.asInteger@20).string_("").align_(\left).font_
		.stringColor_(Color.white);
		views[\bypass]=Button(cMain, 20@20).states_([ [\I],[\I, Color.black, Color.green] ]).action_{|b|
			var bypass=(b.value>0).not;
			pathNameNumbered.parent.cues.keysValuesDo{|key, cue|
				cue.bypass_(bypass);
			};
		}.value_(1).canFocus_(false);
		views[\upcoming]=StaticText(cMain, (bounds.x*0.5-10).floor.asInteger@20).string_("").align_(\right).font_(font)
		.stringColor_(Color.white);
		cMain.decorator.nextLine;
		cListViews=CompositeView(cMain, bounds.x@(bounds.y-cMain.decorator.top));
		cListViews.addFlowLayout(0@0, 0@0);
		cListViews.background_(Color.grey);
		pathNameNumbered.action=pathNameNumbered.action.addFunc({arg index, p;
			var folderInfo=p.deepFolderNamesWithoutNumbers[index];
			{
				var boundsPathNames=(bounds.x/folderInfo.size).floor@cListViews.bounds.height;
				cListViews.removeAll;
				cListViews.decorator.reset;
				views[\pathNames]=folderInfo.collect{arg folderInfo, depth;
					this.makePathNameGUI(cListViews, boundsPathNames, folderInfo[0], {|l|
						if (l.selection.size==1, {
							p.folderID_(folderInfo[2][l.value])
						},{
							l.selection;
						})
					}, folderInfo[1], depth, folderInfo[3]);
				};
				[\previous, \currentPath, \upcoming].do{|key,i|
					views[key].string_(
						//this.makePathNameWithoutNumbers(p.deepFoldersRelative.clipAt(index+i-1))
						this.makeDeepestPathNameWithoutNumbers(p.deepFoldersRelative.clipAt(index+i-1))
					);
				};
			}.defer;
		});
		pathNameNumbered.folderID_(pathNameNumbered.folderID);
	}
}