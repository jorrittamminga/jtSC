SoundIn2  {

	*ar { arg bus = 0;
		var chanOffset;
		chanOffset = this.channelOffset;
		if(bus.isArray.not,{
			^In.ar(chanOffset + bus, 1)
		});

		// check to see if channels array is consecutive [n,n+1,n+2...]
		if(bus.every({arg item, i;
				(i==0) or: {item == (bus.at(i-1)+1)}
			}),{
			^In.ar(chanOffset + bus.first, bus.size)
		},{
			// allow In to multi channel expand
			^In.ar(chanOffset + bus)
		})
	}

	*channelOffset {
		^NumOutputBuses.ir
	}
}
