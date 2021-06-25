+ TempoClock {
	sync {arg server=Server.default;//arg latency=0.2;
		var latency=server.latency??{0}, latencyBeats=latency*this.tempo, beats=this.beats, beatsi=beats.asInteger, beatsil, beatsr=beats.round(0.5).asInteger, beatsrl=beatsr+latencyBeats;
		//[beats, beatsi, beatsr, beatsrl, this.tempo, latencyBeats].postln;
		this.beats_(beatsrl);
		/*
		if (beatsr>beatsi, {
		this.beats_(beatsrl)
		},{
		beatsil=(this.beats-latencyBeats).asInteger;
		if ( beatsil<beatsi, {
		tempo=this.tempo;
		zero=0.00001;
		{
		this.beats_(beatsrl);
		this.tempo_(zero);
		latency.wait;
		this.tempo_(tempo);
		}.fork
		},{
		this.beats_(beatsrl)
		})
		})
		*/
		/*
		var tempo, tmpTempo=750.0, beats, diffbeats, latencybeats, waitTime, zero=0.00000001, latency=server.latency.copy;
		latencybeats=latency*this.tempo;
		tempo=this.tempo;
		beats=this.beats-latencybeats;
		diffbeats=(beats.round(1.0)-beats);
		if (diffbeats<0.0, {
		waitTime=diffbeats.abs*this.beatDur;
		{
		this.tempo_(zero);
		waitTime.wait;
		this.tempo_(tempo);
		}.fork;
		},{
		tmpTempo=server.sampleRate/server.options.blockSize;
		//var nextB=this.beats.ceil;
		waitTime=tmpTempo.reciprocal*diffbeats;
		{
		//server.latency=nil;
		this.tempo_(tmpTempo);
		waitTime.wait;
		//server.latency=latency;
		this.tempo_(tempo);
		}.fork;
		});
		*/
	}
	/*
	sync2 {
	if (this.beats.frac>0.5, {
	this.beats_(this.beats.ceil.postln)
	},{
	this.beats_(this.beats.floor.postln)
	});
	}
	*/
}