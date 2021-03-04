CuesJT : PresetsFileJT {
	var <objectDefault;

	*new {arg object, cueName;
		^if (object.class==PresetsJT, {
			CuesPresetsJT(object, cueName)
		},{
			super.basicNew(object, cueName)
		})
	}
	//--------------------------------------------------------------------------------- INITS
	initPathName {
		basename=pathName;
	}
	initGetAction {
		{ arg presetsJT;
			var val=presetsJT.value??{()};
			val[\basename]=object.basename;
			val
		}
	}
	initSetAction {
		if (object.class==Event, {action={object[\routinesJT].do(_.stop)}});
		action=action.addFunc({arg value;//default setAction
			var val, method, durations, performArray, class=value.class, defaults, defaultsKeys, extras;
			switch(class, Event,  {
				defaults=(extras: 0, durations: 0, method:0);//dit zijn die extra dingen
				defaultsKeys=defaults.keys.copy;
				defaults.removeAt(\extras);
				if (value[\method]==nil, {
					object.performMsg([defaultMethod, defaults]);
					object.performMsg([defaultMethod, value]);
				},{
					//---------------------------------- DIT HIERONDER KAN ECHT EFFICIENTER
					val=value.deepCopy;
					defaultsKeys.do{arg key; defaults[key]=val[key]??{defaults[key]}};
					if (val[\event]==nil, {
						var keys=object.keys.sect(val.keys);
						keys=keys.difference(defaults.keys);
						val[\event]=();
						keys.do{|key|
							val[\event][key]=val[key];
							val.removeAt(key)
						}
					});
					if (val[\method].class!=Symbol, {
						val[\method]=methodsArray[val[\method]]??{defaultMethod} });
					if (val[\extras]!=nil, {
						if (val[\extras][\durations]!=nil, {
							val[\extras][\durations][\common]=(val[\durations]??{0});
						});
						val=val++val[\extras];
						val.removeAt(\extras);
					});
					performArray=class.findRespondingMethodFor(val[\method]).argNames.copyToEnd(1).collect{|key|
						val[key]
					};
					object.performMsg([defaultMethod, defaults]);
					if (defaults[\extras]==nil, {object[\extras].valueAction_(nil)});
					{
						val[\preAction].value(val, enviroment);
						object.performMsg([val[\method]]++performArray);
						val[\postAction].value(val, enviroment);
					}.fork(AppClock)
				})
			}, Array, {
				object.performMsg(value)
			}, Function, {
				value.value(this)
			}, FunctionList, {
				value.value(this)
			},
			{
				object.performMsg([defaultMethod, value]);
			})
		});

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
		switch( presets.object.class, Event, {
			presets.object[\method]=PopUpMenu(c, (bounds.x*0.1).floor@bounds.y).items_(
				presets.methodsArray
			).action_{|p|
				if (p.value==0, {
					{presets.object[\durations].value_(0)}.defer;
				});
				presets.methodsArray[p.value];
			};
			presets.object[\durations]=EZNumber(c, (bounds.x*0.1).floor@bounds.y, nil, ControlSpec(0.0, 60.0), {|ez|
				if (ez.value<=0.0, {
					{presets.object[\method].valueAction_(0)}.defer
				});
			}, 0.0, false, 0).round2_(0.01);
			presets.object[\extras]=TextField(c, (bounds.x*0.8).floor@bounds.y).string_("").action_{arg t;
			};
		}, PresetsJT, {//
			"presets/cuesJT ".post; presets.postln;
			views[\method]=PopUpMenu(c, (bounds.x*0.1).floor@bounds.y).items_(
				presets.methodsArray
			).action_{|p|
				presets.postln;
				presets.value[\method]=p.value;
				if (p.value==0, {
					{views[\durations].value_(0)}.defer;
				});
				presets.value.postln;
				//presets.value[\method]=presets.methodsArray[p.value];
			};
			views[\durations]=EZNumber(c, (bounds.x*0.1).floor@bounds.y, nil, ControlSpec(0.0, 60.0), {|ez|
				presets.value[\durations]=ez.value;
				if (ez.value<=0.0, {
					{views[\method].valueAction_(0)}.defer
				});
				presets.value.postln;
			}, 0.0, false, 0).round2_(0.01);
			views[\extras]=TextField(c, (bounds.x*0.8).floor@bounds.y).string_("").action_{arg t;
				presets.value;
			};
		}, {

		});
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