Numbered {
	var <pathName, <directory, <index=0, <>numDigits=4, <target, <addAction, <basename, <entries, <renumberFlag;
	var <entriesFullPath;
	var <root, <>rootPath, <rootPathName;
	var <>funcs, <gui;
	classvar <systemIsCaseSensitive;
	*initClass {
		var f = this.filenameSymbol.asString;
		systemIsCaseSensitive = not(File.exists(f.toLower) and: {File.exists(f.toUpper)});
	}
	prInit {arg addAction;
		renumberFlag=true;
		numDigits=numDigits??{target.getNumDigits.minItem??{4}};
		if ((addAction==\addAfter) || (addAction==\addBefore), {
			index=entries.collect(_.fullPath).indexOfEqual(target.fullPath);
		});
		switch(addAction, \addAfter, {
			index=index+1;
			if (entries.size<=index, {renumberFlag=false});
		}, \addBefore, {
			index=index;
		}, \addToTail, {
			index=entries.size;
			renumberFlag=false;
		}, \addToHead, {
			index=0;
		}, {
			index=index;
		});
	}
	directory_ {arg dir; directory=dir.asPathName}
}

NumberedFile : Numbered {
	var <extension;

	*new { arg pathName, target, addAction=\addAfter, numDigits=4, item=();
		^super.new.init(pathName, target, addAction, numDigits).writeAsCompileString( item )
	}
	read{^pathName.fullPath.load}
	load{^pathName.fullPath.load}
	*write {arg item, pathName, target, addAction=\addAfter, numDigits=4;
		^super.new.init(pathName, target, addAction, numDigits).writeAsCompileString(item)
	}
	*tail{arg target, pathName, numDigits=4;
		^super.new.init(pathName, target, \addToTail, numDigits).writeAsCompileString( () )
	}
	*head{arg target, pathName, numDigits=4;
		^super.new.init(pathName, target, \addToHead, numDigits).writeAsCompileString( () )
	}
	*after{arg target, pathName, numDigits=4;
		^super.new.init(pathName, target, \addAfter, numDigits).writeAsCompileString( () )
	}
	*before{arg target, pathName, numDigits=4;
		^super.new.init(pathName, target, \addBefore, numDigits).writeAsCompileString( () )
	}
	writeAsCompileString {arg item, close=true;
		var file;
		"write in new File with path ".post; pathName.fullPath.postln;
		file=File(pathName.fullPath, "w");
		file.write(item.asCompileString);
		if (close, {file.close});
	}
	init {arg argpathName, argtarget, argaddAction, argnumDigits;
		pathName=argpathName.asPathName;
		target=argtarget.asPathName??{};
		addAction=argaddAction??{\addAfter};
		numDigits=argnumDigits;
		extension=pathName.extension;
		if (extension.size==0, {extension="scd"; pathName=PathName(pathName.fullPath++"."++extension)});
		if (target.isFolder, {
			directory=target;
			if (addAction==\addAfter, {addAction=\addToTail});
			if (addAction==\addBefore, {addAction=\addToHead});
		},{
			directory=target.pathOnly.asPathName;
		});
		entries=directory.entriesFilesOnly;
		this.prInit(addAction);
		if (renumberFlag, {
			this.renumberFiles(index, 1);
		});
		pathName=pathName.fullPath.numberedPath(directory, index, numDigits).asPathName;
	}
	*renumberFiles {arg directory, start=0, add=1, numDigits=4;
		^super.newCopyArgs(0, directory, start, numDigits).renumberFiles(start, add);
	}
	renumberFiles {arg start=0, add=1;
		if (start<0, {start=0; add=0});
		directory=directory.asPathName;
		directory.entriesFilesOnly.copyToEnd(start).do{|p, i|
			var fileName=p.fileName;
			var pathNameFrom=p.fullPath, pathNameTo;
			fileName=fileName.renumberNumbered(i+start+add, numDigits);
			pathNameTo=directory.fullPath++fileName;
			File.rename(pathNameFrom, pathNameTo);
		};
	}
	*delete {arg pathName;
		var number, digits;
		var numDigits, index, directory;
		pathName=pathName.asPathName;
		digits=pathName.fileNameWithoutExtension.split($_)[0];
		numDigits=digits.size;
		number=digits.interpret;
		index=pathName.pathOnly.asPathName.entriesFilesOnly.collect(_.fullPath).indexOfEqual(pathName.fullPath);
		if (index!=number, {index= -1;});
		if (numDigits==0, {numDigits=4});
		directory=pathName.pathOnly.asPathName;
		^super.newCopyArgs(pathName, directory, index, numDigits).delete
	}
	delete {
		File.delete(pathName.fullPath);
		//this.renumberFiles(directory, index, 0, numDigits);
		this.renumberFiles(index, 0);
	}
	*rename {arg basename="test", pathName;
		var directory, index, extension="/", numDigits;
		pathName=pathName.asPathName;
		numDigits=pathName.fileNameWithoutExtension.split($_)[0].size;
		index=pathName.fileNameWithoutExtension.split($_)[0].interpret;
		directory=pathName.pathOnly;
		extension=pathName.extension;
		^super.newCopyArgs(pathName, directory, index, numDigits).rename(basename, extension);
	}
	rename {arg basename="test", ext;
		var path, func;
		extension=ext??{extension??{".scd"}};
		path=(basename++"."++extension).numberedPath(directory, index, numDigits);
		func={
			var cond=Condition.new;
			("mv "++pathName.fullPath++" " ++ path).unixCmd({cond.unhang});
			cond.hang;
		};
		if ((thisProcess.mainThread.state>3), {func.value},{{ func.value}.fork(AppClock)})
		^path;
	}
}

