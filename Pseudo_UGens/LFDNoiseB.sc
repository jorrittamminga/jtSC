//maak dit Demand
LFDNoiseB {

	*kr{arg speedL=1.0, speedH=10.0, pwL=0.01, pwH=0.5, durL=0.1, durH=10.0, min1=0.5, min2=8.0, max1=3.0, max2=40.0, lagU=0.01, lagD=1.0;
		var speed,out,pw,speedP;
		speed={LFDNoise1.kr(speedL).exprange(speedL, speedH)}!4;
		speedP=LFDNoise1.kr(speed[0]).exprange(durL.reciprocal, durH.reciprocal);
		out=LFPulse.kr(speedP, 0, LFDNoise1.kr(speed[1]).exprange(pwL,pwH)).exprange(
			LFDNoise1.kr(speed[2]).exprange(min1,min2)
			, LFDNoise1.kr(speed[3]).exprange(max1,max2)
		).lag(speedP.reciprocal*lagU, speedP.reciprocal*lagD);
		^out
	}
}

LFDNoiseL {
	*kr{arg min=100,max=400,speedL=0.1, speedH=1.0, lagL=0.1, lagH=1.0;
		var speed, lagT, out; speed=LFDNoise0.kr(speedL).exprange(speedL,speedH);
		lagT=speed.reciprocal*LFDNoise1.kr(speed).exprange(lagL,lagH);
		out=LFDNoise0.kr(speed.lag2(lagT)).exprange(min,max).lag(lagT);

		^out
	}


}