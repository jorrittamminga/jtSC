/*
- opschonen die hap!
- drie manieren om een buffer te gebruiken:
1) copyData en maak de buffer precies groot genoeg
2) write(of copyWrite)
3) gebruik de buffer van BufWrJT met de juiste startPos (en numFrames)
*/
BufWrSystemJT : JT {
	var <>buffer;
	var <>latency;
	var <>freeBuffers, <>deleteFiles, <buffers;
	var <chunkers, <pathArray, <>fileNameBase;
	var <cmdNameT;
	var <numFrames, <>headerFormat, <>sampleFormat, <>time, <recordingTime;
	var <outBusK, <>maxRecTime;
	var phaseOutFlag, tphaseOutFlag, t_phaseRequestFlag, tphaseRequestFlag;
	var <isRecording, <isPaused, <hasEnvelope, <hasOverdub;
	var <currentFrame, functionPhaseTrigger, functionPhaseRequest, oscFuncs;
	var <startFrame, <endFrame;
	var <argsEvent, <numberOfBuffers, <bufferNr, <bufArray;

	//======================================== plugin funcs
	var <bypass, <>bypassFunc, <>runFunc, <>windowBounds;

	initAll {
		id=UniqueID.next;
		synthDef=(\BufWr++id).asSymbol;
		cmdName='/phase_request'++id;
		cmdNameT='/phase_requestT'++id;
		oscFuncs=();
		//threaded=thisProcess.mainThread.state>3;
		this.isThreaded;
		server=target.server??{Server.default};
		if (inBus.class!=Bus, {inBus=inBus.asBus(\audio, 1, server)});
		numChannels=inBus.numChannels;
		currentFrame=0;
		hasEnvelope=hasEnvelope??{true};
		hasOverdub=hasOverdub??{false};
		headerFormat="AIFF";
		sampleFormat="int24";
		chunkers=[];
		pathArray=[];
		latency=2048/server.sampleRate;
		isRecording=false;
		isPaused=false;
		this.initPath;
		bypass=false;
		freeBuffers=true;
		deleteFiles=true;
		numberOfBuffers=numberOfBuffers??{1};
		buffers=Array.newClear(numberOfBuffers);
		if (buffer.class==Buffer, {buffers[0]=buffer});
		bufferNr= -1;
		time=0;
		fileNameBase="bufwrjt_tmp"++id;
		argsEvent=(fadeIn: 0, fadeOut: 0, doneAction:1, preLevel:1);
	}

	initPath {
		path=path??{
			folderName=thisProcess.platform.recordingsDir++"/";
			//fileName=Date.localtime.stamp;
			this.makePath;
		};
		if (folderName==nil, {
			folderName=if (path.isFolder, {
				if (path.last!=$/, {
					path++"/"
				},{
					path;
				})
			},{
				path.folderName++"/";
			})
		});
	}
	/* deze komt uit Recorder
	makePath {
	var timestamp;
	var dir = thisProcess.platform.recordingsDir;
	timestamp = Date.localtime.stamp;
	^dir +/+ filePrefix ++ timestamp ++ "." ++ server.recHeaderFormat;
	}
	*/
	makePath {arg filename;
		filename=(filename??{fileNameBase++"_"++Date.localtime.stamp});
		path=(folderName++filename++"."++headerFormat.toLower);
		^path
	}

	autoPath {arg filename;
		filename=(filename??{fileNameBase})++"_"++pathArray.size;
		fileName=filename;
		this.makePath(filename);
	}

	initThreaded {
		if (phaseOutFlag, {
			outBus=Bus.audio(server, 1); server.syncJT;
		});
		if (tphaseOutFlag, {
			outBusK=Bus.control(server, 1); server.syncJT;
		});
		if (buffer==nil, {this.allocBuffer});
		this.makeSynthDef; server.syncJT;
		this.makeSynth; server.syncJT;
	}

	allocBuffer {arg completionMessage;
		var bufnum=nil;
		if (buffer.class==Buffer, {bufnum=buffer.bufnum});
		buffer=Buffer.alloc(server, maxRecTime*server.sampleRate, numChannels
			, completionMessage, bufnum);
		server.syncJT;
		buffers.wrapPut(bufferNr, buffer);
		//buffers.wrapAt(bufferNr)=buffer;
	}

	set {arg ...args;
		var event=(args.asOSCArgArray);
		event.keysValuesDo{|key,val| argsEvent[key]=val};
		if (synth.isPlaying, {synth.set(args)});
		^this
	}

	makeSynthDef {
		SynthDef(synthDef, {arg inB, inBusT, bufnum, t_reset, startFrame=0, resetFrame=0
			, loop=1, t_phaseRequest, outBus, gate=1.0, fadeIn=0.0, fadeOut=0.0, preLevel=1
			, doneAction=1;
			var in, inTK, inTA;
			var phase, env;
			in=In.ar(inB, inBus.numChannels);
			if (tphaseRequestFlag || tphaseOutFlag, {
				inTK=In.kr(inBusT);
			});
			phase=Phasor.ar(t_reset, 1, startFrame, BufFrames.ir(bufnum), resetFrame);
			if (hasOverdub, {
				in=BufRd.ar(numChannels, bufnum, phase)*preLevel+in
			});
			if (hasEnvelope, {
				env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut, -4.0), gate
					, doneAction:doneAction
				);
				BufWr.ar(in*env,bufnum, phase, loop);
			},{
				BufWr.ar(in, bufnum, phase, loop);
			});
			if (t_phaseRequestFlag, {
				SendReply.kr(t_phaseRequest, cmdName, phase)});
			if (tphaseRequestFlag, {
				SendReply.kr(inTK, cmdNameT, phase);
			});
			if (phaseOutFlag, {
				Out.ar(outBus.index, phase)});
			if (tphaseOutFlag, {
				Out.kr(outBusK.index, Latch.kr(phase, inTK ))});
		}, metadata: (specs: (
			fadeIn: ControlSpec(0, 30, 4.0), fadeOut: ControlSpec(0.0, 30.0, 4.0)
			, preLevel: \amp.asSpec
		))).add;
	}

	free {
		oscFuncs.do{|o| o.free};
		buffers.do(_.free);
		synth.free;
		if (gui!=nil, {{gui.close}.defer});
		if (freeBuffers, {
			bufArray.do{|buf| if (buf.path!=nil, {
				if ( File.exists(buf.path), {File.delete(buf.path)});
			}); buf.free};
		});
		if (deleteFiles, {
			pathArray.do{|path| if ( File.exists(path), {
				File.delete(path)}); };
		});
	}

	close {
		this.free;
	}

	makeSynth {
		synth=Synth.after(target, synthDef, [\inB, inBus.index,
			\bufnum, buffer.bufnum]++argsEvent.asKeyValuePairs).run(false).register;
		^synth
	}

	startRecording {arg clearBuffer=true;
		isRecording=true;
		{
			bufferNr=bufferNr+1%numberOfBuffers;
			buffer=buffers.wrapAt(bufferNr);
			if (clearBuffer, {this.allocBuffer});
			if (synth.isPlaying.not, {
				this.makeSynth(true);
			},{
				synth.run(true);
				synth.set(\t_reset, 1, \gate, 1);
			});
			time=Main.elapsedTime;
			recordingTime=0;
			isPaused=false;
		}.fork;
		if (gui!=nil, {{
			if (gui.views[\startRecording].class==Button, {
				if (gui.views[\startRecording].value==0, {
					gui.views[\startRecording].value_(1);
				})
			})
		}.defer});
	}

	stopRecording {arg name, func;
		isRecording=false;
		{
			if (name.class==String, {
				this.autoPath(name);
			});
			if (hasEnvelope, {
				synth.set(\gate, 0);
				argsEvent[\fadeOut].wait;
			},{
				synth.free;//.set(\gate, 0)
			});
			time=Main.elapsedTime-time+recordingTime;
			isPaused=false;
			if ( (name.class==String), {
				this.write(path, 0, time.min(maxRecTime)*server.sampleRate);
				pathArray=pathArray.add(path);
				server.syncJT;
			});
			recordingTime=0;
			func.value(this);
		}.fork;
		if (gui!=nil, {{
			if (gui.views[\startRecording].class==Button, {
				if (gui.views[\startRecording].value==1, {
					gui.views[\startRecording].value_(0);
				})
			})
		}.defer});
	}

	pauseRecording {arg action;
		isPaused=true;
		if (isPaused.not && synth.isRunning, {
			{
				isPaused=true;

				if (hasEnvelope, {
					synth.set(\gate, 0);
					argsEvent[\fadeOut].wait;
				},{
					synth.run(false);
				});
				recordingTime=Main.elapsedTime-time;
				time=Main.elapsedTime;
			}.fork
		});
		if (gui!=nil, {{
			if (gui.views[\pauseRecording].class==Button, {
				if (gui.views[\pauseRecording].value==0, {
					gui.views[\pauseRecording].value_(1);
				})
			})
		}.defer});
	}

	resumeRecording {
		isPaused=false;
		if (synth.isRunning.not, {
			synth.run(true);
			if (hasEnvelope, {
				synth.set(\gate, 1);
			});
			time=Main.elapsedTime;
		});
		isPaused=false;

		if (gui!=nil, {{
			if (gui.views[\pauseRecording].class==Button, {
				if (gui.views[\pauseRecording].value==1, {
					gui.views[\pauseRecording].value_(0);
				})
			})
		}.defer});
	}

	writeBefore {arg buffer, numFrames, path, headerFormat, sampleFormat
		, completionMessage, method='\getFrame';
		if (this.isThreaded, {
			^this.prWriteInBuffer(buffer, path, headerFormat, sampleFormat, numFrames
				, completionMessage, numFrames, method)
		},{{
			this.prWriteInBuffer(buffer, path, headerFormat, sampleFormat, numFrames
				, completionMessage, numFrames, method)
		}.fork})
	}

	writeAfter {arg buffer, numFrames, path, headerFormat, sampleFormat
		, completionMessage, method='\getFrame';
		{
			(numFrames/server.sampleRate).wait;
			this.prWriteInBuffer(buffer, path, headerFormat, sampleFormat, numFrames
				, completionMessage, numFrames, method);
		}.fork
	}

	//Private methods
	prWriteInBuffer {arg buf, path, headerformat, sampleformat, numframes
		, completionMessage, offset=0, method=\getFrame;
		var bufnum, cond=Condition.new, function;
		headerformat=headerformat??{headerFormat};
		sampleformat=sampleformat??{sampleFormat};
		numframes=numframes??{maxRecTime*server.sampleRate};
		if (buf!=nil, {
			if (buf.class==Buffer, {
				bufnum=buf.bufnum;
				if (path==nil, { if (buf.path!=nil, {path=buf.path})});
			},{
				bufnum=buf
			})
		});
		if (path==nil, {
			//dit zou this.autoPath moeten zijn!
			//path=(folderName??{Platform.recordingsDir++"/"})++"tmp"++UniqueID.next
			//++"."++headerformat.toLower;
			this.autoPath
		});
		buf=Buffer.alloc(server, numframes, bufnum: bufnum);
		server.syncJT;
		function={arg frame;
			{
				this.copy(buf,0, (frame-offset).wrap(0,buffer.numFrames), numframes);
				cond.unhang;
			}.fork
		};
		//this.perform(method, function);
		this.getFrame(function);
		cond.hang;
		buf.write(path, headerformat, sampleformat, -1, 0, false);
		buf.path=path;
		server.syncJT;
		bufArray=bufArray.add(buf);
		pathArray=pathArray.add(buf);
		completionMessage.value;
		^buf
	}
	/*
	prWriteCopy {arg targetBuffer, dstStartAt = 0, srcStartAt = 0, numSamples;
	var tmpstartFrame, tmpnumFrames, bufnum;

	numSamples=numSamples??{time.min(maxRecTime)*server.sampleRate};
	if (targetBuffer.class!=Buffer, {
	targetBuffer=Buffer.alloc(server, numSamples, bufnum: targetBuffer);
	},{
	if (targetBuffer.numFrames!=numSamples, {
	targetBuffer=Buffer.alloc(server, numSamples, bufnum: targetBuffer.bufnum);
	});
	});
	^targetBuffer
	}
	*/
	write {arg //targetBuffer,
		path, startFrame, numFrames, completionMessage;
		//if (filename!=nil, {fileName=filename});
		//this.makePath;
		if (startFrame+numFrames > buffer.numFrames, {
			var tmpbuffer=Buffer.alloc(server, numFrames, numChannels);
			server.syncJT;
			this.copy(tmpbuffer, 0, startFrame, numFrames);
			server.syncJT;
			tmpbuffer.write(path, headerFormat, sampleFormat, -1, 0, false
				, completionMessage);
			server.syncJT;
			tmpbuffer.free;
		},{
			buffer.write(path, headerFormat, sampleFormat, numFrames, startFrame, false
				, completionMessage)
		});
	}

	copy {arg targetBuffer, dstStartFrame=0, srcStartFrame=0, numSamples;
		var tmpstartFrame, tmpnumFrames, bufnum;

		numSamples=numSamples??{time.min(maxRecTime)*server.sampleRate};
		if (targetBuffer.class!=Buffer, {
			targetBuffer=Buffer.alloc(server, numSamples, bufnum: targetBuffer);
			server.syncJT;
		},{
			if (targetBuffer.numFrames!=numSamples, {
				targetBuffer=Buffer.alloc(server, numSamples, bufnum: targetBuffer.bufnum);
				server.syncJT;
			});
		});

		if (srcStartFrame+numSamples>buffer.numFrames, {
			tmpstartFrame=[srcStartFrame, 0];
			tmpnumFrames=[
				(buffer.numFrames-tmpstartFrame[0]),
				numSamples-(buffer.numFrames-tmpstartFrame[0])
			];
			buffer.copyData(targetBuffer, 0, tmpstartFrame[0], tmpnumFrames[0]);
			server.syncJT;
			buffer.copyData(targetBuffer, tmpnumFrames[0], tmpstartFrame[1]
				, tmpnumFrames[1]);
			server.syncJT;
		},{
			buffer.copyData(targetBuffer, dstStartFrame, srcStartFrame, numSamples);
			server.syncJT;
		});
		^targetBuffer
	}

	copywrite {arg targetBuffer, argpath, dstStartFrame=0, srcStartFrame=0, numSamples
		, completionMessage;
		var tmpBuf;
		numSamples=numSamples??{time.min(maxRecTime)*server.sampleRate};
		tmpBuf=this.copy(targetBuffer, dstStartFrame, srcStartFrame, numSamples);
		tmpBuf.write(path, headerFormat, sampleFormat, numSamples, dstStartFrame, false
			, completionMessage);
		^tmpBuf
	}

	getFrame {arg function={arg frame; ("current frame is " ++ frame).postln};

		if (synth.isRunning && (oscFuncs[\getFrame]==nil) && (t_phaseRequestFlag), {
			oscFuncs[\getFrame]=OSCFunc({arg msg;
				currentFrame=msg[3];
				function.value(currentFrame);
				oscFuncs[\getFrame]=nil;
			}, cmdName).oneShot;
			synth.set(\t_phaseRequest, 1);
			oscFuncs[\getFrame]
		},{
			"BufWrJT is not recording yet. Please .startRecording first".postln;
		});
	}

	getFrameT {arg function={arg frame; ("current frame is " ++ frame).postln};
		if (synth.isRunning && (oscFuncs[\getFrameT]==nil) && (tphaseRequestFlag), {
			oscFuncs[\getFrameT]=OSCFunc({arg msg;
				currentFrame=msg[3];
				function.value(currentFrame);
				oscFuncs[\getFrameT]=nil;
			}, cmdNameT).oneShot;
		},{
			"BufWrJT is not recording yet. Please .startRecording first".postln;
		});
	}

	addgetFrame {arg function={};
		if (functionPhaseRequest==nil, {functionPhaseRequest=function},{
			functionPhaseRequest=functionPhaseRequest.add(function);
		});
		if (synth.isRunning && (oscFuncs[\getFrame]==nil) && (t_phaseRequestFlag), {
			oscFuncs[\getFramecon]=OSCFunc({arg msg;
				currentFrame=msg[3];
				functionPhaseRequest.value(currentFrame);
			}, cmdName);
		},{
			"BufWrJT is not recording yet. Please .startRecording first".postln;
		});
	}

	addgetFrameT {arg function={};
		if (functionPhaseTrigger==nil, {functionPhaseTrigger=function},{
			functionPhaseTrigger=functionPhaseTrigger.add(function);
		});
		if (synth.isRunning && (oscFuncs[\getFrameT]==nil) && (tphaseRequestFlag), {
			oscFuncs[\getFrameTcon]=OSCFunc({arg msg;
				currentFrame=msg[3];
				functionPhaseTrigger.value(currentFrame);
			}, cmdNameT);
		},{
			"BufWrJT is not recording yet. Please .startRecording first".postln;
		});
	}

	//============================================= PLUGIN FUNCS
	run {arg flag=true;
		//isRunning=flag;
		//runFunc.value(flag);
		//synth.asArray.do(_.run(flag));
	}

	bypass_ {arg flag=true;
		bypass=flag;
		bypassFunc.value(flag);
		//synth.asArray.do(_.run(flag.not));
	}

}