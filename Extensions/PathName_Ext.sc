+PathName {
	deepFolders {
		var paths=[];
		this.deepFiles.do{arg pathname;
			var path=pathname.pathOnly;
			if (paths.includesEqual(path).not, {
				paths=paths.add(path)
			});
		};
		^paths.collect{|p| PathName(p)}
	}
	deepFoldersRelative {
		^this.deepFolders.collect{|pathname| PathName(pathname.fullPath.replace(fullPath, "/"))}
	}
	deepCollectEvent {arg function={arg pathname; pathname.fullPath.load}, event=(), key;
		this.entries.do({ | item |
			if( (item.isFolder), {
				key=item.folderName;
				if (event[key]==nil, {
					event[key]=();},{
				});
				event[key]=item.deepCollectEvent(function,
					event[key]
					, key)
			},{
				if (item.isFile, {
					key=item.fileNameWithoutExtension;
					event[key]=function.value(item, key, event)
				})
			})
		});
		^event
	}
	moveDir {arg depth=1;
		var folders=this.allFolders;
		^PathName("/"++folders.copyRange(0, (folders.size-1-depth).max(0)).join($/)++"/")
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
}
+String {
	mkdirNumbered {arg pathName, addAction=\addAfter, numDigits=4; ^File.mkdirNumbered(this, pathName, addAction, numDigits)}
}
+File {
	*move {arg pathNameFrom, pathNameTo;
		this.rename(pathNameFrom, pathNameTo)
	}
	*deleteNumbered {arg pathName, numDigits=4;
		var index, pathOnly;
		if (pathName.class==String, {pathName=PathName(pathName)});
		pathOnly=pathName.moveDir(1);
		index=pathOnly.entries.collect(_.fullPath).indexOfEqual(pathName.fullPath);
		File.delete(pathName.fullPath);
		this.renumberEntries(pathOnly, index, -1, numDigits);
	}
	*renumberEntries {arg pathName, start=0, add=1, numDigits=4;
		if (pathName.class==String, {pathName=PathName(pathName)});
		pathName.entries.copyToEnd(start).do{|p, i|
			var folderName=p.folderName;
			var pathOnly=p.pathOnly;
			var pathNameFrom=p.fullPath, pathNameTo;
			folderName=(start+add+i).asDigits(10, numDigits).join++"_"++folderName.split($_).copyToEnd(1).join($_)++"/";
			pathNameTo=pathName.fullPath++folderName;
			File.rename(pathNameFrom, pathNameTo);
		};
	}
	*mkdirNumbered {arg folderName="untitled_folder", pathName, addAction=\addAfter, numDigits=4;
		var index, directory, entries, newIndex;
		var directoryFullPath;
		if (pathName.class==String, {pathName=PathName(pathName)});
		directory=pathName.moveDir(1);
		directoryFullPath=directory.fullPath;
		entries=directory.entries;
		index=entries.collect(_.fullPath).indexOfEqual(pathName.fullPath) + 1;
		if (addAction==\addAfter, {
			newIndex=index.copy;
			index=index;
		},{
			newIndex=index-1;
			index=index-1;
			//index=index-1;
		});
		entries.copyToEnd(index).do{|p, i|
			var folderName=p.folderName;
			var pathNameFrom=p.fullPath, pathNameTo;
			folderName=(index+1+i).asDigits(10, numDigits).join++"_"++folderName.split($_).copyToEnd(1).join($_)++"/";
			pathNameTo=directoryFullPath++folderName;
			File.rename(pathNameFrom, pathNameTo);
		};
		pathName= (directoryFullPath++(newIndex).asDigits(10, numDigits).join++"_"++folderName++"/");
		File.mkdir(pathName);
		^pathName
	}
	*rename {arg pathNameFrom, pathNameTo;
		if ((thisProcess.mainThread.state>3), {
			var cond=Condition.new;
			("mv "++pathNameFrom++" " ++ pathNameTo).unixCmd({cond.unhang});
			cond.hang;
			//^this.primitiveFailed
		},{
			{
				var cond=Condition.new;
				("mv "++pathNameFrom++" " ++ pathNameTo).unixCmd({cond.unhang});
				cond.hang;
				//^this.primitiveFailed
			}.fork
		})
	}

}