+ Tuning {
	*calcLimit {arg intervals=[3,5,7], maxPowers, maxIntervals=2, interval=[1, -1], octaveRatio=2.0
		, name="new tuning";
		var t;
		maxPowers=maxPowers??{[1]};
		maxPowers=maxPowers.asArray.lace(intervals.size);
		maxIntervals=maxIntervals.min(intervals.size);
		interval=interval.asArray;
		t=maxPowers.collect{|p| ((0..p)*.t interval).flat.asSet.asArray };
		t=t.allTuples;
		if (maxIntervals<intervals.size, {
			t=t.select({|a| a.occurrencesOf(0)> (intervals.size-maxIntervals-1) });
		});
		t=t.collect{|p|
			var x=intervals.pow(p).product;
			x=octaveRatio.pow((x.log/octaveRatio.log).floor).reciprocal*x;
			x
		}.asSet.asArray.sort;
		^this.new(t.ratiomidi, octaveRatio, name)
	}
}
