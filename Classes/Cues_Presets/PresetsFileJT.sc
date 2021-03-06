PresetsFileJT : Numbered {
	classvar <allMethods;
	classvar <>extension = "scd";

	var <array, <value;//related to entries (paths of all the presets) and basename (filename without number and extension)
	var <actionArray;//array with all the actions to be taken when new preset is selected
	var <object, <>action, <>getAction, <>entriesAction;//action=setAction
	var <objectDefault, <presetsJT;
	var <keys, <>enviroment, <defaultMethod;
	var <fileNames, <fileNamesWithoutNumbers;//paths, pathsWithoutNumbers
	var <methodsArray, <removeKeyWhenSave;
	var <cueJT;
	var history, <controlSpecs;
	var <>folderID;
	var <blender, <neuralnet;

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
		funcs=(update:nil, basename:nil, index:nil, delete:nil, directory: nil, add: nil, store: nil, restore: nil);
		removeKeyWhenSave=[\routinesJT];
		enviroment=();
		//----------------------------------------------------------------------------- OBJECT inits
		object=argobject;
		pathName=argPath;
		this.initObject;//analyze object
		//----------------------------------------------------------------------------- ACTIONS inits
		this.initGetAction;//get values from object
		this.initSetAction;//set values to object
		this.initEntriesAction;
		//----------------------------------------------------------------------------- FILE SYSTEM inits
		this.initPathName;//analyze the content/class of the pathname
		//----------------------------------------------------------------------------- POST INITS inits
		if (array.size>0, {
			this.restore(0)}
		)
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
			object.sortedKeysValuesDo{|key,obj|
				var cs=ControlSpec(0.0, 1.0);
				cs=obj.controlSpec;
				if (cs.step<0.01, {cs=cs.warp});
				value[key]=obj.value;
				controlSpecs[key]=cs;
			}
		});
	}
	initGetAction {
		getAction=switch(object.class, Event, {
			{ object.collect(_.value) };//default getAction
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
				//hier--------------------------------------------------------------------------------
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
			}
		},{
			if (object.class==Event, {
				{arg val;
					var valObject=();
					valObject=object.removeAllWithoutActions(val);
					{
						valObject.keysValuesDo{|key,val|
							object[key].action.value(val);
							{object[key].value_(val)}.defer;
						}
					};
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
				basename=keys[index];
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
		var preset=array[index];
		actionArray[index].value;
		value=preset;//was not here before, is this usefull?
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
	store {arg i;
		this.index_(i);
		this.getValue;
		//value=value++extra;
		if (array.size==0, {
			this.add(basename, '\addToHead', directory);
		},{
			//array[index]={ };//makeFunction
			array[index]=value;
			actionArray[index]=action.value(value);
			this.prSave;
		});
		funcs[\store].value(index)
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
		//this.entries_(paths);
		entries=this.entriesAction.value(paths);
		entries=entries.collect{|path| path.asPathName};
		entriesFullPath=entries.collect(_.fullPath);
		entries.removeAllSuchThat({arg entry; entry.isFolder});
		fileNames=entries.collect{|entry| entry.fileNameWithoutExtension};
		fileNamesWithoutNumbers=fileNames.collect{|filename| filename.split($_).copyToEnd(1).join($_)};
		keys=fileNamesWithoutNumbers;
		array=entries.collect{|entry| entry.fullPath.load};
		actionArray=array.collect{|val| action.value(val)};
		funcs[\update].value
	}
	add {arg filename, addAction=\addAfter, target;
		var file, entry, val;
		basename=filename??{basename};
		this.getValue;
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
		}.fork(AppClock)
	}
	delete {arg i, indexOffset=0, doRestore=true;
		this.index_(i);
		{
			NumberedFile.delete(entries[index]);
			funcs[\delete].value(keys[index]);
			this.update;
			this.index_((index+indexOffset).min(entries.size-1));
			if (doRestore, {this.restore});//misschien zonder actie?
		}.fork(AppClock)
	}
	//----------------------------------------------------------------------------- GUI	related
}