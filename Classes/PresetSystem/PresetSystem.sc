/*
- maak een dynamic variant (waarbij de views/controlspecs/etc kunnen veranderen per preset), is nu voornamelijk static (omdat bij de initialisatie alles al wordt vastgelegd)
- wat is het verschil (of het nut) van canInterpolate en interpolate?
- maak het mogelijk dat een preset een folder is! (b.v. voor PresetMorph of PresetNN)
-
- make UNDO function!!!! priority number one!
- maak een backup systeem, dat je makkelijk terug kan naar de vorige preset als je 'm per ongeluk overschrijft (zoals ik de hele tijd doe....)
-
- misschien entries van de slave ook vullen met nil, zoals de presets????
- update is wat lomp hoor, bij initialisatie wordt deze functie 2x uitgevoerd voor master
- check of er nieuwe gui objecten zijn toegevoegd die nog niet in de opgeslagen presets zitten
- PresetScript doet veel dingen dubbel, zoals index_ en getValues. fork(AppClock) is niet het meest efficient maar anders gaat het mis met view.value

- KDTree achtige dingen inbouwen, addKDTree b.v. PresetSysteem.kdtree.blabla(preset)

- zo clean mogelijk houden!
- doe veel {10000.do{}}.bench testen om te kijken welke procedure het snelst is
- ws zijn alle functions[iets] gui functions, ff checken en zo ja rename naar guiFunctions en in een {}.defer zetten.
- this.update is een beetje brute force....
- uitzoeken wat sneller is: preset=fullPath.load of file(); preset=blabla; file.close;
- preLoad / restore / restoreI / restoreS / etc ==>
- vooral restore/restoreI/restoreS moet zo snel mogelijk reageren
- maak een variable presetKeys, met de keys die door de preset worden aangepaast
- soms dubbelopperdepop met slaves.dingen / update / path / etc
- update {} is iets teveel brute force, in sommige gevallen
- ik denk bij store en storeI this.stopAll (voor de sicherheitsdienst)
- maak ook een EZPresetSlider(parent, bounds)
- disableSlaves, enableSlaves
*/
PresetSystem {
	var <path, <localpath, <folderName, <>fileNameWithoutExtension, <>fileName
	, <>fullPath, <fileNames, <fileNamesWithoutExtensions, <entries, <fileExists, <>size
	, <preLoad, <fileNamesWithoutNumbers;
	var <presets;
	var <views, <values, <defaultValues, <controlSpecs, <actions, <newValues;
	var <allActions, <allViews, <enabledViews, <disabledViews;
	var <index, <indices;
	var <type, <slaves, <allSlaves, <disabledSlaves, <enabledSlaves, <masters;
	var <>restoreAction, <>nextAction, <>prevAction, <restoreActionType;
	//interpolation variables
	var <>timeKey, <>curveKey, <>extraKey, <routines, <routineFunctions, <time, steps, stepSize, <>resolution, waitTime, <extra, <>interpolate, <canInterpolate
	, <interpolationCurve, <interpolationCurves;
	//script variables
	var <scriptpath, <scripts, <canScript, <script;
	//morph variables
	//gui variables
	var <guis, <>parent, <parents, <presetName, <windows, <>guiFlag, <hasGUI;
	var <>hasMorph;
	var <functions, <>sortedKeys, <>repairFlag;
	var <current;

	//for NeuralNet presets
	var <presetsNormalized, <factors, <controlSpecsList, <keys, <reshapeFlag;


	*new {arg views, path, folderName, type=\master, size, preLoad=true, fileName;
		^super.new.init(views, path, folderName, type, size, preLoad, fileName)
	}

	init {arg argviews, argpath, argFolderName, argtype, argsize, argpreLoad, argfileName;
		index=0;
		size=argsize;
		preLoad=argpreLoad;
		/*
		repairFlag=true;
		preLoad=switch(preLoad, true, {true}, false, {repairFlag=true; false}, \norepair, {
		repairFlag=false;
		true;
		});
		*/
		fileName=argfileName;
		//--------------------------------------------- MASTER/SLAVES
		type=argtype;
		//if (type==\master, {slaves=[]});
		slaves=[];
		masters=[];
		allSlaves=[];
		disabledSlaves=[];
		enabledSlaves=[];
		functions=();//zet hier functies in
		//--------------------------------------------- VIEWS
		views=argviews??{()};
		guiFlag=true;
		hasGUI=false;
		parents=[];
		//--------------------------------------------- INITS
		values=();
		actions=();
		controlSpecs=();
		repairFlag=true;
		//---------------------------------------------
		this.getValues;//(views)
		this.getActions;//(views)
		this.getControlSpecs;//(views)
		this.getParents;
		//--------------------------------------------- PATH
		folderName=argFolderName??{"master"};
		this.path_(argpath);
		this.update;//????
		//--------------------------------------------------- ACTIONS
		this.initFunctions([\update, \index, \store, \delete, \restore, \interpolate
			, \move, \delete, \path, \rename, \renumber]);//restore
		//restoreAction=if (sortedKeys, {{}},{restore

		this.restoreActionType_(0);

		functions[\path]={this.restore(0)};
		nextAction={
			this.restore;
		};
		prevAction={
			this.restore;
		};
		//--------------------------------------------- INTERPOLATION
		canInterpolate=false;//default no interpolation
		//this.initInterpolate;
		//--------------------------------------------- MORPH
		hasMorph=false;
		//-------------------------------------- SCRIPTS
		canScript=false;
		//this.initScript;//alleen als de gebruiker dit wil, niet default?
		//---------------------------------------------
		this.restore;
		//---------------------------------------------
		windows=();
	}
	//--------------------------------------------------- VIEWS HANDELING
	getValues {//arg views;
		values=();//values=values??{()};
		views.keys.do{|key|
			values[key]=views[key].value;
		};
	}
	getActions {//arg views;
		//actions=actions??{()};
		actions=views.collect{|view| view.action};
		//views.keysValuesDo{|key, view| actions[key]=view.action};
	}
	getControlSpecs{//arg views;  arg argspecs????
		controlSpecs=();//controlSpecs=controlSpecs??{()};
		defaultValues=();
		if (views!=nil, {
			views.keysValuesDo{|key, gui|
				if (gui.isKindOf(EZGui), {
					controlSpecs[key]=gui.controlSpec;
					defaultValues[key]=gui.controlSpec.default;
				},{
					defaultValues[key]=0.5;
				})
			}
		})
	}
	getParents {
		parents=[];
		views.do{|view|
			var parent;
			parent=if (view.isKindOf(EZGui), {view.findWindow},{view.parent.findWindow});
			parents=parents.add(parent);
		};
		parents=parents.asSet.asArray;
	}
	//--------------------------------------------------- FILE HANDELING
	path_ {arg argpath, loadAll=false;
		path=argpath ?? {
			var p=thisProcess.nowExecutingPath;
			if (p==nil, {
				"~/presets/".standardizePath},{
				p
			})
		};
		if (PathName(path).isFile, {path=path.dirname++"/presets/"});
		if (File.exists(path).not, {
			File.mkdir(path);
		});
		localpath=path++folderName++"/";
		if (File.exists(localpath).not, {
			File.mkdir(localpath);
		});
		index=0;
		fullPath=PathName(localpath).entries[0]??{
			var file;
			//this.newFileName;
			fileName=fileName??{"0000.scd"};
			file=File(localpath++fileName, "w");
			file.write(values.asCompileString);
			file.close;
			//writeFlag=true;
			(localpath++fileName).asPathName
		};
		fileNameWithoutExtension=fullPath.fileNameWithoutExtension;
		fileName=fullPath.fileName;
		fullPath=fullPath.fullPath;
		slaves.do{|p|
			p.path_(path);
		};

		masters.do{|p|
			p.path_(path, loadAll);
		};

		if (type==\master, {
			this.update;
			functions[\path].value;
		});
	}
	name {arg name;
		fileNameWithoutExtension=fileNameWithoutExtension.split($_)[0]++"_"++name;
		fileName=fileNameWithoutExtension++".scd";
		fullPath=(localpath++fileName);
		this.update;
	}
	renumber {arg name, i;
		var file, tmpfullPath=fullPath;
		this.index_(i);
		//this.name(name);
		fileNameWithoutExtension=fileNameWithoutExtension.split($_)[0]++"_"++name;
		fileName=fileNameWithoutExtension++".scd";
		fullPath=(localpath++fileName);
		if (File.exists(tmpfullPath), {
			File.copy(tmpfullPath, fullPath);
			File.delete(tmpfullPath);
			slaves.do{|ps| ps.renumber(name, i)};//dit kan een boosdoener zijn!!!
		});
		functions[\renumber].value;
		masters.do{|ps| ps.renumber(name, i)};
		this.update;//beetje brute force hoor.....
	}
	//folderName_ {}
	update {
		//var indices;
		entries=PathName(localpath).entries;
		if (type==\master, {
			size=entries.size;
			slaves.do{|ps| ps.size=size};
		});
		fileNamesWithoutExtensions=entries.collect{|path|
			path.fileNameWithoutExtension
		};
		indices=fileNamesWithoutExtensions.collect{|name|
			name.split($_)[0].interpret
		};
		if (preLoad, {
			presets=Array.newClear(size);
			entries.do{|pathName, i|
				var tmpPreset, extra;
				tmpPreset=pathName.fullPath.load;

				extra=views.keys.difference(tmpPreset.keys);
				extra.do{|key| tmpPreset[key]=views[key].value};

				tmpPreset.keysValuesDo{|key,val|
					if (views[key].value.class==String, {

					}, {
						//repair!
						//if (repairFlag, {
						if (views[key].value.size!=val.size, {
							tmpPreset[key]=values[key];
						});
						//});
						if (tmpPreset[key]==nil, {
							if (views[key].value!=nil, {
								tmpPreset[key]=views[key].value;
							});

						});
					})
				};
				views.keysValuesDo{|key, view|
					if (tmpPreset[key]==nil, {
						tmpPreset[key]=view.value
					});
				};

				presets[indices[i]]=tmpPreset;
				//presets[indices[i]]=pathName.fullPath.load;
			};
		});
		slaves.do{|ps| ps.update};//brute force....
		masters.do{|ps| ps.update};//brute force....
		functions[\update].value;
	}
	index_ {arg i, action=true;
		if (i!=nil, {
			//index=i.clip(0, size-1);
			index=i;
			if (action, {
				if (type==\master, {
					if (preLoad, {
						fullPath=entries[index];
					},{
						fullPath=PathName(localpath).entries[index];
					});
					fileName=fullPath.fileName;
					fileNameWithoutExtension=fullPath.fileNameWithoutExtension;
					fullPath=fullPath.fullPath;
					/*
					if (fileNameWithoutExtension.split($_)[0].interpret.asInteger!=index, {this.newFileName});
					*/
					this.slaveIndex;
					functions[\index].value;
				},{

				});
			});
			masters.do{|ps| ps.index_(i, action)};
		});
	}
	slaveIndex{
		slaves.do{|ps|
			ps.index_(index);
			ps.fileName=fileName;
			ps.fileNameWithoutExtension=fileNameWithoutExtension;
			ps.fullPath=(ps.localpath++fileName);
		};
	}
	newFileName {arg f;
		fileNameWithoutExtension=index.asDigits(10, 4).asDigit;
		fileName=fileNameWithoutExtension++".scd";
		fullPath=(localpath++fileName);
	}
	doLoad {
		restoreAction.value(this);
		//----------------------------------------------- end of restore
		slaves.do{|presetsystem| presetsystem.load };
		masters.do{|presetsystem| presetsystem.doLoad };
		functions[\restore].value;//of dit voor de slaves doen?
	}
	doRestore {
		restoreAction.value(this);
		//----------------------------------------------- end of restore
		slaves.do{|presetsystem|
			if (presetsystem.canInterpolate, {
				presetsystem.restoreI(index)
			},{
				presetsystem.restore//(index) beter???
			})
		};
		masters.do{|presetsystem| presetsystem.doRestore };
		functions[\restore].value;//of dit voor de slaves doen?
	}
	load {arg i;
		var file, extra;
		//this.setCurrent;
		this.index_(i);
		if (File.exists(fullPath), {
			file=File(fullPath, "r");
			newValues=file.readAllString.interpret;
			file.close;
			extra=views.keys.difference(newValues.keys);
			extra.do{|key| newValues[key]=views[key].value};
			this.checkValues;
			this.doRestore;//?????
		});
	}
	restore {arg i;
		var file, extra;
		//this.setCurrent;
		this.index_(i);
		if (preLoad, {
			if (presets[index]!=nil, {
				newValues=presets[index];
				/*
				extra=views.keys.difference(newValues.keys);//is al gebeurd
				extra.do{|key| newValues[key]=views[key].value};//is al gebeurd
				*/
				//this.checkValues;
				this.doRestore;
			});
		},{
			this.load;
		})
	}
	checkValues {
		newValues.keysValuesDo{|key,val|
			if (val==nil, {
				newValues[key]=values[key]??{defaultValues[key]};
			});
			if (repairFlag, {
				if (values[key].size!=val.size, {
					newValues[key]=values[key];
				});
			});
		};
	}
	setCurrent {
		this.getValues;
		current=values
	}
	restoreCurrent {
		if (current!=nil, {
			newValues=current;
			"current ".post; current.postln;
			"do restore".postln;
			this.doRestore;
		});
		/*
		values.keysValuesDo{|key,val|
		views[key].valueAction_(val)
		}
		*/
	}
	store {arg i, name;//make difference between store and write (or save)
		var file;
		this.index_(i);
		if (name!=nil, {
			if (File.exists(fullPath), {File.delete(fullPath)});
			this.name(name);
			slaves.do{|ps| ps.renumber(name, i)};//klopt dit?
		});
		file=File(fullPath, "w");
		this.getValues;

		file.write(values.asCompileString);
		file.close;
		if (preLoad, {
			if (index<presets.size, {
				presets[index]=values});
		});
		functions[\store].value(index);
		masters.do{|ps| ps.store(i, name)};//klopt dit?
	}
	storeAll {arg i, name;
		this.store(i, name);
		slaves.do{|presetsystem| presetsystem.store(i, name) };
	}
	add {arg increment=1;
		index=index+increment;
		this.shiftfiles(index);
		this.newFileName;
		this.store;
		this.slaveIndex;
		this.update;
		masters.do{|ps| ps.add(increment)};
	}
	insert {arg i=0, fileName="", all=false;
		this.getValues;
		index=i;
		presets=presets.insert(index, values);
		this.shiftfiles(index, 1);
		entries=entries.insert(index, this.fileWrite(index.asDigits(10, 4).asDigit
			++fileName).asPathName);
		if (all, {
			slaves.do{|ps| ps.insert(i, fileName)};
		});
		this.update;
		this.slaveIndex;
	}
	swapPreset {arg source, target;
	}
	move {arg source, target;
		var tmpfullPath, sourcePath, tmpfullPath2, tmpfileName;
		var fileName1=entries[target].fileNameWithoutExtension, fileName2;
		var source2, target2;
		index=target;
		presets=presets.move(source, target);
		tmpfullPath=entries[source];
		if (tmpfullPath!=nil, {
			tmpfileName=tmpfullPath.fileNameWithoutExtension
			.split($_).copyToEnd(1).join;
			tmpfullPath=tmpfullPath.fullPath.dirname++"/"++
			"9999"++"_"++
			tmpfileName
			++".scd"
			;
		});
		sourcePath=entries[source].fullPath;
		this.rename(sourcePath.basename, tmpfullPath.basename);
		slaves.do{|ps| ps.rename(sourcePath.basename, tmpfullPath.basename)};
		entries[source]=tmpfullPath.asPathName;
		entries=entries.move(source, target);
		if ((source-target).abs>1, {
			this.shiftfiles(
				[source,target].minItem
				//, -1
				, if (source<target, {-1},{1})
				, ([source,target].maxItem-1)
			);
		},{
			fileName2=source.asDigits(10,4).asDigit++"_"
			++(fileName1.split($_).copyToEnd(1).join);
			fileName1=fileName1++".scd";
			fileName2=fileName2++".scd";
			this.rename(fileName1, fileName2);
			slaves.do{|ps| ps.rename(fileName1, fileName2)};
		});
		if (tmpfullPath!=nil, {
			tmpfullPath2=
			tmpfullPath.dirname++"/"++
			target.asDigits(10, 4).asDigit++"_"++
			tmpfileName
			++".scd"
			;
		});
		this.rename(tmpfullPath.basename, tmpfullPath2.basename);
		slaves.do{|ps| ps.rename(tmpfullPath.basename, tmpfullPath2.basename)};
		entries[target]=tmpfullPath.asPathName;
		this.update;
		this.slaveIndex;
	}

	fileWrite {arg fileName;
		var tmppath, file;
		file=File(tmppath=(localpath++fileName++".scd"), "w");
		file.write(values.asCompileString);
		file.close;
		^tmppath
	}
	addAfter {
		this.add(1);
	}
	addBefore {
		this.add(0);
	}
	copy {arg source, target;
		//File.copy(path1, path2);
	}
	delete {arg i;
		this.index_(i);
		if (File.exists(fullPath)&&(PathName(localpath).entries.size>1), {
			File.delete(fullPath);
			functions[\delete].value(fileName);
			slaves.do{|presetsystem|
				if (File.exists(presetsystem.fullPath), {
					File.delete(presetsystem.fullPath)
				})
			};
			if (type==\master, {
				this.shiftfiles(index, -1);
				this.update;
				this.index_(index.clip(0, fileNamesWithoutExtensions.size-1));
				this.restore;
			},{
				this.update;
			});
		});
		masters.do{|ps| ps.delete(i)};
	}
	deleteFile {arg path;
		if (File.exists(path), {
			File.delete(path)
		})
	}

	rename {arg fileName1, fileName2;
		var path1=localpath++fileName1, path2=localpath++fileName2;
		if (File.exists(path1), {
			File.copy(path1, path2);
			File.delete(path1);
		});
		functions[\rename].value(fileName1, fileName2);
	}
	renameFile {arg path1, path2;
		if (File.exists(path1), {
			File.copy(path1, path2);
			File.delete(path1);
		});
	}
	shiftfiles {arg i, shift=1, end;
		var pathnames;
		pathnames=if (end==nil, {
			PathName(localpath).entries.copyToEnd(i);
		},{
			PathName(localpath).entries.copyRange(i, end);
		});
		if (shift>0, {pathnames=pathnames.reverse});
		pathnames.do{|pathName, j|
			var newPathName, fileNameWithoutExtension=pathName.fileNameWithoutExtension
			, split, fileName=pathName.fileName;
			split=fileNameWithoutExtension.split($_);
			split[0]=(split[0].interpret+shift).asDigits(10, 4).asDigit;
			fileNameWithoutExtension=split.join("_");
			newPathName=localpath++fileNameWithoutExtension++".scd";
			this.rename(fileName, fileNameWithoutExtension++".scd");
			slaves.do{|presetsystem|
				presetsystem.rename(fileName, fileNameWithoutExtension++".scd")
			};
		};
		masters.do{|ps| ps.shiftfiles(i, shift)};
	}
	next {arg incr=1;
		if (index+incr<fileNamesWithoutExtensions.size, {
			//index=index+1;
			this.index_(index+incr);
			this.nextAction.value;
			//if (guis[\presetList]!=nil, {guis[\presetList].value_(index)});
		});
	}
	prev {arg incr= -1;
		if (index+incr>=0, {
			//index=index+1;
			this.index_(index+incr);
			this.nextAction.value;
			//if (guis[\presetList]!=nil, {guis[\presetList].value_(index)});
		});
	}
	/*
	renumberFiles{arg old, new, incr=10000;
	var path1, path2;
	old.do{|i, k|
	var fileName, fileNameRest;
	new[k];
	path1=entries[k];
	fileNameRest=path1.fileName.split($_);
	path2=path1.folderName++"/";
	this.renameFile(path1.fullPath, path2.fullPath);
	};
	new.do{|i,k|
	path1;
	path2;
	this.renameFile(path1, path2);
	};
	}
	renumberFile{arg incr=10000;

	}
	*/
	//--------------------------------------------------- INIT FUNCTIONS
	initFunctions {arg keys;
		//functionKeys=[\update, \index];
		keys.do{|key|
			functions[key]={};
		}
	}
	//--------------------------------------------------- MASTER AND SLAVES
	/*
	addToMaster {arg views, folderName, type=\master;
	var presetSystem;
	if (folderName==nil, {folderName="masters"++masters.size});
	masters=masters.add(
	PresetSystem(views, path, folderName, \master, size, preLoad)
	//and maybe addInterpolation e.d. als dit aanstaat
	);
	}
	*/
	addSlave {arg views, folderName, type=\slave, preload;
		var presetSystem;
		if (folderName==nil, {folderName="slave"++slaves.size});
		presetSystem=PresetSystem.new(views, path, folderName, type, size, preload??{preLoad}
			, fileName??{"0000.scd"});
		slaves=slaves.add(presetSystem);
		allSlaves=allSlaves.add(presetSystem);
		^presetSystem
		//views, path, folderName, type=\master;
	}
	addToMaster {arg vviews;
		vviews.keysValuesDo{|key,view| views[key]=view};
		this.getValues(vviews);
		this.getActions(vviews);
		this.getControlSpecs(vviews);
	}
	addToSlave {arg vviews, slaveNr=0;
		var sslave=slaves.clipAt(slaveNr);
		vviews.keysValuesDo{|key,view| sslave.views[key]=view};
		sslave.getValues(vviews);
		sslave.getActions(vviews);
		sslave.getControlSpecs(vviews);
		sslave.update;
	}
	disableSlave {arg id;
		var slaveCopy=allSlaves.deepCopy;
		if (id==nil, {
			disabledSlaves=(0..(allSlaves.size-1));
			enabledSlaves=[];
			slaves=[];
		},{
			disabledSlaves=disabledSlaves.add(id).flat;
			enabledSlaves.removeAll(id.asArray);
			disabledSlaves=disabledSlaves.asSet.asArray.sort;
			enabledSlaves=enabledSlaves.asSet.asArray.sort;
			slaves=enabledSlaves.collect{|i| slaveCopy[i]};
		});
	}
	enableSlaves {arg id;
		var slaveCopy=allSlaves.deepCopy;
		if (id==nil, {
			enabledSlaves=(0..(allSlaves.size-1));
			disabledSlaves=[];
			slaves=slaveCopy;
		},{
			disabledSlaves.removeAll(id.asArray);
			enabledSlaves=enabledSlaves.add(id).flat;
			disabledSlaves=disabledSlaves.asSet.asArray.sort;
			enabledSlaves=enabledSlaves.asSet.asArray.sort;
			slaves=enabledSlaves.collect{|i| slaveCopy[i]};
		})
	}
	disableView {arg key;

	}
	enableView {arg key;

	}

	restoreActionType_ {arg type=0, includeSlaves=false;
		//0=action.value and value_, 1=sortedKeys, 2={}.defer, 3={}.defer sorted
		restoreActionType=type;
		restoreAction=switch(restoreActionType, 0, {{|p|
			p.views.keysValuesDo{|key,view|
				view.action.value(p.newValues[key])
			};

			if (guiFlag, {
				p.views.keysValuesDo{|key,view|
					{view.value_(p.newValues[key])}.defer
				};
			})
		}}, 1, {{|p|
			p.views.sortedKeysValuesDo{|key,view|
				view.action.value(p.newValues[key])
			};

			if (guiFlag, {
				p.views.sortedKeysValuesDo{|key,view|
					{view.value_(p.newValues[key])}.defer
				};
			})

		}}, 2, {{|p|
			p.newValues.keysValuesDo{|key,val|
				{p.views[key].valueAction_(val)}.defer
			};

		}}, 3, {{|p|
			p.newValues.sortedKeysValuesDo{|key,val|
				{p.views[key].valueAction_(val)}.defer
			};

		}}
		);
		if (includeSlaves, {
			slaves.do{|ps| ps.restoreActionType_(type)};//dit kan een boosdoener zijn!!!
		});
		^this
	}

	//========================================================= NN
	normalizePresets {
		presetsNormalized=presets.collect{|index| this.normalizePreset(index)};
		^presetsNormalized
	}

	normalizePreset {arg indeks;//-1 is current values?
		var ppreset, presetNormalized, keys=controlSpecs.keys;
		if (indeks==nil, {indeks=index});
		ppreset=if (indeks<0, {
			this.setCurrent;
			this.getValues;
			values
		},{
			presets[indeks].deepCopy;
		});
		presetNormalized=Array.fill(controlSpecs.size, 0.5);
		keys.do{|key,k|
			var value=0.5;
			if (ppreset[key]!=nil, {
				value=ppreset[key];
			});
			presetNormalized[k]=controlSpecs[key].unmap(value).clip(0.0, 1.0).abs;
		};
		^(presetNormalized.flat)
	}

	getWarps {
		//=========================================================init all
		var viewsList=[];
		var actionsList=[];
		var keys=[];
		var shape=[];
		var sizes=[];

		controlSpecsList=[];
		//=========================================================
		views.sortedKeysValuesDo{|key, view|
			var cs=ControlSpec(0.0, 1.0);
			viewsList=viewsList.add(view);
			actionsList=actionsList.add(if (view.action!=nil, {
				view.action
			},{
				nil
			}));

			if (controlSpecs[key]!=nil, {
				cs=views[key].controlSpec
			},{
				if (view.class==Button, {
					cs=ControlSpec(0, view.states.size.asFloat);
				});
				if (view.class==PopUpMenu, {
					cs=ControlSpec(0, view.items.size.asFloat);
				});
				if (view.class==ListView, {
					cs=ControlSpec(0, view.items.size.asFloat);
				});
			});
			if (cs.step<0.01, {
				controlSpecsList=controlSpecsList.add(cs.warp);
			},{
				controlSpecsList=controlSpecsList.add(cs);
			});
			keys=keys.add(key);
			shape=shape.add(values[key]);
			sizes=sizes.add(values[key].size.max(1));
		};
		if (sizes.sum!=controlSpecsList.size, {
			reshapeFlag=true;
		});
	}
}