+PathName {
	asPathName {^this}
	entriesFilesOnly {
		var entries=[];
		this.entries.do{|pathName| if (pathName.isFile, {entries=entries.add(pathName)})};
		^entries
	}
	entriesFoldersOnly {
		var entries=[];
		this.entries.do{|pathName| if (pathName.isFolder, {entries=entries.add(pathName)})};
		^entries
	}
	deepFolders {
		var folders;
		folders=this.entries.collect({ | item |
			if(item.isFolder, {
				if (item.entries.size==0, {
					item
				},{
					if ( item.entries.collect{|p| p.isFolder.binaryValue}.includes(1), {
						item.deepFolders
					},{
						item
					})
				})
			},{
				nil
			})
		}).flat;
		folders.removeAllSuchThat({arg i; i==nil});
		^folders
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
	higher {arg depth=1;
		^PathName(this.fullPath.copyRange(0, this.colonIndices.clipAt(this.colonIndices.size-1-depth)))
	}
	moveDir {arg depth=1;
		var folders=this.allFolders;
		^PathName("/"++folders.copyRange(0, (folders.size-1-depth).max(0)).join($/)++"/")
	}
}

//beter: maak een subclass PathNameNumbered oid met al deze methods
//.asPathNameNumbered
+File {
	*rmdir {arg path;

	}
	*move {arg pathNameFrom, pathNameTo;
		this.rename(pathNameFrom, pathNameTo)
	}
	*rename {arg pathNameFrom, pathNameTo;
		var func={
			var cond=Condition.new;
			("mv "++pathNameFrom++" " ++ pathNameTo).unixCmd({cond.unhang});
			cond.hang;
		};
		if ((thisProcess.mainThread.state>3), {func.value},{
			{
				func.value;
			}.fork
		})
	}
}