SoundFileViewJT {
	/*
	var <classJT, <views, <path, <dirname, <fileName;
	var <soundFileList, <paths, <bounds, <controlSpec, stopFunc, routine, <oscF;

	*new {arg classJT, parent, bounds=50@20, path;
		^super.new.init(classJT, parent, bounds, path);
	}

	makeSoundFileList {arg argpath;
		var path=argpath??player.path;
		paths=[];
		soundFileList=[];
		PathName(path.dirname).entries.do{|p|
			if (p.isFile, {
				paths=paths.add(p.fullPath);
				soundFileList=soundFileList.add(p.fileNameWithoutExtension);
			});
		};
	}

	addOSCFunc {
		oscF=OSCFunc({arg msg;
			{
				views[\soundView].timeCursorPosition_(msg[3]*player.sampleRate);
			}.defer;
		}, player.player.cmdName, player.player.server.addr);
	}

	removeOSCFunc {
		oscF.free;
	}

	changeDirectory {
		Dialog.openPanel({arg path;
			player.path_(path);
			this.makeSoundFileList(path);
			views[\soundFileList].items_(soundFileList);
			views[\soundFileList].value_(soundFileList.indexOfEqual(
				PathName(path).fileNameWithoutExtension))
		});
	}

	init {arg argclassJT, argparent, argbounds, argpath;
		classJT=argclassJT;
		bounds=argbounds;
		path=argpath??{
			dirname=thisProcess.platform.recordingsDir++"/";
		};
		this.makeSoundFileList;
		parent=argparent??{this.makeWindow("player", (argbounds.x+8)@(argbounds.y+4*6+8))};
		views=();
		views[\startPlaying]=Button(parent, (bounds.x/8).floor@bounds.y)
		.states_([[\play],[\play, Color.black, Color.green]])
		.action_{|b|
			views[\pausePlaying].value_(0);
			if (b.value==1, {
				argplayer.startPlaying
			},{
				argplayer.stopPlaying;
				views[\soundView].timeCursorPosition_(player.startFrame);
			})
		};
		views[\pausePlaying]=Button(parent, (bounds.x/8).floor@bounds.y)
		.states_([[\pause],[\pause,Color.black,Color.yellow]]).action_{|b|
			if ((b.value==1)&&(views[\startPlaying].value==1), {
				argplayer.pausePlaying;
			});
			if ((b.value==0)&&(views[\startPlaying].value==1), {
				argplayer.resumePlaying;
			});
		};
		views[\loop]=Button(parent, (bounds.x/8).floor@bounds.y)
		.states_([[\loop],[\loop,Color.white,Color.blue]]).action_{|b|
			player.player.loop=(b.value==1);
			player.loop=(b.value==1);
		}.value_(player.loop.binaryValue);
		views[\soundFileList]=PopUpMenu(parent, (bounds.x/2).floor@bounds.y)
		.items_(soundFileList).action_{|p|
			player.path_(paths[p.value])
		};
		if (player.path!=nil, {views[\soundFileList].value_(
			soundFileList.indexOfEqual(PathName(player.path).fileNameWithoutExtension))
		});
		views[\open]=Button(parent, (bounds.x/8).floor@bounds.y).states_([ [\open] ]).action_{
			this.changeDirectory

		};
		views[\amp]=EZSlider(parent, bounds, \monitor, \amp.asSpec, {|ez|
			player.setampmonitor(ez.value)}, 0.0);

		views[\soundView]=SoundFileView(parent, bounds.x@(bounds.x/4));
		views[\soundView].soundfile_(player.soundFile);
		views[\soundView].read(0, player.soundFile.numFrames);
		views[\soundView].gridOn = false;
		views[\soundView].setSelectionColor(0, Color.red);
		views[\soundView].timeCursorColor=Color.white;
		views[\soundView].timeCursorOn=true;
		//views[\soundView].action_{|ez| ez.postln};
		views[\soundView].mouseUpAction = {
			var value=
			(views[\soundView].selections[views[\soundView].currentSelection].integrate);
			if ((value[0]-value[1]).abs<1000, {value=[0, player.numFrames]});
			player.startFrame_(value[0]);
			player.endFrame_(value[1]);
			{
				var play=false;
				if (player.player.playNode.isPlaying, {play=true;
					player.stopPlaying;
					player.player.server.sync;
				});
				if (play, {
					player.startPlaying
				});
			}.fork
		};
		stopFunc={ {
			views[\startPlaying].value_(0);
			views[\soundView].timeCursorPosition_(player.startFrame);
		}.defer };

		if (player.player.stopFunc==nil, {
			player.player.stopFunc=stopFunc;
		},{
			player.player.stopFunc=player.player.stopFunc.add(stopFunc);
		});
		window.onClose=window.onClose.addFunc({
			routine.stop;
			player.player.stopFunc.removeFunc(stopFunc);
		});
		this.addOSCFunc;
		//parent.view.resizeParent
	}
	*/
}