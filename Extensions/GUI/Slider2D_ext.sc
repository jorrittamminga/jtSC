+ Slider2D {

	value {
		^[this.x, this.y]
	}

	value_ {arg val;
		this.setXY(val[0],val[1])
	}

	valueAction_ {arg val;
		this.setXYActive(val[0],val[1])
	}
}