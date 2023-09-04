GrainBufInJT : GrainInJT {
	/*
	*kr {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
	, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005
	, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5;
	^this.ar(numChannels in, dur, rate, rOverLap, maxdelaytime, delayTime, az, interp, envbufnum, maxGrains, rateScaling, timeJitter
	, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5
	}
	*/
	*ar {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
		, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime
			, delayTime, run, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);
		//if (az.rate==\demand, { az=if (impulse.rate==\audio, {Demand.ar(impulse, 0, az)},{Demand.kr(impulse, 0, az)}) });//dit kan mooier, iets met .performNogIets
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		^GrainBuf.ar(numChannels, impulse, grainDur, buf, grainRate, pos, interp, az, envbufnum, maxGrains, amplitude, add);
	}
}

TGrainsInJT : GrainInJT {
	*ar {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
		, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime
			, delayTime, run, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);//\TGrains

		//if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		pos=(grainRate*grainDur*0.5)+(pos*BufDur.ir(buf));
		//pos=(pos*BufDur.ir(buf));
		^(TGrains.ar(numChannels, impulse, buf, grainRate, pos, grainDur, az, amplitude*1.1, interp)+add)
	}
}

TGrains2InJT : GrainInJT {
	*ar {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
		, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005
		, att=0.01, dec=0.02
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime
			, delayTime, run, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse, rOverLapIsImpulse);//\TGrains

		//if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		//if (att.rate==\demand, {att=Demand.ar(impulse, 0, att)});
		if (att.rate==\demand, { att=Demand.multiNewList([impulse.rate, impulse, 0]++att.asArray)});
		//if (dec.rate==\demand, {dec=Demand.ar(impulse, 0, dec)});
		if (dec.rate==\demand, { dec=Demand.multiNewList([impulse.rate, impulse, 0]++dec.asArray)});

		pos=(grainRate*grainDur*0.5)+(pos*BufDur.ir(buf));
		//pos=(pos*BufDur.ir(buf));
		^(TGrains2.ar(numChannels, impulse, buf, grainRate, pos, grainDur, az, amplitude, att, dec, interp)+add)
	}
}

TGrains3InJT : GrainInJT {
	*ar {arg numChannels=1, in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
		, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005
		, att=0.01, dec=0.02, window=1
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime
			, delayTime, run, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);//\TGrains
		/*
		if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		if (att.rate==\demand, {att=Demand.ar(impulse, 0, att)});
		if (dec.rate==\demand, {dec=Demand.ar(impulse, 0, dec)});
		*/
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		if (att.rate==\demand, { att=Demand.multiNewList([impulse.rate, impulse, 0]++att.asArray)});
		if (dec.rate==\demand, { dec=Demand.multiNewList([impulse.rate, impulse, 0]++dec.asArray)});

		pos=(grainRate*grainDur*0.5)+(pos*BufDur.ir(buf));
		//pos=(pos*BufDur.ir(buf));
		^(TGrains3.ar(numChannels, impulse, buf, grainRate, pos, grainDur, az, amplitude, att, dec, window, interp)+add)
	}
}

BufGrainBFInJT : GrainInJT {
	*ar {arg in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5.0, delayTime=0.005, az=0.0, interp=2
		, elevation=0.0, rho=1.0, rateScaling= -0.85, timeJitter=0.005, wComp=0
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling= 0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime, delayTime, run, rateScaling
			, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);

		//[az, elevation, rho].do{|par| if (par.rate==\demand, {par=Demand.ar(impulse, 0, par)})};
		//if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		//if (elevation.rate==\demand, {elevation=Demand.ar(impulse, 0, elevation)});
		//if (rho.rate==\demand, {rho=Demand.ar(impulse, 0, rho)});

		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		if (elevation.rate==\demand, { elevation=Demand.multiNewList([impulse.rate, impulse, 0]++elevation.asArray)});
		if (rho.rate==\demand, { rho=Demand.multiNewList([impulse.rate, impulse, 0]++rho.asArray)});


		^BufGrainBF.ar(impulse, grainDur, buf, grainRate, pos, az, elevation, rho, interp, wComp, amplitude*3.0, add);
	}
}

BufGrainIBFInJT : GrainInJT {
	*ar {arg in=0.0, dur=0.10, rate=1.0, rOverLap=0.25, maxdelaytime=5, delayTime=0.005, az=0.0, interp=2
		, envbufnum1= -1, envbufnum2= -1, ifac=0.0
		, elevation=0.0, rho=1.0, rateScaling= -0.85, timeJitter=0.005, wComp=0
		, mul=1.0, add=0.0, fb=0.0, run=1.0, rOverLapScaling=0.5, rateMin=1.0, rOverLapIsImpulse=false;

		var buf, impulse, pos, grainDur, grainRate, amplitude;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime, delayTime, run
			, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);
		/*
		if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		if (elevation.rate==\demand, {elevation=Demand.ar(impulse, 0, elevation)});
		if (rho.rate==\demand, {rho=Demand.ar(impulse, 0, rho)});
		if (ifac.rate==\demand, {ifac=Demand.ar(impulse, 0, ifac)});
		*/
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});
		if (elevation.rate==\demand, { elevation=Demand.multiNewList([impulse.rate, impulse, 0]++elevation.asArray)});
		if (rho.rate==\demand, { rho=Demand.multiNewList([impulse.rate, impulse, 0]++rho.asArray)});
		if (ifac.rate==\demand, { ifac=Demand.multiNewList([impulse.rate, impulse, 0]++ifac.asArray)});


		^BufGrainIBF.ar(impulse, grainDur, buf, grainRate, pos, envbufnum1, envbufnum2, ifac, az, elevation, rho, interp
			, wComp, amplitude*3.0, add);
	}
}

//--------------------------------------------------------------------------------

PitchShifterJT : GrainInJT {
	*ar {arg in=0.0, rate=1.0, dur=0.10, overLap=4.0, maxdelaytime=5.0, delayTime=0.005, interp=2, numChannels=1, az=0.0
		, envbufnum= -1, maxGrains = 65536, rateScaling= -0.85, timeJitter=0.005, mul=1.0, add=0.0, fb=0.0, run=1.0
		, lagTime=0.2, tr=0.001, minRateScaling=1.0, rateMin=1.0, rOverLapIsImpulse=false;//last three arguments are obsolete...

		var buf, impulse, pos, grainDur, grainRate, amplitude, rOverLap=overLap.reciprocal, rOverLapScaling= 0.5;

		#buf, impulse, pos, grainDur, grainRate, amplitude=this.inits(in, dur, rate, rOverLap, maxdelaytime
			, delayTime, run, rateScaling, timeJitter, mul, fb, rOverLapScaling, rateMin, rOverLapIsImpulse);

		//if (az.rate==\demand, {az=Demand.ar(impulse, 0, az)});
		if (az.rate==\demand, { az=Demand.multiNewList([impulse.rate, impulse, 0]++az.asArray)});

		^GrainBuf.ar(numChannels, impulse, grainDur, buf, grainRate, pos, interp, az, envbufnum, maxGrains, amplitude, add);
	}
}
