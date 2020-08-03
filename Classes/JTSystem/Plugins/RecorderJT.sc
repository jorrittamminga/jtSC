RecorderJT : JT {
	var <>sampleFormat, <>headerFormat, <isRecording, <isPaused;
	var <buffer, <bufnum, <fadeIn, <fadeOut;
	var <>stopRecordingFunc, <>startRecordingFunc, <>pathFunc;
	var <autoFileName;
	var <time;

	*new {arg inBus, target, path, sampleFormat="int24", headerFormat="AIFF";
		^super.new.init(inBus, target, path, sampleFormat, headerFormat);
	}

	free {
		fadeOut=0.0;
		this.stopRecording;
		if (gui!=nil, {
			if (gui.hasWindow, {
				windowBounds=gui.window.bounds;
			});
			gui.close;
		});
	}

	close { this.free }

	init {arg arginBus, argtarget, argpath, argsampleFormat, argheaderFormat;
		fadeIn=0.0;
		fadeOut=0.0;
		stopRecordingFunc={};
		startRecordingFunc={};
		pathFunc={};
		isPaused=false;
		isRecording=false;
		id=UniqueID.next;
		cmdName=('/recordingTime'++id).asSymbol;
		inBus=arginBus;
		target=argtarget;
		addAction=\addAfter;
		path=argpath??{thisProcess.platform.recordingsDir};
		path=path.checkPath;
		if (path.isFolder, {
			fileName="";
			folderName=path;
			autoFileName=true;
		},{
			folderName=path.dirname++"/";
			fileName=path.basename;
			autoFileName=false;
		});
		sampleFormat=argsampleFormat;
		headerFormat=argheaderFormat;
		server=this.getServer(target).unbubble;
		updateFreq=10;
		inBus=inBus.asArray.collect{|b|
			if (b.class==Bus, {
				Array.series(b.numChannels, b.index).unbubble
			},{b})
		}.flat;
		numChannels=inBus.asArray.size;
		this.allocBuffer;
		this.makeSynthDef;
	}

	fileName_ {arg name;
		fileName=name;
		path=folderName ++ fileName ++ "." ++ headerFormat.toLower;
	}

	path_ { arg name;
		path=name;
	}

	autoFileName_ {arg flag;
		//if ((flag==true) && (fileName=="")
		autoFileName=flag;
	}

	autoPath {
		path=folderName ++ fileName ++ Date.localtime.stamp ++ "." ++ headerFormat.toLower;
		pathFunc.value(this);
	}

	allocBuffer {
		buffer=Buffer.alloc(server, 262144, numChannels, bufnum: bufnum);
	}

	makeSynthDef {
		synthDef=(\Recorder++id).asSymbol;
		SynthDef(synthDef, {arg bufnum, gate=1.0, fadeIn=0.0, fadeOut=0.0, updateFreq=1;
			var in;
			var tick = Impulse.kr(updateFreq);
			var timer = PulseCount.kr(tick) - 1;
			var env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut),gate,doneAction:2);
			//var doneAction = if(duration <= 0, 0, 2);
			//Line.kr(0, 0, duration, doneAction:doneAction);
			SendReply.kr(tick, cmdName, timer/updateFreq, id);
			in=inBus.collect{|inBus| In.ar(inBus) };
			DiskOut.ar(bufnum, in*env)
		}).send(server);
	}

	fadeIn_ {arg value;
		fadeIn=value;
	}

	fadeOut_ {arg value;
		fadeOut=value;
		if (synth.isPlaying, {synth.set(\fadeOut, fadeOut)});
	}

	updateFreq_ {arg value;
		updateFreq=value;
		if (synth.isPlaying, {synth.set(\updateFreq, updateFreq)});
	}

	startRecording {
		{
			isRecording=true;
			if (autoFileName, {this.autoPath});
			buffer.write(path, headerFormat, sampleFormat, 0, 0, true);
			server.sync;
			bufnum=buffer.bufnum;
			("Recording audio file " ++ path ++ "\n").postln;
			synth=Synth(synthDef, [\bufnum, bufnum, \fadeIn, fadeIn, \fadeOut, fadeOut
				, \updateFreq, updateFreq]
			, target
			, addAction).register;
			startRecordingFunc.value(this);
			time=Main.elapsedTime;
		}.fork
	}

	pauseRecording {arg flag=true;
		if (synth.isPlaying, {
			isPaused=flag;
			synth.run(flag.not)
		});
	}

	resumeRecording { this.pauseRecording(false) }

	stopRecording {
		if (isRecording, {
			{
				if (isPaused, {this.resumeRecording;});
				if (synth.isPlaying, {
					synth.set(\gate, 0);
					fadeOut.wait;
				});
				isRecording=false;
				buffer.close;
				buffer.free;
				server.sync;
				buffer=nil;
				stopRecordingFunc.value(this);
				this.allocBuffer;
				time=Main.elapsedTime-time;
				("recording time: " ++ time.asTimeString).postln;
				("Written audio file " ++ path ++ "\n").postln;
				//})
			}.fork;
		})
	}

	makeGUI {arg parent, bounds=150@20, margin=0@0, gap=0@0, userCanClose=true;
		gui=RecorderJTGUI(this, parent, bounds, margin, gap, userCanClose)
	}
}


