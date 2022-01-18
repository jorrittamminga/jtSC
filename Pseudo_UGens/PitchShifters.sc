/*
er gaat nog wel eens wat mis met hoge pitchshifts.... dan lijkt het alsof de buffer niet overschreven wordt, er klinkt hoge troepjes uit het 'verleden'
*/

QuasiPsola {
	*ar{arg in, rate=2.0, overlap=2.0, timeDev=0.0, minFreq=60.0, maxFreq=800, delayframes=0, threshold=0.93, ws=1024, overlapws=512;
		var phase, freq, hasFreq, bufnum=LocalBuf(SampleRate.ir).clear;
		var grainSize=0.0, pos;

		phase=Phasor.ar(0, BufRateScale.ir(bufnum), 0, BufFrames.ir(bufnum), 0);
		BufWr.ar(in,bufnum,phase);

		#freq,hasFreq=Tartini.kr(in, threshold, ws, 0, overlapws);//threshold=0.93, ws=1024, overlap=512

		//#freq,hasFreq=Pitch.kr(in, minFreq, maxFreq);

		freq=Gate.kr(freq,(freq<=maxFreq)*(freq>=minFreq)).clip(minFreq,maxFreq);

		grainSize=freq.reciprocal;
		pos=Latch.ar((phase/SampleRate.ir-grainSize
			-(delayframes/SampleRate.ir)-WhiteNoise.kr(timeDev*grainSize).abs
		)/BufDur.ir(bufnum),Impulse.ar(freq));

		^GrainBuf.ar(1, Impulse.ar(freq*rate), grainSize, bufnum, 1, pos, 1, 0, -1)*overlap.max(1).reciprocal.sqrt;
	}

}

//make duration and rate ALWAYS audio rate (otherwise you can have artifacts (why?????))
//check the rate of rate, dur, etc and change the rate of the Impulse accordingly

GrainDelay {

	*ar{ arg in, rate=1.0, dur=0.2, overlap=4.0, maxdelaytime=120, delayTime=0.2, numChannels=2, az=0.0, ampl=0.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear;
		var phase, centerpos, trigger;
		trigger=Impulse.ar(dur.reciprocal*overlap);
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf), 0);
		BufWr.ar(in, buf, phase);
		centerpos=phase*SampleDur.ir- delayTime - ((rate.abs-1).max(0)*dur);
		^TGrains.ar(numChannels, trigger, buf, rate, centerpos, dur, az, ampl, 4)
	}
}

PitchShifter {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning, impulse;

		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;

		rate=rate*DC.ar(1);

		run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		isRunning=(run.lag(0,dur)>tr);
		phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);

		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));

		BufWr.ar( input,buf,phase,1);

		dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;

		delayTime=delayTime-((rate<0)*rate.abs*dur);

		pos=(
			phase*bsR
			-((rate.abs-1).max(0)*dur)
			- delayTime.max(0)
			- WhiteNoise.ar(timeJitter,timeJitter)
			- ControlRate.ir.reciprocal
			- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;

		impulse=Impulse.ar(dur.reciprocal*overLap);
		//(pos/bdR).poll(impulse);

		^GrainBuf.ar(numChannels
			, impulse
			, dur, buf, rate, pos, interp, az
			, envbufnum
			, maxGrains
			, overLap.max(1).reciprocal.sqrt*mul
			, add);
	}
}

PitchShiftJT {
	*ar {arg in = 0.0, windowSize = 0.2, pitchRatio = 2.0, pitchDispersion = 0.0, timeDispersion = 0.1, mul = 1.0, add = 0.0;
		var overlap=4, interpolation=2, maxWindowSize=5;
		var buf=LocalBuf(SampleRate.ir*maxWindowSize).clear, phase, pos, out, factor=BufFrames.ir(buf).reciprocal, factor2=SampleRate.ir*factor, timeDev, ws;
		phase=Phasor.ar(1, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in, buf, phase, 1);
		timeDev=timeDispersion.min(windowSize)*factor2;
		ws=windowSize*factor2;
		pos=(
			phase*factor
			-((pitchRatio.abs-1).max(0)*ws)
			- WhiteNoise.ar.range(0, timeDev)
		);
		^GrainBuf.ar(1, Impulse.ar(windowSize.reciprocal*overlap), windowSize, buf, pitchRatio, pos, interpolation, 0, -1, 512, mul*overlap.reciprocal.sqrt, add)
	}
}

