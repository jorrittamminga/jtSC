CueListJT {
	var <root, <cues, <>enviroment;
	var <pathNameNumberedManager, <gui;
	var array, entries;
	var indices, prevIndex= -1;

	*new {arg path, cues=(), enviroment=();
		^super.new.init(path, cues, enviroment)
	}
	init { arg pathname, argcues, argenviroment;
		root=pathname.asPathName;
		cues=argcues??{()};
		enviroment=argenviroment??{()};
		pathNameNumberedManager=PathNameNumberedManager(root);
		indices=();
		this.initCues;
		pathNameNumberedManager.updateAction={arg pm, key;
			if (key==nil, {
				this.updateCues;
			},{
				this.updateCue(cues[key],key)
			})
		};
		pathNameNumberedManager.action=pathNameNumberedManager.action.addFunc({arg index, pm, restoreFlag=true;
			var deepFoldersRelative, entries, entriesFullPath, keys, allKeys=cues.keys.asArray.copy, jump=false;
			restoreFlag=restoreFlag??{true};
			#deepFoldersRelative, entries, entriesFullPath, keys=this.getCurrent(pm);
			jump=(index-prevIndex)!=1;
			/*
			cues.keysValuesDo{|key,cue|
			cue.directory_(pm.currentPathName);
			cue.funcs[\directory].value(deepFoldersRelative, keys.includesEqual(key) );
			};
			*/
			this.changeCuesDirectory(pm.currentPathName, deepFoldersRelative, keys);
			entriesFullPath.do{arg path,i;
				var key, cue, index;
				key=keys[i];
				cue=cues[key];
				if (cue!=nil, {
					index=cue.entriesFullPath.indexOf(path);
					if ((index!=nil) && (restoreFlag), {
						cue.restore(index);
					});
					indices[key]=index;
					allKeys.remove(key);
				});
			};
			if (jump, {
				allKeys.do{|key|
					var i=index.copy, flag=true;
					var cue=cues[key];
					var folders=cue.entries.collect{|p| p.pathOnly};
					while({flag&&(i>0)},{
						i=i-1;
						flag=folders.includesEqual(pm.deepFolders[i]).not;
					});
					i=folders.indexOfEqual(pm.deepFolders[i]);
					if (i!=indices[key], {
						indices[key]=i;
						if (restoreFlag, {
							cue.restore(i);
						})
					});
				}
			});
			prevIndex=index;
		});
	}
	getCurrent {arg pm;
		var deepFoldersRelative=pm.deepFoldersRelative[pm.folderID];
		var entries=pathNameNumberedManager.deepFilesPathName[pathNameNumberedManager.folderID];
		var entriesFullPath=pathNameNumberedManager.deepFiles[pathNameNumberedManager.folderID];
		var keys=pathNameNumberedManager.deepKeys[pathNameNumberedManager.folderID];
		^[deepFoldersRelative, entries, entriesFullPath, keys]
	}
	changeCuesDirectory {arg dir, deepFoldersRelative, keys;
		cues.keysValuesDo{|key,cue|
			this.changeCueDirectory(cue,key, dir, deepFoldersRelative, keys)
		};
	}
	changeCueDirectory {arg cue, key, dir, deepFoldersRelative, keys;
		cue.directory_(dir);
		cue.funcs[\directory].value(deepFoldersRelative, keys.includesEqual(key) );
	}
	updateCues {
		cues.keysValuesDo{arg key, cue;
			this.updateCue(cue,key);
		}
	}
	getCueEntries {arg cue, key;
		var entries=[];
		pathNameNumberedManager.deepKeys.do{arg keys, index;
			keys.do{|k,i|
				if (k==key, {
					entries=entries.add(pathNameNumberedManager.deepFilesPathName[index][i])
				})
			}
		};
		//entries.do{|entry| entry.fullPath};
		^entries
	}
	updateCue {arg cue, key;
		var entries=this.getCueEntries(cue,key);
		if (entries.size>0, {
			cue.update(entries);
		});
	}
	initCues {
		var deepFoldersRelative, entries, entriesFullPath, keys;
		#deepFoldersRelative, entries, entriesFullPath, keys=this.getCurrent(pathNameNumberedManager);
		cues.keysValuesDo{arg key, cue;
			this.initCue(cue,key);
			this.updateCue(cue,key);
		}
	}
	initCue {arg cue, key, deepFoldersRelative, keys;
		cue.funcs[\add]=cue.funcs[\add].addFunc({arg index,cue;
			pathNameNumberedManager.updatePaths(actionArgs:false)//brute force!!!
		});
		cue.funcs[\delete]=cue.funcs[\delete].addFunc({
			pathNameNumberedManager.updatePaths(actionArgs:false)//brute force!!!
		});
		cue.entriesAction={arg paths;
			paths??{this.getCueEntries(cue, key)};
		};
		cue.rootPath=pathNameNumberedManager.rootPath;
		this.changeCueDirectory(cue, key, pathNameNumberedManager.currentPathName, deepFoldersRelative, keys);
	}
	addCue {arg cue;
		var key=cue.basename.asSymbol, flag=false;
		var deepFoldersRelative, entries, entriesFullPath, keys;
		#deepFoldersRelative, entries, entriesFullPath, keys=this.getCurrent(pathNameNumberedManager);
		cues[key]=cue;
		this.initCue(cue, key, deepFoldersRelative, keys);
		this.updateCue(cue,key);
		if (cue.entries.size>0, {cue.restore});
		//cue.funcs[\directory].value(pathNameNumberedManager.deepFoldersRelative[pathNameNumberedManager.folderID], flag);
	}
	makeGui {arg parent, bounds=350@20, boundsList;
		{gui=CueListGUI(this, parent, bounds, boundsList)}.defer
	}
}
CueListGUI {
	var <cueList;
	var <views, <parent, <bounds;
	*new {arg cueList, parent, bounds, boundsList;
		^super.newCopyArgs(cueList).init(parent, bounds, boundsList)
	}
	init {arg argparent, argbounds, argboundsList;
		var c;
		views=();
		parent=argparent;
		bounds=argbounds;
		//c=CompositeView(parent, bounds.x@(bounds.x+bounds.y));
		c=CompositeView(parent, argboundsList??{bounds.x@bounds.x});
		c.addFlowLayout(0@0,0@0);
		views[\PathNameNumbered]=PathNameNumberedGUI(cueList.pathNameNumberedManager, c, argboundsList??{bounds.x@bounds.x});
	}
}