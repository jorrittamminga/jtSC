+ Quaternion {

	w {^a}
	x {^b}
	y {^c}
	z {^d}

	eulerAngles {
		/*
		var w = this.w, x = this.x, y = this.y, z = this.z;
		var roll, pitch, yaw;

		// Roll (rotation around X axis)
		roll = atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y));

		// Pitch (rotation around Y axis)
		pitch = asin(2 * (w * y - z * x));
		//^asin((2 * (a * c + (b * d))).clip(-1.0, 1.0));

		// Yaw (rotation around Z axis)
		yaw = atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z));

		^[roll, pitch, yaw]; // in radians

		//[1, roll, pitch, yaw].postln;
		//[2, this.tilt, this.tumble, this.rotate].postln;
		*/
		//     roll        pitch       yaw
		^[this.tilt, this.tumble, this.rotate]
	}

	panning {
		//azimuth, elevation, roll
		^[this.rotate, this.tilt, this.tumble]
	}

	rotationAngle {
		//Quaternion should be normalized
		^(2 * acos(a))//in radians
	}

	/*
	eulerAngles {arg rotate=1;
	var q;
	var sinr_cosp, cosr_cosp, sinp, cosp, siny_cosp, cosy_cosp;
	var roll, pitch, yaw;

	//rotate
	q=Quaternion(*this.coordinates.rotate(rotate));

	//normalize first
	q=q/q.norm;

	// roll (x-axis rotation)
	sinr_cosp = 2 * ((q.w * q.x) + (q.y * q.z));
	cosr_cosp = 1 - (2 * ((q.x * q.x) + (q.y * q.y)));
	roll = atan2(sinr_cosp, cosr_cosp);

	// pitch (y-axis rotation)
	sinp = sqrt(1 + (2 * ((q.w * q.y) - (q.x * q.z))));
	cosp = sqrt(1 - (2 * ((q.w * q.y) - (q.x * q.z))));
	pitch = (2 * atan2(sinp, cosp)) - (pi *0.5 );

	// yaw (z-axis rotation)
	siny_cosp = 2 * ((q.w * q.z) + (q.x * q.y));
	cosy_cosp = 1 - (2 * ((q.y * q.y) + (q.z * q.z)));
	yaw = atan2(siny_cosp, cosy_cosp);

	^[roll, pitch, yaw]//x,y,z  azimuth, elevation,
	}
	*/
	/*
	EulerAngles ToEulerAngles(Quaternion q) {
	EulerAngles angles;

	// roll (x-axis rotation)
	double sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
	double cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
	angles.roll = std::atan2(sinr_cosp, cosr_cosp);

	// pitch (y-axis rotation)
	double sinp = std::sqrt(1 + 2 * (q.w * q.y - q.x * q.z));
	double cosp = std::sqrt(1 - 2 * (q.w * q.y - q.x * q.z));
	angles.pitch = 2 * std::atan2(sinp, cosp) - M_PI / 2;

	// yaw (z-axis rotation)
	double siny_cosp = 2 * (q.w * q.z + q.x * q.y);
	double cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
	angles.yaw = std::atan2(siny_cosp, cosy_cosp);

	return angles;
	}

	*/
}

+ Array {

	/*
	norm {
	^sqrt(sum(collect(this.coordinates, _.squared)))
	}

	*/
	eulerAngles {
		var r;
		r=this.deepCopy.clump(4);
		//r=r.collect{|q| var qu=Quaternion(*q); [qu.tilt, qu.tumble, qu.rotate]}.flat;
		^r.collect{|q| var qu=Quaternion(*q); qu.eulerAngles}.flat;
	}

}