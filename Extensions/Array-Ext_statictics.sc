+Array {
	//---------------------------------------------------- STATISTICS
	meanWeighted {arg weights;
		^(this.collect{|v,i| v*weights[i]}.sum/weights.sum)
	}

	geometricmean {
		^this.product.pow(this.size.reciprocal)
	}

	harmonicmean {
		^this.size/this.reciprocal.sum
	}

	powermean {arg power=2;
		^((this.pow(power).sum/this.size).pow(power.reciprocal))
	}


	variance {
		^((this-this.mean).squared.mean)
	}

	stdev {
		^(this.variance.sqrt)
	}

	skew {
		var skew, stdev=this.stdev;
		skew=if (stdev!=0, {(3*(this.mean-this.median)/stdev)}, {0});
		^skew
	}

	kurtosis {arg flag=false;
		var n=this.size, k, stdev=this.stdev;
		k=if (stdev!=0, {
			((this-this.mean).pow(4).mean / stdev.pow(4));
		},{0});
		if (flag, {k=((n-1) / ((n-2)*(n-3))) * ((n+1) * k - (3*(n-1))) + 3});
		^k
	}

	meanexp{
		^this.log.mean.exp
	}

}