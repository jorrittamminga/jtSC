OpLPF
{
	*ar
	{|in, freq|

		^OnePole.ar(in, exp(-2pi * (freq * SampleDur.ir)));
	}
}

OpHPF
{
	*ar
	{|in, freq|

		^(in - OnePole.ar(in, exp(-2pi * (freq * SampleDur.ir))));
	}
}


CombStretch {

	*ar { arg in, maxdelaytime=0.2, freq=100, decaytime=3.0, damping=0.05, stretch=1.0, inputfreq=19000, mul=1.0, add=0;
		var shift, dt, octave=1;
		decaytime=DC.kr(1)*decaytime;
		octave=0.5*(decaytime>0)+0.5;
		stretch=stretch.reciprocal-1;
		shift=stretch*freq;
		dt=(freq-shift).max(20).reciprocal*octave;

		//		OpHPF.ar(
		^FreqShift.ar(Pluck.ar(OpHPF.ar(OpLPF.ar(in, inputfreq), freq), in, maxdelaytime, dt
			, decaytime//(1+damping).pow(16)*decaytime
			, damping), shift
		, 0, mul, add

		)
		//			, freq)



	}
}