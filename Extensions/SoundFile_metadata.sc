+ SoundFile {
	*writeMetaData {arg pathName, title, album, artist, date, copyright, license, comment;
		var file;
		file = this.new(pathName);
		if(file.openRead(pathName)) {
			file.writeMetaData(title, album, artist, date, copyright, license, comment);
			file.close;
		} {
			^nil
		};
	}

	writeMetaData {arg title, album, artist, date, copyright, license, comment;
		var string="/usr/local/bin/sndfile-metadata-set ";
		if (title.class==String, {string=string++"--str-title \""++title++"\" "});
		if (album.class==String, {string=string++"--str-album \""++album++"\" "});
		if (artist.class==String, {string=string++"--str-artist \""++artist++"\" "});
		if (date.class==String, {string=string++"--str-date \""++date++"\" "});
		if (copyright.class==String, {string=string++"--str-copyright \""++copyright++"\" "});
		if (license.class==String, {string=string++"--str-license \""++license++"\" "});
		if (comment.class==String, {string=string++"--str-comment \""++comment++"\" "});
		string=string++this.path.unixPath;
		string.postln;
		//^string.runInTerminal
		^string.unixCmd
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