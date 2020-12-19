/*
IN PROGRESS!
*/
GrainBufDelayD {
	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048
		, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var timeR=maxdelaytime.reciprocal, framesR=(SampleRate.ir*maxdelaytime).reciprocal;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		var phase, trigger, pos;
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf), 0);
		trigger=TDuty.ar(overLap.reciprocal*dur, 0, 1, 0, 1);
		#rate, delayTime, az=Demand.ar(trigger, 0, [rate, delayTime, az]);
		pos=phase*framesR;
		pos=pos - ((delayTime - ((rate.abs-1).max(0)*dur))*timeR);
		BufWr.ar(in, buf, phase, 1);
		^GrainBuf.ar(numChannels, trigger, dur, buf, rate, pos, interp, az, envbufnum, maxGrains, overLap.max(1).reciprocal.sqrt*mul, add);
	}
	*kr {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048
		, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var timeR=maxdelaytime.reciprocal, framesR=(SampleRate.ir*maxdelaytime).reciprocal;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		var phase, trigger, pos;
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf), 0);
		trigger=TDuty.kr(overLap.reciprocal*dur, 0, 1, 0, 1);
		#rate, delayTime, az=Demand.kr(trigger, 0, [rate, delayTime, az]);
		pos=phase*framesR;
		pos=pos - ((delayTime - ((rate.abs-1).max(0)*dur))*timeR);
		BufWr.ar(in, buf, phase, 1);
		^GrainBuf.ar(numChannels, trigger, dur, buf, rate, pos, interp, az, envbufnum, maxGrains, overLap.max(1).reciprocal.sqrt*mul, add);
	}
}

GrainBufDelay {
	*ar {arg in=0.0//, maxdelaytime=5.0, delayTime=1.0, delayTime=0.2, decayTime=0.5
		, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048
		, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var timeR=maxdelaytime.reciprocal, framesR=(SampleRate.ir*maxdelaytime).reciprocal;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		var phase, trigger, pos;
		var decayTime=3.0, coef=0.3;
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf), 0);
		trigger=Impulse.ar(dur.reciprocal*overLap, 0, 1, 0, 1);
		pos=phase*framesR;
		pos=pos - ((delayTime - ((rate.abs-1).max(0)*dur))*timeR);
		//in=CombLP.ar(in, 1, 1.0, delayTime[1].min(1.0), decayTime, coef, decayTime.min(0.5), in);
		BufWr.ar(in, buf, phase, 1);
		^GrainBuf.ar(numChannels, trigger, dur, buf, rate, pos, interp, az, envbufnum, maxGrains, overLap.max(1).reciprocal.sqrt*mul, add);
	}
	*kr {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048
		, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var timeR=maxdelaytime.reciprocal, framesR=(SampleRate.ir*maxdelaytime).reciprocal;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		var phase, trigger, pos;
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf), 0);
		trigger=Impulse.kr(dur.reciprocal*overLap, 0, 1, 0, 1);
		pos=phase*framesR;
		pos=pos - ((delayTime - ((rate.abs-1).max(0)*dur))*timeR);
		BufWr.ar(in, buf, phase, 1);
		^GrainBuf.ar(numChannels, trigger, dur, buf, rate, pos, interp, az, envbufnum, maxGrains, overLap.max(1).reciprocal.sqrt*mul, add);
	}
}