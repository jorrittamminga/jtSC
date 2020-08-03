+ File {
	*checkPath {arg pathName, make=true;
		var name, path, dirname, basename, isFolder=false, flag=false, file;

		path=thisProcess.nowExecutingPath??{PathName("~/Desktop/test").fullPath};
		path=path.dirname;

		if (pathName.class==String, {
			if (File.exists(pathName), {
				flag=true;
				isFolder=PathName(pathName).isFolder;
				if (isFolder, {
					dirname=pathName;
				},{
					dirname=pathName.dirname;
					basename=pathName.basename;
				});
			},{
				if (pathName[0]!=$/, {
					pathName="/"++pathName;
				});
				if (pathName.contains("."), {
					isFolder=false;
				},{
					isFolder=true;
				});
			});
		});

		if (flag.not&&make, {
			pathName=path++pathName;
			if (File.exists(pathName), {
				flag=true
			},{
				if (isFolder, {
					pathName.mkdir;
				},{
					file=File(pathName, "w");
					file.close;
				});
			});
		});
		if (isFolder, {if (pathName.last!=$/, {pathName=pathName++"/"})});
		^pathName
	}
}

+ String {
	checkPath {arg make=true;
		^File.checkPath(this, make)
	}
}