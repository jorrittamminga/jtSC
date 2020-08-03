BufWrJT : BufWrSystemJT {
	*new {arg inBus, target, maxRecTime=60, phaseOutFlag=false, tphaseOutFlag=false
		, t_phaseRequestFlag=true, tphaseRequestFlag=true, hasEnvelope=true
		, hasOverdub=false, buf;
		^super.new.init(inBus, target, maxRecTime, phaseOutFlag, tphaseOutFlag
			, t_phaseRequestFlag, tphaseRequestFlag, hasEnvelope, hasOverdub, buf);
	}

	init {arg arginBus, argtarget, argmaxRecTime, argphaseOutFlag, argtphaseOutFlag
		, argt_phaseRequestFlag, argtphaseRequestFlag, arghasEnvelope, arghasOverdub, argbuf;
		var threadFunc;

		inBus=arginBus;
		target=argtarget;
		maxRecTime=argmaxRecTime;
		phaseOutFlag=argphaseOutFlag;
		tphaseOutFlag=argtphaseOutFlag;
		t_phaseRequestFlag=argt_phaseRequestFlag;
		tphaseRequestFlag=argtphaseRequestFlag;
		hasEnvelope=arghasEnvelope;
		hasOverdub=arghasOverdub;
		buffer=argbuf;
		this.initAll;
		threadFunc={
			this.initThreaded;
			//completionFunc.value
		};
		if (threaded, { threadFunc.value }, {{threadFunc.value}.fork});
	}

	makeGUI {arg parent, bounds=350@20, freeOnClose=true;
		gui=BufWrJTGUI(this, parent, bounds, freeOnClose);
		^gui
	}
	/*
	addChunker {arg chunkAction=\addToChunkList;
	chunkers=chunkers.add(ChunkerJT(this, chunkAction))
	}
	*/
}


BufWrJTGUI : GUIJT {
	var <bufWrJT;

	*new {arg bufWrJT, parent, bounds, freeOnClose=true;
		^super.new.init(bufWrJT, parent, bounds, freeOnClose);
	}

	init {arg bufWrJT, argparent, argbounds, argfreeOnClose;
		var butBounds;
		classJT=bufWrJT;
		parent=argparent;
		bounds=argbounds;
		freeOnClose=argfreeOnClose;
		this.initAll;
		//gap=0@4;
		butBounds=(bounds.x-(2*margin.x)/2-(2*gap.x))@bounds.y;
		views[\startRecording]=Button(parent,butBounds).states_([ [\rec]
			,[\rec,Color.white, Color.red]]).action_{|b|
			if (b.value==1, {
				classJT.startRecording;
			},{
				classJT.stopRecording(classJT.fileName);
				if (views[\pauseRecording].value==1, {views[\pauseRecording].value_(0)});
			})
		}.value_(bufWrJT.isRecording.binaryValue);
		views[\pauseRecording]=Button(parent,butBounds).states_([ [\pause]
			,[\pause,Color.white, Color.yellow]]).action_{|b|
			if (b.value==1, {
				classJT.pauseRecording;
			},{
				classJT.resumeRecording;
			})
		}.value_(bufWrJT.isPaused.binaryValue);
		/*
		views[\fileName]=TextField(parent, (bounds.x/2).floor@bounds.y).string_(
			bufWrJT.fileName
		).action_{|t|
			classJT.fileName=t.string;
		};
		*/
		parent.rebounds;
		if (window.class==Window, {window.rebounds});
	}

	close {
		this.initClose
	}
}