PitchShift2JT {
	*ar {arg in = 0.0, windowSize = 0.2, pitchRatio = 2.0, pitchDispersion = 0.0, timeDispersion = 0.1, mul = 1.0, add = 0.0
		, overlap=4, envelope= -1, interpolation=2, maxWindowSize=5;
		var buf=LocalBuf(SampleRate.ir*maxWindowSize).clear, phase, pos, out, factor=BufFrames.ir(buf).reciprocal, factor2=SampleRate.ir*factor, timeDev, ws;
		phase=Phasor.ar(1, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in, buf, phase, 1);
		timeDev=timeDispersion.min(windowSize)*factor2;
		ws=windowSize*factor2;
		pos=(
			phase*factor
			-((pitchRatio.abs-1).max(0)*ws)
			- WhiteNoise.ar.range(0, timeDev)
		);
		^GrainBuf.ar(1, Impulse.ar(windowSize.reciprocal*overlap), windowSize, buf, pitchRatio, pos, interpolation, 0, envelope, 512, mul*overlap.reciprocal.sqrt, add)
	}
}

PitchShiftMod {
	*ar {arg in=0.0, windowSize=0.1, pitchRatio=1.0, pitchDispersion=0, timeDispersion=0.005, mul=1.0, add=0.0, overLap=8;
		var buf=LocalBuf(SampleRate.ir*5).clear;
		var phase, pos, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;
		//pitchRatio=pitchRatio*DC.ar(1);//is dit nog nodig?
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in,buf,phase,1);
		pos=(
			phase*bsR
			-((pitchRatio.abs-1).max(0)*windowSize)
			- WhiteNoise.ar(timeDispersion,timeDispersion)
			- ControlRate.ir.reciprocal
		)*bdR;

		^GrainBuf.ar(1
			, Impulse.ar(windowSize.reciprocal*overLap)
			, windowSize, buf, pitchRatio, pos, 4, 0
			, -1
			, 512
			, overLap.max(1).reciprocal.sqrt*mul, add);
	}
}

PitchShiftModk {
	*ar {arg in=0.0, windowSize=0.1, pitchRatio=1.0, pitchDispersion=0, timeDispersion=0.005, mul=1.0, add=0.0, overLap=8;
		var buf=LocalBuf(SampleRate.ir*5).clear;
		var phase, pos, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;
		//pitchRatio=pitchRatio*DC.ar(1);//is dit nog nodig?
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in,buf,phase,1);
		pos=(
			phase*bsR
			-((pitchRatio.abs-1).max(0)*windowSize)
			- WhiteNoise.kr(timeDispersion,timeDispersion)
			- ControlRate.ir.reciprocal
		)*bdR;

		^GrainBuf.ar(1
			, Impulse.kr(windowSize.reciprocal*overLap)
			, windowSize, buf, pitchRatio, pos, 4, 0
			, -1
			, 512
			, overLap.max(1).reciprocal.sqrt*mul, add);
	}
}

PitchShiftModT {
	*ar {arg in=0.0, windowSize=0.1, pitchRatio=1.0, pitchDispersion=0, timeDispersion=0.005, mul=1.0, add=0.0, overLap=8, t_trig, tDur=0.05, tAmp=0.8, tBuf= -1;
		var buf=LocalBuf(SampleRate.ir*5).clear;
		//var bufT=LocalBuf(8192).set(Env.new([0.0, 1.0, 1.0, 0.0], [0.0, 0.9, 0.1], \sin).discretize(8192).as(Array));
		//var bufT=LocalBuf.newFrom(Env.new([0.0, 1.0, 1.0, 0.0], [0.0, 0.9, 0.1], \sin).discretize(8192).as(Array));
		var phase, pos, posT, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal, delayTime;
		//pitchRatio=pitchRatio*DC.ar(1);//is dit nog nodig?
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in,buf,phase,1);

		delayTime=((pitchRatio.abs-1).max(0)*tDur);
		pos=(
			phase*bsR
			- ((pitchRatio.abs-1).max(0)*windowSize)
			- WhiteNoise.ar(timeDispersion,timeDispersion)
			- ControlRate.ir.reciprocal
		)*bdR;
		posT=(phase*bsR-delayTime)*bdR;
		t_trig=TDelay.kr(t_trig, delayTime);
		^GrainBuf.ar(1
			, t_trig
			, tDur
			, buf
			, pitchRatio
			, posT
			, 4, 0, tBuf, 512, tAmp,
			GrainBuf.ar(1
				, Impulse.ar(windowSize.reciprocal*overLap)
				, windowSize, buf, pitchRatio, pos, 4, 0
				, -1
				, 512
				, overLap.max(1).reciprocal.sqrt*mul, add)
		)
	}
}

