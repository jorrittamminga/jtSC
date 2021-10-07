/*
UNDER CONSTRUCTION!

DGrainInJT {

	*inits {arg in=0.0, dur=0.10, rate=1.0, overLap=4.0, maxdelaytime=5, delayTime=0.005, run=1.0, rateScaling= -0.85, timeJitter=0.005, mul=1.0, fb=0.0, overLapScaling=0.5.neg;

		var buf, impulse, pos;
		var isRunning, phase, input, output, bdR, bsR, jitterR;
		var deviation=0, phaseTmp, maxDur, method=\ir;

		buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		bdR=BufDur.ir(buf).reciprocal;
		bsR=BufSampleRate.ir(buf).reciprocal;
		if (rate.rate==\demand, {
			rate=Dunique(rate, 65536)
		});
		dur=rate.abs.pow(rateScaling).min(1.0)*dur;
		dur=Dunique(dur, 65536);
		impulse=TDuty.ar(dur/overLap);
		maxDur = Slew.ar(Demand.ar(impulse, 0, dur), SampleRate.ir, 1.0);
		phase=Phasor.ar(0, (run.lag(0, maxDur)>0.0001), 0, BufFrames.ir(buf),0);
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(maxDur)));
		BufWr.ar( input, buf, phase, 1);
		phase=Gate.ar(phase, run-0.1);
		if (rate.rate==\scalar, {
			if (rate<0, {delayTime=delayTime-(rate.abs*dur)})
		},{
			delayTime=delayTime-((rate<0)*rate.abs*dur);
		});
		timeJitter=Dwhite(0, timeJitter);
		deviation=((rate.abs-1).max(0)*dur) + delayTime + timeJitter + deviation + ControlDur.ir;
		deviation=deviation + ((1-run)*dur);
		deviation=deviation.max(0);
		pos=(phase*bsR-Demand.ar(impulse, 0, deviation))*bdR;
		^[buf, impulse, pos, dur.min(maxdelaytime)
			, if (rate.rate==\scalar, {rate},{Demand.ar(impulse, 0, rate)})
			, overLap.max(1).pow(overLapScaling)*mul]
	}
}

DGrainBufInJT : DGrainInJT {
	*ar {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=4, envbufnum= -1, maxGrains = 2048
		, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, overLapScaling= -0.5;
		var buf, impulse, pos, grainDur, amplitude;
		#buf, impulse, pos, grainDur, rate, amplitude=this.inits(in, dur, rate, overLap, maxdelaytime, delayTime, run, rateScaling, timeJitter, mul, fb, overLapScaling);
		^GrainBuf.ar(numChannels, impulse, Demand.ar(impulse, 0, grainDur), buf, rate, pos, interp
			, if (az.rate==\demand, {if (impulse.rate==\control, {Demand.kr(impulse, 0, az)},{Demand.ar(impulse, 0, az)})},{az})
			, envbufnum, maxGrains, amplitude, add);
	}
}
*/