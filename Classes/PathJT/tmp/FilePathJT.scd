/*
(root/) subfolder(s)/ (dir/) entries
entries is een array van orderedfiles of een array van OrderedFileSystems
*/
FilePathJT : PathJT {
	prPath {arg argpath;
		arrayOfPathJTs=[];
		//---------------------------------- PATHNAME info
		path=argpath;
		if (file.class==File, {file.close});
		file=File(path, "r");
		//path=path.withTrailingSlash;
		pathname=PathName(path);
		name=pathname.fileName;
		extension=name.extension;
		//----------------------------------
	}
	path_ {arg argpath;
		if (File.exists(argpath), {this.prPath(argpath)},{
			("file "++argpath++"does not exist").postln;
		});
	}
	getData {

	}
	//------------------------------------------ FILE ACTIONS
	store {arg i;

	}
	restore {arg i;

	}
	save {arg i;
		var tmppath;
		if (i==nil, {this.prSave},{
			tmppath=root.entries[i];
			if (tmppath!=nil, {tmppath=tmppath.fullPath; this.prSave(tmppath);})
		});
	}

	load {arg i;
		var tmppath;
		if (i==nil, {this.prSave},{
			tmppath=root.entries[i];
			if (tmppath!=nil, {tmppath=tmppath.fullPath; this.prLoad(tmppath,i);})
		});
	}
	//------------------------------------------ FILE PRIVATE
	prLoad {arg argpath, argindex;
		data=(argpath??{path}).load;
		root.data[(argindex??{index})]=data;
	}
	prSave {arg argpath;
		file=File((argpath??{path}), "w");
		file.write(data);
		file.close;
		this.prStore;
	}
	prStore {arg argindex;
		root.data[(argindex??{index})]=data;
	}
	prRestore {arg argindex;
		data=root.data[(argindex??{index})];
	}
}