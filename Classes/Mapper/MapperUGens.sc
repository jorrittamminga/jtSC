+ UGen {
	amplin {arg inMin, inMax, outMin, outMax, clip = \minmax;
		var inRange, outRange, inUnMapped, outMapped;
		inRange=inMax-inMin;
		outRange=outMax-outMin;
		inUnMapped=if(inRange.isPositive) {
			((this.prune(inMin, inMax, clip) - inMin) / inRange).sqrt
		}{
			1 - sqrt(1 - ((this.prune(inMin, inMax, clip) - inMin) / inRange))
		};
		^MulAdd(inUnMapped, outRange, outMin);
	}
	ampexp {arg inMin, inMax, outMin, outMax, clip = \minmax;
		var inRange, outRange, inUnMapped, outMapped;
		inRange=inMax-inMin;
		outRange=outMax-outMin;
		inUnMapped=if(inRange.isPositive) {
			((this.prune(inMin, inMax, clip) - inMin) / inRange).sqrt
		}{
			1 - sqrt(1 - ((this.prune(inMin, inMax, clip) - inMin) / inRange))
		};
		^inUnMapped.linexp(0, 1, outMin, outMax, nil)
	}
	linamp {arg inMin, inMax, outMin, outMax, clip = \minmax;
		var scaled, range;
		range = outMax-outMin;
		scaled = (this.prune(inMin, inMax, clip) - inMin) / (inMax - inMin);

		^if(range.isPositive) {
			scaled.squared * range + outMin
		}{
			// formula can be reduced to (2*v) - v.squared
			// but the 2 subtractions would be faster
			(1 - (1-scaled).squared) * range + outMin
		};
	}
	expamp {arg inMin, inMax, outMin, outMax, clip = \minmax;
		var scaled, range;
		range = outMax-outMin;
		scaled=(log(this.prune(inMin, inMax, clip)/inMin))/ (log(inMax/inMin));
		^if(range.isPositive) {
			scaled.squared * range + outMin
		}{
			// formula can be reduced to (2*v) - v.squared
			// but the 2 subtractions would be faster
			(1 - (1-scaled).squared) * range + outMin
		};
	}
	ampdbMapped {arg inMin=0, inMax=1, outMin= -inf, outMax=0, clip = \minmax;
		var scale=(outMax-outMin);
		^MulAdd(this.prune(inMin, inMax, clip).ampdb, scale, outMin)
	}
	dbampMapped {arg inMin= -inf, inMax=0, outMin=0.0, outMax=1.0, clip = \minmax;
		var scale=(outMax-outMin);
		//this.poll(Changed.kr(this),\input);
		^MulAdd(this.prune(inMin, inMax, clip).dbamp, scale, outMin)
	}
	expcurve {arg inMin=0.01, inMax=1, outMin=0, outMax=1, curve=2.0, clip = \minmax;
		var scaled;
		scaled=(log(this.prune(inMin, inMax, clip)/inMin))/ (log(inMax/inMin));
		^scaled.lincurve(0.0, 1.0, outMin, outMax, nil)
	}
	curveexp {arg inMin=0.0, inMax=1, outMin=0.01, outMax=1, curve=2.0, clip = \minmax;
		var scaled;
		^this.curvelin(inMin, inMax, 0, 1, curve, clip).linexp(0.0, 1.0, outMin, outMax, nil)
	}
	coslin{arg inMin=0, inMax=1.0, outMin=0.0, outMax=1.0, clip = \minmax;
		var scaled, range;
		scaled=this.prune(inMin, inMax, clip) * (inMax-inMin) + inMin;
		scaled=acos(1.0 - (scaled * 2.0)) / pi;
		^MulAdd(scaled, outMax-outMin, outMin)
	}
	lincos {arg inMin=0, inMax=1.0, outMin=0.0, outMax=1.0, clip = \minmax;
		var scaled, range;
		scaled = (this.prune(inMin, inMax, clip) - inMin) / (inMax - inMin);
		^MulAdd(  0.5 - (cos(pi * scaled) * 0.5), (outMax-outMin), outMin )
	}
	cosexp{arg inMin=0, inMax=1.0, outMin=0.01, outMax=1.0, clip = \minmax;
		var scaled, range;
		scaled=this.prune(inMin, inMax, clip) * (inMax-inMin) + inMin;
		scaled=acos(1.0 - (scaled * 2.0)) / pi;
		^scaled.linexp(0.0, 1.0, outMin, outMax)
	}
	expcos {arg inMin=0.01, inMax=1.0, outMin=0.0, outMax=1.0, clip = \minmax;
		var scaled;
		scaled=(log(this.prune(inMin, inMax, clip)/inMin))/ (log(inMax/inMin));
		^MulAdd(  0.5 - (cos(pi * scaled) * 0.5), (outMax-outMin), outMin )
	}
	/*
	sinlin {}
	linsin {}
	dbcurve {}
	curvedb {}
	expcurve {}
	curveexp {}
	*/
}