PitchShifterK {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, minRateScaling=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;

		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal
		, bsR=BufSampleRate.ir(buf).reciprocal;

		//run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		phase=Phasor.ar(0, run>0, 0, BufFrames.ir(buf),0);

		//input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));
		input=in*run.lag(dur);

		BufWr.ar(input,buf,phase,1);

		dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;

		delayTime=delayTime-((rate<0)*rate.abs*dur);

		pos=(
			A2K.kr(phase)*bsR
			-((rate.abs-1).max(0)*dur)
			- delayTime.max(0)
			- WhiteNoise.kr(timeJitter,timeJitter)
			- ControlRate.ir.reciprocal
			- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;

		^GrainBuf.ar(numChannels
			, Impulse.kr(dur.reciprocal*overLap)
			, dur, buf, rate, pos, interp, az
			, envbufnum
			, maxGrains
			,overLap.max(1).reciprocal.sqrt*mul, add);
	}
}