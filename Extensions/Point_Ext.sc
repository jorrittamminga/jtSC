+Point {

	azimuth {//azimith in radians
		^(0.5pi-this.theta)
	}

	azimuth2 {//for PanAz and PanB2
		^(0.5pi-this.theta)/pi
	}

}