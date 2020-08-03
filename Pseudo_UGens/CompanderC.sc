//compander with amplitude compensation

CompanderC : UGen {
	*ar { arg in = 0.0, control=0.0, thresh = 0.5, slopeBelow = 1.0, slopeAbove = 1.0,
		clampTime = 0.01, relaxTime = 0.01, mul = 1.0, add = 0.0;

		//makeUp = ( (thresh.neg * ( 1 - ratio )) * makeUp ).dbamp; // autogainq

		^Compander.ar(in, control, thresh,
				slopeBelow, slopeAbove, clampTime, relaxTime).madd((
			(thresh.ampdb.neg * ( 1 - slopeAbove ))  ).dbamp*mul
		, add)
	}
}
