/*
- inBusA kan zijn: 1: Bus, 2: [Bus, Bus, Bus], 3: [0,1,5,6,9,10], 4: [1,3,5,[6,7,8,9]]
- eventueel kun je ook een BufWrJT gebruiken en daar de phase uithalen
*/
ConcatJTRecorder : ConcatJTSystem {
	var <>concatJTplayer;
	var <fadeIn, <fadeOut, <hasLocalBuf, <startFrame;
	var <>makeKDTreeFlag=true, <>writeKDTreeFlag=true;

	*new {arg inBus, inBusA, inBusFFT, bufRecording, target, path, maxRecTime=30;
		^super.new.init(inBus, inBusA, inBusFFT, bufRecording, target, path, maxRecTime=30);
	}

	init {arg arginBus, arginBusA, arginBusFFT, argbufRecording, argtarget, argpath
		, argmaxRecTime;
		var threadedFunc;
		id=UniqueID.next;

		inBus=arginBus;
		inBusA=arginBusA;
		inBusFFT=arginBusFFT;
		target=argtarget;
		path=argpath;

		this.initSettings;
		fadeIn=0.01; fadeOut=0.01;
		makeKDTreeFlag=true; writeKDTreeFlag=true;

		threadedFunc={
			this.makeSynthDef; server.sync;
			if (argbufRecording.class==Buffer, {
				bufRecording=argbufRecording;
				hasLocalBuf=false;
			},{
				hasLocalBuf=true;
				bufRecording=Buffer.alloc(server, maxRecTime*server.sampleRate);
				server.sync;
			});
			bufData=Buffer.alloc(server, maxRecTime*server.sampleRate, dataSize);
			server.sync;
		};

		if (threaded, {threadedFunc.value},{{threadedFunc.value}.fork})
	}

	fadeIn_ {arg time;
		fadeIn=time;
		if (synth!=nil, {synth.set(\fadeIn, time)});
	}

	fadeOut_ {arg time;
		fadeOut=time;
		if (synth!=nil, {synth.set(\fadeOut, time)});
	}

	free {
		oscF.free;
		synth.free;
		bufData.free;
		bufRecording.free;
		bufKDTree.free;
		if (gui!=nil, {if (gui.window!=nil, {gui.window.close})});
	}

	close { this.free }

	makeFileName {
		^("concat_"++Date.localtime.stamp)
	}

	makeSynthDef {
		synthDef=(\RecorderConcat_++id).asSymbol;
		cmdName=('/concat'++id).asSymbol;
		SynthDef(synthDef, {arg inBus, inBusT, bufnum, bufnumData, count=0, fadeIn=0.01
			, fadeOut=0.01, gate=1;
			var in, fft, analysis, phase;
			var env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut),gate,doneAction:2);
			if (hasLocalBuf, {
				in=In.ar(inBus)*env;
			},{
				phase=In.ar(inBus);
			});
			fft=In.kr(inBusT);
			//analysis=inBusA.collect{|bus,i| In.kr(bus[0],bus[1])}.flat;
			analysis=inBusA.collect{|bus| In.kr(bus)};
			if (hasLocalBuf, {
				phase=Phasor.ar(1, 1, 0, BufFrames.ir(bufnum));
				BufWr.ar(in, bufnum, phase);
			});
			count=Stepper.kr(fft, 0, 0, BufFrames.ir(bufnumData));
			BufWr.kr(analysis++A2K.kr(phase), bufnumData, count);
			SendReply.kr(fft, cmdName, count);
		}).add;
	}


	startRecording {
		{
			startFrame=0;
			bufRecording=Buffer.alloc(server, maxRecTime*server.sampleRate
				, bufnum: bufRecording.bufnum); server.sync;
			bufData=Buffer.alloc(server, maxRecTime*server.sampleRate, dataSize
				, bufnum: bufData.bufnum); server.sync;
			synth=Synth(synthDef
				, [\inBus, inBus
					, \inBusT,inBusFFT
					, \bufnum, bufRecording, \bufnumData, bufData
					, \fadeIn, fadeIn, \fadeOut, fadeOut
			]);
			recTime=Main.elapsedTime;
			server.sync;
			oscF=OSCFunc({|msg| counter=msg[3]}, cmdName, server.addr);
			if (fileName==nil, {fileName=this.makeFileName});
			if (gui!=nil, {{
				gui.guis[\recB].value_(1);
				gui.guis[\fileName].string_(fileName);
			}.defer});
		}.fork;
	}

	pauseRecording { }
	resumeRecording { }

	stopRecording {arg name, action={};
		{
			recTime=Main.elapsedTime-recTime;
			oscF.free;
			synth.set(\gate, 0);
			fadeOut.wait;
			if (makeKDTreeFlag && writeKDTreeFlag, {
				this.makeAndWriteKDTree(counter, name, action);
			},{
				if (makeKDTreeFlag && writeKDTreeFlag.not, {
					this.makeKDTree(counter, action);
				})
			});
			if (gui!=nil, {{
				gui.guis[\recB].value_(0);
				//gui.guis[\fileName].string_(fileName);
			}.defer});
		}.fork
	}

	writeKDTree {arg name, action={}, initFileName=true;
		{
			var nn, file;
			name=name??{fileName};
			if (name==nil, {name=this.makeFileName});
			nn=path++"NN/"++name++".aiff";
			file=path++name++".aiff";
			bufKDTree.write(nn, "aiff", "float");
			server.sync;
			bufRecording.write(file, "aiff", "int24", recTime*server.sampleRate);
			server.sync;
			if (initFileName, {fileName=nil});
			action.value;

			if (concatJTplayer!=nil, {
				concatJTplayer.getFileNames;
				if (concatJTplayer.gui!=nil, {{
					concatJTplayer.gui.guis[\fileNames].value_(
						concatJTplayer.fileNameArray.index(fileName)
					)
				}.defer});
			});


		}.fork
	}

	makeKDTree {arg count= -1, action;
		bufKDTree.free;
		bufData.loadToFloatArray(0, count, {arg array;
			var treedata;
			array=array.clump(dataSize);
			if (hasLocalBuf.not, {
				startFrame=array[0].last;
				"startFrame is ".post; startFrame.postln;
			});
			treedata = NearestN.makeBufferData(KDTree(array, lastIsLabel: true));
			{
				bufKDTree=Buffer.loadCollection(server, treedata.flat, treedata[0].size);
				server.sync;
				action.value;
			}.fork;
		});
	}

	makeAndWriteKDTree {arg count= -1, name, action;
		this.makeKDTree(count, {this.writeKDTree(name, action)});
	}

	makeGUI {arg parent, bounds=350@20;

		{
			gui=ConcatJTRecorderGUI(this, parent, bounds)
		}

	}
}

