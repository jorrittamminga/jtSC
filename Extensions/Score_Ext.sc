/*
render alle path dingen .unixPath maken
*/

+ Score {

	isPlaying2 {
		^isPlaying
	}
	/*
	//replace original play
	play { arg server, clock, quant=0.0, stopRoutine=false;
	var size, osccmd, timekeep, inserver, rout;
	isPlaying.not.if({
	inserver = server ? Server.default;
	size = score.size;
	timekeep = 0;
	routine = Routine({
	size.do { |i|
	var deltatime, msg;
	osccmd = score[i];
	deltatime = osccmd[0];
	msg = osccmd.copyToEnd(1);
	(deltatime-timekeep).wait;
	inserver.listSendBundle(inserver.latency, msg);
	timekeep = deltatime;
	};
	isPlaying = false;
	});

	isPlaying = true;

	routine.play(clock, quant);
	}, {"Score already playing".warn;}
	);
	}
	*/

	/*
	recordNRT { arg oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100, headerFormat =
	"AIFF", sampleFormat = "int16", options, completionString="", duration = nil, action = nil;
	this.writeOSCFile(oscFilePath, 0, duration);
	unixCmd(program + " -N" + oscFilePath.quote
	+ if(inputFilePath.notNil, { inputFilePath.quote }, { "_" })
	+ outputFilePath.quote
	+ sampleRate + headerFormat + sampleFormat +
	(options ? Score.options).asOptionsString
	+ completionString, action);
	}

	*recordNRT { arg list, oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100,
	headerFormat = "AIFF", sampleFormat = "int16", options, completionString="", duration = nil, action = nil;

	oscFilePath,
	outputFilePath,
	inputFilePath,
	sampleRate = 44100,
	headerFormat = "AIFF",
	sampleFormat = "int16",
	options

	thisScore, "help-oscFile.osc", ~path ++ "FreezeChakra"  ++ "-" ++ key ++ ".aif"
	Score
	lists.do{|list, i|
	this.new(list).recordNRT(oscFilePath, outputFilePath, inputFilePath, sampleRate.wrapAt(i),
	headerFormat.wrapAt(i), sampleFormat.wrapAt(i), options.wrapAt(i), completionString.wrapAt(i), duration.wrapAt(i), action);
	};

	}
	*/
	*playRT { arg list, server;
		^this.new(list).playRT(server);
	}

	*render {arg score, path, numberOfChannels=2, sampleRate, headerFormat ="AIFF"
		, sampleFormat = "int24", action, open=false, normalize=false, condition
		, deleteOSCfile=true, options;
		var o, extension;
		var tmpSampleFormat=sampleFormat, tmpPath;
		var oscFilePath;
		path=path??{thisProcess.nowExecutingPath};
		if (path==nil, {path="~/tmp".standardizePath});
		extension=headerFormat.toLower;
		path=path.splitext[0];
		sampleRate=sampleRate??{Server.default.sampleRate};
		if (sampleRate==nil, {sampleRate=44100});
		//------------------------------------------------- start SERVEROPTIONS
		if (options.class==ServerOptions, {o=options},{
			o = ServerOptions.new;
			o.verbosity_(-1);
			o.numOutputBusChannels = numberOfChannels;
			o.maxNodes=2**18;
			o.memSize=2**18;
			o.maxSynthDefs = 2**18;
		});
		o.numOutputBusChannels = numberOfChannels??{o.numOutputBusChannels};
		//------------------------------------------------- end SERVEROPTIONS
		path=path++"."++extension;
		tmpPath=path;
		if (normalize, {
			tmpSampleFormat="float";
			tmpPath=path++"tmp";
			//tmpPath="/Users/jorrittamminga/Desktop/test.aif";
		});
		//"\nRendering, please wait.....\n".postln;
		{
			var cond = Condition.new, outputFile;
			oscFilePath=path.dirname++"/help-oscFile";
			this.recordNRT(score, oscFilePath, tmpPath, nil, sampleRate
				, headerFormat
				, tmpSampleFormat, o, action:
				{cond.unhang}
				//action
			);
			cond.hang;
			if (deleteOSCfile, {File.delete(oscFilePath)});
			if (normalize, {
				action=action.addFunc({
					cond.unhang;
					("rm "++tmpPath.unixPath).unixCmd;
					if (open, {
						("open " ++ (path.unixPath)).unixCmd;
						if (condition.class==Condition, {
							condition.unhang
						});
					},{


					});
				});
				outputFile=SoundFile.normalizeJT(tmpPath, path, headerFormat, sampleFormat
					, threaded: true, actionWhenReady: action
				);
				cond.hang;

				/*
				SoundFile.normalizeNRT(tmpPath, path
					, headerFormat, sampleFormat
					, threaded:true
					, action: action
				);
				*/
			},{
				action.value;
				if (condition.class==Condition, {
					condition.unhang
				});
				if (open, {
					("open " ++ (path.asUnixPath)).unixCmd;
				})
			});
			if (condition.class==Condition, {
				condition.unhang
			});
			"".postln;
			//action.value
		}.fork
	}


	playRT { arg server, clock, quant=0.0, time=0, index=0, nodes, extraosccmd;
		var size, osccmd, timekeep, inserver, rout;
		isPlaying.not.if({
			inserver = server ? Server.default;
			size = score.size-index;
			timekeep = time;
			routine = Task({
				size.do { |i|
					var deltatime, msg;
					osccmd = score[i+index];
					deltatime = osccmd[0];
					msg = osccmd.copyToEnd(1);
					(deltatime-timekeep).wait;
					inserver.listSendBundle(inserver.latency, msg);
					timekeep = deltatime;
				};
				isPlaying = false;
			});
			isPlaying = true;

			if (extraosccmd!=nil, {inserver.listSendBundle(inserver.latency, extraosccmd)});
			routine.play(clock);//, quant
		}, {"Score already playing".warn;}
		);
	}

	pause {
		if (routine.class==Task, {if (routine.isPlaying, {routine.pause})})
	}

	resume {
		if (routine.class==Task, {routine.resume})
	}

	reset {
		routine.reset
	}


	resetTo { arg time, completionMessage;
		var index=0, gate, nodes=[], freeNodes=[], osc=Array[], nodeID, isPlaying;
		var osccmd=();
		isPlaying=routine.isPlaying;
		this.stop;
		Server.default.freeAll;
		while({score[index][0]<=time},{
			score[index].copyToEnd(1).do({|i|
				osc=osc.add(i);
				/*
				if (i[0]=='\n_free', {
				i.copyToEnd(1).do({|id|
				freeNodes=freeNodes.add(id);
				nodes.remove(id);
				});
				});
				if (i[0]=='\s_new', {
				nodeID=i[2];
				osccmd[nodeID]=(\n_set:(), \n_setn:());
				osccmd[nodeID][\s_new]=i.copyToEnd(1);
				if (nodes.includes(nodeID).not, {
				nodes=nodes.add(i[2]);
				//osc=osc.add(i);
				})
				},{
				//osc=osc.add(i);
				if ((i[0]=='\n_set') || (i[0]=='\n_setn'), {
				if (osccmd[nodeID]!=nil, {
				nodeID=i[1];
				i.copyToEnd(2).clump(2).do({|q| osccmd[nodeID][i[0]][q[0]]=q[1]});
				});
				nodeID=i[1];
				gate=i.indexOf(\gate);
				if (gate!=nil, {if (i[gate+1]==0, {freeNodes=freeNodes.add(i[1]);nodes.remove(i[1])})});
				});
				});
				*/
			});
			//s.listSendBundle(nil,~score[~index].copyToEnd(1));
			index=index+1
		});

		Server.default.listSendBundle(nil,osc);
		//osccmd  ('freq':1000, 'amp':0.1).asKeyValuePairs;
		//	if (isPlaying, {
		this.playRT(time:time, index:index, extraosccmd: completionMessage);
		//		},{
		//		Server.default.listSendBundle(nil,completionMessage.postln);
		//		});
		^index
	}

}