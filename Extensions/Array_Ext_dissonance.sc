+Array {

	*harmonicEntropyCurve {
		var x, intervals, intervalWeights, intervalsCents, k, s, a, reyniEntropy;
		var gaussianDevCents = 17;
		var maxRatio = 200;
		var gaussian;
		var ka, sa, convKaS, convKS;

		gaussian = { |x, stdev|
			(1.0 / (stdev * sqrt(2pi))) * exp(-0.5 * (x.squared / stdev.squared))
		};

		x = (0..1200);//one octave

		intervals = List.new;
		intervalWeights = List.new;

		(1..maxRatio).do { |i|
			(1..maxRatio).do { |j|
				var ratio = i / j;
				if ((i.gcd(j) == 1) && (ratio >= 1.0) && (ratio <= 2.0)) {
					intervals.add(ratio);
					intervalWeights.add(sqrt(i * j));
				};
			};
		};

		intervals = intervals.asArray;
		intervalWeights = intervalWeights.asArray;
		intervalsCents = intervals.collect { |r| 1200 * r.log2 };

		k = Array.fill(1201, 0.0);
		intervals.size.do { |i|
			var closestCent = intervalsCents[i].round.asInteger;
			var weight = 1.0 / intervalWeights[i];

			if ((k[closestCent] == 0.0) || (k[closestCent] < weight)) {
				k[closestCent] = weight;
			};
		};

		s = Array.fill(100, { |i|
			gaussian.(i - 50, gaussianDevCents)
		});

		a = 100;

		ka = k.collect(_ ** a);
		sa = s.collect(_ ** a);

		convKaS = Array.fill(1201, { |idx|
			var sum = 0;
			s.size.do { |si|
				var ki = idx - si + 50;
				if ((ki >= 0) && (ki < 1201)) {
					sum = sum + (ka[ki] * sa[si]);
				};
			};
			sum
		});

		convKS = Array.fill(1201, { |idx|
			var sum = 0;
			s.size.do { |si|
				var ki = idx - si + 50;
				if ((ki >= 0) && (ki < 1201)) {
					sum = sum + (k[ki] * s[si]);
				};
			};
			sum
		});

		reyniEntropy = (1.0 / (1.0 - a)) * (convKaS / (convKS ** a)).collect { |val|
			if (val > 0) { val.log } { 0 }
		};

		^reyniEntropy
	}
}