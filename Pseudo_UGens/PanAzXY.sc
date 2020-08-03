//numChans, in, pos = 0.0, level = 1.0, width = 2.0, orientation = 0.5;

PanAzXY{
	*ar	{arg numChans, in, x=0, y=0, theta=0.0, level=1.0, width=2.0;
		var theta2=Point(x,y).theta, rho=Point(x,y).rho.min(1);
		var thetaDelta=rho;
		var alpha, beta, angles, distance, xfade, azimuths;
		beta=theta2-theta-(0.5pi);
		alpha=(beta.cos*rho).asin;//+[theta, theta-pi]
		angles=[theta+alpha, theta-pi-alpha];
		distance=(angles[0]-angles[1]).wrap2(-pi,pi).abs.max(0.0000000001);

		xfade=(((theta2-angles[0]).wrap(-pi,pi).abs/distance)).clip(0, 1.0)*2-1.0;
		//deze kan beter?

		azimuths=(angles/pi.neg+0.5).wrap(-1.0, 1.0);	//convert to azimuthPan
		width=width.clip(2, numChans);
		in=azimuths.collect{|az,i|
			PanAz.ar(numChans, in, az,  2/width, width)
		};
		//^LinXFade2.ar(in[0], in[1], xfade, level)
		^XFade2.ar(in[0], in[1], xfade, level)
	}


}
