/*
- stores the settings of Views and other value/action objects to an array with the settings
preset (=value from File) interpretation is heel erg belangrijk. Dat is eigenlijk de (set)action.
en misschien nog goed nadenken over een storeAction, hoe de output van een getAction wordt opgeslagen in array en in een file? Of zit dit al in getAction? misschien wel...
er is een 'value' output van het object (output van getAction), een value output van de preset (array[index], file.load, etc)
*/
PresetsFileJT : Numbered {
	classvar <allMethods;
	classvar <>extension = "scd";

	var <array, <value;//related to entries (paths of all the presets) and basename (filename without number and extension)
	var <object, <>action, <>getAction, <>entriesAction;//action=setAction
	var <keys, <>enviroment, <defaultMethod;
	var <fileNames, <fileNamesWithoutNumbers;//paths, pathsWithoutNumbers
	var <methodsArray, <removeKeyWhenSave;
	var history;
	//var <setAction, <getAction;
	*initClass {
		allMethods=(
			Event: [\doActions, \valuesActionsTransition]
			, PresetJT: [\restore, \restoreI]
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
		funcs=(update:nil, basename:nil, index:nil, delete:nil, directory: nil, add: nil);
		removeKeyWhenSave=[\routinesJT];
		enviroment=();
		//----------------------------------------------------------------------------- OBJECT inits
		object=argobject;
		pathName=argPath;
		this.initObject;//analyze the content/class of the object
		//----------------------------------------------------------------------------- ACTIONS inits
		this.initGetAction;
		this.initSetAction;
		this.initEntriesAction;
		//----------------------------------------------------------------------------- FILE SYSTEM inits
		this.initPathName;//analyze the content/class of the pathname
		//----------------------------------------------------------------------------- POST INITS inits
		if (array.size>0, {
			this.restore(0)}
		)
	}
	initGetAction {
		getAction=switch(object.class, Event, {
			{
				object.collect(_.value) };//default getAction
		}, PresetJT, {
			{ object.basename }
		}, {
			{ object.collect(_.value) };
		});
	}
	initSetAction {
		action=action.addFunc({arg value;//default setAction
			var val, method, durations, performArray, class=value.class, defaults;
			switch(class, Event, {
				object.performMsg([defaultMethod, value]);
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
	initObject {
		methodsArray=allMethods[object.class.asSymbol];
		defaultMethod=methodsArray[0]??{\value};
		switch(object.class, PresetJT, {
			object.funcs[\basename]=object.funcs[\basename].addFunc({arg old,new;
				array.do{|preset,i|
					var presetString=preset.asCompileString, file;
					if (presetString.contains(old.asString), {
						presetString.replace(old.asString, new.asString);
						preset=presetString.interpret;
						//------------------------------------- dit zou een method kunnen zijn!
						//this.storeValueAt(preset, i)
						array[i]=preset;
						file=File(entries[i].fullPath, "w");
						file.write(presetString);
						file.close
					});
				};
			});
			object.funcs[\delete]=object.funcs[\delete].addFunc({arg name;
				var flag=false;
				array.do{|preset,i|
					var file;
					if (preset==name, {//PresetJT
						NumberedFile.delete(entries[i]);
						flag=true;
					},{//cueJT
						preset=preset.deepRemove(name);
						array[i]=preset;
					});
				};
				if (flag, {
					this.update
				});
			});
		});
		//CuesJT
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
		var preset;
		this.index_(i);
		this.valueAction_(preset=array[index]);
		^preset
	}
	//of is dit meer iets voor CuesJT? JA!
	restoreI {arg i, durations, curves, delayTimes, specs, actions, resolution=10, nrt=false;
		this.index_(i);
		object.valuesActionsTransition(array[index], durations, curves, delayTimes, specs, actions, resolution, nrt)
	}
	put {arg i, val;
		this.index_(i);
		array[index]=val??{value};
		this.prSave;
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
			this.prSave;
		});
	}
	//prStore {}
	prSave {
		var file, val;
		val=value.deepCopy;
		removeKeyWhenSave.do{|key| val.removeAt(key)};
		file=File(entries[index].fullPath, "w");
		file.write(val.asCompileString);
		file.close
	}
	prev { if (index>0, {this.restore(index-1)}) }
	next { if (index<(array.size-1), {this.restore(index+1)}) }
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
		funcs[\update].value
	}
	add {arg filename, addAction='\addAfter', target;
		var file, entry, val;
		basename=filename??{basename};
		this.getValue;
		{
			val=value.deepCopy;
			removeKeyWhenSave.do{|key| val.removeAt(key)};

			file=NumberedFile( basename++"."++extension
				, target??directory//{entries[index]??{directory}}
				, addAction, numDigits, val);
			funcs[\add].value(index, this);
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