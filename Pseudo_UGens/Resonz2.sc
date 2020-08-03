Resonz2 {
	
	*ar{ arg in = 0.0, freq = 440.0, bwr = 1.0, mul = 1.0, add = 0.0;
		//2.do({in=Resonz.ar(in,freq,bwr,mul,add)});
		2.do({in=Resonz.ar(in,freq,bwr)});
		in=in*mul+add;
		^in	
		}
}