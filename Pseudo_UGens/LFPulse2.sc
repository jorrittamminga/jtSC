LFPulse2 {

	*ar {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^LFPulse.ar(freq, iphase, width, mul, width.neg*mul+add)
	}
	*kr {
		arg freq = 440.0, iphase = 0.0, width = 0.5, mul = 1.0, add = 0.0;
		^LFPulse.kr(freq, iphase, width, mul, width.neg*mul+add)
	}
	signalRange { ^\bipolar }

}