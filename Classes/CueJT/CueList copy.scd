/*
Dit is de CueMaster eigenlijk
zorg dat je hier juist de preWait, postWait en al dat soort gezelligheid inbouwt (plus editor), NIET bij de cuesJT. In een soort 0000_cuesInfo.scd file
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
CueList {
	var <root, <cues, <enviroment;
	var <pathNameNumberedManager;
	var updateAction, <actionList, <cueList, <actions;
	var <cueID;
	var <routine;

	*new {arg root, cues=(), enviroment=();
		^super.newCopyArgs(root, cues, enviroment).init
	}
	init {
		actions=();
		routine=Routine.new;
		this.analyzeCues;
		updateAction={arg pm;
			cueList=[];
			actionList=pm.deepFiles.collect{|paths,i|
				cueList=cueList.add(nil);
				paths.collect{|path,j|
					var key=PathName(path).fileNameWithoutExtension.split($_).copyToEnd.join($_).asSymbol;
					var out=path.load;
					var action;
					if (cues[key]!=nil, {
						/*
						cueInfo[key][\paths][i]=path;
						cuePaths[i][key]=path;
						cuesInfo[key][\action];
						action=this.makeAction();
						*/
						cueList[i]=cueList[i].addFunc({
							actions[key].value(out)
						});
					});
					[key, out];
				};
			};
		};
		pathNameNumberedManager=PathNameNumberedManager(root, updateAction);
		pathNameNumberedManager.action=pathNameNumberedManager.action.addFunc({arg index, pm;
			{cueList[index].value(index,pm)}.fork
		});
	}
	cueID_ {arg index;
		if (index<cueList.size, {
			routine.stop;
			routine={cueList[index].value(index, this, enviroment)}.fork;
			cueID=index;
		})
	}
	at {arg index;
		this.cueID_(index);
	}
	addCue {arg key, cue;
		cues[key]=cue;
		this.analyzeCue(key, cue);
		updateAction.value(pathNameNumberedManager);
	}
	analyzeCues {
		cues.keysValuesDo{arg key, cue;
			this.analyzeCue(key, cue)
		}
	}
	analyzeCue {arg key, cue;
		case(cue.asClass.superclasses.includesEqual(MetaPresetJT), {
			actions[key];
		});
	}
	update {arg updatePaths=false;
		if (updatePaths, {
			pathNameNumberedManager.updatePaths
		},{
			updateAction.value(pathNameNumberedManager)
		});
	}
	store {arg key, values;
		//cueList[i]
	}
	makeAction {arg action;
		^action
	}
	//addFunc{
	putAction {arg cueID=0, action;
		cueList[cueID]=cueList[cueID].addFunc(action)
	}
	makeGui {}
}