NumberedFolder : Numbered {
	*new { arg pathName="newFolder", target, addAction=\addAfter, numDigits=4, moveType=false;
		^super.new.init(pathName, target, addAction, numDigits, moveType)
	}
	*groupNumbered {arg pathName="group_folder", targets, addAction=\addBefore, numDigits=4;
		^super.new.init(pathName, targets[0], addAction, numDigits, targets)
	}
	*mkdir {arg pathName="newFolder", target, addAction=\addAfter, numDigits=4, moveType=false;
		^super.new.init(pathName, target, addAction, numDigits, moveType)
	}
	*tail{arg target, pathName="newFolder", numDigits=4, moveType=false; ^super.new.init(pathName, target, \addToTail, numDigits, moveType)}
	*head{arg target, pathName="newFolder", numDigits=4, moveType=false; ^super.new.init(pathName, target, \addToHead, numDigits, moveType)}
	*after{arg target, pathName="newFolder", numDigits=4, moveType=false; ^super.new.init(pathName, target, \addAfter, numDigits, moveType)}
	*before{arg target, pathName="newFolder", numDigits=4, moveType=false; ^super.new.init(pathName, target, \addBefore, numDigits, moveType)}
	init {arg argpathName, argtarget, argaddAction, argnumDigits, moveType;
		var func;
		var renumberStart;
		pathName=argpathName.asPathName??{Date.localtime.stamp};
		pathName=pathName.asPathName;
		target=argtarget.asPathName??{};
		addAction=argaddAction??{\addAfter};
		numDigits=argnumDigits;//??{target.getNumDigits.minItem??{4}};
		if (target.isFolder, {
			directory=target;
		},{
			directory=target.pathOnly.asPathName;
		});
		if ((addAction==\addAfter) || (addAction==\addBefore), {
			directory=directory.higher;
			entries=directory.entriesFoldersOnly;
		},{
			entries=directory.entriesFoldersOnly;
		});
		this.prInit(addAction);
		func={
			var cond=Condition.new, tmp;
			pathName=pathName.fullPath.numberedPath(directory, index, numDigits)++"/";
			File.mkdir(pathName);
			if (moveType.class==Array, {
				var containsFiles=false, containsFolders=false;
				moveType.do{|p|
					var from=p.asPathName, to, name;
					name=if (from.isFile, {containsFiles=true; from.fileName},{containsFolders=true; from.folderName});
					to=pathName++name;
					("mv " ++ from.fullPath ++ " " ++ to).unixCmd({cond.unhang});
					cond.hang;
				};
				if (containsFiles, {directory=pathName.asPathName; this.renumberFiles(0,0)});
				if (containsFolders, {NumberedFolder.renumberFolders(pathName.asPathName, 0, 0, numDigits)});
			},{
				if (moveType==\allFiles, {
					directory.entriesFilesOnly.do{|p|
						var pathFrom=p.fullPath, pathTo=pathName++p.fileName;
						("mv "++pathFrom++" "++pathTo).unixCmd({cond.unhang});
						cond.hang;
					};
					//this.renumberFiles(pathName, -1, 0, numDigits);
				});
			});
			if (renumberFlag, {
				this.renumberFolders(directory, index+1, 0, numDigits, pathName);
			});
		};
		if ((thisProcess.mainThread.state>3), {func.value},{{func.value}.fork(AppClock)});
	}
	*renumberFolders {arg directory, start=0, add=0, numDigits=4;
		^super.newCopyArgs(0, directory, start-1, numDigits).renumberFolders(directory, start, add)
	}
	renumberFolders {arg directory, start=0, add=0;
		var cond=Condition.new, func;
		var entries;
		if (directory.class==Array, {
			entries=directory.collect(_.asPathName);
			directory=entries[0];
			directory=if (directory.isFolder, {directory.higher},{directory.pathOnly.asPathName});
		},{
			entries=directory.asPathName.entriesFoldersOnly.copyToEnd(start);
		});
		func={
			entries.do{|p, i|
				var folderName=p.folderName;
				var pathFrom=p.fullPath, pathTo;
				var number=i+start+add;
				folderName=folderName.renumberNumbered(number, numDigits);
				pathTo=directory.fullPath++folderName;
				("mv "++pathFrom++" "++pathTo).unixCmd({cond.unhang});
				cond.hang;
			};
		};
		if ((thisProcess.mainThread.state>3), {func.value},{{func.value}.fork(AppClock)});
	}
	*rename {arg folderName="test", pathName;
		var directory, index, numDigits;
		pathName=pathName.asPathName;
		numDigits=pathName.folderName.split($_)[0].size;
		index=pathName.folderName.split($_)[0].interpret??{0};
		directory=pathName.fullPath.copyRange(0,pathName.colonIndices.clipAt(pathName.colonIndices.size-2));
		^super.newCopyArgs(pathName, directory, index, numDigits).rename(folderName);
	}
	rename {arg folderName="test";
		var path, func;
		folderName=folderName++"/";
		path=folderName.numberedPath(directory, index, numDigits);
		func={
			var cond=Condition.new;
			("mv "++pathName.fullPath++" " ++ path).unixCmd({cond.unhang});
			cond.hang;
		};
		if ((thisProcess.mainThread.state>3), {func.value},{{ func.value}.fork(AppClock)})
		^path;
	}
	*delete {arg pathName;
		var number, digits;
		var numDigits, index, directory;
		pathName=pathName.asPathName;
		directory=pathName.higher;
		digits=pathName.folderName.split($_)[0];
		numDigits=digits.size;
		number=digits.interpret;
		index=directory.entriesFoldersOnly.collect(_.fullPath).indexOfEqual(pathName.fullPath);
		if (index!=number, {index= 0;});
		if (numDigits==0, {numDigits=4});
		^super.newCopyArgs(pathName, directory, index, numDigits).delete
	}
	delete {
		var func, cond=Condition.new;
		func={
			("rm -r "++pathName.fullPath).unixCmd({cond.unhang});
			cond.hang;
			NumberedFolder.renumberFolders(directory, index, 0, numDigits);
		};
		if ((thisProcess.mainThread.state>3), {func.value},{{ func.value}.fork(AppClock)})
	}
}

