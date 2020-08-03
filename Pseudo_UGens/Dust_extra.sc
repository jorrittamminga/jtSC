DustEP {
	*ar{ arg density = 0.0, mul = 1.0, add = 0.0;
		^Dust.ar(density, density.explin(20, 20000, 1.0, 0.166666667)*mul, add)
	}

		*kr{ arg density = 0.0, mul = 1.0, add = 0.0;
		^Dust.ar(density, density.explin(20, 20000, 1.0, 0.166666667)*mul, add)
	}
}


Dust2EP {
	*ar{ arg density = 0.0, mul = 1.0, add = 0.0;
		^Dust2.ar(density, density.explin(20, 20000, 1.0, 0.166666667)*mul, add)
	}

		*kr{ arg density = 0.0, mul = 1.0, add = 0.0;
		^Dust2.ar(density, density.explin(20, 20000, 1.0, 0.166666667)*mul, add)
	}
}