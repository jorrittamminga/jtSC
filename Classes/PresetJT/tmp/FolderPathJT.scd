/*
(root/) subfolder(s)/ (dir/) entries
entries is een array van FolderPathJT of een array van FilePathJT
*/
FolderPathJT : PathJT {
	prPath {arg argpath;
		arrayOfPathJTs=[];
		//---------------------------------- PATHNAME info
		path=argpath;
		pathname=PathName(path);
		name=pathname.folderName;
		entries=pathname.entries;
		namesEntries=entries.collect{|pathName|
			if (pathName.isFolder, {
				pathName.folderName
			},{
				pathName.fileName
			});
		};
		namesEntriesWithoutNumbers=namesEntries.collect{|name|
			name.split($_).copyToEnd(1).join($_);
		};
		namesEntriesWithoutNumbersWithoutExtensions=namesEntriesWithoutNumbers.collect{|fileName|
			fileName.reverseDo({ | char, i |
				if(char == $.,{
					fileName=fileName.copyRange(0,fileName.size - (i + 2))
				})
			});
			fileName
		};
		//---------------------------------- recursion
		if (entries.size>0, {
			entry=if (entries[0].isFolder, {
				var o=FolderPathJT(entries[0].fullPath, this);
				arrayOfPathJTs=this.deepArrayOfPathJTs(o);
				o
			},{
				this.getDataFromEntries;
				FilePathJT(entries[0].fullPath, this);
			});
		},{
			//what if the folder is empty? force it to create an empty file?
			nil
		});
		//----------------------------------
	}
	path_{arg argpath;
		this.prPath(argpath);
		if (arrayOfPathJTs.last.class==FilePathJT, {
			depthDir=(arrayOfPathJTs.size-2).max(0);
			arrayOfPathJTs[depthDir].isDir=true;
			dir=arrayOfPathJTs[depthDir];
		},{
			depthDir=arrayOfPathJTs.size-1;
			arrayOfPathJTs.last.isDir=true;
			dir=arrayOfPathJTs.last;
		});
	}
	getDataFromEntries {
		data=entries.collect{|pathName,i|
			pathName.fullPath.load
		};
		^data
	}
}

RootPathJT : FolderPathJT {
	var <cueList;

	init {arg argpath, argroot;
		isMaster=true;
		isRoot=true;
		root=this;
		rootOfAll=this;
		this.path_(argpath.withTrailingSlash, argroot);
		forceCreateNewFile=true;
	}
	updateCueList {

	}
}
