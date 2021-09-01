/*
valPreset=object.removeAllWithoutActions(valPreset);//WARNING: THIS IS A NEW LINE!! COULD BREAK THIS
*/
CuesJT : PresetsFileJT {
	var <objectDefault;
	var <neuralNet, <blender;
	var <cueListJT;
	//	var <keysNoTransition;
	*new {arg object, cueName;
		/*
		^if (object.class==PresetsJT, {
		CuesPresetsJT(object, cueName)
		},{
		*/
		^super.basicNew(object, cueName)
		//})
	}
	//--------------------------------------------------------------------------------- INITS
	/*
	keysNoTransition_ {
	keysNoTransition=keys;
	//this.initSetAction
	}
	*/
	initPathName {
		basename=pathName;
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
		pathName=basename;
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

}

CuesGUIJT {
	var <presets;
	var <views, <parent, <bounds;
	var <prevIndex=0;
	*new {arg presets, parent, bounds;
		^super.newCopyArgs(presets).init(parent, bounds)
	}
	init {arg argparent, argbounds;
		var boundsName=(argbounds.x/3).floor@argbounds.y;
		var boundsButton=(boundsName.x/7).floor@argbounds.y;
		var c;
		bounds=argbounds??{350@20};
		views=();
		c=CompositeView(argparent, bounds.x@(bounds.y*2));
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
			presets.delete
		};
		views[\basename]=StaticText(c, boundsName).string_(presets.directory??{presets.basename}).font_(Font("Monaco", boundsName.y*0.45));
		views[\store]=Button(c, boundsButton).states_([ ["s"] ]).action_{
			//views[\basename].stringColor_(Color.black);
			presets.store
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
		presets.object[\method]=PopUpMenu(c, (bounds.x*0.1).floor@bounds.y).items_(
			[\restore, \valuesActionsTransition]
		).action_{|p|
			/*
			if (p.value==0, {
			{presets.object[\durations].value_(0)}.defer;
			});
			//presets.methodsArray[p.value];
			*/
		};
		presets.object[\durations]=EZNumber(c, (bounds.x*0.1).floor@bounds.y, nil, ControlSpec(0.0, 60.0), {|ez|
			if (ez.value<=0.0, {
				{presets.object[\method].valueAction_(0)}.defer
			});
		}, 0.0, false, 0).round2_(0.01);
		presets.object[\extras]=TextField(c, (bounds.x*0.8).floor@bounds.y).string_("").action_{arg t;
		};
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
		//presets.funcs[\directory].value;
	}
}