ConcatJTRecorderGUI {
	var <guis, <window, <concatJTRecorder, parent, bounds, cv;

	*new {arg concatJTRecorder, parent, bounds;
		^super.new.init(concatJTRecorder, parent, bounds);
	}

	init {arg argconcatJTRecorder, argparent, argbounds;
		concatJTRecorder=argconcatJTRecorder;
		parent=argparent;
		bounds=argbounds;
		if (parent==nil, {
			window=Window("ConcatJTRecorder", (bounds.x+8)@(bounds.y+8)).front;
			window.alwaysOnTop_(true);
			window.addFlowLayout;
		});
		cv=CompositeView(parent, bounds);
		cv.addFlowLayout(0@0, 4@4);
		guis=();
		guis[\recB]=Button(cv, (bounds.x*(1/6)-4).flat@bounds.y)
		.states_([ [\rec],[\rec,Color.black, Color.red]]).action_{|b|
			if (b.value==1, {
				concatJTRecorder.startRecording;
			},{
				concatJTRecorder.stopRecording;
			})
		};
		guis[\fileName]=TextField(parent, (bounds.x*(5/6)-4)@bounds.y)
		.string_("").action_{|t|
			concatJTRecorder.fileNameW=t.string;
		};
		if (window!=nil, {
			window.onClose_{concatJTRecorder.close}
		});
	}
}