TBRand {
	*ar { arg trig;
		^((TIRand.ar(0,1,trig)>0)*2-1)
	}

	*kr { arg trig;
		^((TIRand.kr(0,1,trig)>0)*2-1)
	}
}
