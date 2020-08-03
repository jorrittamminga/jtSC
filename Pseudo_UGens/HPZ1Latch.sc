HPZ1Latch {
	*kr {arg in = 0.0, mul = 1.0, add = 0.0;
		^Latch.kr(HPZ1.kr(in), Changed.kr(in));
	}
}


HPZ1LatchT {
	*kr {arg in = 0.0, trig=0, mul = 1.0, add = 0.0;
		^Latch.kr(HPZ1.kr(in), trig);
	}
}
