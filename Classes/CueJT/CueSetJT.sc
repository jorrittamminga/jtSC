CueViewJT : CueJT {}
CueEZGuiJT : CueJT {}
CueFunctionJT : CueJT {
	var <document, documents;
	*new {arg object, name, enviroment;
		^super.basicNew(object, name, enviroment)
	}
	store {
		var file, pathName;
		pathName=path++fileName.asString++"."++extension;
		if (File.exists(pathName), {
			//Document.open(path);
			value=path.load;
		},{
			value={arg e; };
			file=File(pathName, "w");
			file.write(value.asCompileString);
			file.close;
			if (document.class==Document, {document.close});
			document=Document.open(path);
			documents=documents.add(document);
		});
		funcList[cueID]=this.makeFunc(value.deepCopy);
		func[\store].value(this);
		//this.storeAction;
	}
	makeFunc {arg val;
		^val
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CueFunctionJTGUI(this, parent, bounds)}.defer
	}
}
CueEventJT : CueJT {}
