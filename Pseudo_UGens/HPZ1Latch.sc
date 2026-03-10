HPZ1T {
	*kr {arg in = 0.0, trig, mul = 1.0, add = 0.0;
		if (trig.isNil) {trig=Changed.kr(in)};
		^Latch.kr(HPZ1.kr(in,mul), trig);
	}
}

HPZ1Latch {
	*kr {arg in = 0.0, mul = 1.0, add = 0.0;
		^Latch.kr(HPZ1.kr(in,mul), Changed.kr(in));
	}
}


HPZ1LatchT {
	*kr {arg in = 0.0, trig=0, mul = 1.0, add = 0.0;
		^Latch.kr(HPZ1.kr(in,mul), trig);
	}
}
