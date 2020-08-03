/*
- {|p| p.interpolateOnce(\amp, 10)};
-
- als er een preset ge-delete of ge-add wordt zorg dan de als de scripteditor openstaat het huidige script zichtbaar is
- zorg dat saveScript, loadScript, restoreScript en storeScript goed werken, zowel qua {} als que GUI
- vervang restoreS met loadS en viceversa
- maak een scriptupdate
- maak lege scripts voor het aantal presets?
- rename scripts als de presets omgehusseld worden
- wat te doen met foldernamen als er ook scripts bij de slaves komen? script_slavename oid?
*/
+ PresetSystem {
	addScripts {arg argslaves=false;
		this.initScript;
		//onderstaande if's kunnen ws eleganter.....
		if ((argslaves==false).not, {
			if (slaves.size>0, {
				argslaves=argslaves??{(0..(slaves.size-1))};
				if (argslaves==nil, {
					slaves.do{|ps|
						ps.initScript
					}
				})
			})
		});
		this.restoreS
	}

	removeScripts {}

	disableScripts {}
	enableScripts {}

	initScript {//arg argiets;
		canScript=true;
		this.scriptpath_;
		this.makeScriptArray;
		//onderstaande functie is nog wat lomp.....
		functions[\renumber]=functions[\renumber].addFunc({|p|
			var split=fileNameWithoutExtension.split($_)[0].interpret.asInteger, old, new;
			PathName(scriptpath).entries.do{|path|
				if (path.fileNameWithoutExtension.split($_)[0].interpret.asInteger==split, {
					old=path.fullPath;
					new=scriptpath++fileName;
				});
			};
			if (new!=nil, {
				File.copy(old, new);
				File.delete(old);
			});
		});
		functions[\update]=functions[\update].addFunc({|p|
			this.makeScriptArray;
			//{this.updateScriptEditor}.defer;
		});

		functions[\delete]=functions[\delete].addFunc({arg fileName;
			this.deleteFile(scriptpath++fileName);
			//this.updateScriptEditor;
		});
		functions[\rename]=functions[\rename].addFunc({arg fileName1, fileName2;
			this.renameFile(scriptpath++fileName1,scriptpath++fileName2);
			//this.updateScriptEditor;
		});
		functions[\path]={this.restoreS(0)};
		nextAction={
			this.restoreS;
		};
		prevAction={
			this.restoreS;
		};
	}

	scriptpath_ {
		scriptpath=path++"scripts/";
		if (File.exists(scriptpath).not, {
			File.mkdir(scriptpath);
		});
	}

	makeScriptArray {
		scripts=Array.newClear(size);
		PathName(scriptpath).entries.do{|p|
			var i=p.fileNameWithoutExtension.split($_)[0].interpret.asInteger;
			var file=File(p.fullPath, "r");
			var code=file.readAllString;
			file.close;
			/*
			if (code[0]!="{", {
			code=code.addFirst("{");
			code=code.add("}");
			});
			*/
			scripts[i]=(code.interpret);
		};
	}

	addScript {arg i, open=true;//argscript={"poepen met drollen"};
		var file, filepath;
		this.index_(i);
		filepath=scriptpath++fileName;
		if (File.exists(filepath).not, {
			file=File(filepath, "w");
			file.close
		});
		if (open, {("open " ++ filepath.asUnixPath).unixCmd});
	}
	storeScript {arg i, argscript;
		this.index_(i);
		script=argscript??{script};
		scripts[index]=script.interpret;
	}
	saveScript {arg i, argscript;
		var file, filepath;
		this.index_(i);
		script=argscript??{script};
		filepath=scriptpath++fileName;
		//script=argscript??{script};
		file=File(filepath, "w");
		//			script=file.readAllString.interpret;
		file.write(script);//.asCompileString als het geen string is maar een function
		file.close;
		if (preLoad, {
			scripts[index]=script.interpret;
		});
	}
	/*
	openScript {arg i;
	var scriptfullPath;
	scriptfullPath=scriptpath++fileName;
	if (File.exists(scriptfullPath), {
	("open " ++ scriptfullPath.asUnixPath).unixCmd;
	//guis[\ScriptEditor]=TextView
	});
	}
	*/
	loadScript {arg i;
		var scriptfullPath;
		this.index_(i);
		scriptfullPath=scriptpath++fileName;
		script=scriptfullPath.load;
		if (preLoad, {scripts[index]=script});
	}

	loadStoreS {arg i;//action which are similar between loadS and restoreS
		var script, scriptfullPath;
		this.index_(i);
		//this.stopAll;
		this.getValues;
		slaves.do{|ps| ps.getValues};
	}

	loadS {arg i, t, curve=0;
		var script, scriptfullPath;
		this.index_(i);
		//this.stopAll;
		this.getValues;
		slaves.do{|ps| ps.getValues};
		scriptfullPath=scriptpath++fileName;
		{
			if (File.exists(scriptfullPath), {
				script=scriptfullPath.load;
				if (script.class==Function, {script.value(this)});
			});
			if (interpolate==1, {
				this.loadI(nil, t, curve, false)
			},{
				this.load(nil)
			});
			{
				//dit moet dit in de PresetGUI, bij functions[\restoreS] oid
				this.updateScriptEditor
			}.defer;
		}.fork
	}
	restoreScript {arg index=0;
		{
			script=scripts[index];
			script.value(this);
			{
				//dit moet dit in de PresetGUI, bij functions[\restoreS] oid
				this.updateScriptEditor;
			}.defer;
		}.fork;
	}
	restoreS {arg i, t, curve=0;
		var scriptfullPath;
		this.index_(i);//dit gebeurt ook al in restoreI en restore, waarom dan ook hier???
		//this.stopAll;
		this.getValues;
		slaves.do{|ps| ps.getValues};

		{
			script=scripts[index];
			script.value(this);
			if (interpolate==1, {
				this.restoreI(nil, t, curve, false);
			},{
				this.restore(nil)
			});
			{
				//dit moet dit in de PresetGUI, bij functions[\restoreS] oid
				this.updateScriptEditor;
			}.defer;
		}.fork(AppClock);
	}
	updateScriptEditor {
		if (windows[\scriptEditorGUI]!=nil, {//isInFront (of hoe heet dat)
			guis[\ScriptEditor].string_(scripts[index].asCompileString)
		});
	}
	doScript{//arg argscript;
		script.value(this)
	}
	//--------------------------------------------------- GUI, kan dus in PresetGUI
	scriptEditorGUI {arg bounds=350@350;
		var w, buttonBounds=(bounds.x/3-4)@20;
		w=Window("Script Editor", Rect(0,0,bounds.x+8, bounds.y+28)).front;
		w.addFlowLayout;
		w.alwaysOnTop_(true);
		w.onClose_{
			//scriptEditor
			//guis[\scriptEditorGUI]=nil;
		};
		Button(w, buttonBounds).states_([ ["save"]]).action_{
			script=guis[\ScriptEditor].string;
			this.saveScript(index);
		};
		Button(w, buttonBounds).states_([ ["restore"]]).action_{
			guis[\ScriptEditor].string_(scripts[index]);
		};
		Button(w, buttonBounds).states_([ ["execute"]]).action_{
			guis[\ScriptEditor].string.interpret.value(this)
		};
		guis[\ScriptEditor]=TextView(w, bounds)
		.string_(scripts[index].asCompileString)//moet .asCompileString?
		.resize_(5)
		;
		guis[\ScriptEditor].tabWidth=20;
		guis[\ScriptEditorWindow]=w;
		guis[\ScriptEditor].syntaxColorize;
		windows[\scriptEditorGUI]=w;
		w.front;
		w.view.keyDownAction={arg doc, char, mod, unicode, keycode, key;
			if ((mod==1048576) && (keycode==1), {
				script=guis[\ScriptEditor].string;
				this.saveScript(index);
			});
			if ((mod==262144) && (keycode==1), {
				script=guis[\ScriptEditor].string;
				this.saveScript(index);
			});
			//[ 262144, 1 ]
		};
	}

	//--------------------------------------------------- CONTROL

}