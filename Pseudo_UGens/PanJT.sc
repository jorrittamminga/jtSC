PanAzXYJT {
	*ar	{arg numChans, in, x=0, y=0, level=1.0, orientation=0.5, minRadius=1.0, maxRadius=10, minLevel=0.2;
		var point, radius, az, piR=pi.reciprocal, speedOfSoundR=1/343;
		point=Point(x,y);
		radius=point.rho;
		az=point.theta.neg+0.5pi;
		in=DelayL.ar(in, 1.0, (radius-minRadius)*speedOfSoundR, radius.linexp(minRadius, maxRadius, 1.0, minLevel.max(0.001)));
		^PanAz.ar(numChans, in, az*piR, level, radius.linlin(0.0, minRadius, numChans, 2), orientation);
	}
}

PanAzJT {
	*ar	{arg numChans, in, az=0.0, radius=1.0, level=1.0, orientation=0.5, minRadius=1.0, maxRadius=10, minLevel=0.2;
		var speedOfSoundR=1/343;
		in=DelayL.ar(in, 1.0, (radius-minRadius)*speedOfSoundR, radius.linexp(minRadius, maxRadius, 1.0, minLevel.max(0.001)));
		^PanAz.ar(numChans, in, az, level, radius.linlin(0.0, minRadius, numChans, 2), orientation);
	}
}

PanB2XYJT {
	*ar	{arg in, x=0, y=0, level=1.0, minRadius=1.0, maxRadius=10, minLevel=0.2;
		var point, radius, az, piR=pi.reciprocal, speedOfSoundR=1/343;
		point=Point(x,y);
		radius=point.rho;
		az=point.theta.neg+0.5pi;
		in=DelayL.ar(in, 1.0, (radius-minRadius)*speedOfSoundR, radius.linexp(minRadius, maxRadius, 1.0, minLevel.max(0.001)));
		az=az*piR;
		^(PanB2.ar(in, az)+PanB2.ar(in, az+1, radius.linlin(0, minRadius, 1.0, 0)))
	}
}

PanB2JT {
	*ar	{arg in, az=0.0, radius=1.0, level=1.0, minRadius=1.0, maxRadius=10, minLevel=0.2;
		var speedOfSoundR=1/343;
		in=DelayL.ar(in, 1.0, (radius-minRadius)*speedOfSoundR, radius.linexp(minRadius, maxRadius, 1.0, minLevel.max(0.001)));
		^(PanB2.ar(in, az)+PanB2.ar(in, az+1, radius.linlin(0, minRadius, 1.0, 0)))
	}
}