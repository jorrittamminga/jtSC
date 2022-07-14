/*
- maak een 're-order' functie waarmee je de volgorde kunt instellen waarme de cues verlopen, dus de 0000, 0001 etc veranderen
-
*/
CueListJT {
	var <root, <cues, <>enviroment;
	var <pathNameNumberedManager, <gui, <value=0;
	var array, entries;
	var indices, prevIndex= -1;

	*new {arg path, cues=(), enviroment=(), numDigits=4;
		^super.new.init(path, cues, enviroment, numDigits)
	}
	init { arg pathname, argcues, argenviroment, argnumDigits;
		if (pathname.last!=$/, {pathname=pathname++"/"});
		root=pathname.asPathName;
		cues=argcues??{()};
		enviroment=argenviroment??{()};
		pathNameNumberedManager=PathNameNumberedManager(root, numDigits:argnumDigits);
		pathNameNumberedManager.parent=this;
		indices=();
		this.initCues;
		pathNameNumberedManager.updateAction={arg pm, key;
			if (key==nil, {
				this.updateCues;
			},{
				this.updateCue(cues[key],key)
			})
		};
		pathNameNumberedManager.action=pathNameNumberedManager.action.addFunc({arg index, pm, restoreFlag=true, method;
			var deepFoldersRelative, entries, entriesFullPath, keys, allKeys=cues.keys.asArray.copy, jump=false;
			restoreFlag=restoreFlag??{true};
			#deepFoldersRelative, entries, entriesFullPath, keys=this.getCurrent(pm);
			jump=(index-prevIndex)!=1;
			if (method==\renameFolder, {jump=false});
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
		if (cue.entries.size>0, {
			cue.restore
		});
		//cue.funcs[\directory].value(pathNameNumberedManager.deepFoldersRelative[pathNameNumberedManager.folderID], flag);
	}
	removeCue {arg cue;
		var key=cue.basename.asSymbol;
		cues.removeAt(key);
	}
	next {
		pathNameNumberedManager.next
	}
	prev {
		pathNameNumberedManager.prev
	}
	value_ {arg val;
		value=val;
		pathNameNumberedManager.folderID_(val);
	}
	makeGui {arg parent, bounds=350@20, boundsList;
		{gui=CueListGUI(this, parent, bounds, boundsList)}.defer
	}
	do {arg function, render=false;
		this.pathNameNumberedManager.deepFolders.copyRange(0, render.binaryValue
			*(this.pathNameNumberedManager.deepFolders.size-1)).do(function)
	}
	restore {arg id, values=(), specs=(), targets=(), scoreJT, time=0, render=false;
		var par=(), routines=(), target;
		PathName(this.pathNameNumberedManager.deepFolders[id]).entries.do{|pathname|
			var key, methodFlag=false;
			var durations, out, delayTimes, curves, extras;
			key=pathname.fileNameWithoutExtension.split($_).copyToEnd(1).join($_).asSymbol;
			par[key]=values[key].deepCopy;
			values[key]=pathname.fullPath.load;
			if ((values[key][\method_CuesJT]==nil), {
				par[key]=values[key].deepCopy
			},{
				if (values[key][\method_CuesJT]<1, {
					par[key]=values[key].deepCopy;
				},{
					methodFlag=true
				});
			});
			[\durations_CuesJT, \method_CuesJT, \extras_CuesJT, \routinesJT].do{|key2| par[key].removeAt(key2)};
			if (methodFlag&&render,{
				target=targets[key]??{values[key]};
				switch(target.class, Synth, {
					scoreJT.addTransition(time, targets[key], par[key], values[key]);
				}, Event, {
					[\routinesJT].do{|key2| par[key].removeAt(key2); values[key].removeAt(key2)};
					durations=values[key][\durations_CuesJT].deepCopy;
					extras=values[key][\extras];
					if (extras!=nil, {
						if (extras[\durations_CuesJT]!=nil, {

						});

					},{
						extras=();
					});
					out=par[key].valuesActionsTransition(values[key], durations, specs: specs[key].deepCopy.asSpec
						, nrt:render);
					values[key][\routinesJT]=out[\routinesJT];
				}, Function, {

				})
			},{
				switch(targets[key].class, Synth, {
					scoreJT.add([time, targets[key].setMsg(*par[key].asKeyValuePairs)])
				}, Event, {

				})
			})
		};
		//--------------------------- OUTPUT
		^values.deepCopy
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
		views[\PathNameNumbered]=PathNameNumberedGUI(cueList.pathNameNumberedManager, c
			, argboundsList??{bounds.x@bounds.x});
	}
}