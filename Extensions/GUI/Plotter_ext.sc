+ Plotter {
	controlSpec {arg index=0;
		^if (index==nil, {
			this.specs
		},{
			this.specs[index]
		})
	}
	action {^nil}
}