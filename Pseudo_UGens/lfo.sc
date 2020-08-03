LFDNoiseB1 {
	*kr{arg speedLow=0.01, speedHigh=10.0, val=1.0, mul=1, add=0.0;
		var value, freq=0.0, speed, factor;
		factor=((1-(val.abs))*0.5+0.5);
		value=LocalIn.kr(1);
		speed=((value-val).abs*factor).exprange(speedLow, speedHigh);
		value=LFDNoise0.kr(speed);
		LocalOut.kr(value);
		^(value.lag(speed.reciprocal)*mul+add)
	}
}

LFDNoiseB0 {
	*kr{arg speedLow=0.01, speedHigh=10.0, val=1.0, mul=1, add=0.0;
		var value, freq=0.0, speed, factor;
		factor=((1-(val.abs))*0.5+0.5);
		value=LocalIn.kr(1);
		speed=((value-val).abs*factor).exprange(speedLow, speedHigh);
		value=LFDNoise0.kr(speed);
		LocalOut.kr(value);
		^(value*mul+add)
	}
}

LFDNoiseB3 {
	*kr{arg speedLow=0.01, speedHigh=10.0, val=1.0, mul=1, add=0.0;
		var value, freq=0.0, speed, factor;
		factor=((1-(val.abs))*0.5+0.5);
		value=LocalIn.kr(1);
		speed=((value-val).abs*factor).exprange(speedLow, speedHigh);
		value=LFDNoise0.kr(speed);
		LocalOut.kr(value);
		^(value.lag3(speed.reciprocal)*mul+add)
	}
}