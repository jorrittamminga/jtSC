+KDTree {

	kNearestSort {arg point, k=1, maxiterations=50;
		var radius=1.0, step=1.0, size, iterations=0, prevSize=0, out;
		size=this.radiusSearch( point, radius).size;

		while( { (size!=k) && (iterations<maxiterations)}, {
			step=(step.abs*0.9).max(0.0000001);
			step=if (size>k, {step.abs.neg},{step.abs});
			radius=radius+step;
			size=this.radiusSearch( point, radius).size;
			iterations=iterations+1;
		});
		out=this.radiusSearch( point, radius);
		//("radius: " ++ radius).postln;
		//^out.sort({|a,b| a=(a.location-point).abs.sum; b=(b.location-point).abs.sum; a<b});
		^out.sort({|a,b| a=(a.location-point).squared.sum.sqrt; b=(b.location-point).squared.sum.sqrt; a<b});
	}

}