NHHallSurround {
	*ar {
		|
		in,
		rt60 = 1,
		stereo = 0.5,
		lowFreq = 200,
		lowRatio = 0.5,
		hiFreq = 4000,
		hiRatio = 0.5,
		earlyDiffusion = 0.5,
		lateDiffusion = 0.5,
		modRate = 0.2,
		modDepth = 0.3,
		diffuse = 0.0
		|
		var n, inLaced;
		in = in.asArray;
		n=in.size;
		in=in.clump(2);
		in=in.collect{|x,i|
			[in.wrapAt(i-1)[1], in.wrapAt(i+1)[0]]*diffuse+x
		};
		^in.collect{|in|
			NHHall.ar(
				in,
				rt60,
				stereo,
				lowFreq,
				lowRatio,
				hiFreq,
				hiRatio,
				earlyDiffusion,
				lateDiffusion,
				modRate,
				modDepth
			)
		}.flat.lace(n)
	}
}
//[0,1,2,3].clump(2)
//BFGVerb
/*
x=[0,1,2,3,4].clump(2);
x.do{|d,i| d.post; [x.wrapAt(i-1).wrapAt(1), x.wrapAt(i+1)[0]].postln}
x.collect{|d,i| ([x.wrapAt(i-1).wrapAt(1), x.wrapAt(i+1)[0]]+d).postln}
*/