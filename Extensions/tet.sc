+ SimpleNumber {
	cpstet { arg octave=2.0, division=12.0, offset=69.0, a=440.0;
		^(((this/a).log/octave.log*division)+offset)
	}

	tetcps {arg octave=2.0, division=12.0, offset=69.0, a=440.0;
		^(octave.pow(this-offset/division)*a)
	}
}


+ Array {
	cpstet { arg octave=2.0, division=12.0, offset=69.0, a=440.0;
		^this.collect(_.cpstet(octave, division, offset, a))
	}

		tetcps { arg octave=2.0, division=12.0, offset=69.0, a=440.0;
		^this.collect(_.tetcps(octave, division, offset, a))
	}
}