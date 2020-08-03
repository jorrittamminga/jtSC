PV_BrickWall_LPF : PV_ChainUGen {
	*new { arg buffer, freq=19990;
		^PV_BrickWall(buffer, (freq/(SampleRate.ir*0.5))-1)
	}
}

PV_BrickWall_HPF : PV_ChainUGen {
	*new { arg buffer, freq=20;
		^PV_BrickWall(buffer, freq/(SampleRate.ir*0.5));
	}
}


PV_LPF : PV_ChainUGen {
	*new { arg buffer, freq=19990;
		^PV_BrickWall(buffer, (freq/(SampleRate.ir*0.5))-1)
	}
}

PV_HPF : PV_ChainUGen {
	*new { arg buffer, freq=20;
		^PV_BrickWall(buffer, freq/(SampleRate.ir*0.5));
	}
}


