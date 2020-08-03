//use more Polar and Point etc
//make PanD more efficient, for instance table lookup instead of cos formula
PanD {
	*ar{arg numChans=2, in, azimuth=0.0, radius=1.0, headWidth=0.19, width=1.0, damping=1.0, ampR=0.5, maxDelayTime=0.05, mul=1.0, add=0.0;
		var buf, speakerAz, azDelta, distances, delayTimes, amps;
		buf={LocalBuf(SampleRate.ir*maxDelayTime)}!numChans;
		speakerAz=numChans.asAzimuthArray;
		azDelta=(azimuth-speakerAz).abs.wrap2(pi).abs;//azimuth difference between sound source and speakers
		distances=(radius.squared+1.0-(azDelta.cos*radius*2.0)).sqrt;//cosinus rule: b^2+c^2-2bccosangle
		delayTimes=distances/340*headWidth;
		amps=(azDelta/(numChans.reciprocal*2*pi)*((radius*width).clip2(1.0))).clip2(1.0);
		amps=(amps*pi*0.5).cos;
		in=OnePole.ar(in, 1.0-((radius*damping).reciprocal.clip2(1.0)), (radius*ampR).reciprocal.clip2(1.0));
		^BufDelayL.ar(buf, in, delayTimes, amps);
	}
}

PanDxy {
	*ar{arg numChans=2, in, xPos=0.0, yPos=1.0, headWidth=0.19, width=1.0, damping=1.0, ampR=0.5, maxDelayTime=1.0, mul=1.0, add=0.0;
		var azimuth=atan2(xPos, yPos);
		var radius=hypot(xPos, yPos);
		^PanD.ar(numChans, in, azimuth, radius, headWidth, width, damping, maxDelayTime, mul, add)
	}
}

PanAzD {
	*ar{arg numChans=4, in, pos=0, level=1, width=2, orientation=0.5, delayWidth=0.2;

		var points=numChans.asAzimuthArray(orientation).collect{|az|
			Polar(1, az).asPoint};
		var distances;
		var delayTimes;
		var buf={LocalBuf(SampleRate.ir*0.1)}!numChans;
		distances=points.collect{|point|
			Polar(1, pos*pi).asPoint.dist(point)
		};
		delayTimes=distances/340;
		delayTimes=delayTimes*delayWidth;

		^BufDelayC.ar(
			buf,
			PanAz.ar(numChans, in, pos, level, width, orientation),
			delayTimes
		)

	}
}

PanP {
	*ar{arg numChans=4, in, pos=0, level=1, orientation=0.5, delayWidth=0.2;

		var points=numChans.asAzimuthArray(orientation).collect{|az|
			Polar(1, az).asPoint};
		var distances;
		var delayTimes;
		var buf=LocalBuf(SampleRate.ir*0.1);
		distances=points.collect{|point|
			Polar(1, pos*pi).asPoint.dist(point)
		};
		delayTimes=distances/340;
		delayTimes=delayTimes*delayWidth;

		^BufDelayC.ar(
			buf,
			in,
			delayTimes,
			level
		)

	}
}