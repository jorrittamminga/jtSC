+ ControlSpec {
	init {
		if (warp.class==CurveWarp, {
			this.warp_(warp.curve)
		},{
			warp = warp.asWarp(this);
		});
		clipLo = min(minval, maxval);
		clipHi = max(minval, maxval);
	}
}