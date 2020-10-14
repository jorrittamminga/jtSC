/*
The idea is to put an unique increasing pathID in front of the folder/file name in order to have a sorted list in any file system (osx/windows/linux).
so a filename will be 0000_foldername, where 0000 is the pathID (the amount of digits is also controlable, 4 digits by default)

The CueFileJT contains an Event with (method: restore, args: [0, 1.0, \sin], functionPre: {}, functionPost: {}) or (path: "/xyz/", etc) or a Function {...args; }
The CueFileJT is connected to an PresetJT, PresetJTCollection, Function, etc
A CueList (from Main CueSystem) is a 'compiled' list [{},{},{}...] of all the interpreted CueFileJT instructions

e=(freq: EZSlider, amp: EZSlider);
e=e.asEventJT;
p=PresetJT(e, path);
pc=PresetJTCollection(p);
cf=CueFileJT(c, args, masterCuePathJT)

CuePathJT = Node
CueFolderJT = Group = folder
CueFileJT = Synth = file
RootCueFolderJT = RootNodeJT = Server...
rootpath = server (=RootCueFolderJT)
pathID=nodeID (dat hele 0000-0001 gedoe)

of kijk naar Array/Collection, welke methods dit gebruikt om dingen toe te voegen
*/
CuePathJT {
	var pathID, <>rootpath, <>folder;
	var <>isActive;
	classvar <addActions;

	*initClass {
		addActions = (
			addToHead: 0,
			addToTail: 1,
			addBefore: 2,
			addAfter: 3,
			addReplace: 4,
			h: 0,
			t: 1,
			// valid action numbers should stay the same
			0: 0, 1: 1, 2: 2, 3: 3, 4: 4
		);
	}
	*basicNew { arg rootpath, pathID;
		rootpath = rootpath ? RootCueFolderJT.default;//
		^super.newCopyArgs(pathID ?? { rootpath.nextpathID}, rootpath)
	}
	free {}
	onFree {}
	asTargetPath { ^this }
	set {}
	trace {}
	query {}
	moveBefore {arg aPath;}
	moveAfter {arg aPath;}
	moveToHead {arg aFolder;}
	moveToTail {arg aFolder;}
	run {arg flag; isActive=flag;}

	//renumber {}
	//path actions:
	//delete {}
	//rename {}
	//copy {}
}

CueFolderJT : CuePathJT {
	var <entries;//array of pathnames (or paths) within this folder

	*new {arg target, addAction=\addToHead;
		var folder, rootpath, addActionID;
		target = target.asTargetPath;
		rootpath = target.rootpath;
		folder = this.basicNew(rootpath);
		addActionID = addActions[addAction];
		folder.folder = if(addActionID < 2) { target } { target.folder };
		[rootpath, folder.nodeID, addActionID, target.nodID];
		//rootpath.sendMsg(this.creationCmd, folder.nodeID, addActionID, target.nodeID);
		^folder
	}
	dumpTree {}
	queryTree {}
	freeAll {}

}

CueFileJT : CuePathJT {
	*new { arg defName, args, target, addAction=\addToHead;
		var file, rootpath, addActionID;
		target = target.asTargetPath;
		rootpath = target.rootpath;
		addActionID = addActions[addAction];
		file = this.basicNew(defName, rootpath);
		file.folder = if(addActionID < 2) { target } { target.folder };
		^file
	}
	*replace {}
	*basicNew { arg defName, rootpath, pathID;
		^super.basicNew(rootpath, pathID)//.defName_(defName.asDefName)
	}

	get {}
}

RootCueFolderJT : CueFolderJT {
	classvar <roots, <>default;

	*new { arg rootpath;
		rootpath = rootpath ? RootCuePathJT.default;
		^(roots.at(rootpath.name) ?? {
			^super.basicNew(rootpath, 0).rninit
		})
	}
	*default {
		^this.new
	}
	rninit {
		roots.put(rootpath.name, this);
		isActive = true;
		folder = this; // self
	}
	nextpathID {}
	*initClass {
		roots = IdentityDictionary.new;
		default = "~/".asAbsolutePath;
	}

	run { "run has no effect on RootNode".warn }
	free { "free has no effect on RootNode".warn }
	moveBefore { "moveBefore has no effect on RootNode".warn }
	moveAfter { "moveAfter has no effect on RootNode".warn }
	moveToHead { "moveToHead has no effect on RootNode".warn }
	moveToTail{ "moveToTail has no effect on RootNode".warn }

	*freeAll {
		roots.do({ arg rn; rn.freeAll })
	}
}
RootCuePathJT : RootCueFolderJT

+Nil {
	asTargetPath { ^RootCuePathJT.default.asTarget }
}

+Integer {
	asTargetPath { ^CueFolderJT.basicNew(RootCueFolderJT.default, this) }
	asNodeID { ^this }
}