+ SimpleNumber {

	//decaytime.ringzamp
	ringzamp {arg power=0.5, server;
		var sr;
		server=server??{Server.default};
		sr=server.sampleRate??{44100};
		^(((sr/this.max(sr.reciprocal)).pow(power)*sr.reciprocal).min(0.5))
	}

	//freq.resonzamp(rq)
	resonzamp {arg rq=1.0, power= -0.5, freqPow= -0.5, root=50;
		^(rq.pow(power)*((this.max(root)/root).pow(freqPow))*7)
	}

	//returns a scaling amplitude for Comb filter, delayTime.combamp(decayTime, 0.2155)
	combamp {arg decayTime=3.0, power=0.2155;//power=0.2155 for sustained input such as WhiteNoise.ar
		var fb=0.001.pow(this/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(power))*(power*2))
	}
	//delayTime.combamp(decayTime, 0.2155, 0.5)
	comblpamp {arg decayTime=3.0, power=0.2155, coef=0.0;//power=0.2155 for sustained input such as WhiteNoise.ar
		var fb=0.001.pow(this/decayTime);
		^((((1+fb.squared)-(2*fb)).pow(power))*(power*2)*((1-coef).reciprocal))
	}

	cpsamp {arg root=20, exp=0.333;
		^((root / this) ** exp)
	}

	cpsampa {
		var k =  3.5041384e16;
		var c1 = 424.31867740601;
		var c2 = 11589.093052022;
		var c3 = 544440.67046057;
		var c4 = 148698928.24309;
		var r = squared(this);
		var m1 = pow(r,4);
		var n1 = squared(c1 + r);
		var n2 = c2 + r;
		var n3 = c3 + r;
		var n4 = squared(c4 + r);
		var level = k * m1 / (n1 * n2 * n3 * n4);
		^sqrt(level).reciprocal
	}
}


+ Array {
	cpsamp {
		^this.collect(_.cpsamp)
	}
	cpsampa {
		^this.collect(_.cpsampa)
	}
}
