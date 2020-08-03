DelayX {
	*ar {
		arg in, maxdelaytime=0.2, delaytime=0.2, lag=0.1, mul=1.0, add=0.0;
		var buf={LocalBuf(SampleRate.ir*maxdelaytime)}!2;
		var trig, ff, fadepan;

		in=Line.kr(0,1,delaytime)*in;

		delaytime=Latch.kr(delaytime, Impulse.kr(lag.reciprocal*0.5));
		trig=Changed.kr(delaytime);

//		trig=Trig1.kr(Changed.kr(delaytime), lag*2);
//		trig=trig+TDelay.kr(trig, lag*5);

		ff=ToggleFF.kr(trig);
		fadepan=ff*2-1;

		^XFade2.ar(
			BufDelayN.ar(buf[0], in, Latch.kr(delaytime, 1-ff))
			, BufDelayN.ar(buf[1], in, Latch.kr(delaytime, ff))
			, Ramp.kr(fadepan, lag)
			, mul)+add	
	}
}


CombX {
	*ar {
		arg in, maxdelaytime=0.2, delaytime=0.2, decaytime=3.0, lag=0.1, mul=1.0, add=0.0;
		var buf={LocalBuf(SampleRate.ir*maxdelaytime)}!2;
		var trig, ff, ramp;

		in=Line.kr(0,1,delaytime)*in;
		
		delaytime=Latch.kr(delaytime, Impulse.kr(lag.reciprocal*0.5));
		trig=Changed.kr(delaytime);

//		trig=Trig1.kr(Changed.kr(delaytime), lag*2);
//		trig=trig+TDelay.kr(trig, lag*5);		

		ff=ToggleFF.kr(trig);
		ramp=Ramp.kr(ff, lag);
		
		^XFade2.ar(
			BufCombN.ar(buf[0], in, Latch.kr(delaytime, 1-ff), (1-ramp)*decaytime)
			, BufCombN.ar(buf[1], in, Latch.kr(delaytime, ff), ramp*decaytime)
			, ramp*2-1
			, mul)+add
	}
}


AllpassX {
	*ar {
		arg in, maxdelaytime=0.2, delaytime=0.2, decaytime=3.0, lag=0.1, mul=1.0, add=0.0;
		var buf={LocalBuf(SampleRate.ir*maxdelaytime)}!2;
		var trig, ff, ramp;

		in=Line.kr(0,1,delaytime)*in;

		delaytime=Latch.kr(delaytime, Impulse.kr(lag.reciprocal*0.5));
		trig=Changed.kr(delaytime);

//		trig=Trig1.kr(Changed.kr(delaytime), lag*2);
//		trig=trig+TDelay.kr(trig, lag*5);

		ff=ToggleFF.kr(trig);		
		ramp=Ramp.kr(ff, lag);
		
		^XFade2.ar(
			BufAllpassN.ar(buf[0], in, Latch.kr(delaytime, 1-ff), (1-ramp)*decaytime)
			, BufAllpassN.ar(buf[1], in, Latch.kr(delaytime, ff), ramp*decaytime)
			, ramp*2-1
			, mul)+add
	}
}


/*

(
s.waitForBoot{
	x={arg dt=1.0, decay=4, lag=0.1;
		var in=SinOsc.ar(LFDNoise1.kr(1.0).exprange(440,1880),0,0.1);
		CombX.ar(in, 5.0, dt, decay, lag)
	}.play;
};
w=Window.new.front; w.addFlowLayout; w.onClose_{s.freeAll};
EZSlider(w, 350@20, \dt, ControlSpec(0.0, 4.0), {|ez| x.set(\dt, ez.value)}, 1);
EZSlider(w, 350@20, \decay, ControlSpec(0.01, 14.0, \exp), {|ez| x.set(\decay, ez.value)}, 4);
EZSlider(w, 350@20, \lag, ControlSpec(0.001, 2.0, \exp), {|ez| x.set(\lag, ez.value)}, 0.1);
)

*/