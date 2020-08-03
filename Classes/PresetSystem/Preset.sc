/*
maak een class Preset én apart een GUI voor deze class (b.v. EZPreset oid)
én een aparte class voor presetlist (met knoppen voor next en prev) EZPresetList oid
(en EZQList, a la QLab????)
.folder -> .dirname
.store(path, values) of .store(path). if (path==filename, {poth=dirname+path}); zoiets
.restore(path, values) -> die hele morph toestand als aparte class maken
.path
.restore(path, skipMorph=true) oid
.values_(sliders) EZPreset
.objects_(guiobjects) EZPresets
.rename(old, new)
.save(newpath)//.currentpreset
.currentpath
.currentpreset
ook een variabele binnen een preset om bepaalde sliders niet op te slaan (of juist alleen bepaalde sliders)
*/
Preset {
	var <>path;
	var <>dirname, <>filename, <>preset;
	var <presetList, <presetListNamesOnly;
	var <>extension="scd";

	*new {arg path, preset;
		^super.new.init(path, preset)
	}


	init {arg argpath, argpreset;
		this.splitFileNameAndFolderName(argpath);
		preset=argpreset;
		this.updatepresetList;
	}


	store {arg argpreset, argpath;
		var file, flag=true;
		if (argpath!=nil, {
			this.splitFileNameAndFolderName(argpath);
		});
		if (argpreset!=nil, {
			preset=argpreset;
			},{
				if (preset==nil, {
					flag=false;
				});
		});
		if (flag, {
			file=File(path, "w");
			file.write(preset.asCompileString);
			file.close;
			this.updatepresetList;//alleen als het een nieuwe preset betreft
		});
	}


	restore {arg argpath;
		var argpreset, file;
		if (argpath!=nil, {
			this.splitFileNameAndFolderName(argpath);
		});
		file=File(path, "r");
		argpreset=file.readAllString.interpret;
		file.close;
		if (argpreset!=nil, {
			preset=argpreset
		});

		^preset
	}


	splitFileNameAndFolderName {arg argpath;

		if (argpath==nil, {
			if (thisProcess.nowExecutingPath!=nil, {
			argpath=thisProcess.nowExecutingPath.dirname++"/presets/"
				},{
					argpath="~/presets/".standardizePath;
			})
		});

		if (argpath.contains("/"), {
			if (PathName(argpath).isFile, {
				filename=argpath.basename;
				dirname=argpath.dirname++"/";
				},{
					dirname=argpath;
					filename="init";
			});
			},{
				filename=argpath;
				if (dirname==nil, {
					if (thisProcess.nowExecutingPath!=nil, {
						dirname=thisProcess.nowExecutingPath.dirname++"/presets/"
						},{
					dirname="~/presets/".standardizePath;
					})
				});
		});
		if( File.exists(dirname).not) {("mkdir" + dirname).unixCmd};
		path=dirname++filename++"."++extension;
	}


	updatepresetList {
		presetList=PathName(dirname).entries;
		presetListNamesOnly=presetList.collect{|path|
			path.fileNameWithoutExtension
		};
		presetList=presetList.collect(_.fullPath);
		^presetList

	}
}