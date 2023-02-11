/*
Converts a ratio (=musical interval, for instance a fifth 7.midiratio) to a delaytime, given a modulation speed of a SinOsc.kr.
Can be usefull to tune for instance a chorus effect, with a constant pitchshift.
Disclaimer: not sure if this a good forumula.... it estimates the tranposition, based on experiments, not science or mathematics
{DelayL.ar(in, 0.1, SinOsc.kr(speed, 0, ratio.ratiosecs(speed).min(delayTime), delayTime), wet, in)}.play
4pi.reciprocal
*/
+ SimpleNumber {
	ratiosecs {arg speed=1.0;//for SinOsc.kr
		// 0.079577471545948 = 4pi.reciprocal
		///4pi
		^(this.log*0.079577471545948*speed.reciprocal)
	}
	ratiotime {arg speed;
		^this.ratiosecs(speed)
	}
	ratiosecstri {arg speed=1.0;//for LFTri.kr
		^(this.log*0.12483568134285*speed.reciprocal)
	}

}

+ Array {
	ratiosecs {arg speed=1.0;
		^this.collect(_.ratiosecs(speed))
	}
	ratiotime {arg speed;
		^this.ratiosecs(speed)
	}
}

+ UGen {
	ratiosecs {arg speed=1.0;
		^(this.log*0.079577471545948*speed.reciprocal)
	}
	ratiotime {arg speed;
		^this.ratiosecs(speed)
	}
	ratiosecstri {arg speed=1.0;//for LFTri.kr
		^(this.log*0.12483568134285*speed.reciprocal)
	}
}

+ MultiOutUGen {
	ratiosecs {arg speed=1.0;
		^this.collect(_.ratiosecs(speed))
	}
	ratiosecstri {arg speed=1.0;
		^this.collect(_.ratiosecstri(speed))
	}
	ratiotime {arg speed;
		^this.ratiosecs(speed)
	}
}