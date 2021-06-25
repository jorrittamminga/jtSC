+Integer {

	allSums {arg min=1, sizes=[2];
		var that=[];
		sizes.do{|size|
			({(min..this-min)}!size).allTuples.do{|tuple| if (tuple.sum==this, {that=that.add(tuple)})};
		};
		^that
	}

}