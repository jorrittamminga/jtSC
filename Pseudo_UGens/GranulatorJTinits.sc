GrainInJT {

	*inits {arg in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5, delayTime=0.005, run=1.0, rateScaling= -0.85
		, timeJitter=0.005, mul=1.0, fb=0.0, rOverLapScaling=0.5;//type=\TGrains

		var buf, impulse, pos=WhiteNoise.ar(1.0);
		var isRunning, phase, input, output, bdR, bsR, jitterR, rate2;
		var deviation=0, phaseTmp, maxDur, method=\ir;
		var tmp1, tmp2;

		buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		bdR=BufDur.ir(buf).reciprocal;
		bsR=BufSampleRate.ir(buf).reciprocal;

		if (rate.rate==\demand, {rate=Ddup(2, rate)});
		if (rOverLap.rate==\demand, {rOverLap=Ddup(2, rOverLap)});

		dur=rate.abs.pow(rateScaling).min(1.0)*dur;//scale the during according to the pitch/rate.maybe get rid off the .min

		switch ( dur.rate,
			\audio, {
				impulse=TDuty.ar(dur*rOverLap);
				maxDur = Slew.ar(dur, SampleRate.ir, 1.0);
			}, \control, {
				impulse=TDuty.kr(dur*rOverLap);
				maxDur = Slew.kr(dur, SampleRate.ir, 1.0);
			}, \demand, {
				dur=Ddup(3, dur);
				impulse=TDuty.ar(dur*rOverLap);
				maxDur = Slew.ar(Demand.ar(impulse, 0, dur), SampleRate.ir, 1.0);
		}, {maxDur = dur});

		//??[dur, rate, mul, delayTime, rOverLap].do{|par| if (par.rate==\demand, {par=Demand.ar(impulse, 0, par)})};
		if (dur.rate==\demand, {dur=Demand.ar(impulse, 0, dur)});
		if (rate.rate==\demand, {rate=Demand.ar(impulse, 0, rate)});
		if (mul.rate==\demand, {mul=Demand.ar(impulse, 0, mul)});
		if (delayTime.rate==\demand, {delayTime=Demand.ar(impulse, 0, delayTime)});
		if (rOverLap.rate==\demand, {rOverLap=Demand.ar(impulse, 0, rOverLap)});
		timeJitter=Demand.ar(impulse, 0, Dwhite(0, timeJitter));

		phase=Phasor.ar(0, (run.lag(0, maxDur)>0.0001), 0, BufFrames.ir(buf),0);
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(maxDur)));
		BufWr.ar( input, buf, phase, 1);
		phase=Gate.ar(phase, run-0.1);

		if (rate.rate==\scalar, {
			if (rate<0, {delayTime=delayTime-(rate.abs*dur)})
		},{
			delayTime=delayTime-((rate<0)*rate.abs*dur);
		});

		deviation=delayTime + timeJitter;
		deviation=((rate.abs-1).max(0)*dur) + deviation;
		deviation=deviation + ((1-run)*dur);
		deviation=deviation.max(0);

		pos=(phase*bsR-deviation)*bdR;

		dur=dur.min(maxdelaytime);
		mul=rOverLap.min(1).pow(rOverLapScaling)*mul;

		/*
		tmp1=(dur*SampleRate.ir)+phase;
		tmp2=(pos*BufFrames.ir(buf))+(dur*SampleRate.ir*rate);
		[tmp1, tmp2, rate, dur].poll(Trig1.ar(tmp2>tmp1, 0.0000001));
		//[dur, rate].poll(impulse);
		*/

		^[buf, impulse, pos, dur, rate, mul]
	}
}