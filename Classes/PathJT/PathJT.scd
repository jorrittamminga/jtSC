/*
PathJT=Node
FolderJT=Group = folder
FilJT=Synth = file
RootFolderJT=RootNodeJT=Server...
rootpath=server (=RootFolderJT)
pathID=nodeID
*/
PathJT {
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
		rootpath = rootpath ? RootFolderJT.default;//
		^super.newCopyArgs(pathID ?? { rootpath.nextpathID}, rootpath)
	}
	free {}
	onFree {}
	asTargetPath { ^this }
	set {}
	//trace
	query {}
	moveBefore {arg aPath;}
	moveAfter {arg aPath;}
	moveToHead {arg aFolder;}
	moveToTail {arg aFolder;}
	run {arg flag; isActive=flag;}
}

FolderJT : PathJT {
	var <entries;

	*new {arg target, addAction=\addToHead;
		var folder, rootpath, addActionID;
		target = target.asTargetPath;
		rootpath = target.rootpath;
		folder = this.basicNew(server);
		addActionID = addActions[addAction];
		folder.folder = if(addActionID < 2) { target } { target.folder };
		server.sendMsg(this.creationCmd, folder.nodeID, addActionID, target.nodeID);
		^folder
	}
	dumpTree {}
	queryTree {}
	freeAll {}

}

FileJT : PathJT {
	*new { arg defName, args, target, addAction=\addToHead;
		var file, rootpath, addActionID;
		target = target.asTargetPath;
		server = target.rootpath;
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

RootFolderJT : FolderJT {
	classvar <roots, <>default;

	*new { arg rootpath;
		rootpath = rootpath ? RootPathJT.default;
		^(roots.at(rootpath.name) ?? {
			^super.basicNew(rootpath, 0).rninit
		})
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

+Nil {
	asTargetPath { ^Server.default.asTarget }
}

+Integer {
	asTargetPath { ^FolderJT.basicNew(RootFolderJT.default, this) }
	asNodeID { ^this }
}