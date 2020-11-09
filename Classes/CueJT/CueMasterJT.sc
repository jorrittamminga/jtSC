/*
cue -> (index:0, method: \restore, args:[], preAction:{}, postAction: {}, preWait: 0, postWait: 30, continue: false)
maak een duidelijk verschil (qua naamgeving) tussen cueJTs (CueSlaveJT) en objects (b.v. PresetJT)
maar een cue kan misschien ipv een index:0 ook een presetnaam hebben? dus b.v. index: "greatreverb" oid
cueJTs[index].doMethod(method)(*args)
cueJTs=(delay: [PresetJT, PresetCollection], reverb: PresetJT)
CueJT( (000)
PresetJT
cueList = [(000master: (), grain: ()), (000master: (), grain: ()), etc]
pathsRelative = ["/0000_Section1/0000_Part1/0000_start/","/0000_Section1/0000_Part1/0001_continue/" ,"/0000_Section1/0000_Part1/0002_stop/"]
cueFunctionLists = [FunctionList, FunctionList, FunctionList]
pathsRelativeNested = ()
Node PresetJT
*/
CueJTMaster {
	var <value, <gui;
	var name;
	var <>path, <>pathRelative, <>rootPath, <cueID=0, fileName, extension="scd", numDigits=4;
	var <>paths, <>pathsRelative, <enviroment;
	var <>funcList, <>values;

	var <cues, <cueJTs, <cueList, cueFuncs, <cueStructure;
	var <pathSystem;

	*new {arg path, cueJTs, name="0000_master", enviroment;
		^super.new.init(path, cueJTs, name, enviroment)
	}
	init {arg argpath, argcueJTs, argname, argenviroment;
		rootPath=argpath;
		pathSystem=PathSystemJT(rootPath);
		pathSystem.slaveAction=pathSystem.slaveAction.addFunc({arg index; this.cueID_(index)});
		paths=pathSystem.paths;
		pathsRelative=pathSystem.pathsRelative;

		cueJTs=argcueJTs??{()};
		fileName=argname??{"0000_master"};
		value=();
		enviroment=argenviroment??{()};
		//------------------------------------ cueJTs, most of the time CueJTSlaves
		if (cueJTs.class==Array, {
			var tmp=();
			cueJTs.do{|object| tmp[object.fileName.asSymbol]=object};
			cueJTs=tmp;
		});
		//------------------------------------------------------------ INIT cueJTs
		cueJTs.keysValuesDo{|key,object|
			this.initCueJT(object);
		};
		//-------------------------------------
		this.initCueJTs;
		this.cueID_(0);
		//this.analyzeStructure;
	}
	initCueJT {arg cueJT;
		cueJT.rootPath=rootPath;
		cueJT.paths=Array.fill(pathSystem.paths.size, nil);
		cueJT.pathsRelative=Array.fill(pathSystem.paths.size, nil);
		cueJT.funcList=Array.fill(pathSystem.paths.size, nil);
		cueJT.values=Array.fill(pathSystem.paths.size, nil);
		cueJT.objectList=Array.fill(pathSystem.paths.size, nil);
		cueJT.cueMaster=this;
	}
	storeAction {
		//cueList[cueID][fileName.asSymbol]=value;
	}
	storeAll {}
	cueID_ {arg index;
		var jumpToCue=( (index-cueID) == 1).not;
		cueID=index;
		path=paths[cueID];
		pathRelative=pathsRelative[cueID];
		cueJTs.keysValuesDo{|key, cueJT|
			var index=cueJT.cueID;
			if (cueJT.paths[cueID]!=nil, {
				if (cueID!=cueJT.cueID, {
					cueJT.cueID_(cueID);
					cueJT.restore;
				},{
					if (cueJT.gui!=nil, {
						{cueJT.gui.views[\cueName].stringColor_(Color.black)}.defer;
					})
				})
			},{
				if (jumpToCue, {
					if (cueJT.cueID!=cueID, {
						cueJT.previousCue(cueID);
						if (index!=cueJT.cueID, {
							cueJT.restore;
						})
					})
				},{

				});
				if (cueJT.gui!=nil, {
					{cueJT.gui.views[\cueName].stringColor_(Color.red)}.defer;
				});
			})
		};
	}
	//------------------------------------------
	addCueJT {arg cueJT, key;
		if (cueJTs.keys.asArray.includesEqual(key).not, {
			cueJTs[key]=cueJT;
			this.initCueJT(cueJT);
			pathSystem.paths.do{|path, i|
				if ( PathName(path).entries.collect{|p| p.fileNameWithoutExtension}.includesEqual(key.asString), {
					cueJT.paths[i]=path;
					cueJT.pathsRelative[i]=pathSystem.pathsRelative[i];
				});
				if (i==0, {
					if (cueJT.paths[0]==nil, {
						cueJT.paths[0]=path;
						cueJT.pathsRelative[0]=pathSystem.pathsRelative[0];
						cueJT.cueID_(0);
						cueJT.store;
					});
				});
			}
		},{
			"Warning: key ".post; key.post; " is already used!".postln;
		});
	}
	initCueJTs {
		//moet het eigenlijk collect zijn? is .do niet voldoende?
		pathSystem.paths.collect{|path, i|
			PathName(path).entries.do{arg pathname;
				var fileName=pathname.fileNameWithoutExtension.asSymbol;
				if (cueJTs[fileName]!=nil, {
					cueJTs[fileName].paths[i]=path;
					cueJTs[fileName].pathsRelative[i]=pathSystem.pathsRelative[i];
					cueJTs[fileName].funcList[i]=cueJTs[fileName].makeFunc(pathname.fullPath.load);
				});
			};
			if (i==0, {
				cueJTs.keysValuesDo{|key,cueJT|
					if (cueJT.paths[0]==nil, {
						cueJT.paths[0]=path;
						cueJT.pathsRelative[0]=pathSystem.pathsRelative[0];
						cueJT.funcList[0]=cueJT.makeFunc(path.fullPath.load);
						cueJT.cueID_(0);
						cueJT.store;
					});
				};
			},{

			});
		};
	}
	loadFunc {
		^{arg pathname, key, event;
			var object, values, index;
			if (cueJTs[key]==nil, {
				pathname.fullPath.load??{()}
			},{
				values=pathname.fullPath.load??{()};
				index=values[\index]??{0};
				if (cueJTs[key]!=nil, {
					if (cueJTs[key].class==Array, {
						object=cueJTs[key][index];
					});
				});
				this.makeFunc(object, values)
			})
		}
	}
	makeGui {arg parent, bounds=350@240;
		{gui=CueJTMasterGUI(this, parent, bounds)}.defer
	}
}
//==================================================
CueJTMasterGUI {
	var <pathSystemGUI;
	var parent, bounds;
	var cMain, <>views, cListViews, numberOfColumns, indices;
	var cueJT;

	*new {arg cueSystem, parent, bounds;
		^super.new.init(cueSystem, parent, bounds)
	}

	init {arg cueSystem, argparent, argbounds;
		cueJT=cueSystem;
		parent=argparent??{var w=Window("Cues", Rect(400,400,400,400)); w.addFlowLayout(0@0, 0@0); w.alwaysOnTop_(true); w};
		bounds=argbounds??{350@240};
		parent=CompositeView(parent, bounds);
		parent.background_(Color.blue);
		parent.addFlowLayout(0@0, 0@0);
		pathSystemGUI=PathSystemJTGUI(cueJT.pathSystem, parent);
	}
}