RecorderJTGUI : GUIJT {
	var <recorder;

	*new {arg recorder, parent, bounds, margin, gap, userCanClose;
		^super.new.init(recorder, parent, bounds, margin, gap, userCanClose);
	}

	init {arg argclassJT, argparent, argbounds, argmargin, arggap, arguserCanClose;
		classJT=argclassJT;
		parent=argparent;
		bounds=argbounds;
		argmargin=argmargin;
		arggap=arggap;
		freeOnClose=true;
		userCanClose=arguserCanClose??{true};
		this.initAll;
		views[\recB]=Button(parent, (bounds.x/2-gap.x)@bounds.y)
		.states_([[\rec],[\rec,Color.white,Color.red]]).action_{|b|
			views[\pauseB].value_(0);
			if (b.value==1, {
				classJT.startRecording
			},{
				classJT.stopRecording;
			})
		}.font_(font);
		views[\pauseB]=Button(parent, (bounds.x/2-gap.x)@bounds.y)
		.states_([[\pause],[\pause,Color.black,Color.yellow]]).action_{|b|
			if (b.value==1, {
				classJT.pauseRecording
			},{
				classJT.resumeRecording
			})
		}.font_(font);

		views[\fileName]=TextField(parent, (bounds.x*0.6-gap.x)@bounds.y)
		.action_{|f| classJT.fileName_(f.value)}
		.font_(font);
		views[\recTime]=StaticText(parent, (bounds.x*0.4-gap.x)@bounds.y)
		.string_(0.asTimeString.copyRange(1,9))
		.align_(\right).font_(Font(font.name, bounds.y*0.5));

		views[\auto]=Button(parent, (bounds.x/3-gap.x)@bounds.y)
		.states_([[\auto],[\auto,Color.white,Color.blue]]).action_{arg b;
			classJT.autoFileName_(b.value>0)
		}.value_(classJT.autoFileName.binaryValue).font_(font);

		views[\headerFormat]=PopUpMenu(parent, (bounds.x/3-gap.x)@bounds.y)
		.items_(["AIFF", "WAV"]).action_{|p| classJT.headerFormat_(p.items[p.value])}
		.font_(font);
		views[\headerFormat].value_(
			views[\headerFormat].items.indexOfEqual(classJT.headerFormat));
		views[\sampleFormat]=PopUpMenu(parent, (bounds.x/3-gap.x)@bounds.y)
		.items_(["int16", "int24", "int32", "float"])
		.action_{|p| classJT.sampleFormat_(p.items[p.value])}.font_(font);
		views[\sampleFormat].value_(
			views[\sampleFormat].items.indexOfEqual(classJT.sampleFormat));

		oscGUI[\recTime]=OSCFunc({arg msg;
			{views[\recTime].string_(msg[3].asTimeString.copyRange(1,9))}.defer
		}, classJT.cmdName, classJT.server.addr);

		parent.rebounds;

		window.onClose=window.onClose.addFunc({oscGUI.do{|osc| osc.free}});

		if (hasWindow, {window.rebounds});
	}
}