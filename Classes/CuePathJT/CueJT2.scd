/*
values=(index:0, method: \restore, args:[], preAction:{}, postAction: {}, preWait: 0, postWait: 30, continue: false)
path / folders / cuename / master.scd (en alle andere slaves b.v. delay.scd, granular.scd etc)
dus een folder als laatste met de cuename met hierin de files van de master en slaves

[ ["part1", [ ["section1", ()], ["section2", ()], ["section3", ()], ["section4", ()] ]
*/
CueSystemJT {
	var <path, <fileName;
	*new {}
	init {}
}
CueFileJT {
	var <cueName, <directories, <targetName, <target, <gui, <values, <cueSystem, paths, path, <cueFlagList;
	var <cueIndices, <>cueEvent;

	getAllPaths {
		paths=PathName(path).deepFiles.collect{arg p; p.fullPath};
	}
	store {
		var pathName=path++directories.join($/)++"/"++targetName++".scd";
		var file=File(pathName, "w");
		var event=cueSystem.cueEvent;
		file.write(values.asCompileString);
		file.close;
		directories.do{|dir| event=event[dir.asSymbol]};
		event[targetName.asSymbol]=values;
		cueSystem.makeCueList;
	}
	load {arg pathName;
		var func;
		pathName=pathName??{(path++directories.join($/)++"/"++targetName++".scd")};
		values=pathName.load;
		if (values[\preAction]!=nil, {
			func=values[\preAction];
		});
		func=func.addFunc({
			target[values[\index]].method(*values[\args])
		});
		if (values[\postAction]!=nil, {
			func=func.addFunc(values[\postAction]);
		});
		^func
	}
	prInit {
		if (target.class!=Array, {target=[target]});
		cueEvent=();

	}
	makeGui {arg parent, bounds=350@20;
		{}.defer
	}
}
CueMasterFileJT : CueFileJT {
	var <cueList, <cue=0, <cueName;
	var <>cueSlaves, <pathToCueIndex;
	var pathStructure;

	*new {arg target, path;
		^super.new.init(target, path)
	}
	init {arg argtarget, path;
		this.prInit;
		targetName="000Master";
		cueSystem=this;
		this.loadAll;
	}
	loadAll {
		var lastFunc, flag=false, lastCueName="";
		cueList=[];
		cueEvent=();
		pathToCueIndex=();
		pathStructure=[];

		PathName(path).deepFolders.collect{arg p, index;
			var pathName=p.fullPath, folders, event=cueEvent, cueName, funcList, targetName;
			pathName=pathName.replace(path,"");
			folders=PathName(pathName).allFolders;
			folders=folders.collect{|cueName| cueName.split($_).copyToEnd(1).join($_)};
			cueName=folders[folders.size-1]
			folders.do{|folder|
				if (event[folder]==nil, {event[folder]=()});
				event=event[folder]
			};
			p.entries.do{|pp,i|
				var key=pp.fileNameWithoutExtension;
				var func=pp.fullPath.load;
				if (key.asString==targetName, {
					cueEvent[index]=func;
				},{
					cueSlaves[key.asSymbol].cueEvent[index]=func;
				});
				funcList=funcList.addFunc(func);
			};
			cueList=cueList.add(funcList);
		};
		cueSlaves.do{|slave|
			slave.cueIndices=slave.cueEvent.keys.asArray.sort;
		}
	}
	cue_ {arg value;
		cue=value;
		cueList[cue].value;
	}
	storeAll {}
	next {}
	prev {}
	add {}
	addBefore {}
	remove {}
	cueName_ {}
}

CueSlaveFileJT : CueFileJT {
	*new {arg target, cueSystem, targetName;

	}
	init {
		this.prInit;
		cueSystem.cueSlaves=cueSystem.cueSlaves.add(this);
	}
}