+ Plot {
	getIndex { |x|
		var ycoord = this.dataCoordinates;
		var xcoord = this.domainCoordinates(ycoord.size);
		var binwidth = 0;
		var offset;

		if (plotter.domain.notNil) {
			if (this.hasSteplikeDisplay) {
				// round down to index
				^plotter.domain.indexInBetween(this.getRelativePositionX(x)).floor.asInteger
			} {
				// round to nearest index
				^plotter.domain.indexIn(this.getRelativePositionX(x))
			};
		} {
			var tmp;
			if (xcoord.size > 0) {
				binwidth = (xcoord[1] ?? {plotBounds.right}) - xcoord[0]
			};
			offset = if(this.hasSteplikeDisplay) { binwidth * 0.5 } { 0.0 };
			tmp=(((x - offset - plotBounds.left) / plotBounds.width));

			^switch(domainSpec.warp.class, ExponentialWarp, {
				[1, value.size, \exp].asSpec.map(tmp).round.clip(0, value.size).asInteger-1
			}, LinearWarp, {
				(tmp * (value.size - 1)).round.clip(0, value.size-1).asInteger
			},{
				[0,value.size-1, domainSpec.warp.asSpecifier].asSpec.map(tmp).round.clip(0, value.size-1).asInteger
			});
		}
	}
}