/*
het gaat mis als de folder of path leeg is...
bij loop en 'verkeerde' samplerate gaat het bij de tweede (en daaropvolgende) loops mis qua afspeelsnelheid!
er gaat soms nog iets mis met ff, vooral als je een nieuwe sample laadt....
*/
PlayerJT : JT {
	var <isPlaying, <isPaused, <sampleRate, <bufferIsClosed;
	var <buffer, <bufnum, <fadeIn, <fadeOut;
	var <>stopPlayingFunc, <>startPlayingFunc, <>pathFunc;
	var <time, <duration, <numFrames;
	var <paths, <fileNames, <>startFrame, <>endFrame, <>loop;
	var <cmdNameT, <ff, <pathFunction;
	var <monitor, <monitornumChannels, <monitoroutBus, <monitoramp, <monitorserverID
	, <hasMonitor;

	*new {arg outBus, target, path, addAction=\addBefore;
		^super.new.init(outBus, target, path, addAction);
	}

	free {
		fadeOut=0.0;
		this.stopPlaying;
		if (monitor!=nil, {if (monitor.isPlaying, {monitor.free})});
		oscFunc.do(_.free);
		if (gui!=nil, {
			if (gui.hasWindow, {
				windowBounds=gui.window.bounds;
			});
			gui.close;
		});
	}

	close { this.free }

	init {arg argoutBus, argtarget, argpath, argaddAction;
		argpath=argpath??{Platform.resourceDir +/+ "sounds/a11wlk01.wav"};
		hasMonitor=false;
		monitoramp=0;
		fadeIn=0.0;
		fadeOut=0.0;
		startFrame=0;
		ff=0;
		stopPlayingFunc={};
		startPlayingFunc={};
		pathFunc={};
		isPaused=false;
		isPlaying=false;
		loop=false;
		bufferIsClosed=true;
		id=UniqueID.next;
		cmdName=('/playingTime'++id).asSymbol;
		cmdNameT=('/finished'++id).asSymbol;
		outBus=argoutBus;
		target=argtarget;
		addAction=argaddAction??{\addBefore};
		servers=this.getServer(target);
		oscFunc=();
		updateFreq=10;
		outBus=outBus.asArray.collect{|b|
			if (b.class==Bus, {
				//Array.series(b.numChannels, b.index).unbubble
				b.index
			},{b})
		};
		numChannels=2;

		pathFunction={
			var soundFile, init=false;
			var tmpIsPlaying=false;
			var tmpIsPaused=false;
			if (isPaused, {tmpIsPaused=isPaused});
			if (isPlaying, {
				tmpIsPlaying=true;
				this.stopPlaying;
			});

			soundFile=SoundFile.openRead(path);
			sampleRate=soundFile.sampleRate;
			duration=soundFile.duration;
			numFrames=soundFile.numFrames;
			soundFile.close;
			if (soundFile.numChannels!=numChannels, {
				numChannels=soundFile.numChannels;
				init=true;
				//this.initBufferAndSynthDef;
				this.makeSynthDef;

				if (monitor!=nil, {if (monitor.isPlaying, {
					monitor.free;
					this.addMonitor(monitoroutBus, monitornumChannels, monitoramp
						, monitorserverID);
				})});

			});
			numChannels=soundFile.numChannels;
			startFrame=0;
			endFrame=numFrames;
			this.allocBuffer;
			this.cueSoundFile(startFrame, false);
			bufferIsClosed=false;
			isPaused=tmpIsPaused;

			if (tmpIsPlaying, {
				if (isPaused.not, {
					this.startPlaying
				})
			});
			pathFunc.value(this);
		};
		this.path_(argpath);
	}

	initBufferAndSynthDef {
		this.allocBuffer;
		this.makeSynthDef;
	}

	path_ { arg name;
		var tmpIsPlaying=false;
		var soundFile, init=false;
		if (File.exists(name), {
			if (name.isFolder, {
				if (PathName(name).entries.size>0, {
					name=PathName(name).entries[0].fullPath
				},{
					name=nil
				});
			});
			path=name;
			if (path!=nil, {
				if (this.isThreaded, {
					pathFunction.value
				},{
					{pathFunction.value}.fork
				});
			});
		},{
			"file " ++ name ++ " does not exist!".postln;
		});
	}

	allocBuffer {
		ff=ff+1%2;
		buffer=servers.collect{|server, serverID|
			2.collect{
				var bufnum=if (buffer!=nil, {buffer[serverID][ff].bufnum},{nil});
				var b=Buffer.alloc(server, 262144, numChannels, bufnum:bufnum);
				server.sync;
				b
			}
		}
	}

	cueSoundFile {arg value, flipflop=true;
		startFrame=value??{startFrame};
		if (flipflop, {
			ff=ff+1%2;
		});
		servers.do{|server,serverID|
			buffer[serverID][ff].close;
			bufferIsClosed=false;
			buffer[serverID][ff].cueSoundFile(path, startFrame);
			//server.sync;
		}
	}

	makeSynthDef {
		servers.do{|server, serverID|
			synthDef=(\Player++id++serverID).asSymbol;
			SynthDef(synthDef, {arg bufnum, gate=1.0, fadeIn=0.0, fadeOut=0.0
				, updateFreq=1, rate=1.0, startFrame=0, endFrame=480000;
				var in;
				var tick;
				var timer, frame;
				var env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut),gate,doneAction:2);
				in=VDiskIn.ar(numChannels, bufnum, BufRateScale.kr(bufnum))*env;
				if (serverID==0, {
					tick = Impulse.kr(updateFreq);
					//timer = PulseCount.kr(tick) - 1;
					frame=Line.ar(startFrame
						, endFrame
						, (endFrame-startFrame) * SampleDur.ir
						* BufRateScale.ir(bufnum).reciprocal
					);
					SendReply.kr(tick, cmdName, frame, id);
					SendReply.kr(Done.kr(frame), cmdNameT);
				});
				if (outBus[serverID].asArray.size<2, {
					Out.ar(outBus.asArray.wrapAt(serverID).unbubble, in)
				},{
					outBus[serverID].collect{|outBus,i|
						Out.ar(outBus, in[i]);
					};
				})
			}).add;//.load(server);
			server.sync;
		}
	}
	/*
	fadeIn_ {arg value;
	fadeIn=value;
	}

	fadeOut_ {arg value;
	fadeOut=value;
	if (synth.isPlaying, {synth.set(\fadeOut, fadeOut)});
	}
	*/
	updateFreq_ {arg value;
		updateFreq=value;
		synth.do{|synth|
			if (synth.isPlaying, {synth.set(\updateFreq, updateFreq)});
		};
	}

	restartPlaying {arg startF, endF, loopFlag;


	}

	startPlaying {arg startF, endF, loopFlag;
		if (isPlaying.not, {
			{
				var waitTime, restWaitTime, steps, stepTime=0.1;
				if (startF!=nil, {
					if (startF!=startFrame, {
						this.cueSoundFile(startF)
					});
				});
				if (bufferIsClosed, { this.cueSoundFile(startFrame) });
				isPlaying=true;
				if (endF!=nil, {if (endF!=endFrame, {endFrame=endF})});
				if (endFrame!=startFrame, {
					synth=servers.collect{|server,serverID|
						var synthDef=(\Player++id++serverID).asSymbol;
						Synth(synthDef, [\bufnum, buffer[serverID][ff].bufnum
							, \fadeIn, fadeIn, \fadeOut, fadeOut
							, \updateFreq, updateFreq
							, \startFrame, startFrame, \endFrame, endFrame
							//, \rate, sampleRate/server.sampleRate
						]
						, target.asArray[serverID]
						, addAction).register;
					};
					startPlayingFunc.value(this);
					time=Main.elapsedTime;

					oscFunc[\finished]=OSCFunc({
						this.stopPlaying(loop)
					}, cmdNameT, servers[0].addr).oneShot;
				},{
					"WARNING: startTime is equal to endTime".postln
				})
			}.fork
		});
	}

	pausePlaying {arg flag=true;
		if (synth.collect{|syn| syn.isPlaying.binaryValue}.sum>0, {
			isPaused=flag;
			synth.do{|syn| syn.run(flag.not)};
		});
		/*
		if (flag, {
		},{
		})
		*/
	}

	resumePlaying { this.pausePlaying(false) }

	stopPlaying {arg rePlayflag=false;
		oscFunc.do(_.free);
		if (isPlaying, {
			{
				if (synth!=nil, {
					if (synth.collect{|syn| syn.isPlaying.binaryValue}.sum>0, {
						synth.do{|syn| syn.set(\gate, 0)};
						//fadeOut.wait;
					});
				});
				isPlaying=false;
				isPaused=false;
				this.cueSoundFile;
				if (rePlayflag, {
					this.startPlaying
				},{
					stopPlayingFunc.value(this);
				});
			}.fork;
		})
	}

	setMonitor {arg value;
		monitoramp=value;
		if (monitor!=nil, {if (monitor.isPlaying, {monitor.set(\amp, value)})});
	}

	removeMonitor {
		hasMonitor=false;
		monitor.free;
		monitor=nil;
	}

	addMonitor {arg bus=0, noc=2, amp=0.0, serverID=0;
		monitornumChannels=noc;
		hasMonitor=true;
		monitoroutBus=bus;
		monitoramp=amp;
		monitorserverID=serverID;

		monitor={arg amp=0.0;
			Out.ar(bus,
				if (noc==2, {
					Splay.ar(In.ar(outBus.asArray[serverID].asArray.minItem, numChannels)
						, 1, amp.lag(0.1))
				},{

					SplayAz.ar(noc
						, In.ar(outBus.asArray[serverID].asArray.minItem, numChannels)
						, 1, amp.lag(0.1))

				})
			)
		}.play(target.asArray[serverID], outBus, 0.02, \addAfter, [\amp, amp]);
		monitor.register;

		^this
	}

	makeGUI {arg parent, bounds=150@20, margin=0@0, gap=0@0, userCanClose=true;
		gui=PlayerJTGUI(this, parent, bounds, margin, gap, userCanClose);
		^gui
	}
}


