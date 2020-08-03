/*
10.do({|i|
x=1.0.noise(i, x, 5);
x[0].linexp(0.0, 1.0, 100, 1000);
});

*/
+SequenceableCollection {

	keyToDegree2 { arg scale, stepsPerOctave=12;
		^this.collect { arg val; val.keyToDegree2(scale, stepsPerOctave) }
	}

	*geomStartEndDuration {arg duration=10.0, start=0.1, end=0.2;
		var out=duration.geomDurStartEndToSizeGrow(start, end);
		^this.geom(out[0], start, out[1])
	}

	*geomSum {arg sum=1.0, start=0.1, grow=0.9, adjust=\start, max=1000, minValue=0.0001;
		var size=1.0.geomEndToSize(start, grow, max, minValue).round(1.0).asInteger;
		switch(adjust, \grow,
			{grow=sum.geomEndToGrow(start, size)}
			, \start,
			{start=(sum/this.geom(size, start, grow).sum)*start}
		);
		^this.geom(size, start, grow)
	}

	*geomSumIntegrate {arg sum=1.0, start=0.1, grow=0.9, adjust=\start, max=1000
		, minValue=0.0001, includeLast=false;
		var size,geom;
		#size,start,grow=sum.geomSumArgs(start, grow, adjust);
		geom=this.geom(size, start, grow);
		^([0]++geom.integrate.copyRange(0, geom.size-[1,2][includeLast.not.binaryValue]))
	}

	*geomIntegrate {arg size=16, start=0.1, grow=0.9, includeLast=false;
		var geom;
		geom=this.geom(size, start, grow);
		^([0]++geom.integrate.copyRange(0, geom.size-[1,2][includeLast.not.binaryValue]))
	}

}