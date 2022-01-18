(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var overlap=124;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25);
		//in=SinOsc.ar(440, 0, 0.25);

		GrainBufInJT.ar(2
			, in*Line.kr(0.0, 1.0, 5.0)
			//, Dseq([1, 1/2, 1/3, 1/4, 1/5],inf)
			//, Dseq([1, 0.5, 0.3],inf), Dseq([1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7],inf)*Dwhite(0.9, 1.1)
			, Dwhite(0.2, 0.3), Drand([0.25, 0.5, 1,2,3,4,5,6,7,8],inf)*Dwhite(0.99, 1.01)
			, 1.0/64
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, run:run)
	}.play
}
)
x.set(\run, 1)

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var overlap=124;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25);
		//in=SinOsc.ar(440, 0, 0.25);

		TGrainsInJT.ar(2
			, in*Line.kr(0.0, 1.0, 5.0)
			//, Dseq([1, 1/2, 1/3, 1/4, 1/5],inf)
			//, Dseq([1, 0.5, 0.3],inf), Dseq([1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7],inf)*Dwhite(0.9, 1.1)
			, Dwhite(0.2, 0.3), Drand([0.25, 0.5, 1,2,3,4,5,6,7,8],inf)*Dwhite(0.99, 1.01)
			, 1.0/64
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, run:run)
	}.play
}
)


(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);

		GrainBufInJT.ar(2
			, in
			, Dwhite(0.2, 0.3), Drand([0.5, 1, 2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, run:run)
	}.play
}
)
EZSlider(nil, 350@40, \overlap, [1, 1024, \exp, 1], {|ez| x.set(\rOverLap, 1/ez.value)}, 128);
x.free;


(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		TGrainsInJT.ar(2
			, in
			, Dwhite(0.2, 0.3), Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, 0.005)
	}.play
}
)
EZSlider(nil, 350@40, \overlap, [1, 1024, \exp, 1], {|ez| x.set(\rOverLap, 1/ez.value)}, 128);
x.set(\rOverLap, 1/60)
x.free


(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		TGrains2InJT.ar(2
			, in
			, Dwhite(0.2, 0.3), Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, 0.005, 0.01, 0.02, 0.1)
	}.play
}
)

(
s.waitForBoot{
	var y, h;
	c=Buffer.alloc(s, s.sampleRate * 0.2, bufnum:1); s.sync;
	y=Signal.hanningWindow(s.sampleRate * 0.4); // twice the size of our buffer, 'cause we only use 1/2
	h=Array.fill(s.sampleRate * 0.2, {|i| y[i]});
	c.loadCollection(h); s.sync;

	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		TGrains3InJT.ar(2
			, in
			, Dwhite(0.2, 0.3), Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.85, 0.005, 0.01, 0.02, 1, 0.1)
	}.play
}
)

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in, out;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		out=BufGrainBFInJT.ar(in
			, Dwhite(0.2, 0.3)
			, Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, 0, 1.0, -0.85, 0.005);
		DecodeB2.ar(2, out[0], out[1], out[2])
	}.play
}
)
a.plot

(
s.waitForBoot{
	a=Buffer.loadCollection(s, Env.perc(0.1, 1.0).discretize(1024).as(Array)); s.sync;
	b=Buffer.loadCollection(s, Env.sine.discretize(1024).as(Array)); s.sync;
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in, out, ifac;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		ifac=LFDNoise1.kr(0.1).range(0.0, 1.0);
		out=BufGrainIBFInJT.ar(in
			, Dwhite(0.2, 0.3)
			, Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, rOverLap
			, 60.0
			, Dwhite(0.0, 2.0)
			, Dwhite(-1.0, 1.0)
			, 2, a.bufnum, b.bufnum, ifac, 0, 1.0, -0.85, 0.005);
		DecodeB2.ar(2, out[0], out[1], out[2])
	}.play
}
)
EZSlider(nil, 350@40, \overlap, [1, 128, \exp, 1], {|ez| x.set(\rOverLap, 1/ez.value)}, 128);


(
s.waitForBoot{
	a=Buffer.loadCollection(s, Env.perc(0.1, 1.0).discretize(1024).as(Array)); s.sync;
	b=Buffer.loadCollection(s, Env.sine.discretize(1024).as(Array)); s.sync;
	x={arg run=1, freqL=200, freqH=2000, del=0.0, rOverLap=0.0078125;
		var in, out, ifac;
		RandSeed.ir(1, 1973);
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		ifac=LFDNoise1.kr(0.1).range(0.0, 1.0);
		out=GranulatorIBF.ar(in, WhiteNoise.ar(1.0).range(0.5, 2), WhiteNoise.ar(1.0).range(0.2, 0.3), 128, 60, WhiteNoise.ar(1.0).range(0.0, 2.0), 2, WhiteNoise.ar(1.0), 0, 1, a.bufnum, b.bufnum, ifac);
		DecodeB2.ar(2, out[0], out[1], out[2])
	}.play
}
)

//==============================================================================================================================
//==============================================================================================================================
//==============================================================================================================================

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var in;
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		TGrainsInJT.ar(2
			, in
			, Dwhite(0.2, 0.2), Drand([0.5,1,2],inf)*Dwhite(0.99, 1.01)
			, 1.0/12
			, 60.0
			, Dwhite(0.0, 0.0)
			, Dwhite(-1.0, 1.0)
			, 2, -1, 65536, -0.01, 0.005)
	}.play
}
)

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var in;
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		//in=SoundIn.ar(0);
		PitchShift.ar(in, 0.2, [0.5, 1, 2.0], 0.001, 0.1, 0.5).sum!2
	}.play
}
)



(
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrit/Dropbox/Samples/malespeaker.aiff", bufnum:10); s.sync;
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var in, dur;
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		dur=WhiteNoise.ar(0.2, 0.3);
		GrainBuf.ar(2, Impulse.ar(dur.reciprocal*128), dur, b.bufnum, WhiteNoise.ar(0.01, 1.0), WhiteNoise.ar(1.0), 2, WhiteNoise.ar(1.0), -1, 65536, 0.1)
	}.play
}
)



x.set(\run, 1)

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var in;
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);
		PitchShifter.ar(in, WhiteNoise.ar(0.01, 1.0), WhiteNoise.ar(0.05, 0.25), 128, 60, WhiteNoise.ar(1.0, 1.0), 2, 2, WhiteNoise.ar(1.0), -1, 65536);
	}.play
}
)

(
s.waitForBoot{
	x={arg run=1, freqL=200, freqH=2000, del=0.0;
		var in;
		in=SinOsc.ar(LFDNoise1.kr(0.1).exprange(freqL.lag(1.0), freqH.lag(1.0)), 0, 0.25)*Line.kr(0.0, 1.0, 5.0);
		//in=SinOsc.ar(440, 0, 0.25);

		GrainBufInJT.ar(2
			, in
			, WhiteNoise.ar(0.05, 2.5)
			, WhiteNoise.ar(0.01, 1.0)
			, 1.0/128
			, 60.0
			, WhiteNoise.ar(1.0, 1.0)
			, WhiteNoise.ar(1.0)
			, 2, -1, 65536, -0.85, run:run)
	}.play
}
)