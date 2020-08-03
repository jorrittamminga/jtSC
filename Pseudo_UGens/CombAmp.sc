CombAmp : PureUGen {
	/*
	*ir { arg freq = 60.midicps, root = 60.midicps, exp = 0.3333;
	^this.multiNew('scalar', freq, root, exp)
	}
	var fb=0.001.pow(this/decayTime);
	^((((1+fb.squared)-(2*fb)).pow(power))*(power*2))
	*/
	*ar { arg delayTime=0.01, decayTime=0.1, exp = 0.2155;
		var fb=0.001.pow(delayTime/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(exp))*(exp*2))
		//^this.multiNew('audio', freq, root, exp)
	}
	*kr { arg delayTime=0.01, decayTime=0.1, exp = 0.2155;
		var fb=0.001.pow(delayTime/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(exp))*(exp*2))
		//^this.multiNew('audio', freq, root, exp)
	}
	//checkInputs { ^if(rate === \audio) { this.checkSameRateAsFirstInput } }
}

CombLPAmp : CombAmp {
	/*
	*ir { arg freq = 60.midicps, root = 60.midicps, exp = 0.3333;
	^this.multiNew('scalar', freq, root, exp)
	}
	var fb=0.001.pow(this/decayTime);
	^((((1+fb.squared)-(2*fb)).pow(power))*(power*2))
	*/
	*ar { arg delayTime=0.01, decayTime=0.1, exp = 0.2155, coef=0.0;
		var fb=0.001.pow(delayTime/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(exp))*(exp*2)*((1-coef.min(0.8)).reciprocal))
		//^this.multiNew('audio', freq, root, exp)
	}
	*kr { arg delayTime=0.01, decayTime=0.1, exp = 0.2155, coef=0.0;
		var fb=0.001.pow(delayTime/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(exp))*(exp*2)*((1-coef.min(0.8)).reciprocal)*(delayTime.linlin(20000.reciprocal, 20.reciprocal, 2.0.sqrt, 1)))
		//^this.multiNew('audio', freq, root, exp)
	}
	//checkInputs { ^if(rate === \audio) { this.checkSameRateAsFirstInput } }
}