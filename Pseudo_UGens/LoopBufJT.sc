LoopBufJT {
	*ar{arg numChannels, bufnum=0, rate=1.0, trigger=0, startPos=0.0, endPos= -1, interpolation=2, fadeTime=0.1;
		//arg fadeTime=0.0, startPos=0, endPos= -1, bufnum=0, rate=1.0;
		var phase, env, gate, trig, bufferChanged, out, ff;
		var masterPhasor, maxPhase;
		var numFrames, duration;

		switch ( trigger.rate,
			 \audio, {
				//index = Stepper.ar( trigger, 0, 0, n-1 );
			 },
			 \control, {
				trigger=Trig1.kr(trigger, 0.0001);
			},
			\demand, {
				trigger = TDuty.ar( trigger ); // audio rate precision for demand ugens
			},
			{ trigger=Trig1.kr(trigger, 0.00001) }
		);

		rate=rate*BufRateScale.kr(bufnum);
		endPos=endPos.lag(0.0);//silly way to turn this into a UGen.... there must be a smarter way....
		endPos=((endPos<0)*BufFrames.kr(bufnum)+1)+endPos;
		numFrames=endPos-startPos;
		duration=numFrames*BufSampleRate.kr(bufnum);
		fadeTime=fadeTime.min(duration*rate.abs.reciprocal*0.5);
		bufferChanged=Changed.kr(BufFrames.kr(bufnum));
		masterPhasor=Phasor.ar(trigger+bufferChanged, rate, 0, numFrames-(fadeTime*BufSampleRate.kr(bufnum)), 0);
		trig=Trig1.ar(masterPhasor-1, 0.001);
		ff=ToggleFF.ar(trig);
		ff=[ff, 1-ff];
		phase=Phasor.ar(ff, rate, startPos, BufFrames.kr(bufnum), startPos);
		fadeTime=fadeTime.reciprocal;
		env=Slew.ar(ff, fadeTime, fadeTime).sqrt;
		out=BufRd.ar(numChannels, bufnum, phase, 1, 4)*env;
		^out.sum
	}
}
