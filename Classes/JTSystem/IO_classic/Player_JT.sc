/*
- gaat nog vaak mis als je op stop drukt
- noem inputbus anders.... en is het een bus of een index?
- er klopt geen zak van als ie op loop staat (verschil tussen end en stop)
- verander in VDiskIn en pas de afspeelsnelheid aan aan de sampleRate
- Player_JTMC
*/
Player_JT {
	var <>path, currentPath, <buf, <synth, inputbus, server, <window, <guis, directbus, playRoutine, isPlaying;
	var headerFormat, sampleFormat, group, fileNames, <bufnum;
	var <sampleRate, <channels, buf, <>endLoop, <>startLoop, <duration, <resolution, <>loop, directAmp, isLoaded, <parent;

	//arg w, path, busses=[0,1], target, fileName="[automatic]";

	*new {arg server, window, inputbus, group, path, directbus=0, showGUI=true;
		^super.new.init(server, window, inputbus, group, path, directbus, showGUI=true);
	}

	init {arg argserver, argwindow, arginputbus, arggroup, argpath, argdirectbus, argshowGUI;
		var newWindow=false,f;
		server=argserver ?? {Server.default};
		window=argwindow ?? {newWindow=true; Window("Player", Rect(0,1000,275,100))};
		if (newWindow, {window.addFlowLayout; window.front;});
		inputbus=arginputbus ?? {0};
		group=arggroup ?? {1};
		path=argpath ?? {"/"};
		path=if (PathName(path).isFile, {
			[path]
		}, {

			if( File.exists(path).not) {
				systemCmd("mkdir" + path.quote);
				f=File((path++"tmp"),"w"); f.close;
			};
			if (PathName( path ).files.size==0, {f=File((path++"tmp"),"w"); f.close;});
			PathName( path ).files.collect({|i| i.fullPath});
		});
		currentPath=path[0];
		directbus=argdirectbus ?? {0};
		resolution=10;
		loop=0;
		isPlaying=false;
		isLoaded=false;
		startLoop=0;
		endLoop=1;
		fileNames=path.collect({|i| PathName(i).fileNameWithoutExtension});
		directAmp=0;
		sampleRate=44100;
		channels=2;
		duration=1.0;
		//		if (argshowGUI, {
		{this.gui;}.defer;
		//		});
	}

	playsynth {
		synth=Synth(\Player_JT, [\bufnum, buf, \directBus, directbus, \amp, directAmp
			, \rate, sampleRate/server.sampleRate
			, \outputBus, if (inputbus.class==Bus, {inputbus.index}, {inputbus})], group, \addToHead).register;

	}

	play {
		if (isLoaded, {
			if (playRoutine!=nil, {
				//if (playRoutine.isPlaying, {playRoutine.stop;})
			});
			playRoutine=Task({
				{guis[\play].value_(1)}.defer;
				//HIER IETS DOEN!!!!
				buf.close( buf.cueSoundFileMsg(currentPath, guis[\range].value[0]*sampleRate, channels));
				this.playsynth;
				(endLoop-startLoop*resolution+1).do({|i|
					{guis[\progressing].value_(i/resolution+startLoop)}.defer;
					resolution.reciprocal.wait;
				});
				if (synth.isPlaying, {synth.free});
				{guis[\play].value_(0);}.defer;
				this.end;
			});
			isPlaying=true;
			playRoutine.start;
		})
	}

	end {
		//this.stop;
		{guis[\play].value_(0)}.defer;
		if (synth.isPlaying, {synth.free});
		isPlaying=false;
		if (loop==1, {
			//guis[\play].value=1;
			//playRoutine.reset;
			//playRoutine.start;
			this.play
		});
	}

	stop {
		playRoutine.stop;
		if (synth.isPlaying, {synth.free});
		if (guis[\pause].value==1, {guis[\pause].value=0});
	}

	pause {
		if (synth.isPlaying, {synth.free});
		if (playRoutine.isPlaying, {playRoutine.pause})
	}

	resume {
		if (isPlaying, {
			this.playsynth;
			playRoutine.resume;
		});
	}

	close {
		this.stop;
	}

	synthDefs {
		SynthDef(\Player_JT, {|bufnum, outputBus, directBus=0, amp=1, fadeIn=0.0, fadeOut=0.0, gate=1, rate=1|
			var output;
			var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut), gate, doneAction:2);
			//output=DiskIn.ar(channels, bufnum, 0);
			output=VDiskIn.ar(channels, bufnum, rate, 0);
			Out.ar(outputBus, output);
			Out.ar(directBus, amp.lag(0.1)*Splay.ar(output));
		}).send(server);
	}

	load {
		var soundFile=SoundFile.openRead(currentPath);
		if (soundFile!=nil, {
			channels=soundFile.numChannels;
			duration=soundFile.duration;
			sampleRate=soundFile.sampleRate;
			//[\progressing, \range].do({|i| guis[i].controlSpec_(ControlSpec(0, duration)); });
			//guis[\range].valueAction=[0, duration];
			soundFile.close;

			{
				this.synthDefs;
				server.sync;
				if (bufnum==nil, {
					buf=Buffer.alloc(server, 65536, channels); server.sync; bufnum=buf.bufnum},{
					buf=Buffer.alloc(server, 65536, channels, bufnum:bufnum); server.sync;
				}
				);
				if (buf.class==Buffer, {
					//buf.close;
					server.sendMsg("/b_close", bufnum);
					server.sync
				});
				//server.sendMsg("/b_alloc", bufnum, 65536, channels);

				server.sendMsg("/b_read", bufnum, currentPath, 0, 65536, 0, 1);
				//buf=Buffer.cueSoundFile(server, currentPath, startLoop*sampleRate, channels);
				isLoaded=true;
			}.fork;
		}, {nil})
	}
	//---------------------------------------------------------------------------------------------------------------------------------------------
	//GUI
	//---------------------------------------------------------------------------------------------------------------------------------------------
	gui {
		var c, d;
		var buttonWidth=27, buttonHeight=20, textWidth=96, font=Font("Helvetica", 9);
		var containerWidth=(buttonWidth+4)*5+textWidth+4+4, containerHeight=(buttonHeight+4)*3+10;
		guis=();

		c=CompositeView(window, containerWidth@containerHeight);
		c.decorator=d=FlowLayout(c.bounds);
		//c.background_(Color.green(1.9));

		guis[\play]=Button(c, buttonWidth@buttonHeight).states_([ [\play],[\play, Color.black, Color.green]]).font_(font).canFocus_(false).action_({|but|
			if (but.value==1, {
				this.play
			},{
				//if (loop==1, {this.stop; isPlaying=false;}, {this.end});
				this.stop
				//this.end
			});
		});

		guis[\pause]=Button(c, buttonWidth@buttonHeight).states_([ [\pause],[\pause, Color.black, Color.yellow]]).font_(font).canFocus_(false).action_({|but|
			if (but.value==1, {
				this.pause
			},{
				this.resume
			});
		});

		guis[\stop]=Button(c, buttonWidth@buttonHeight).states_([ [\stop]]).font_(font).canFocus_(false).action_({|but|
			this.stop;
			guis[\play].value_(0);
			guis[\progressing].value_(0);

		});

		guis[\loop]=Button(c, buttonWidth@buttonHeight).states_([ [\loop],[\loop, Color.black, Color.blue]]).font_(font).canFocus_(false).action_({|but| loop=but.value}).value_(loop);

		guis[\load]=Button(c, buttonWidth@buttonHeight).states_([ [\load]] ).canFocus_(false).font_(font).action_({
			Dialog.openPanel({|pathss|
				var items;
				currentPath=pathss;
				this.load;
				fileNames=path.collect({|path| PathName(path).fileNameWithoutExtension});
				guis[\pathNames].items=fileNames;
				guis[\pathNames].value=0;
				[\progressing, \range].do({|i| guis[i].controlSpec_(ControlSpec(0, duration)); });
				guis[\range].valueAction=[0, duration];
			})
		});

		guis[\pathNames]=PopUpMenu(c, textWidth@buttonHeight).items_(fileNames).font_(font).canFocus_(false)
		//.background_(Color.grey(1.5))
		.action_({|pop|
			currentPath=path[pop.value];
			this.load;
			[\progressing, \range].do({|i| guis[i].controlSpec_(ControlSpec(0, duration)); });
			guis[\range].valueAction=[0, duration];
		});

		guis[\amp]=EZSlider(c, (containerWidth-8)@12, \amp, \amp.asSpec, {|ez|
			if (synth.isPlaying, {synth.set(\amp, ez.value)});
			directAmp=ez.value;
		}, numberWidth: 30, labelWidth:34).font_(font);

		guis[\progressing]=EZSlider(c, (containerWidth-8)@12, \time, ControlSpec(0,100,\lin,1), numberWidth: 30, labelWidth:34).font_(font);

		guis[\range]=EZRanger(c, (containerWidth-8)@12, "", ControlSpec(0,100,\lin,1), {|ez|
			#startLoop,endLoop=ez.value;
			guis[\progressing].value=startLoop;
		}, numberWidth: 30, labelWidth:0).font_(font);

		[\amp, \progressing].do({|i| guis[i].sliderView.canFocus_(false);guis[i].numberView.canFocus_(false);});
		guis[\range].rangeSlider.canFocus_(false);guis[\range].loBox.canFocus_(false);guis[\range].hiBox.canFocus_(false);
		guis[\pathNames].doAction;

		//c.getParents.last.findWindow.addToOnClose({this.close});
		parent=c.getParents.last.findWindow;
		parent.onClose_(parent.onClose.addFunc({this.close}));

	}

}