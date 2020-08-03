RingzJT {

	*ar{ arg in = 0.0, freq = 440.0, decaytime = 1.0, mul = 1.0, add = 0.0;
		^Ringz.ar(in, freq, decaytime, (0.00003/decaytime).pow(0.48)*mul, add)
	}

	*kr{ arg in = 0.0, freq = 440.0, decaytime = 1.0, mul = 1.0, add = 0.0;
		^Ringz.kr(in, freq, decaytime, (0.00003/decaytime).pow(0.48)*mul, add)
	}

}