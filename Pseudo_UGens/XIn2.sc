//InX
//dit moet toch een stuk subtieler kunnen?

InX {
	
	*ar {
		arg inBus=50, numChannels = 1, xfade=0.1, t_trig=1; 
		var count=0, change=Changed.kr(inBus)+Trig1.kr(t_trig), in;
		var buf=LocalBuf(2).set([inBus,inBus]);
		count=Demand.kr(change,0, Dseq([1,0],inf));
		Demand.kr(change,0, Dbufwr(inBus, buf, Dseq([1,0],inf), 1));
		in=In.ar(Demand.kr(change,0, Dbufrd(buf, [0,1])), numChannels);
		in=XFade2.ar(in[0], in[1], Ramp.kr(count, xfade)*2-1);
		^in
	}
	
}

XIn2 : InX

/*
s.boot;
z={arg inBus=50; InX.ar(inBus, 1, 1)}.play;
x={Out.ar(50, SinOsc.ar(1000,0,0.1))}.play;
y={Out.ar(51, PinkNoise.ar(0.1))}.play;
z.set(\inBus, 51);
z.set(\inBus, 50);
*/