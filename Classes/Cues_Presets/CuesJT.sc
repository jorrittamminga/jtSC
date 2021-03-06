CuesJT : PresetsFileJT {
	var <objectDefault;

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
	initPathName {
		basename=pathName;
	}
	initSetAction {
		var actionFunc;
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
					valPreset.keysValuesDo{|key,val|
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
						presetsObject[key].action.value(val);
						{presetsObject[key].value_(val)}.defer;
					};
				};
				actionPreset={
					valPreset.keysValuesDo{|key,val|
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
							actionPreset={presetsObject.valuesActionsTransition(valPreset, extras[\durations], extras[\curves], extras[\delayTimes]
								, extras[\specs], extras[\actions], extras[\resolution]??{10})}
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
			})
		};
		action=action.addFunc(actionFunc);
	}
	addToCueList {arg cueList;
		pathName=basename;
		cueList.addCue(this);
	}
	cueName {^basename}
	makeGui {arg parent, bounds=350@20;
		{gui=CuesGUIJT(this, parent, bounds)}.defer;
	}

}

CuesGUIJT {
	var <presets;
	var <views, <parent, <bounds;
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
				views[\basename].stringColor_(Color.black);
				presets.add(presets.basename, \addToHead, presets.directory)
			},{
				presets.store
			});
		};
		views[\addAfter]=Button(c, boundsButton).states_([ ["+"] ])
		.action_{
			if (views[\basename].stringColor==Color.red, {
				views[\basename].stringColor_(Color.black);
				presets.add(presets.basename, \addToTail, presets.directory)
			},{
				presets.store
			});
		};
		views[\delete]=Button(c, boundsButton).states_([ ["-"] ]).action_{
			presets.delete
		};
		views[\store]=Button(c, boundsButton).states_([ ["s"] ]).action_{
			//views[\basename].stringColor_(Color.black);
			presets.store
		};
		views[\restore]=Button(c, boundsButton).states_([ ["r"] ]).action_{ presets.restore };
		views[\basename]=StaticText(c, boundsName).string_(presets.directory??{presets.basename}).font_(Font("Monaco", boundsName.y*0.45));
		views[\prev]=Button(c, boundsButton).states_([ ["<"] ]).action_{ presets.prev };
		views[\presets]=PopUpMenu(c, boundsName)
		.items_(if (presets.array.size>0, {presets.entriesFullPath},{["(empty)"]}))
		.action_{|p|
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
				).stringColor_(if (exists, {Color.black},{Color.red}))}.defer;
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
	}
}