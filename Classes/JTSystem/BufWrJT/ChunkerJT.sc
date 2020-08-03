/*
- maak een optie voor 'beat'/tempo sync
- maak een ChunkPlayerJT (waardoor het een onderdeeltje wordt van Looper, of beter, Looper2)
- maak ook een optie voor PV_RecordBuf
- maak een optie voor offline analyse (en doe iets met deze analyse, zoals onset extraction etc), offline analyzer kan ook een third-party app zijn zoals SonicAnnotator
*/
ChunkerJT : BufWrSystemJT {
	var <>minRecTime, <bufWrJT, <>chunkAction, <chunkArray;
	var <chunkStarted;
	var <>latencyPre, <>latencyPost;
	var <>startChunkFunction, <>endChunkFunction;

	*new {arg bufWrJT, chunkAction=\addToChunkList, minRecTime=2.0
		, latencyPre=0.0, latencyPost=0.0, folderName
		, deleteFiles=true, freeBuffers=true;
		^super.new.init(bufWrJT, chunkAction, minRecTime, latencyPre, latencyPost
			, folderName, deleteFiles
			, freeBuffers);
	}

	init {arg argbufWrJT, argchunkAction, argminRecTime, arglatencyPre, arglatencyPost
		, argfolderName, argdeleteFiles
		, argfreeBuffers;
		id=UniqueID.next;
		bufWrJT=argbufWrJT;
		chunkAction=argchunkAction;
		minRecTime=argminRecTime??{2.0};
		latencyPre=arglatencyPre??{0};
		latencyPost=arglatencyPost??{0};
		folderName=argfolderName??{bufWrJT.folderName};
		if (File.exists(folderName).not, {folderName.mkdir});
		deleteFiles=argdeleteFiles;
		freeBuffers=argfreeBuffers;

		fileNameBase="chunk_"++id;
		server=bufWrJT.server;
		headerFormat=bufWrJT.headerFormat;
		sampleFormat=bufWrJT.sampleFormat;
		bufArray=[];
		pathArray=[];
		chunkArray=[];
		latency=2048;
		chunkStarted=false;
		startChunkFunction={};
		endChunkFunction={};
	}

	free {
		if (deleteFiles, {
			pathArray.do{|path|
				("rm " ++ path.unixPath).unixCmd;
			}
		});
		if (freeBuffers, {bufArray.do{|b| b.free}});
		if (gui!=nil, {{gui.close}.defer});
	}

	close {
		this.free;
	}

	startChunk {arg func;
		var funcList;
		if (chunkStarted.not, {
			chunkStarted=true;
			recordingTime=Main.elapsedTime;
			funcList={arg frame;
				startFrame=frame;
			};
			if (func.class==Function, {funcList=funcList.add(func)});
			bufWrJT.getFrame(funcList);
			if (gui!=nil, {
				{gui.views[\startChunk].value_(1)}.defer
			});
			startChunkFunction.value;
		});
	}

	endChunkFunc {arg func;
		switch (chunkAction, \write, {
			{
				this.autoPath(fileNameBase);
				pathArray=pathArray.add(path);
				bufWrJT.write(path, startFrame, numFrames);
				server.sync;
				func.value;
			}.fork
		}, \copy, {
			{
				buffer=Buffer.alloc(server, numFrames, bufWrJT.numChannels);
				server.sync;
				bufWrJT.copy(buffer, 0, startFrame, numFrames);
				server.sync;
				bufArray=bufArray.add(buffer);
				func.value;
			}.fork;
		}, \addToChunkList, {
			chunkArray=chunkArray.add([startFrame, numFrames, endFrame]);
			func.value;
		});
	}

	endChunk {arg endchunkfunc, func;
		var funcList;
		if (chunkStarted, {
			chunkStarted=false;
			if (Main.elapsedTime-recordingTime>=minRecTime,{
				funcList={arg frame;
					recordingTime=Main.elapsedTime-recordingTime;
					endFrame=frame;
					startFrame=(startFrame-(latencyPre*server.sampleRate)-latency).wrap(0
						, bufWrJT.buffer.numFrames);
					if (endFrame<startFrame, {endFrame=endFrame+bufWrJT.buffer.numFrames});
					endFrame=(endFrame-(latencyPost*server.sampleRate));
					numFrames=endFrame-startFrame;
					endFrame=endFrame.wrap(0, bufWrJT.buffer.numFrames);
					if ((endchunkfunc.class==Function)||(endchunkfunc.class==FunctionList),{
						endchunkfunc=endchunkfunc.addFunc(endChunkFunction);
					},{
						endchunkfunc=endChunkFunction;
					});
					this.endChunkFunc(endchunkfunc);
				};
				if (func.class==Function, {funcList=funcList.addFunc(func)});
				bufWrJT.getFrame(funcList);
			});
		});
		if (gui!=nil, {
			{gui.views[\startChunk].value_(0)}.defer
		});

	}

	makeGUI {arg parent, bounds=350@20, freeOnClose=true;
		//{
			gui=ChunkerJTGUI(this, parent, bounds, freeOnClose);
			^gui
		//}.defer;
	}
}

ChunkerJTGUI : GUIJT {
	var <chunkerJT;

	*new {arg chunkerJT, parent, bounds, freeOnClose;
		^super.new.init(chunkerJT, parent, bounds, freeOnClose);
	}

	init {arg chunkerJT, argparent, argbounds, argfreeOnClose;
		var chunkActions=[\addToChunkList, \write, \copy];
		classJT=chunkerJT;
		parent=argparent;
		bounds=argbounds;
		freeOnClose=argfreeOnClose;
		this.initAll;
		views[\startChunk]=Button(parent, (bounds.x/8).floor@bounds.y)
		.states_([ [\start],[\end, Color.white, Color.green]])
		.action_{|b|
			if (b.value==1, {
				classJT.startChunk;
			},{
				classJT.endChunk;
			})
		};
		views[\minRecTime]=EZNumber(parent, (bounds.x/8).floor@bounds.y, nil
			, ControlSpec(0.1, classJT.bufWrJT.maxRecTime)
			, {|ez| classJT.minRecTime=ez.value}, classJT.minRecTime, false);
		views[\latencyPre]=EZNumber(parent, (bounds.x/8).floor@bounds.y, nil
			, ControlSpec(0.0, 4.0)
			, {|ez| classJT.latencyPre=ez.value}, classJT.latencyPre, false);
		views[\latencyPost]=EZNumber(parent, (bounds.x/8).floor@bounds.y, nil
			, ControlSpec(0.0, 4.0)
			, {|ez| classJT.latencyPost=ez.value}, classJT.latencyPost, false);
		views[\fileName]=TextField(parent, (bounds.x/4).floor@bounds.y)
		.string_(classJT.fileNameBase)
		.action_{|t|
			chunkerJT.fileNameBase=t.string;
		};
		views[\chunkAction]=PopUpMenu(parent, (bounds.x/4).floor@bounds.y)
		.items_(chunkActions)
		.action_{|t|
			classJT.chunkAction=t.items[t.value];
		}.value_(chunkActions.indexOf(classJT.chunkAction));
		this.reboundsAll;
	}

	close {
		this.initClose
	}
}