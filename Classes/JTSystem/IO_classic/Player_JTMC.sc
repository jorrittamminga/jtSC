/*
- noem inputbus anders.... en is het een bus of een index?
- er klopt geen zak van als ie op loop staat (verschil tussen end en stop)
*/
Player_JTMC {
	var <>path, <currentPath, <buf, synth, inputbus, server, <window, <guis, directbus, playRoutine, isPlaying;
	var <headerFormat, <sampleFormat, group, <fileNames;
	var <sampleRate, <channels, buf, <>endLoop, <>startLoop, <duration, <resolution, <>loop, directAmp, isLoaded, <bufnum, <parent;

	//arg w, path, busses=[0,1], target, fileName="[automatic]";

	*new {arg server, window, inputbus, group, path, directbus=0, showGUI=true;
		^super.new.init(server, window, inputbus, group, path, directbus, showGUI=true);
	}

	init {arg argserver, argwindow, arginputbus, arggroup, argpath, argdirectbus, argshowGUI;
		var newWindow=false,f;
		server=argserver.asArray ?? {[Server.default]};
		window=argwindow ?? {newWindow=true; Window("Player", Rect(0,1000,275,100))};
		if (newWindow, {window.addFlowLayout; window.front;});
		inputbus=arginputbus ?? {[0]};
		//group=arggroup ?? { server.collect{|ser,i| Group.head(ser,\addToHead)} };//
		group=arggroup ?? { server };//
		path=argpath ?? {"/"};
		path=if (PathName(path).isFile, {
			[path]
		}, {

			if( File.exists(path).not) {
				systemCmd("mkdir" + path.quote);
				f=File((path++"tmp"),"w"); f.close;
			};
			if (PathName( path ).entries.size==0, {f=File((path++"tmp"),"w"); f.close;});

			PathName( path ).entries.collect({|i| i.fullPath});
		});
		buf={0}!server.size;
		bufnum={nil}!server.size;
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
		synth=server.collect{|server, nr|
			var buz=if (inputbus[nr].class==Bus, {inputbus[nr].index}, {inputbus[nr]});

			buz=if (inputbus.class==Bus, {
				inputbus.index;
			}, {
				if (inputbus.size!=inputbus.flatSize, {
					if (inputbus[nr].class==Bus, {inputbus[nr].index}, {inputbus[nr]});
				},{
					if (inputbus[nr].class==Bus, {inputbus[nr].index}, {inputbus[0]});
				}
				);
			});

			Synth(\Player_JTMC, [\bufnum, buf[nr], \directBus, directbus
				, \rate, sampleRate/server.sampleRate
				, \amp, (nr==0).binaryValue*directAmp, \outputBus
				, buz
			]
			, group[nr]
			, \addToHead).register;
		}
	}

	play {
		if (isLoaded, {
			playRoutine=Task({
				{guis[\play].value_(1)}.defer;
				server.do{|server,nr|
					buf[nr].close( buf[nr].cueSoundFileMsg(currentPath, guis[\range].value[0]*sampleRate, channels));
				};
				this.playsynth;

				(endLoop-startLoop*resolution+1).do({|i|
					{guis[\progressing].value_(i/resolution+startLoop)}.defer;
					resolution.reciprocal.wait;
				});

				server.do{|server, nr|
					if (synth[nr].isPlaying, {synth[nr].free});
				};
				//{guis[\play].valueAction_(0);}.defer;
				{guis[\play].value_(0)}.defer;
				this.end;
			});
			isPlaying=true;
			playRoutine.start;
		})
	}

	end {
		/*
		this.stop;
		isPlaying=false;
		if (loop==1, {
		//guis[\play].value=1;
		//playRoutine.reset;
		//playRoutine.start;
		this.play
		});
		*/
		{guis[\play].value_(0)}.defer;
		server.do{|server, nr|
			if (synth[nr].isPlaying, {synth[nr].free});
		};
		isPlaying=false;
		if (loop==1, {
			this.play
		});
	}

	stop {
		playRoutine.stop;
		if (synth!=nil, {
			server.do{|server, nr|
				if (synth[nr]!=nil, {
					if (synth[nr].isPlaying, {synth[nr].free});
				})
			};
		})
	}

	pause {
		server.do{|server, nr|
			if (synth[nr].isPlaying, {synth[nr].free});
		};
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

	synthDefs {arg server;
		SynthDef(\Player_JTMC, {|bufnum, outputBus, directBus=0, amp=1, rate=1|
			var output;
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

			server.do{|server, nr|
				{
					this.synthDefs(server); server.sync;
					if (buf[nr].class==Buffer, {buf[nr].close; buf[nr].free; server.sync});
					buf[nr]=Buffer.cueSoundFile(server, currentPath, startLoop*sampleRate, channels);
					isLoaded=true;
				}.fork;

				{
					this.synthDefs(server);
					server.sync;
					if (bufnum[nr]==nil, {
						buf[nr]=Buffer.alloc(server, 65536, channels);
						server.sync;
						bufnum[nr]=buf[nr].bufnum
					},{
						buf[nr]=Buffer.alloc(server, 65536, channels, bufnum:bufnum[nr]);
						server.sync;
					});
					if (buf[nr].class==Buffer, {
						//buf.close;
						server.sendMsg("/b_close", bufnum[nr]);
						server.sync
					});
					//server.sendMsg("/b_alloc", bufnum, 65536, channels);
					server.sendMsg("/b_read", bufnum[nr], currentPath, 0, 65536, 0, 1);
					//buf=Buffer.cueSoundFile(server, currentPath, startLoop*sampleRate, channels);
					isLoaded=true;
				}.fork;




			}

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

		c=CompositeView(window, containerWidth@containerHeight); c.decorator=d=FlowLayout(c.bounds);
		//c.background_(Color.green(1.9));

		guis[\play]=Button(c, buttonWidth@buttonHeight).states_([ [\play],[\play, Color.black, Color.green]]).font_(font).canFocus_(false).action_({|but|
			if (but.value==1, {
				this.play
			},{
				if (loop==1, {this.stop; isPlaying=false;}, {this.end});
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
				path=pathss;
				//currentPath=path[0];
				currentPath=path;
				path=PathName(path.dirname).files.collect{|path| path.fullPath};
				this.load;

				//				PathName("/Users/jorrit/Desktop/141120_211403.aiff".dirname).files.collect{|path| path}

				fileNames=PathName(currentPath.dirname).entries.collect({|path|
					path.fileNameWithoutExtension});
				guis[\pathNames].items=fileNames;

				guis[\pathNames].value=
				fileNames.indexOfEqual(PathName(currentPath).fileNameWithoutExtension);



				[\progressing, \range].do({|i|
					guis[i].controlSpec_(ControlSpec(0, duration)); });
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
			if (synth!=nil, {
				if (synth[0].isPlaying, {synth[0].set(\amp, ez.value)});
			});
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