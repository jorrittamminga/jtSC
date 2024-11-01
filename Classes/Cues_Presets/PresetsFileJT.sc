PresetsFileJT : Numbered {
	classvar <allMethods;
	classvar <>extension = "scd";

	var <array, <value;//related to entries (paths of all the presets) and basename (filename without number and extension)
	var <actionArray;//array with all the actions to be taken when new preset is selected
	var <object, <>action, <>getAction, <>entriesAction;//action=setAction
	var <objectDefault, <presetsJT;
	var <keys, <>enviroment, <defaultMethod;
	var <fileNames, <fileNamesWithoutNumbers;//paths, pathsWithoutNumbers
	var <methodsArray, <>removeKeyWhenSave;
	var <cueJT;
	var history, <controlSpecs;
	var <>folderID;
	//--------------------------------------------------------------------------------- SELECT
	var <deselectedKeys, <selectedKeys, <>colors, selectActionArray, selectColor, <>selectMouseButton=1
	, <>selectModifierKey=262144;
	//var <blender, <neuralnet;

	*initClass {
		allMethods=(
			Event: [\doActions, \valuesActionsTransition]
			, PresetsJT: [\restore, \valuesActionsTransition]
		)
	}
	*basicNew {arg object, pathName;
		^super.new.init(object, pathName)
	}
	//--------------------------------------------------------------------------------- INITS
	init {arg argobject, argPath;
		array=[];
		entries=[];
		entriesFullPath=[];
		fileNames=[];
		fileNamesWithoutNumbers=[];
		keys=[];
		funcs=(update:nil, basename:nil, index:nil, delete:nil, directory: nil, add: nil, store: nil, restore: nil
			, deselectedKeys: nil
		);
		removeKeyWhenSave=[\routinesJT];
		enviroment=();
		actionArray=[];//is nieuw, is dit ok????
		selectActionArray=();
		selectColor=Color();
		//----------------------------------------------------------------------------- OBJECT inits
		object=argobject;
		pathName=argPath;
		this.initSelect;
		this.initColors;
		this.initObject;//analyze object
		//----------------------------------------------------------------------------- ACTIONS inits
		this.initGetAction;//get values from object
		this.initSetAction;//set values to object
		this.initEntriesAction;
		//----------------------------------------------------------------------------- FILE SYSTEM inits
		this.initPathName;//analyze the content/class of the pathname
		//----------------------------------------------------------------------------- POST INITS inits
		if (array.size>0, {this.restore(0)});
	}
	initColors {
		colors=(
			select: (
				stringBackground: nil
				, stringColor: selectColor
				, sliderBackground:Color(0.16470588235294, 0.16470588235294, 0.16470588235294)
				, numBackground: Color(1.0, 1.0, 1.0)
				, numStringColor:selectColor, numNormalColor:selectColor, numTypingColor:Color(1.0), knobColor:selectColor
				, background: nil
				, knobColors: [
					Color(0.91764705882353, 0.91764705882353, 0.91764705882353), selectColor
					, Color(0.85882352941176, 0.85882352941176, 0.85882352941176), selectColor ]

			),
			deselect: (
				stringBackground: nil
				, stringColor: Color.grey
				, sliderBackground: Color.grey//Color(0.16470588235294, 0.16470588235294, 0.16470588235294)
				, numBackground:Color(1.0, 1.0, 1.0)
				, numStringColor:Color.grey, numNormalColor:Color.grey, numTypingColor:Color(1.0), knobColor:Color.grey
				, background: nil
				, knobColors: [ Color(0.91764705882353, 0.91764705882353, 0.91764705882353), Color.grey
					, Color(0.85882352941176, 0.85882352941176, 0.85882352941176), Color.grey ]
			)
		);
	}
	initObject {
		controlSpecs=();
		switch(object.class, PresetsJT, {
			presetsJT=object;
			//two funcs here under could be a func or so, too much space....
			presetsJT.funcs[\basename]=presetsJT.funcs[\basename].addFunc({arg old,new;
				array.do{|preset,i|
					var presetString=preset.asCompileString, file;
					if (preset[\basename].asString==(old.asString), {
						preset[\basename]=new;
						array[i]=preset;
						actionArray[i]=action.value(preset);
						file=File(entries[i].fullPath, "w");
						file.write(preset.asCompileString);
						file.close
					});
				};
			});
			presetsJT.funcs[\delete]=presetsJT.funcs[\delete].addFunc({arg name;
				var flag=false;
				array.do{|preset,i|
					var file;
					if (preset[\basename]==name, {//PresetsJT
						NumberedFile.delete(entries[i]);
						flag=true;
					},{//cueJT
						preset=preset.deepRemove(name);
						array[i]=preset;
						actionArray[i]=action.value(preset);
					});
				};
				if (flag, {
					this.update
				});
			});
		}, Event, {
			value=();
			selectedKeys=object.keys.deepCopy.asArray;
			object.sortedKeysValuesDo{|key,obj|
				var cs, func;//=ControlSpec(0.0, 1.0);
				value[key]=obj.value;
				cs=obj.controlSpec;
				if (cs!=nil, {
					if (cs.step<0.01, {cs=cs.warp});
					controlSpecs[key]=cs;
				});
				//---------------------------------------------------------------------- DESELECT related
				func={|view, x, y, modifiers, buttonNumber, clickCount|
					if (buttonNumber==selectMouseButton, {
						if (modifiers>=262144, {modifiers=modifiers-262144});
						if (modifiers==selectModifierKey, {
							this.toggleKey(key);
						})
					})
				};
				if (obj.mouseDownAction==nil, {
					obj.mouseDownAction_(func)
				},{
					if (obj.mouseDownAction.class==Array, {
						obj.mouseDownAction.do{|obj|
							if ((obj.mouseDownAction.class==Function) || (obj.mouseDownAction.class==FunctionList), {
								obj.mouseDownAction=obj.mouseDownAction.addFunc(func);
							},{
								obj.mouseDownAction_(func)
							})
						};
					},{
						obj.mouseDownAction=obj.mouseDownAction.addFunc(func);
					})
				});

				//---------------------------------------------------------------------- DESELECT related
			}
		});
	}
	initGetAction {
		getAction=switch(object.class, Event, {
			{
				object.collect(_.value)
			};//default getAction
		}, PresetsJT, {
			object=();
			{ //arg presetsJT;
				var val=object.collect(_.value);
				val[\basename]=presetsJT.basename;
				val
			}
			//{ (basename: object.basename) }
		}, {
			{ object.collect(_.value) };
		});
	}
	//initSetAction is echt heel lelijk en groot...
	initSetAction {
		var actionFunc;
		actionFunc=if (presetsJT.class==PresetsJT, {
			{arg val;
				var valObject=(), valPreset=(), index;//oid
				if (val[\basename]!=nil, {
					index=presetsJT.keys.indexOf(val[\basename]);
					if (index!=nil, {
						valPreset=presetsJT.array[index];
						valPreset=presetsJT.object.removeAllWithoutActions(valPreset);
					})
				});
				valObject=object.removeAllWithoutActions(val);
				//defaults=(extras: 0, durations: 0, method:0);//dit zijn die extra dingen
				//--------------------------------------------------------------------------------
				{
					valObject.keysValuesDo{|key,val|
						object[key].action.value(val);
						{object[key].value_(val)}.defer;
					};
					valPreset.keysValuesDo{|key,val|
						presetsJT.object[key].action.value(val);
						{presetsJT.object[key].value_(val)}.defer;
					}
				};
				//--------------------------------------------------------------------------------
			}
		},{
			if (object.class==Event, {
				{arg val;
					var valObject=(), deselectAction;
					valObject=object.removeAllWithoutActions(val);
					//--------------------------------------------------------------------------------
					{
						valObject.keysValuesDo{|key,val|
							object[key].action.value(val);
							{object[key].value_(val)}.defer;
						}
					};
					//--------------------------------------------------------------------------------
				};
			});
		},{

		});
		action=action.addFunc(actionFunc)
	}
	initEntriesAction {
		entriesAction={arg paths;
			var entries=paths??{directory.entries};
			entries
		};
	}
	initPathName {
		basename="empty";
		if (object.class==PresetsJT, {
			basename=pathName.asSymbol;//??{object.directory.asPathName.folderName};
			pathName=object.directory.asPathName;//eh, klopt dit????
		});
		if (pathName.class==String, {
			if (pathName.contains($/), {
				pathName=pathName.asPathName;
			},{
				basename=pathName;//==cuesJT
			})
		},{
			basename=pathName;//==CuesJT!
		});
		if (pathName.class==PathName, {
			directory=if (pathName.isFolder, {pathName}, {pathName.pathOnly.asPathName});
			if (File.exists(directory.fullPath).not, {File.mkdir(directory.fullPath)});
			this.update;
		});
	}
	//----------------------------------------------------------------------------- OBJECT related
	valueAction_ {arg val;
		value=val;
		this.action.value(this.value);
	}
	getValue {
		value=this.getAction.value(this);
		^value
	}
	//----------------------------------------------------------------------------- ACCESSING ARRAY/presets
	index_ {arg i;
		var tmp=i.copy;
		if (i.notNil, {
			if ((i.class==Symbol) || (i.class==String), {
				i=keys.indexOfEqual(i)??{9999999999};
			});
			if(i<entries.size, {
				index=i;
				basename=keys[index]??{basename};
				funcs[\index].value(index, this);
			})
		});
	}
	at {arg i;
		this.index_(i);
		^array[index]
	}
	restore {arg i;//is at and valueAction
		this.index_(i);
		this.prRestore;
	}
	restoreAtIndex {arg i;
		index=i;
		basename=keys[index];
		funcs[\index].value(index, this);
		this.prRestore;
	}
	prRestore {
		var preset;
		preset=array[index];
		if (actionArray!=nil, {
			actionArray[index].value;
		});
		value=preset;//was not here before, is this usefull?
		//---------------------------------------------------------------------- (DE)SELECT related
		selectActionArray[index].value;
		//----------------------------------------------------------------------
		funcs[\restore].value(index);
		^preset
	}
	put {arg i, val;
		//this.index_(i);
		i=i??{index.copy};
		val=val??{value.deepCopy};
		array[i]=val;
		actionArray[i]=action.value(val);
		this.prSave(i, val);
	}
	store {arg i, funcStoreFlag=true;
		var valueWithoutDeselectedKeys;
		this.index_(i);
		this.getValue;
		//value=value++extra;
		//---------------------------------------------------------------------- (DE)SELECT related
		valueWithoutDeselectedKeys=value.deepCopy;
		if (array.size>0, {
			if (array[index][\deselectedKeysJT]==nil, {
				array[index][\deselectedKeysJT]=deselectedKeys
			},{
				deselectedKeys=array[index][\deselectedKeysJT]
			});
		});
		deselectedKeys.do{|key| valueWithoutDeselectedKeys.removeAt(key)};
		if (deselectedKeys!=nil, {value[\deselectedKeysJT]=deselectedKeys});
		//----------------------------------------------------------------------
		if (array.size==0, {
			this.add(basename, '\addToHead', directory);
		},{
			//array[index]={ };//makeFunction
			array[index]=value;
			actionArray[index]=action.value(value);
			this.prSave;
		});
		if (funcStoreFlag, {
			funcs[\store].value(index);
		});
	}
	//prStore {}
	prSave {arg i, saveValue;
		var file, val;
		val=saveValue??{value.deepCopy};
		removeKeyWhenSave.do{|key| val.removeAt(key)};
		file=File(entries[i??{index}].fullPath, "w");
		file.write(val.asCompileString);
		file.close
	}
	prev { if (index>0, {this.restoreAtIndex(index-1)}) }
	next { if (index<(array.size-1), {this.restoreAtIndex(index+1)}) }
	//----------------------------------------------------------------------------- NUMBERED FILESYSTEM related
	load {}
	update {arg paths;
		var deselectedKeys;
		//this.entries_(paths);
		entries=this.entriesAction.value(paths);
		entries=entries.collect{|path| path.asPathName};
		entriesFullPath=entries.collect(_.fullPath);
		entries.removeAllSuchThat({arg entry; entry.isFolder});
		fileNames=entries.collect{|entry| entry.fileNameWithoutExtension};
		fileNamesWithoutNumbers=fileNames.collect{|filename| filename.split($_).copyToEnd(1).join($_)};
		keys=fileNamesWithoutNumbers;
		selectActionArray=[];
		array=entries.collect{|entry|
			//entry.fullPath.load;
			var data;
			data=entry.fullPath.load;//.deepCopy?
			//-------------------------------------------------- DESELECT
			if (data[\deselectedKeysJT]==nil, {
				data[\deselectedKeysJT]=deselectedKeys
			},{
				deselectedKeys=data[\deselectedKeysJT]
			});
			deselectedKeys.do{|key| data.removeAt(key)};
			selectActionArray=selectActionArray.add(this.selectActionFunc(deselectedKeys));
			//--------------------------------------------------
			data
		};
		actionArray=array.collect{|val|
			action.value(val)
		};
		funcs[\update].value
	}
	add {arg filename, addAction=\addAfter, target;
		var file, entry, val;
		basename=filename??{basename};
		this.getValue(true);
		{
			val=value.deepCopy;
			removeKeyWhenSave.do{|key| val.removeAt(key)};
			file=NumberedFile( basename++"."++extension
				, target??directory//{entries[index]??{directory}}
				, addAction, numDigits, val);
			funcs[\add].value(switch(addAction, \addBefore, {index}, \addAfter, {index+1}, \addToTail, {array.size}
				, \addToHead, {0}, {index}), this);
			this.update;
			this.index_(entries.collect(_.fullPath).indexOfEqual(file.pathName.fullPath));
			//funcs[\add].value(index, this);
		}.fork(AppClock)//moet dit niet een SystemClock zijn???
	}
	delete {arg i, indexOffset=0, doRestore=true;
		this.index_(i);
		{
			NumberedFile.delete(entries[index]);
			funcs[\delete].value(keys[index]);
			this.update;
			this.index_((index+indexOffset).min(entries.size-1));
			if (doRestore, {this.restore});//misschien zonder actie?
		}.fork(AppClock)//moet dit niet een SystemClock zijn???
	}
	//----------------------------------------------------------------------------- deselectedKeys related
	initSelect {}
	selectColor_ {arg color;
		selectColor=color;
		[\select].do{|k|
			[\stringColor, \numStringColor, \numNormalColor, \knobColor, \sliderBackground].do{|l|
				colors[k][l]=color
			}
		};
		colors[\select][\knobColors]=[
			Color(0.91764705882353, 0.91764705882353, 0.91764705882353), color
			, Color(0.85882352941176, 0.85882352941176, 0.85882352941176), color ];
		this.update;
		selectActionArray[index].value;
	}
	selectActionFunc {arg keys;
		var tmpSelectedKeys=object.keys.deepCopy.asArray;
		keys.do{|key| tmpSelectedKeys.remove(key)};
		^{
			{
				tmpSelectedKeys.do{|key| this.setObjectColor(key, \select)};
				keys.do{|key| this.setObjectColor(key, \deselect)};
			}.defer;
		}
	}
	toggleKey {arg key;
		if (array[index]!=nil, {
			if (array[index][\deselectedKeysJT]==nil, {
				this.removeKey(key);
			},{
				if (array[index][\deselectedKeysJT].includes(key), {
					this.addKey(key);
				},{
					this.removeKey(key);
				})
			});
		},{
		})
	}
	setObjectColor {arg key, colorKey=\deselect;
		switch(object[key].class
			, EZSlider, {
				object[key].setColors(
					colors[colorKey][\stringBackground], colors[colorKey][\stringColor], colors[colorKey][\sliderBackground]
					, colors[colorKey][\numBackground], colors[colorKey][\numStringColor], colors[colorKey][\numNormalColor]
					, colors[colorKey][\numTypingColor], colors[colorKey][\knobColor], colors[colorKey][\background]
				);
			}, EZRanger, {
				object[key].setColors(
					colors[colorKey][\stringBackground], colors[colorKey][\stringColor], colors[colorKey][\sliderBackground]
					, colors[colorKey][\numBackground], colors[colorKey][\numStringColor], colors[colorKey][\numNormalColor]
					, colors[colorKey][\numTypingColor], colors[colorKey][\knobColor], colors[colorKey][\background]
				);
			}, EZMultiSlider, {
				object[key].setColors(
					colors[colorKey][\stringBackground]
					, colors[colorKey][\stringColor]
					, colors[colorKey][\sliderBackground]
					, colors[colorKey][\numBackground]
					, colors[colorKey][\numStringColor]
					, colors[colorKey][\numNormalColor]
					, colors[colorKey][\numTypingColor]
					, colors[colorKey][\knobColor]
					, colors[colorKey][\background]
				);
			}, EZNumber, {
				object[key].setColors(
					colors[colorKey][\stringBackground], colors[colorKey][\stringColor]
					, colors[colorKey][\numBackground], colors[colorKey][\numStringColor], colors[colorKey][\numNormalColor]
					, colors[colorKey][\numTypingColor], colors[colorKey][\background]
				);
			}, EZKnob, {
				object[key].setColors(
					colors[colorKey][\stringBackground], colors[colorKey][\stringColor]
					, colors[colorKey][\numBackground], colors[colorKey][\numStringColor]
					, colors[colorKey][\numNormalColor], colors[colorKey][\numTypingColor]
					, colors[colorKey][\knobColors], colors[colorKey][\background]
				);
			}, Button, {
				var states=object[key].states, value=object[key].value.deepCopy;
				states=states.collect{|state|
					[1].do{|i|
						if (state[i]==nil, {state=state++[Color.black]});
						if (colorKey==\deselect, {
							state[i]=Color.grey
						},{
							state[i]=Color.black
						})
					};
					state
				};
				object[key].states=states;
				object[key].value_(value)
		});
	}
	removeKey {arg key;
		key=key.asArray;
		if (array[index][\deselectedKeysJT]==nil, {array[index][\deselectedKeysJT]=[]});
		key.do{|key|
			array[index][\deselectedKeysJT]=array[index][\deselectedKeysJT].add(key);
			selectedKeys.remove(key);
			this.setObjectColor(key, \deselect)
		};
		this.store;
		this.update;//brute force....
	}
	addKey {arg key;
		key=key.asArray;
		if (array[index][\deselectedKeysJT]==nil, {array[index][\deselectedKeysJT]=[]});
		key.do{|key|
			array[index][\deselectedKeysJT].remove(key);
			selectedKeys.add(key);
			this.setObjectColor(key, \select)
		};
		this.store;
		this.update;//brute force....
	}
	//----------------------------------------------------------------------------- GUI	related
}