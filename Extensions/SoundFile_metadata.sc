+ SoundFile {
	*writeMetaData {arg pathName, metadata=(), action;
		var file;
		file = this.new(pathName);
		if(file.openRead(pathName)) {
			file.writeMetaData(metadata.deepCopy, action);
			file.close;
		} {
			^nil
		};
	}

	writeMetaData {arg metadata=(), action;
		var string="/usr/local/bin/sndfile-metadata-set ";
		var keys=[\title, \album, \artist, \date, \copyright, \license, \comment];
		/*
		if (metadata[\title].class==String, {string=string++"--str-title \""++metadata[\title]++"\" "});
		if (metadata[\album].class==String, {string=string++"--str-album \""++metadata[\album]++"\" "});
		if (metadata[\artist].class==String, {string=string++"--str-artist \""++metadata[\artist]++"\" "});
		if (metadata[\date].class==String, {string=string++"--str-date \""++metadata[\date]++"\" "});
		if (metadata[\copyright].class==String, {string=string++"--str-copyright \""++metadata[\copyright]++"\" "});
		if (metadata[\license].class.class==String, {string=string++"--str-license \""++metadata[\license]++"\" "});
		if (metadata[\comment].class==String, {string=string++"--str-comment \""++metadata[\comment]++"\" "});
		*/
		metadata.keysValuesDo{|key,val|
			if (keys.includes(key), {
				string=string++"--str-"++key++" \""++val++"\" ";
			},{
				("metadata " ++key++" is not supported by libsndfile...").postln;
			})
		};
		string=string++this.path.unixPath;
		string.postln;
		//^string.runInTerminal
		^string.unixCmd(action)
	}

	*convertToMP3 {arg pathName, pathNameMP3, quality=0, metadata=(), action;
		var file;
		file = this.new(pathName);
		if(file.openRead(pathName)) {
			file.convertToMP3(pathNameMP3, quality, metadata, action);
			file.close;
		} {
			^nil
		};
	}

	convertToMP3 {arg pathName, quality=0, metadata=(), action;
		var cmd;
		if (pathName==nil, {
			pathName=this.path.dirname++"/"++PathName(this.path).fileNameWithoutExtension++".mp3";
		});
		cmd=("/usr/local/bin/ffmpeg -y -i "++this.path.unixPath++" -codec:a libmp3lame -qscale:a "++quality++" ");
		metadata.sortedKeysValuesDo{|key, val| cmd=cmd++"-metadata "++key.asString++"=\""++val++"\" "};
		cmd=cmd++ pathName.unixPath;
		cmd.postln;
		^cmd.unixCmd(action)
	}

}
/*
sndfile-metadata-set --str-artist "Wim Henderickx" /Users/jorrit/Dropbox/Henderickx-Tamminga/VISIONI_ed_ESTASI/soundfiles/02_Vision_I__Suffering_A1.wav

--str-comment            Set the metadata comment.
--str-title              Set the metadata title.
--str-copyright          Set the metadata copyright.
--str-artist             Set the metadata artist.
--str-date               Set the metadata date.
--str-album              Set the metadata album.
--str-license            Set the metadata license.

"sndfile-metadata-set --str-artist \"Wim Henderickx\" /Users/jorrit/Dropbox/Henderickx-Tamminga/VISIONI_ed_ESTASI/soundfiles/02_Vision_I__Suffering_A1.wav".unixCmd
*/