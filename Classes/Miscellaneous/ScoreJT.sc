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
	add {arg item, latency=0;
		if (nrt, {
			score = score.add(item);
		},{
			server.listSendBundle(server.latency+latency, item.copyToEnd(1))
		})
	}
	recordNRT {}
	render {arg outputFilePath, sampleRate=48000, headerFormat="aiff", sampleFormat="int24", options, action=nil;
		if (nrt, {
			Score(score).recordNRT(outputFilePath: outputFilePath
				, sampleRate: sampleRate, headerFormat: headerFormat, sampleFormat: sampleFormat, options: options, action: action)
		});
	}
	play {
		Score.play(score, server)
	}
}