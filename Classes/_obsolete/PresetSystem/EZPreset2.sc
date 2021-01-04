/*
- morphCurve
- die nummercode verbergen in de listview?
- hoe om te gaan met on-morphable views bij interp???
- hoe om te gaan met on-morphable views bij interpolation presets (begin, half, einde)? nu is het altijd in het begin van de morph
- alleen bij verandering in \interpolate actie uitvoeren?
- alles converten naar een array ipv Event() ??? is dat efficienter???
*/
EZPreset2 {
	var <parent, <bounds, <views, <path, <path2;
	var <routines, <controlSpecs, <functions;
	var <states, <>time, <>resolution, <steps, <waitTime, <stepSize, <currentPathName, <pathName;
	var <>guis, writeFlag;
	var <currentPresetName, <presetNames, <presetNamesListView, <controlSpec;
	var <>currentPresetPath, <presetPaths, <currentPresetNumber;
	var <preset, <presets, <>timeKey, <>tmpTime, <>maxTime, master, <>interpolate, <folderName, <array, <means, <actions, <arrayI, <interpolateKeys, <stateKeys;
	var <interp_interp, <interp_depth;
	//	var <letter, <number, <description;
	var <slaves, <masters, isSlave, numberOfMasters, numberOfSlaves, type, <isCopying;
	var <presetMorph;
	var functionIsChanged;

	*new {arg parent, bounds, views, path, states, type=\master, guiType=\master, folderName="master", controlSpec=ControlSpec(0.0, 120.0, 6.0);
		^super.new.init(parent, bounds, views, path, states, type, guiType, folderName, controlSpec)
	}

	init {arg argparent, argbounds, argviews, argpath, argstates, argType, argguiType, argFolderName, argcontrolSpec;
		masters=();
		slaves=();
		interp_interp=0.0; interp_depth=1.0;
		parent=argparent ?? {var win=Window.new.front; win.addFlowLayout; win};
		bounds=argbounds ?? {350@20};
		folderName=argFolderName;
		type=argType;
		path=argpath ?? {"~/presets/".standardizePath};
		controlSpec=argcontrolSpec;
		isCopying=false;
		functionIsChanged=false;

		if (PathName(path).isFile, {path=path.dirname++"/presets/"});

		//--------------------------------- initialize
		if (File.exists(path).not, {
			File.mkdir(path);
		});
		path2=path++folderName++"/";
		if (File.exists(path2).not, {
			File.mkdir(path2);
		});
		currentPresetNumber=0;
		writeFlag=false;
		currentPresetPath=PathName(path2).entries[0]??{
			var file, fileName="A_000.scd";
			file=File(path2++fileName, "w");
			file.write(preset.asCompileString);
			file.close;
			writeFlag=true;
			(path2++fileName).asPathName
		};
		currentPresetName=currentPresetPath.fileNameWithoutExtension;

		presetNames=PathName(path2).entries.collect{|path|
			path.fileNameWithoutExtension
		};
		//--------------------------------- en verder
		views=argviews;

		//haal 'foute' views eruit, check of het echt GUI objecten zijn die een 'action' en een 'value' kunnen doen.

		states=argstates??{()};
		time=0.0;
		resolution=10;
		steps=time*resolution;
		stepSize=steps.reciprocal;
		waitTime=resolution.reciprocal;
		preset=();
		timeKey=\morphTimeJT;
		isSlave=false;
		interpolate=true;
		currentPresetNumber=0;
		//if (isSlave.not, {masters=(); slaves=()});

		this.getControlSpecs;
		this.getValues;
		this.getActions;
		this.initFunctions;

		if (writeFlag, {
			var file=File(currentPresetPath.fullPath, "w");
			preset[timeKey]=time;
			file.write(preset.asCompileString);
			file.close;
		});
		if (argguiType!=nil, {
			{this.makeGUI(argguiType)}.defer;
		});

		this.forceRestore;

		//this.calcArray(type==\master);
		if (type==\master, {
			numberOfMasters=0;
			numberOfSlaves=0;
		});

	}

	prev {}
	next {}
	/*
	calcArray {arg flag=true, steps=10.0;
	array=();
	means=();
	controlSpecs.keysValuesDo{|key,cs|
	array[key]=[];
	};
	PathName(path2).entries.collect{|p|
	var x, value;
	x=p.fullPath.load;
	controlSpecs.keysValuesDo{|key,cs|
	var value;
	value=x[key]??{cs.minval};
	value=cs.unmap(value);
	array[key]=array[key].add(value);
	};
	};
	controlSpecs.keysValuesDo{|key,cs|
	means[key]=array[key].mean
	};

	//arrayI=array.deepCopy.collect{|ar| ar.resamp1(ar.size*steps)};//testing

	if (flag, {
	guis[\interp].controlSpec=ControlSpec(0.0, PathName(path2).entries.size);
	masters.do{|ez| ez.calcArray(false)};
	slaves.do{|ez| ez.calcArray(false)};
	})
	}
	*/
	addToMaster {arg views, folderName, states;
		if (type==\master, {numberOfMasters=numberOfMasters+1});
		if (folderName==nil, {folderName="master"++numberOfMasters});
		masters[folderName.asSymbol]=EZPreset2.new(parent, bounds, views, path, states, \addmaster, nil, folderName);

	}

	addSlave {arg parent, bounds, views, folderName, states, controlspec;
		if (controlspec==nil, {controlspec=controlSpec});
		if (type==\master, {numberOfSlaves=numberOfSlaves+1});
		if (folderName==nil, {folderName="slave"++numberOfSlaves});
		slaves[folderName.asSymbol]=EZPreset2.new(parent, bounds, views, path, states, \slave, \slave, folderName, controlspec);
	}

	updatePresetList {
		presetNames=PathName(path2).entries.collect{|path|
			path.fileNameWithoutExtension
		};

		presetNamesListView=presetNames.collect{|name|
			var tmpName=name.split($_);
			if (tmpName.size>1, {tmpName.removeAt(1)});
			tmpName.join;
		};
	}

	path_{arg pathh, flag=true;
		path=pathh;
		path2=path++folderName++"/";
		if (File.exists(path).not, {
			File.mkdir(path);
		});
		if (File.exists(path2).not, {
			File.mkdir(path2);
		});
		if (flag, {
			masters.do{|preset|
				preset.path_(pathh, false)
			};
			slaves.do{|preset|
				preset.path_(pathh, false)
			};
			this.updatePresetList;
			guis[\presetList].items_(presetNames);
		});
		currentPresetNumber=0;
		//writeFlag=false;
		currentPresetPath=PathName(path2).entries[0]??{
			var file, fileName="A_000.scd";
			file=File((path2++fileName), "w");
			this.getValues;
			file.write(preset.asCompileString);
			file.close;
			(path2++fileName).asPathName
		};
		if (flag, {
			this.changePreset(currentPresetPath.fullPath);
		})
	}


	updatePreset {

	}

	changePreset {arg pathh, timeFlag=true, guiFlag=true;
		var split, tail, ttime;
		currentPresetPath=pathh.asPathName;
		currentPresetName=currentPresetPath.fileNameWithoutExtension;
		split=currentPresetName.split($_);
		tail=split.copyToEnd(2).join;
		if (guiFlag, {
			guis[\name].string_(tail);//if master
		});
		masters.sortedKeysValuesDo{|key, ezpreset|
			var file, mPath=ezpreset.path2++currentPresetName++".scd";
			if (File.exists(mPath).not || isCopying, {
				ezpreset.currentPresetPath=mPath.asPathName;
				ezpreset.store(false);
			});
			ezpreset.changePreset(mPath, false, false);
		};
		slaves.sortedKeysValuesDo{|key, ezpreset|
			var file, mPath=ezpreset.path2++currentPresetName++".scd";
			if (ezpreset.isCopying, {
				ezpreset.currentPresetPath=mPath.asPathName;
				ezpreset.store(false);
			});
			ezpreset.changePreset(mPath, true, false);
			ezpreset.guis[\presetName].string_(currentPresetName);
			ezpreset.guis[\presetName].stringColor_(
				if (File.exists(ezpreset.currentPresetPath.fullPath), {Color.black}
					,{Color.grey})
			);
			//});
		};
		if (isCopying, {
			isCopying=false;
			guis[\copy].value_(0);

		});
		if (File.exists(currentPresetPath.fullPath), {
			if (interpolate, {
				this.restore(timeFlag);
			},{
				this.forceRestore;
			});
		});
	}

	getValues {
		if (views!=nil, {
			views.keysValuesDo{|key, gui|
				var value, flag=false;
				value=gui.value;
				if (value==nil, {
					flag=true;
					if (controlSpecs[key]!=nil, {
						value=controlSpecs[key].minval
					},{
						value=0
					})
				},{
					if (value.size>0, {
						value=value.collect{|val|
							if (val==nil, {
								flag=true;
								if (controlSpecs[key]!=nil, {
									controlSpecs[key].minval
								},{
									0
								})
							},{
								val
							})
						}
					});
				});
				if (flag, {gui.value_(value)});
				preset[key]=value;
			};
		});
	}

	store {arg flag=true;
		var file;
		file=File(currentPresetPath.fullPath, "w");
		this.getValues;
		if (flag, {
			preset[timeKey]=time;
		});
		file.write(preset.asCompileString);
		currentPresetPath.fullPath;
		file.close;
		//this.calcArray(type==\master);
		masters.sortedKeysValuesDo{|key, ezpreset|
			ezpreset.store(false)
		};
		if (type==\slave, {
			guis[\presetName].stringColor_(Color.black);
		});
		if (presetMorph!=nil, {
			presetMorph.calculate
		})
	}

	restore {arg showTime=true;
		var file;
		file=File(currentPresetPath.fullPath, "r");
		preset=file.readAllString.interpret;
		file.close;
		if (showTime, {
			time=preset[timeKey]??{time};
		},{
			tmpTime=preset[timeKey]??{time};
		});
		this.setTime(showTime);
		preset.removeAt(timeKey);
		functions[\fork]=();

		preset.keysValuesDo{|key, value|
			if (views[key]!=nil, {
				this.makeFunctions(key, value);
			})
		};
		masters.sortedKeysValuesDo{|key, ezpreset|
			ezpreset.time=time;
			ezpreset.tmpTime=time;
			ezpreset.restore(false);
		};
		this.interpolateFork;//this is the interpolation!
	}

	interpolateFork {
		routines[\main].stop;
		routines[\main]={
			(steps+1).do{|i|
				functions[\fork].do{|func| func.value(i)};
				waitTime.wait;
			}
		}.fork
	}

	forceRestore {
		time=0;
		this.restore(false);
		time=tmpTime;
		this.setTime(type==\master);
	}

	setTime {arg showTime=true;
		steps=time*resolution;
		stepSize=steps.reciprocal;
		if (showTime, {	guis[\time].value_(time)});
	}

	deletePreset {arg flag=true;
		var dPath=path2++currentPresetName++".scd";
		if (File.exists(dPath), {
			File.delete(dPath);
		});

		if (type==\slave, {
			guis[\presetName].stringColor_(Color.grey);
		});

		if (flag, {
			masters.do{|ezpreset| ezpreset.deletePreset(false);};
			slaves.do{|ezpreset| ezpreset.deletePreset(false) };
			this.updatePresetList;
			guis[\presetList].items_(presetNames);
			guis[\presetList].valueAction_(currentPresetNumber);
		});
		//this.calcArray(type==\master);
	} //or remove?

	renamePreset {arg old, new;
		var out;
		{
			out=("mv "++path2.asUnixPath++old++".scd " ++ path2.asUnixPath++new++".scd").unixCmdGetStdOut;
			while({out==nil},{0.01.wait});
			this.updatePresetList;
			guis[\presetList].items_(presetNames);
			//currentPresetNumber=x+1;
			guis[\presetList].value_(currentPresetNumber);
			currentPresetName=new;
			slaves.sortedKeysValuesDo{|key, ezpreset|

				if (File.exists(ezpreset.path2++old++".scd"), {
					out=("mv "++ezpreset.path2.asUnixPath++old++".scd "
						++ ezpreset.path2.asUnixPath++new++".scd").unixCmdGetStdOut;
				});
				{
					ezpreset.guis[\presetName].string_(currentPresetName);
				}.defer
			};
		}.fork(AppClock);
		//mv old-file-name new-file-name
		//masters.do
		//slaves.do
	}

	tailName {arg fileName;
		var tail=fileName.find($_);
		fileName=if (tail!=nil, {
			fileName.copyToEnd(tail);
		},{
			""
		});
		^fileName
	}

	number {arg fileName;
		^fileName.copyToEnd(1).split($_)[0];
	}

	/*
	interp {arg index, depth=1.0, type;
	controlSpecs.keysValuesDo{|key, cs|
	var value=cs.map(
	[means[key], array[key].blendAt(index, 'wrapAt')].blendAt(depth));
	actions[key].value(value);
	{views[key].value_(value)}.defer;
	};
	masters.do{|ez| ez.interp(index,depth,type)};
	slaves.do{|ez| ez.interp(index,depth,type)};//moet dit erin?
	}

	interpNoGUI {arg index, depth=1.0, type;
	controlSpecs.keysValuesDo{|key, cs|
	var value=cs.map(
	[means[key], array[key].blendAt(index, 'wrapAt')].blendAt(depth)
	//array[key].blendAt(index, 'wrapAt')
	);
	actions[key].value(value);
	//{views[key].value_(value)}.defer;
	};
	masters.do{|ez| ez.interpNoGUI(index,depth,type)};
	slaves.do{|ez| ez.interpNoGUI(index,depth,type)};//moet dit erin?
	}
	*/

	add {
		var fileName, split;
		var currentChar, currentNumber="000";
		split=presetNames.deepCopy.last.split($_);

		currentChar=split[0];
		currentChar=(currentChar.ascii+1).asAscii;
		//onderstaande is ook een functie (komt ook voor bij addAfter)
		//this.forceRestore;//dit hoeft niet toch?
		currentPresetPath=PathName(path2++currentChar++"_"++currentNumber++".scd");
		this.store;
		this.updatePresetList;
		guis[\presetList].items_(presetNames);
		currentPresetNumber=currentPresetNumber+1;
		guis[\presetList].valueAction_(currentPresetNumber);
	}

	addAfter {arg x;
		var currentChar, nextChar, currentNumber, nextNumber, newNumber, tailName, split;
		split=presetNames[x.asInteger].split($_);
		currentChar=split[0];
		currentNumber=split[1];

		if (x<(presetNames.size-1), {
			split=presetNames[x.asInteger+1].split($_);
			nextChar=split[0];
			nextNumber=split[1];
			if (currentChar==nextChar, {
				currentNumber=((nextNumber.interpret-currentNumber.interpret)/2
					+
					currentNumber.interpret).round(1.0).asInteger.asDigits(10,3).join;
			},{
				currentNumber=((1000-currentNumber.interpret)/2
					+
					currentNumber.interpret).round(1.0).asInteger.asDigits(10,3).join;
			})
		},{
			currentNumber=((1000-currentNumber.interpret)/2
				+
				currentNumber.interpret).round(1.0).asInteger.asDigits(10,3).join;

		});
		//this.forceRestore;//dit hoeft niet toch?
		currentPresetPath=PathName(path2++currentChar++"_"++currentNumber++".scd");
		this.store;
		this.updatePresetList;
		guis[\presetList].items_(presetNames);
		currentPresetNumber=x+1;
		guis[\presetList].valueAction_(currentPresetNumber);
	}

	addBefore {arg x;
		if (x>0, {
			this.addAfter(x-1)
		},{
			"error".postln;
		});
	}

	checkPath {}
	/*
	moveToPreset {arg presetName, times=10.0;
	//functionIsChanged=true;
	}
	*/
	makeFunctions {arg key, value;
		var gui=views[key];
		var flag=stateKeys.includes(key);
		if (flag.not, {
			flag=((gui.value-value).asArray.sum.abs<0.000001) || (steps==0);
		});

		if (flag==true, {//no interpolation
			{gui.value_(value)}.defer;
			gui.action.value(value);
		},{
			var start, end, rico, out, gui=views[key], cs=controlSpecs[key];
			start=cs.unmap(gui.value);
			end=cs.unmap(value);
			rico=(end-start)*stepSize;

			functions[\fork][key]={arg i;
				out=cs.map((rico*i+start));
				{gui.value_(out)}.defer;
				gui.action.value(out);
			}
		});
	}

	initFunctions {
		routines=(main: {}.fork);
		functions=(fork:(), state:());
		interpolateKeys=[];
		stateKeys=[];
		if (views!=nil, {
			views.keysValuesDo{|key, gui|
				var routine;
				var cs;
				//var action=gui.action;
				if (states[key]==\interpolate, {
					interpolateKeys=interpolateKeys.add(key);
				},{
					stateKeys=stateKeys.add(key);
					functions[\state][key]={arg value;
						{gui.value_(value)}.defer;
						gui.action.value(value);
					}
				});
			};
		});
	}

	getActions {
		actions=();
		if (views!=nil, {
			views.keysValuesDo{|key, gui|
				if (gui.action!=nil, {
					actions[key]=gui.action
				});
			};
		})
	}

	getControlSpecs{
		controlSpecs=();
		if (views!=nil, {
			views.keysValuesDo{|key, gui|
				//states[key]=\state;
				if (gui.isKindOf(EZGui), {
					if ((states[key]==nil) || (states[key]==\interpolate), {
						controlSpecs[key]=gui.controlSpec;
						states[key]=\interpolate;
					})
				},{
					if (states[key]==\interpolate, {
						if (gui.class==Button, {
							controlSpecs[key]=ControlSpec(0, gui.states.size, 0, 1);
						},{
							if ([ListView, PopUpMenu, EZListView].includesEqual(gui.class), {
								controlSpecs[key]=ControlSpec(0, gui.items.size, 0, 1);
							},{


							})
						})
					},{
						states[key]=\state;
					})
				});

			};
		})
	}

	nextP {
		if (currentPresetNumber<(presetNames.size-1), {
			currentPresetNumber=currentPresetNumber+1;
			this.changePreset(path2++presetNames[currentPresetNumber]++".scd");
			guis[\presetList].value_(currentPresetNumber)
			//this.changePreset(path2++list.items[list.value]++".scd");
		},{

		})
	}

	prevP {
		if (currentPresetNumber>0, {
			currentPresetNumber=currentPresetNumber-1;
			this.changePreset(path2++presetNames[currentPresetNumber]++".scd");
			guis[\presetList].value_(currentPresetNumber)
			//this.changePreset(path2++list.items[list.value]++".scd");
		},{

	})	}

	addPresetMorph {arg argindices, argparent, argbounds, argmethod='wrapAt', argviews;
		presetMorph=PresetMorph(this, argindices??{[0]}, argparent??{parent}
			, argbounds??{350@20}, argmethod, argviews)
		//	, [\a]
		^presetMorph
	}

	makeGUI {arg guiType=\master, boundsX=350, boundsY=20;
		var width=boundsX-8, height=boundsY, fontSize=height*0.6;
		var u;
		u=CompositeView(parent, boundsX@((boundsY*2)+(4)+8+((guiType==\master).binaryValue*(boundsY+(4*2)+100))));
		u.addFlowLayout;
		u.background_(Color.white);
		//slave: store, restore, force, presetname (StaticText), delete, time
		guis=();
		guis[\store]=Button(u, height@height).states_([ [\s] ]).action_{
			this.store;
		}.canFocus_(false);
		guis[\restore]=Button(u, height@height).states_([ [\r] ]).action_{
			this.restore;
		}.canFocus_(false);
		guis[\force]=Button(u, height@height).states_([ [\f] ])
		.value_(interpolate.binaryValue).action_{|b|
			this.forceRestore;
		}.canFocus_(false);
		guis[\delete]=Button(u, height@height).states_([ ["-"] ])
		.action_{
			this.deletePreset(type==\master)
		}.canFocus_(false);
		guis[\copy]=Button(u, height@height).states_([ ["c"],["c", Color.black, Color.yellow] ])
		.action_{|b|
			isCopying=b.value.asBoolean
		}.canFocus_(false);
		guis[\interpolate]=Button(u, height@height)
		.states_([ [\i],[\i, Color.black, Color.green] ])
		.value_(interpolate.binaryValue).action_{|b|
			interpolate=(b.value==1);
			slaves.do{|ezpreset|
				ezpreset.guis[\interpolate].valueAction_(b.value)
			};
			//	if (b.value==0, {
			//		this.forceRestore;
			//	})
		}.canFocus_(false);
		if (guiType==\master, {
			guis[\add]=Button(u, height@height).states_([ ["+"] ]).action_{
				this.add
			}.canFocus_(false);
			guis[\addBefore]=Button(u, height@height).states_([ ["<"] ])
			.action_{
				this.addBefore(currentPresetNumber)
			}.canFocus_(false);
			guis[\addAfter]=Button(u, height@height).states_([ [">"] ])
			.action_{
				this.addAfter(currentPresetNumber)
			}.canFocus_(false);

			guis[\prev]=Button(u, (2*height)@height).states_([ ["prev"] ])
			.action_{
				this.prevP
			}.canFocus_(false);

			guis[\next]=Button(u, (2*height)@height).states_([ ["next"] ])
			.action_{
				this.nextP
			}.canFocus_(false);

			guis[\presetList]=ListView(u, width@100).items_(presetNames)
			.action_{|list|
				currentPresetNumber=list.value;
				this.changePreset(path2++list.items[list.value]++".scd");
				//this.restore
			}.canFocus_(false);
			guis[\name]=TextField(u, width@height).string_("").action_{|b|
				var prev, new;
				prev=currentPresetName;
				new=prev.split($_);
				new=new[0]++"_"++new[1];
				//new=currentPresetName.split($_)[0];//++"_"++b.string);
				if (b.string.size>0, {new=new++"_"++b.string});
				this.renamePreset(prev, new);
			}.canFocus_(false);
			/*
			//if (iets_als_canInterpolate_ofzo, {
			guis[\interp]=EZSlider(parent, 350@20, \interp, ControlSpec(0.0, 1.0)
			, {|ez|
			interp_interp=ez.value;
			this.interp(ez.value, interp_depth);
			{guis[\presetList].value_(ez.value)}.defer;
			}, interp_interp);
			guis[\depth]=EZSlider(parent, 350@20, \depth, ControlSpec(0.0, 1.0)
			, {|ez|
			interp_depth=ez.value;
			this.interp(interp_interp, ez.value)
			}, interp_depth);
			//});
			*/
		});
		if (guiType==\slave, {
			guis[\presetName]=StaticText(u, (width-(6*height+(6*4)+8))@height).string_(currentPresetName);
		});
		guis[\time]=EZSlider(u, width@height, \time, controlSpec, {|ez|
			time=ez.value
		});

		parent.onClose=parent.onClose.addFunc{
			routines.do{|r| r.stop};
		};
	}
}
/*
path/Master/
path/FX1/
path/FX2/
*/