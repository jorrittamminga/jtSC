+Polar {

	azimuth {//azimith in radians
		^(0.5pi-this.theta).wrap2(pi)
	}

	azimuth2 {//for PanAz and PanB2
		^(0.5pi-this.theta).wrap2(pi)/pi
	}

}