PitchShiftModkT {
	*ar {arg in=0.0, windowSize=0.1, pitchRatio=1.0, pitchDispersion=0, timeDispersion=0.005, mul=1.0, add=0.0, overLap=8, t_trig, tDur=0.05, tAmp=0.8, tBuf= -1;
		var buf=LocalBuf(SampleRate.ir*5).clear;
		//var bufT=LocalBuf(8192).set(Env.new([0.0, 1.0, 1.0, 0.0], [0.0, 0.9, 0.1], \sin).discretize(8192).as(Array));
		//var bufT=LocalBuf.newFrom(Env.new([0.0, 1.0, 1.0, 0.0], [0.0, 0.9, 0.1], \sin).discretize(8192).as(Array));
		var phase, pos, posT, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal, delayTime;
		//pitchRatio=pitchRatio*DC.ar(1);//is dit nog nodig?
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf),0);
		BufWr.ar(in,buf,phase,1);

		delayTime=((pitchRatio.abs-1).max(0)*tDur);
		pos=(
			phase*bsR
			- ((pitchRatio.abs-1).max(0)*windowSize)
			- WhiteNoise.kr(timeDispersion,timeDispersion)
			- ControlRate.ir.reciprocal
		)*bdR;
		posT=(phase*bsR-delayTime)*bdR;
		t_trig=TDelay.kr(t_trig, delayTime);
		^GrainBuf.ar(1
			, t_trig
			, tDur
			, buf
			, pitchRatio
			, posT
			, 4, 0, tBuf, 512, tAmp,
			GrainBuf.ar(1
				, Impulse.kr(windowSize.reciprocal*overLap)
				, windowSize, buf, pitchRatio, pos, 4, 0
				, -1
				, 512
				, overLap.max(1).reciprocal.sqrt*mul, add)
		)
	}
}

GranulatorBF {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, az=0.0, elevation=0.0, rho=1.0, rateScaling= -0.85, timeJitter=0.005, wComp=0, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;
		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;
		rate=rate*DC.ar(1);
		run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		isRunning=(run.lag(0,dur)>tr);
		phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));
		BufWr.ar( input,buf,phase,1);

		dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;
		pos=(
			phase*bsR
			-((rate.abs-1).max(0)*dur)
			- delayTime.max(0)
			- WhiteNoise.ar(timeJitter,timeJitter)
			- ControlRate.ir.reciprocal
			- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;

		^BufGrainBF.ar(
			Impulse.ar(dur.reciprocal*overLap)
			, dur, buf, rate, pos
			, az, elevation, rho, interp
			, wComp
			,overLap.max(1).reciprocal.sqrt*mul, add);
	}

}