+PathName {
	renumberNumbered {arg number, numDigits=4;
		var directory, name;
		directory=if (this.isFolder, {
			name=this.folderName;
			this.higher
		},{
			name=this.fileName;
			this.pathOnly;
		});
		name=name.renumberNumbered(number, numDigits);
		^(directory++name)
	}
	renameNumbered {arg name, numDigits=4;
		var directory, array, numberString, newFullPath;
		name=if (this.isFile, {
			directory=this.pathOnly;
			array=this.fileNameWithoutExtension.split($_);
			numberString=array[0];
			array[0]++"_"++name++"."++this.extension;
		},{
			directory=if (this.fullPath.last==$/, {
				this.moveDir(1).fullPath;
			},{
				this.pathOnly;
			});
			array=this.folderName.split($_);
			numberString=array[0];
			array[0]++"_"++name++"/";
		});
		newFullPath=(directory++name);
		File.rename(this.fullPath, newFullPath);
		^PathName(newFullPath);
	}
	getNumDigits {
		var numDigitsAll=[];
		this.deepFiles.do{|p|
			p.allFolders.do{|folderName|
				if (folderName.includes($_), {
					var size=folderName.split($_)[0].size;
					if (size>0, {numDigitsAll=numDigitsAll.add(size)})
				})
			}
		};
		^numDigitsAll
	}
}

+String {
	removeNumbersFromNumberedPath {
		^this.split($/).collect{|p| p.split($_).copyToEnd(1).join($_)}.join($/)
	}

	numberedPath{arg directory="", number=0, numDigits=4;
		^(directory.asPathName.fullPath++number.asDigits(10, numDigits).join++"_"++this)
	}
	renameNumbered {arg name, numDigits=4;
		^PathName(this).renameNumbered(name, numDigits).fullPath
	}
	renumberNumbered {arg number=0, numDigits=4;
		^number.asDigits(10, numDigits).join++"_"++this.split($_).copyToEnd(1).join($_)
	}
	deleteNumbered {}
	renameNumbered {}
	mkdirNumbered {arg pathName, addAction=\addAfter, numDigits=4; ^File.mkdirNumbered(this, pathName, addAction, numDigits)}
	writeNumbered {arg pathName, item, addAction=\addAfter, numDigits=4;
		var file=File(pathName, "w");
		file.write(item.asCompileString);
		file.close;
	}
}