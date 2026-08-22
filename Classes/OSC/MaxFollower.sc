MaxFollower {
	var <currentMax, <rate, <>clampFactor, <>floor, <>frozen;
	var decay;

	*new { |halfLife=30, rate=60, clampFactor=4, floor=0.001|
		^super.new.init(halfLife, rate, clampFactor, floor);
	}

	init { |hl, r, cf, fl|
		rate = r;
		clampFactor = cf;
		floor = fl;
		frozen = false;
		currentMax = fl;
		this.halfLife_(hl);
	}

	halfLife_ { |hl|
		decay = 2 ** (-1 / (hl * rate));
	}

	halfLife {
		^-1 / (rate * decay.log2)
	}

	// feed one (already EMA-smoothed) magnitude, returns currentMax
	update { |val|
		var mag = val.abs;
		if (frozen.not) {
			// release: decay toward floor
			currentMax = (currentMax * decay).max(floor);
			// attack: rise to new peak, unless it's an outlier leap
			if (mag > currentMax) {
				// bootstrap: accept freely while still at the floor
				if ((currentMax <= floor) or: { mag <= (currentMax * clampFactor) }) {
					currentMax = mag;
				};
				// else: reject as glitch, keep the decayed value
			};
		};
		^currentMax
	}

	// load a learned max and freeze (for scene recall)
	preset { |m|
		currentMax = m;
		frozen = true;
	}

	freeze { frozen = true }
	unfreeze { frozen = false }
	reset { currentMax = floor }
}