GranulatorIBF {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5, delayTime=0.005, interp=2, az=0.0, elevation=0.0, rho=1.0, envbufnum1= -1, envbufnum2= -1, ifac=0.0
		, rateScaling= -0.85, timeJitter=0.005, wComp=0, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.0001, overLapScaling=0.5.neg, overLapScalingDepth=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;
		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal, jitterR;
		var deviation, impulse, phaseTmp;
		var maxDur;

		switch ( dur.rate,
			\audio, {maxDur = Slew.ar(dur, SampleRate.ir, 1.0)},
			\control, {maxDur = Slew.kr(dur, SampleRate.ir, 1.0)},
			/*
			\demand, {
			trigger = TDuty.ar( trigger ); // audio rate precision for demand ugens
			index = Stepper.ar( trigger, 0, 0, n-1 );
			},
			*/
			{ 	maxDur = dur });

		rate=rate*DC.ar(1);
		phase=Phasor.ar(0, (run.lag(0, maxDur)>0.0001), 0, BufFrames.ir(buf),0);
		//phaseTmp=phase;
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));
		BufWr.ar( input,buf,phase,1);
		phase=Gate.ar(phase, run-0.1);
		dur=rate.abs.pow(rateScaling).min(1.0)*dur;
		delayTime=delayTime-((rate<0)*rate.abs*dur);

		deviation=0
		- ((rate.abs-1).max(0)*dur)
		- delayTime
		- WhiteNoise.ar(timeJitter,timeJitter)//maak deze demand rate
		- ControlRate.ir.reciprocal
		//- ((1-run.lag(dur))*((dur).clip(dur, maxdelaytime-0.1)))
		//- ((1-isRunning)*(dur.min(maxdelaytime-0.1)))
		;
		//deviation=deviation*run + ((1-run)*dur.neg);
		deviation=deviation + ((1-run)*dur.neg);
		deviation=deviation.min(0);
		pos=(phase*bsR+deviation)*bdR;
		impulse=Impulse.ar(dur.reciprocal*overLap);
		/*
		[K2A.ar(run), phase
		, phaseTmp
		, pos*BufFrames.ir(buf)+(dur*SampleRate.ir)].poll(impulse);
		pos=(
		phase*bsR
		-((rate.abs-1).max(0)*dur)
		- delayTime.max(0)
		- WhiteNoise.ar(timeJitter,timeJitter)//maak deze demand rate
		- ControlRate.ir.reciprocal
		- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;
		pos.poll(10);
		*/

		^BufGrainIBF.ar(
			impulse
			, dur
			, buf
			, rate
			, pos
			, envbufnum1
			, envbufnum2
			, ifac
			, az, elevation, rho, interp
			, wComp
			//, overLap.max(1).reciprocal.sqrt*mul
			, (overLap.max(1).pow(overLapScaling)*overLapScalingDepth+(1-overLapScalingDepth))*mul
			, add);

	}
}

TGrainsDelay {

	*ar {arg in=0.0, numChannels=2, buf, rate=1.0, pos=0.005, dur=0.10, az=0.0, mul=1.0, interp=2, overLap=4.0, maxdelaytime=10.0, rateBuf, timeBuf, azBuf, ampBuf, rateScaling= -0.85;

		var phase,poss,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;
		var delayTime, impulse, ampl, fbT;
		phase=Phasor.ar(0, 1, 0, BufFrames.ir(buf),0);

		BufWr.ar(in,buf,phase,1);
		fbT=LocalIn.ar(1);
		rate=Index.ar(rateBuf, TRand.ar(0, BufFrames.kr(rateBuf), fbT));
		dur=rate.pow(rateScaling).min(1.0)*dur;
		overLap=(pos.max(dur)*dur.reciprocal).max(1.0)*overLap;

		impulse=Impulse.ar(dur.reciprocal*overLap);
		delayTime=BufRd.ar(1, timeBuf, TRand.ar(0,BufFrames.kr(timeBuf),impulse) );
		//delayTime=WhiteNoise.ar(0.5,0.5);
		ampl=BufRd.ar(1, ampBuf, BufFrames.kr(ampBuf)*delayTime);
		//ampl=1.0;
		//az=BufRd.ar(1, azBuf, BufFrames.kr(azBuf)*delayTime);
		delayTime=delayTime*pos;
		LocalOut.ar(impulse);

		poss=(
			phase*bsR
			-((rate.abs-1).max(0)*dur)
			- ControlRate.ir.reciprocal
			- delayTime
			//- iets van 0.5 pos dingetje TGrains
		);
		/*
		^TGrains.ar(numChannels
		, impulse
		, buf, rate, pos, dur, az, ampl*mul
		, interp);
		*/
		^TGrains.ar(numChannels.max(2), impulse, buf, rate, poss, dur, az, ampl*mul, interp)
	}
}

GranulatorIBF2 {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5, delayTime=0.005, interp=2, az=0.0, elevation=0.0, rho=1.0, envbufnum1= -1, envbufnum2= -1, ifac=0.0, rateScaling= -0.85, timeJitter=0.005, wComp=0, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, overLapScaling=0.5.neg, overLapScalingDepth=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning, trigger, duration;
		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal, jitterR;
		rate=rate*DC.ar(1);
		run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		isRunning=(run.lag(0,dur)>tr);
		phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));

		BufWr.ar( input,buf,phase,1);

		duration=Latch.ar(dur, LocalIn.ar(1));
		duration=rate.abs.pow(rateScaling).min(1.0)*duration;
		trigger=Impulse.ar(duration.clip(1/SampleRate.ir, maxdelaytime).reciprocal*overLap);
		LocalOut.ar(trigger);
		delayTime=delayTime-((rate<0)*rate.abs*duration);

		pos=(
			phase*bsR
			-((rate.abs-1).max(0)*duration)
			- delayTime.max(0)
			- WhiteNoise.ar(timeJitter,timeJitter)//maak deze demand rate
			- ControlRate.ir.reciprocal
			- ((1-run.lag(duration))*((duration*2).clip(duration, maxdelaytime-0.1   )))
		)*bdR;
		/*
		pos=(
		phase*bsR
		-((rate.abs-1).max(0)*dur)
		- delayTime.max(0)
		- WhiteNoise.ar(timeJitter,timeJitter)
		- ControlRate.ir.reciprocal
		- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;
		*/


		^BufGrainIBF.ar(
			trigger
			, duration, buf, rate, pos
			, envbufnum1
			, envbufnum2
			, ifac
			, az, elevation, rho, interp
			, wComp
			//, overLap.max(1).reciprocal.sqrt*mul
			, (overLap.max(1).pow(overLapScaling)*overLapScalingDepth+(1-overLapScalingDepth))*mul
			, add);

	}
}


