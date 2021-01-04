/*
(root/) subfolder(s)/ (dir/) entries
entries is een array van orderedfiles of een array van OrderedFileSystems
zie Node, addAfter, addBefore, addToTail, addToHead (PathJT=Node, FolderPathJT=Group, FilePathJT=Synth, RootpathJT=RootNodeJT)
rootpath=server
*/
PathJT {
	var pathID, <>rootpath, <>folder;
	var <>isActive;
	//--------------------------------------------
	var <isRoot=false, <isMaster, <>isDir=false, <depthDir;
	var <path, <entries, <entry, <root, <rootOfAll, <>dir, <master, <file, <folder;
	var <index=0;
	var <pathname, <folderName, <arrayOfPathJTs;
	var <name, <extension, <nameWithoutNumber, <namesWithoutNumbers;
	var <nameEntry, <nameEntryWithoutNumber, <currentNameEntry;
	var <namesEntries, <namesEntriesWithoutNumbers, <namesEntriesWithoutNumbersWithoutExtensions;
	var <>forceCreateNewFile;
	var <>slaves;
	var <data;

	*new {arg path, root;
		^super.new.init(path,root)
	}
	init {arg argpath, argroot;
		this.root_(argroot);
		this.prPath(argpath);
	}
	//set{}
	//trace
	//query
	//moveBefore {arg aPath}
	//moveAfter {arg aPath}
	//moveToHead {arg aFolder}
	//moveToTail {arg aFolder}
	//run {arg flag; isActive=flag}

	root_ {arg argroot;
		if (argroot.isKindOf(PathJT), {
			root=argroot;
		},{
			root=argroot??{root??{this}};
		});
		rootOfAll=root.rootOfAll;
	}
	deepArrayOfPathJTs {arg o;
		arrayOfPathJTs=arrayOfPathJTs.add(o);
		if (o.entry.isKindOf(PathJT), {
			this.deeparrayOfPathJTs(o.entry)
		},{

		});
		^arrayOfPathJTs.flat
	}
	//---------------------------------------------------- PATH ACTIONS
	setFullPath {

	}
	//---------------------------------------------------- NAVIGATE ACTIONS
	index_ {arg i;
		index=i??{index};
		//root.entries
		^index
	}
	restore {arg i;
		this.doRestore(i);
	}
	next {}
	prev {}
	//---------------------------------------------------- ACTION
	doRestore {

	}
	doLoad {
		root.entries[index]
	}
	//---------------------------------------------------- RENAME
	name_ {arg string,ind;
		var oldName, newName;
		oldName=name.copy;
		nameWithoutNumber=string;
		newName=this.makeName(string, ind??{index}, extension);
		this.doRename(oldName, newName, nameWithoutNumber);
	}
	doRename {arg oldName, newName, newNameWithoutNumber;
		name=newName;
		nameWithoutNumber=newNameWithoutNumber;
		namesWithoutNumbers[index]=nameWithoutNumber;
		this.renameCommand((path++oldName),(path++newName))
	}

	renameCommand {arg oldPath, newPath;
		("mv " ++ oldPath ++ " " ++ newPath).unixCmd
	}
	makeName {arg string, ind, ext;
		ext=ext??{extension??{""}};
		^((ind??{index}).asDigits(10, 4).join++"_"++string++ext);
	}
	//----------------------------------------------------- ADD/REMOVE
	addAfter {arg target; }
	addBefore {arg target; }
	//---------------------------------------------------- CONVERSION
	asFolder {}
	asSlave {}
	asFile {}
	/*
	prPath {}
	prMakeFile {}
	prMakeFolder {}
	prName {}
	prIndex {}
	prRenumberFiles {}
	prUpdate {}
	prNewFileName {}
	prAddSubFolder {}
	prRemoveSubFolder {}

	prLoad {}
	prSave {}
	prStore {}
	prRestore {}
	prCopy {}
	prPaste {}
	prUndo {}
	prDelete {}
	prRename {}

	prAdd {}
	prAddAfter {}
	prAddBefore {}
	prInsert {}
	prSwap {}
	prMove {}
	prShift {}

	prNext {}
	prPrev {}
	*/
}
