+ OSCJT {

	*initClass {
		defaultRanges = (
			ZigSim: (
				accel: [-8, 8]//in G, gravitional force, ±8 g (gravitational force)
				, gyro: (100/9)*[pi.neg, pi]//in radians per second (angular velocity),±2000 degrees per second (dps)=100/9*pi
				, quaternion: [-1.0, 1.0]//orientation, attitude
				, rotation: [-pi, pi]
				, arkitposition: [-1.0, 1.0]
				, arkitrotation: [-pi, pi]
			)
			, TouchOSC: (
				accxyz: [-8, 8]
			)
			, GyrOSC: (
				rmatrix: [-1.0, 1.0]
				, rrate: [-3.0, 3.0]
				, quat: [-1.0, 1.0]
				, grav: [-1.0, 1.0]
				, gyro: [pi.neg, pi]
				, accel: [-16, 16.0]
				, panning: [-pi, pi]
			)
		);
		defaultFuncs = (
			ZigSim: (
				quaternion: {arg quaternionData
					, data=()
					, action=()
					;
					var w,x,y,z, quaternion;
					#x,y,z,w=quaternionData;
					quaternion=Quaternion(w,x,y,z);
					data[\rotation]=quaternion.eulerAngles;
					action[\rotation].value(data[\rotation]);
				}
			)
			, GyrOSC: (
				quat: {arg quaternionData
					, data=()
					, action=()
					;
					var w,x,y,z, quaternion;
					#w,x,y,z=quaternionData;
					quaternion=Quaternion(w,x,y,z);
					data[\panning]=quaternion.panning;
					action[\panning].value(data[\panning]);
				}
			)
		);
		defaultFuncsKeys = (
			ZigSim:(quaternion: \rotation)
			, GyrOSC:(quat: \panning)
		);
		defaultDerivatives = (
			ZigSim: (accel: \jerk)
			, TouchOSC: (accxyz: \jerk)
			, GyrOSC: (accel: \jerk)
		);

	}


}