PitchShifterT {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;
		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;
		rate=rate*DC.ar(1);
		run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		isRunning=(run.lag(0,dur)>tr);
		phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);
		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));
		BufWr.ar( input,buf,phase,1);
		dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;
		delayTime=delayTime-((rate<0)*rate.abs*dur);
		pos=(
			phase*bsR
			- ((rate.abs-1).max(0)*dur)
			//			+ (dur*0.5)//ofzoiets, nog ff beter checken denk ik....
			//			- (((rate.abs<1)*rate.abs.reciprocal)*dur)
			- delayTime.max(0)
			- WhiteNoise.ar(timeJitter,timeJitter)
			- ControlRate.ir.reciprocal
			- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		);
		pos=pos+((rate.abs.min(1.0))*dur*0.5);
		^TGrains.ar(numChannels, Impulse.ar(dur.reciprocal*overLap), buf, rate, pos, dur, az, overLap.max(1).reciprocal.sqrt*mul, interp)
	}

}


PitchShifternoDC {

	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
		var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;

		var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;

		//rate=rate*DC.ar(1);

		run=A2K.kr(K2A.ar(run));//can this be more efficient?????
		isRunning=(run.lag(0,dur)>tr);
		phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);

		input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));

		BufWr.ar( input,buf,phase,1);

		dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;

		delayTime=delayTime-((rate<0)*rate.abs*dur);

		pos=(
			phase*bsR
			-((rate.abs-1).max(0)*dur)
			- delayTime.max(0)
			- WhiteNoise.ar(timeJitter,timeJitter)
			- ControlRate.ir.reciprocal
			- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
		)*bdR;

		^GrainBuf.ar(numChannels
			, Impulse.ar(dur.reciprocal*overLap)
			, dur, buf, rate, pos, interp, az
			, envbufnum
			, maxGrains
			,overLap.max(1).reciprocal.sqrt*mul, add);


	}

}

/*
PitchShifter2 {

*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0, envbufnum= -1, maxGrains = 2048, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0, lagTime=0.2, tr=0.001, minRateScaling=1.0;
var buf=LocalBuf(SampleRate.ir*maxdelaytime).clear, isRunning;

var phase,pos,input,output, bdR=BufDur.ir(buf).reciprocal, bsR=BufSampleRate.ir(buf).reciprocal;

rate=rate*DC.ar(1);

run=A2K.kr(K2A.ar(run));//can this be more efficient?????
isRunning=(run.lag(0,dur)>tr);
phase=Phasor.ar(0, 1*isRunning, 0, BufFrames.ir(buf),0);

input=(fb*BufRd.ar(1,buf,phase,1))+((in*run.lag(dur)));

BufWr.ar( input,buf,phase,1);

dur=rate.abs.pow(rateScaling).min(minRateScaling)*dur;

delayTime=delayTime-((rate<0)*rate.abs*dur);

pos=(
phase*bsR
-((rate.abs-1).max(0)*dur)
- delayTime.max(0)
- WhiteNoise.ar(timeJitter,timeJitter)
- ControlRate.ir.reciprocal
- ((1-run.lag(dur))*((dur*2).clip(dur, maxdelaytime-0.1   )))
)*bdR;

^GrainBuf.ar(numChannels
, Impulse.ar(dur.reciprocal*overLap)
, dur, buf, rate, pos, interp, az
, envbufnum
, maxGrains
,overLap.max(1).reciprocal.sqrt*mul, add);


}

}
*/