PlayerJTGUI : GUIJT {
	var <recorder;
	var <paths, <fileNames;

	*new {arg recorder, parent, bounds, margin, gap, userCanClose;
		^super.new.init(recorder, parent, bounds, margin, gap, userCanClose);
	}

	pathsAndFilesNames {
		paths=PathName(classJT.path.dirname).entries;
		fileNames=paths.collect{|pathname| pathname.fileNameWithoutExtension};
		paths=paths.collect{|p| p.fullPath};
		views[\fileNames].items_(fileNames);
	}

	changePath {
		views[\sf].soundfile=SoundFile.openRead(classJT.path);
		views[\sf].read(0, views[\sf].soundfile.numFrames, closeFile:true);
		views[\sf].refresh;
		views[\sf].timeCursorPosition=0;
		views[\sf].selectNone(0);
		[\start, \end].do{|key,i|
			views[key].controlSpec.maxval=classJT.duration;
			views[key].value=[0, classJT.duration][i]
		};
		if ((path.dirname!=classJT.path.dirname)
			|| (fileNames.size!=PathName(classJT.path.dirname).entries.size)
			, {
				this.pathsAndFilesNames;
		});
		path=classJT.path;
		views[\fileNames].value_(paths.indexOfEqual(classJT.path));
	}

	updateFrames {
		if (classJT.isPlaying, {
			if (classJT.isPaused.not, {
				classJT.stopPlaying(false)//was true, this is safer for now
			},{
				classJT.stopPlaying(false);
				views[\pauseB].value_(0);
			})
		},{
			classJT.cueSoundFile(classJT.startFrame)
		});
	}

	init {arg argclassJT, argparent, argbounds, argmargin, arggap, arguserCanClose;
		var startFunc, stopFunc, pathFunc;
		classJT=argclassJT;
		parent=argparent;
		bounds=argbounds;
		argmargin=argmargin;
		arggap=arggap;
		freeOnClose=true;
		userCanClose=arguserCanClose??{true};
		this.initAll;
		path=classJT.path;

		parent.resize=5;

		startFunc={{views[\playB].value_(1)}.defer};
		stopFunc={{
			views[\playB].value_(0);
			views[\sf].timeCursorPosition=classJT.startFrame;
		}.defer};
		pathFunc={
			{
				//if (refreshFlag, {this.pathsAndFilesNames});
				this.changePath
			}.defer;
		};

		views[\playB]=Button(parent, (bounds.x/3-gap.x)@bounds.y)
		.states_([[\play],[\play,Color.white,Color.green]]).action_{|b|
			views[\pauseB].value_(0);
			if (b.value==1, {
				classJT.startPlaying
			},{
				classJT.stopPlaying
			})
		}.font_(font);
		views[\pauseB]=Button(parent, (bounds.x/3-gap.x)@bounds.y)
		.states_([[\pause],[\pause,Color.black,Color.yellow]]).action_{|b|
			if (b.value==1, {
				classJT.pausePlaying
			},{
				classJT.resumePlaying
			})
		}.font_(font);
		views[\loopB]=Button(parent, (bounds.x/3-gap.x)@bounds.y)
		.states_([[\loop],[\loop,Color.white,Color.blue]]).action_{|b|
			classJT.loop=b.value>0
		}.font_(font);

		views[\load]=Button(parent, (bounds.x*0.2-gap.x)@bounds.y)
		.states_([[\load]]).action_{|b|
			Dialog.openPanel({arg argpath;
				path=argpath;
				classJT.path_(path);
				//this.pathsAndFilesNames;
				//this.changePath;
			}, path: classJT.path.dirname);
		}.font_(Font(font.name, font.size*0.75));

		views[\fileNames]=PopUpMenu(parent, (bounds.x*0.8-gap.x)@bounds.y)
		//.items_(fileNames)
		.action_{|f|
			classJT.path_(paths[f.value]);
			//this.changePath
		}
		.font_(Font(font.name, 10));

		[\start, \end].collect{|key,i|
			views[key]=EZNumber(parent, (bounds.x*0.3-gap.x)@bounds.y, nil
				, ControlSpec(0, classJT.duration, 0, 0.1), {|sl|
					views[\sf].setSelection(0
						, [views[\start].value, views[\end].value-views[\start].value]
						*classJT.sampleRate);
					if (i==0, {
						classJT.startFrame=sl.value*classJT.sampleRate;
					},{
						classJT.endFrame=sl.value*classJT.sampleRate;
					});
					this.updateFrames;
			});
		};
		views[\end].value_(classJT.duration);
		views[\playTime]=StaticText(parent, (bounds.x*0.4-gap.x)@bounds.y)
		.string_(0.asTimeString.copyRange(1,9))
		.align_(\right).font_(Font(font.name, bounds.y*0.5));
		if (classJT.hasMonitor, {
			EZSlider(parent, bounds.x@bounds.y, \monitor, \amp.asSpec, {|ez|
				classJT.setMonitor(ez.value);
			}, classJT.monitoramp, false, 50);
		});
		views[\sf]=SoundFileView(parent, bounds.x@(bounds.y*5));
		views[\sf].resize=5;
		views[\sf].gridOn = false;
		views[\sf].timeCursorOn=true;
		views[\sf].timeCursorColor=Color.white;
		views[\sf].setSelectionColor(0, Color.red);
		views[\sf].mouseUpAction = {
			var frames;
			frames=(views[\sf].selections[views[\sf].currentSelection].integrate);
			if (frames[0]==frames[1], {
				frames[1]=classJT.numFrames;
				if (classJT.loop, {
					views[\sf].setSelection(0, [frames[0], frames[1]-frames[0]]);
				},{
					views[\sf].selectNone(0);
				})
			});
			classJT.startFrame=frames[0];
			classJT.endFrame=frames[1];
			frames.do{|val,i|
				views[[\start, \end][i]].value_(val/classJT.sampleRate);
			};
			this.updateFrames;
		};
		oscGUI[\playTime]=OSCFunc({arg msg;
			{
				views[\playTime].string_(
					(msg[3]/classJT.sampleRate).asTimeString.copyRange(1,9)
				);
				views[\sf].timeCursorPosition=msg[3];
			}.defer
		}, classJT.cmdName, classJT.servers[0].addr);

		this.pathsAndFilesNames;
		this.changePath;

		classJT.startPlayingFunc=classJT.startPlayingFunc.addFunc(startFunc);
		classJT.stopPlayingFunc=classJT.stopPlayingFunc.addFunc(stopFunc);
		classJT.pathFunc=classJT.pathFunc.addFunc(pathFunc);

		window.onClose=window.onClose.addFunc({
			oscGUI.do{|osc| osc.free};
			classJT.startPlayingFunc.removeFunc(startFunc);
			classJT.stopPlayingFunc.removeFunc(stopFunc);
			classJT.pathFunc.removeFunc(pathFunc);
		});

		parent.rebounds;
		if (hasWindow, {window.rebounds});
		[\end, \start].collect{|k| viewsPreset[k]=views[k]};
	}
}
