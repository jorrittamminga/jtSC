/*
object kan een PresetJT zijn, [PresetJT, PresetBlender], Function, Event, EventJT, View, EZGUI
en how about een Array??? (die gebruik ik nu om een CueJTCollection te maken....
*/
MetaCueJT {
	classvar <>classesMethods, <obj;
	var <>cueMaster;
	var <object, <value, <gui;
	var name;
	var <>path, <>pathRelative, <>rootPath, <cueID=0, fileName, extension="scd", numDigits=4;
	var <>paths, <>pathsRelative;
	var <>funcList, <>classMethods, <>values, <>objectList, <enviroment;
	var <>func, <>methode, <document, <documents;

	*initClass {
		classesMethods = (
			PresetJT: [\restore, \restoreI],
			PresetJTCollection: [\restore, \restoreI],
			PresetJTCollectionBlender: [\restore, \restoreI],
			PresetJTNeuralNet: [\restore]
		);
	}
	*basicNew { arg object, dirname, enviroment;
		^super.new.init(object, dirname, enviroment)
	}
	init {arg argobject, name, argenviroment;
		classMethods=classesMethods;
		object=argobject;
		enviroment=argenviroment;
		fileName=name;
		value=0;
		paths=[];
		pathsRelative=[];
		values=[];
		func=();
		documents=[];
		cueID= -1;
		this.prInit;
	}
	prInit {
		methode=\value;
	}
}

CueJT : MetaCueJT {
	*new {arg object, name, enviroment, dirname;
		obj=object.class;

		^if (obj.asClass.superclasses.includesEqual(MetaPresetJT)//(obj==PresetJT) || (obj==PresetJTCollection) || (obj==PresetJTCollectionBlender) //|| (obj==PresetJTNeuralNet)
			, {
				CuePresetJT.new(object, name, enviroment)
			},{
				if (obj==Array, {
					//CueJTCollection(object, name, enviroment)
					CueJTCollection(object.collect{|obj| CueJT(obj, name, enviroment)});
				}, {
					if ((obj==Event) || (obj==Event), {
						CuePresetFromEventJT.new(PresetJT(object, dirname), name, enviroment)
					},{
						super.basicNew(object, name, enviroment)
					})
				})
		})
	}
	cueID_ {arg index;
		cueID=index;
		path=paths[cueID];
		pathRelative=pathsRelative[cueID];
		value=values[cueID];
		func[\cueID].value(this);
	}
	prAdd {
		paths[cueMaster.cueID]=cueMaster.path;
		pathsRelative[cueMaster.cueID]=cueMaster.pathRelative;
		values[cueMaster.cueID]=this.getValue;
		this.cueID_(cueMaster.cueID);
		this.store;
	}
	add {
		this.prAdd
	}
	prDelete {
		if (cueMaster.cueID>0, {
			paths[cueMaster.cueID]=nil;
			pathsRelative[cueMaster.cueID]=nil;
			values[cueMaster.cueID]=nil;
			this.restorePrevious;
		});
	}
	delete {
		this.preDelete
	}
	previousCue {arg index;
		index=index??{cueID};
		while({ (paths[index]==nil) && (index>=0) }, {
			index=index-1;
		});
		this.cueID_(index);
	}
	restorePrevious {
		this.previousCue;
		this.restore;
	}
	restore {
		funcList[cueID].value(enviroment);
	}
	getValue {
		^value
	}
	value_ {arg v;
		value=v;
		values[cueID]=value;
	}
	open {
		var pathName=this.fullPath, document;
		if (Document.openDocuments.collect{|d| d.path}.includesEqual(pathName).not, {
			document=Document.open(pathName);
		});
	}
	fullPath {
		^(path++fileName.asString++"."++extension)
	}
	close {
		var pathName=this.fullPath;
		Document.openDocuments.do{|doc| if (doc.path==pathName, {doc.close})};
	}
	store {
		var file, pathName;
		pathName=this.fullPath;
		file=File(pathName, "w");
		file.write(value.asCompileString);
		file.close;
		funcList[cueID]=this.makeFunc(value.deepCopy);
		func[\store].value(this);
		//this.storeAction;
	}
	//storeAction {cueMaster.cueList[cueID][fileName.asSymbol]=value;}
	getObject {^object}
	makeFunc {arg val;
		//var obj=object, out;
		//obj=this.getObject(val);
		//val=val??{value.deepCopy};
		^val
	}
	deactivateCue {}
	activateCue {}
	deactivate {}
	activate {}
	makeGui {arg parent, bounds=350@20;
		{gui=CueJTGUI(this, parent, bounds)}.defer
	}
}
CueActionJT : CueJT {} // kruising tussen een PresetJT en een CueJT, om b.v. een opname te starten of een dergelijke actie
CueJTCollection {
	var <id=0, <cues, <cue, gui;

	*new {arg cues;
		^super.new.init(cues)
	}
	init {arg argcues;
		cues=argcues;
		this.at(id);
	}
	at {arg index;
		id=index.min(cues.asArray.size-1);
		cue=cues.clipAt(id);
		^cue
	}
	id_ {arg index;
		^this.at(index)
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CueJTCollectionGUI(this, parent, bounds)}.defer
	}
	/*
	objectID_ {arg id;
		objectID=id.min(objects.asArray.size-1);
		object=objects.clipAt(id);
		^object
	}

	getObject {arg val;
		var id=objectID;
		if (val.class==Event, {
			if (val[\objectID]!=nil, {id=val[\objectID]})},
		{if (val.class==Array, {id=val[0]})})
		^objects.clipAt(id);
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CueJTCollectionGUI(this, parent, bounds)}.defer
	}
	*/
}