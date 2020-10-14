+PathName {
	/*
	deepFolders {
	var folders=[];
	this.deepFiles.do{|path|
	var pathOnly=path.pathOnly;
	if (folders.includesEqual(pathOnly).not, {
	folders=folders.add(pathOnly)
	});
	};
	^folders.collect{|i| PathName(i)}
	}
	*/
	deepFolders {arg flat=true;
		var out;
		out=this.entries.collect({ | item |
			if( (item.isFolder) && (item.entries.size>0), {
				item.deepFolders
			},{
				item
			})
		});
		^if (flat, {out.flat},{out})
	}
	/*
	deepFoldersAsEvent {arg event=();
		this.entries.do({ | item |
			var key=item.folderName.asSymbol;
			if (event[key]==nil, {event[key]=0});
			if( (item.isFolder) && (item.entries.size>0), {
				event[key]=();
				item.deepFoldersAsEvent(event[key])
			},{
				item
			})
		});
		^event
	}
	*/
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

}