ScoreJT {
	var <server, <score, <nrt;

	*new {arg nrt=true, server;
		^super.new.init(nrt, server)
	}
	init {arg argnrt, argserver;
		server=argserver??{Server.default};
		nrt=argnrt;
		score=[];
	}
	add {arg bundle, latency=0;
		if (nrt, {
			score = score.add(bundle);
		},{
			server.listSendBundle(server.latency+latency, bundle.copyToEnd(1))
		})
	}
	addSync {arg bundle, latency=0, condition;
		condition=condition??{Condition.new};
		if (nrt, {
			if (bundle[0]==nil, {bundle[0]=0});
			score = score.add(bundle);
		},{
			if ((bundle[0]==nil)||(bundle[0]==0), {
				bundle.copyToEnd(1).do{|msg|
					server.sendMsg(*msg);
					server.sync;
				}
			},{
				server.listSendBundle(server.latency+latency, bundle.copyToEnd(1))
			})
		})
	}
	recordNRT {}
	render {arg outputFilePath, sampleRate=48000, headerFormat="aiff", sampleFormat="int24", numChannels=2, options, action={"READY".postln}, normalize=false;
		var tmpPath, sf;
		var cond=Condition.new;
		outputFilePath=outputFilePath??{
			var path=thisProcess.nowExecutingPath.dirname++"/"++PathName(thisProcess.nowExecutingPath).fileNameWithoutExtension;
			path.dirname++"/"++PathName(path).fileNameWithoutExtension++"."++headerFormat.toLower
		};
		tmpPath=outputFilePath;
		options=options??{ServerOptions.new};

		options.verbosity_(-1);
		options.numOutputBusChannels = numChannels;
		//o.maxNodes=2**18;
		//o.memSize=2**18;
		//o.maxSynthDefs = 2**18;
		//path=path??{thisProcess.nowExecutingPath};
		//if (path==nil, {path="~/tmp".standardizePath});

		if (nrt, {
			if (normalize, {
				tmpPath=outputFilePath++"tmp";
				action=action.addFuncFirst({cond.unhang});
			});
			Score(score).recordNRT(outputFilePath: tmpPath
				, sampleRate: sampleRate, headerFormat: headerFormat, sampleFormat: sampleFormat, options: options, action: action);
			if (normalize, {
				cond.hang;
				sf=SoundFile.normalize(tmpPath, outputFilePath);//, threaded:true
				("rm "++tmpPath.unixPath).unixCmd;
			});
		});
	}
	play {
		Score.play(score, server)
	}
}