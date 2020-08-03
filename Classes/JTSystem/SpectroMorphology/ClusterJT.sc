/*
TODO's:
- maak ook multichannel
- ook meerdere buffers per cluster
- maak ook een PlayBufCF versie
- voeg fadein/fadeout toe om eventuele tikjes te voorkomen (bij PlayBufCF gaat dit automatisch!)
*/
ClusterJT : JT {
	classvar initArgsEvent, initSynthdef, initScoreFunction, clusterDiskInSynthDef
	, clusterPlayBufSynthDef, <addActions, <initFuncsEvent, <initControlSpecs;
	var args, funcs, <>funcsEvent, <>argsEvent, <buffer, <type;
	var <bufferDiskIn, <bufferPlayBuf;
	var <scoreFunction, <score, <>latency, <>serverOptions;
	var <nr, <paths, <oscFuncs, <hasRendered, <>normalize;
	var <synths;
	var elapsedTime;

	*new {arg type, args=[], target, addAction=\addBefore;
		^super.new.init(type, args, target, addAction)
	}


	init {arg argtype, argargs, argtarget, argaddAction=\addBefore;

		target=argtarget;
		addAction=argaddAction;
		args=argargs;
		argsEvent=args.asEvent;
		type=argtype;
		//------------------------------------------------------- find server
		if (target==nil, {
			if (argsEvent[\buffer].class==Buffer, {
				target=argsEvent[\buffer].server
			},{
				target=Server.default
			})
		});
		server=if (target.class==Server, {target},{target.server});
		target=target.asTarget;
		//------------------------------------------------------- init parameters
		funcsEvent=();
		initArgsEvent.keysValuesDo{|key,val|
			if (argsEvent[key]==nil, {argsEvent[key]=val})};
		initFuncsEvent.keysValuesDo{|key,val| if (funcsEvent[key]==nil
			, {funcsEvent[key]=val})};
		initSynthdef.do{|synthdef|
			synthdef.add;
			//synthdef.load(server);//dit is niet ok! schrijf deze synthdef in de score
		};
		clusterDiskInSynthDef.do{|synthdef| synthdef.add};
		clusterPlayBufSynthDef.do{|synthdef| synthdef.add};
		scoreFunction=this.makeInitScoreFunction;
		controlSpecs=initControlSpecs.copy;
		synthDef=initSynthdef;//is dus een lijst met synthdefs!
		latency=server.latency;
		folderName=Platform.recordingsDir++"/";
		fileName="tmp";
		hasRendered=false;
		paths=[]; oscFuncs=List[]; synths=List[];
		serverOptions=ServerOptions.new;
		serverOptions.numOutputBusChannels_(argsEvent[\numChannels]);
		serverOptions.maxSynthDefs = 2**18;
		serverOptions.memSize = 2**18;
		serverOptions.verbosity_(-1);
		serverOptions.maxNodes=65536;
		normalize=false;
	}


	set { arg ... args;
		var tmpEvent=args.asEvent;
		tmpEvent.keysValuesDo{|key,val| argsEvent[key]=val};
		^this
	}

	setFunc {arg ...funcs;
		var tmpEvent=funcs.asEvent;
		tmpEvent.keysValuesDo{|key,val| funcsEvent[key]=val};
		^this
	}

	play {arg renderNew=true, newArgs=[], newFuncs=[], target, addAction, normalize=true
		, nr, filename, delete=false;
		var newArgsEvent=newArgs.asEvent;
		var newFuncsEvent=newFuncs.asEvent;
		elapsedTime=Main.elapsedTime;
		if (hasRendered.not, {renderNew=true});

		if (type==\realtime, {
			if (renderNew, {score=this.makeScore(newArgs, newFuncs)});
			this.playRealTime
		},{
			if (renderNew, {
				this.render(newArgs, newFuncs, {
					this.playDiskIn(
						newArgsEvent[\outBus]??{0}
						, newArgsEvent[\amp]??{1.0}
						, target, addAction, nil, delete)}
				, filename, normalize, delete
				);
			},{
				this.playDiskIn(
					newArgsEvent[\outBus]??{0}
					, newArgsEvent[\amp]??{1.0}
					, target, addAction
					, nr, delete
				)
			})
		});
		^score
	}

	type_ {arg t;
		if (t==\realtime, {score=this.makeScore});
		type=t;
		^this
	}

	playRealTime {
		Score.play(score, server)
	}

	playDiskIn {arg outBus=0, amp=1.0, tarGet, addaction, nr, delete=false;
		var pat=if (nr==nil, {path}, {paths.wrapAt(nr)});
		var tmpBuf;
		var synth, o;
		tarGet=tarGet??{target};
		addaction=addaction??{addAction};
		if (pat!=nil, {
			{
				tmpBuf=Buffer.cueSoundFile(server, pat, 0, argsEvent[\numChannels], 262144);
				server.sync;
				synth=Synth((\clusterDiskInSynthDef++argsEvent[\numChannels]).asSymbol
					, [\bufnum, tmpBuf, \outBus, outBus, \amp, amp]
					, tarGet
					, addaction).register;
				server.sync;
				synths.add(synth);
				oscFuncs.add(
					o=OSCFunc({arg msg;
						tmpBuf.close;
						tmpBuf.free;
						oscFuncs.remove(o);
						synths.remove(synth);
						if (delete, {File.delete(path)});
					}, '/n_end', server.addr, nil, [synth.nodeID]).oneShot;
				);
			}.fork
		});
	}

	playPlayBuf {arg outBus=0, amp=1.0, tarGet, addaction, nr;
		var pat=if (nr==nil, {path}, {paths.wrapAt(nr)});
		var tmpBuf;
		var synth, o;
		tarGet=tarGet??{target};
		addaction=addaction??{addAction};
		if (pat!=nil, {{
			tmpBuf=Buffer.read(server, pat, 0);
			server.sync;
			synth=Synth((\clusterPlayBufSynthDef++argsEvent[\numChannels]).asSymbol
				, [\bufnum, tmpBuf, \outBus, outBus, \amp, amp]
				, tarGet
				, addaction).register;
			server.sync;
			synths.add(synth);
			oscFuncs.add(
				o=OSCFunc({arg msg;
					tmpBuf.close;
					tmpBuf.free;
					oscFuncs.remove(o);
					synths.remove(synth);
					//if (deleteAfterPlay, {File.delete(path)});
				}, '/n_end', server.addr, nil, [synth.nodeID]).oneShot;
			);
		}.fork
		});
	}

	makeScore {arg newArgs, newFuncs;//argtarget, argaddAction;
		var newArgsEvent, newFuncsEvent;
		newArgs=newArgs??{[]};
		newFuncs=newFuncs??{[]};
		newArgsEvent=newArgs.asEvent;
		newFuncsEvent=newFuncs.asEvent;
		newArgsEvent.keysValuesDo{arg key,val; argsEvent[key]=val};
		newFuncsEvent.keysValuesDo{arg key,val; funcsEvent[key]=val};
		score=scoreFunction.value(argsEvent, funcsEvent);
		^score
	}

	render {arg newArgs, newFuncs, action, filename, normalizeFile=false, delete=false;
		score=this.makeScore(newArgs, newFuncs);
		fileName=filename??{fileName};
		nr=UniqueID.next;
		path=folderName++fileName++nr++"."++argsEvent[\headerFormat].toLower;
		if (delete.not, {paths=paths.add(path)});
		serverOptions.numOutputBusChannels_(argsEvent[\numChannels]);
		hasRendered=true;
		Score.render(
			score
			, path
			, argsEvent[\numChannels]
			, server.sampleRate
			, argsEvent[\headerFormat]
			, argsEvent[\sampleFormat]
			, action
			, false
			, normalizeFile??{normalize}
			, nil//condition
			, true
			, serverOptions
		)
	}

	stopPlaying {
		if (synth.isPlaying, {synth.free});
	}

	stopPlayingAll {
		synths.do{arg synth; if (synth.isPlaying, {synth.free})};
		synths=List[];
	}

	scoreFunction_ {arg func;
		scoreFunction=func;
	}

	read {arg path;
		if (path!=nil, {
			argsEvent[\buffer]=Buffer.read(server, path, bufnum:argsEvent[\buffer].bufnum)
		})
	}

	close {arg deleteFiles=true;
		if (deleteFiles, {paths.do{arg path; File.delete(path)}});
		oscFuncs.do(_.free);
		synths.do{arg synth; if (synth.isPlaying, {synth.free})};
	}

	free {arg deleteFiles=true;
		this.close(deleteFiles);
	}

	makeGUI {arg parent, bounds=350@20;
		gui=ClusterJTGUI(this, parent, bounds);
		^gui
	}
}