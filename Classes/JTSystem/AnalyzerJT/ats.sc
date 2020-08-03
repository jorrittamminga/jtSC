/*
(Options: 1=amp.and freq. only, 2=amp.,freq. and phase, 3=amp.,freq. and residual, 4=amp.,freq.,phase, and residual)
a=(b:0.1, w:1, M: 0.50000);
q="";
a.keysValuesDo{|key,val| q=q++" -"++key ++ " " ++ val};
*/

AtsRecPlay {
	var <>voice, <>buf, <>atsbuf, target, inBus, recVoice, playVoice;
	var atsOptions, <>playSynth, <>recSynth, <>server, <>path, <>pathAts, isPlaying, isRecording;
	var fadeIn, fadeOut, recordingTime, startTime, <>options, <>parameters;
	var <atsFile;

	*new {arg inBus=0, target, maxRecTime=60, fadeIn=0.1, fadeOut=0.1, path, options=(b: 0.000000), parameters;
		^super.new.init(inBus, target, maxRecTime, fadeIn=0.1, fadeOut=0.1, path, options, parameters)
	}

	init {arg arginBus, argtarget, maxRecTime, argfadeIn, argfadeOut, argpath, argoptions, argparameters;
		recVoice=0;
		playVoice=0;
		recordingTime=0;
		startTime=Main.elapsedTime;
		isPlaying=false;
		isRecording=false;
		inBus=arginBus;
		fadeIn=argfadeIn;
		fadeOut=argfadeOut;
		target=argtarget??{server=Server.default; server};
		options=argoptions;
		parameters=(outBus:0, pan:0, speed:1.0, amp:1.0, freqMul:1.0, freqAdd:0, fadeIn:0.1, fadeOut:0.1
			, sinePct:1.0, noisePct: 1.0, numBands: 25, bandStart: 0, bandSkip: 1);
		if (argparameters!=nil, {argparameters.keysValuesDo{|key,val| parameters[key]=val}});
		//if (target.class!=Server, {server=target.server.postln});
		server=Server.default;
		[target, inBus, server].postln;
		this.synthDefs;
		buf={Buffer.alloc(server, server.sampleRate*maxRecTime)}!2;
		pathAts=[nil,nil];
		path=2.collect{|i|
			var p=thisProcess.platform.recordingsDir +/+ "SC_" ++ Date.localtime.stamp ++ i.asString;
			pathAts[i]=p++".ats";
			p ++ "." ++ "aif"
		};
	}

	synthDefs {
		SynthDef(\AtsRecord, {arg inBus, bufnum, gate=1, fadeIn=0.1, fadeOut=0.1;
			var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut), gate, doneAction:2);
			var in;
			in=SoundIn.ar(inBus)*env;
			RecordBuf.ar(in, bufnum, 0, 1, 0, 1, 1);
		}).add;

		SynthDef(\AtsSynth, {arg outBus=0, bufnum, numPartials, speed=1.0, durR, freqMul=1, freqAdd=0, partialStart = 0, partialSkip = 1, amp=1.0, pan=0.0;
			Out.ar(outBus, Pan2.ar(AtsSynth.ar(bufnum
				, numPartials
				, partialStart
				, partialSkip
				, LFSaw.kr(durR*speed, 1, 0.5, 0.5)
				, freqMul
				, freqAdd
				, amp

			), pan))
		}).add;

		SynthDef(\AtsNoiSynth, {arg outBus=0, bufnum, numPartials, speed=1.0, durR, sinePct=1.0, noisePct = 1.0, freqMul = 1.0, freqAdd = 0.0, numBands = 25, bandStart = 0, bandSkip = 1, amp=1.0, partialStart=0.0, partialSkip=1, pan=0.0;
			Out.ar(outBus, Pan2.ar(AtsNoiSynth.ar(bufnum
				, numPartials
				, partialStart
				, 1
				, LFSaw.kr(durR*speed, 1, 0.5, 0.5)
				, sinePct
				, noisePct
				, freqMul
				, freqAdd
				, numBands
				, bandStart
				, bandSkip
				, amp
			), pan))
		}).add;
	}


	write {}

	writeAts {}

	load {}

	loadAts {}

	free {
		if (playSynth.class==Synth, {playSynth.free});
		if (recSynth.class==Synth, {recSynth.free});

		path.do{|p|
			p=p.replace(" ", "\\ ");
			("rm " ++ p).unixCmd;
		};
		pathAts.do{|p|
			p=p.replace(" ", "\\ ");
			("rm " ++ p).unixCmd;
		};

		if (atsFile.class==AtsFile, {atsFile.freeBuffer});
	}

	startRecording {
		startTime=Main.elapsedTime;
		recVoice=recVoice+1%2;
		buf[recVoice].zero;
		recSynth=Synth.after(target, \AtsRecord, [\inBus, inBus, \bufnum, buf[recVoice], \fadeIn, fadeIn, \fadeOut, fadeOut]).register;
	}

	stopRecording {arg autoWrite=true, autoWriteAts=true;
		var tmpVoice=recVoice;
		{
			var cond = Condition.new;
			recSynth.set(\gate, 0.0);
			recordingTime=(Main.elapsedTime-startTime+fadeOut);
			(fadeOut).wait;
			buf[tmpVoice].normalize;
			buf[tmpVoice].write(path[tmpVoice], numFrames:recordingTime*server.sampleRate
				//, completionMessage: {this.writeAtsFile(tmpVoice)}
			);
			server.sync;
			this.writeAtsFile(tmpVoice);
		}.fork;

	}

	writeAtsFile {arg index;
		var op=" ", p1, p2;
		var p, l=0;
		options.keysValuesDo{|key,val| op=op++" -"++key ++ val};
		index=index??{recVoice};
		p1=path[index].replace(" ", "\\ ");
		p2=pathAts[index].replace(" ", "\\ ");
		("/usr/local/bin/atsa " ++ p1 ++ " " ++ p2 ++ op).postln;
		p=("/usr/local/bin/atsa " ++ p1 ++ " " ++ p2 ++ op).unixCmdGetStdOut;//.unixCmd;//.unixCmdGetStdOut;//		++ op
		"READY".postln;
		this.loadAtsFile;
	}

	loadAtsFile {arg autoPlay=true;
		if (atsFile.class==AtsFile, {atsFile.freeBuffer});
		"LOAD ATS FILE IN MEMORY".postln;
		if (File.fileSize(pathAts[recVoice])>0, {
			atsFile = AtsFile.new(pathAts[recVoice]).load;
			if (autoPlay, {
				this.startPlaying
			})
		});
	}

	startPlaying { arg synthDef=\AtsSynth; //\AtsSynth, \AtsNoiSynth
		if (playSynth==nil, {
			playSynth=Synth(synthDef, [\outBus, 0, \bufnum, atsFile.bufnum
				, \numPartials, atsFile.numPartials, \speed, 1, \durR, atsFile.sndDur.reciprocal].postln
			).register;
		});
	}

	stopPlaying {
		playSynth.free;
		playSynth=nil;
	}

	set {arg ... args;
		//if (synthFreeze[ff].isRunning, {synthFreeze[ff].set(*args)});
		args.pairsDo({|key,val|
			parameters[key]=val;
			//guiO[key].valueAction=val
		});
	}
}
