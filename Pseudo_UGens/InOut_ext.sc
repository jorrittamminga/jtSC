InAz {
	*ar { arg bus = 0, numChannels = 1, azimuth;
		var ch, in, chFloor;
		ch=(azimuth+numChannels.reciprocal*(numChannels/2))%numChannels;
		chFloor=ch.floor;
		in=[In.ar(bus+chFloor), In.ar((chFloor+1).wrap(0, numChannels)+bus)];
		^XFade2.ar(in[0],in[1], ch.frac*2-1);
	}
}

//2 channel
InAz2 {
	*ar { arg bus = 0, numChannels = 1, azimuth;
		var ch, in;
		ch=(azimuth+numChannels.reciprocal*(numChannels/2))%numChannels;
		ch=ch.floor;
		in=[In.ar(bus+ch), In.ar((ch+1).wrap(0, numChannels)+bus)];
		^XFade2.ar(in[0],in[1], ch.frac*2-1);
	}
}

SelectAz {
	*ar {arg azimuth, in, numChannels=2;
		var ch=(azimuth+numChannels.reciprocal*(numChannels/2))%numChannels;
		^SelectX.ar(ch, in++in[0])
	}


}

SelectAzFocus {
	*ar {arg azimuth, in, numChannels=2, focus=1;
		var ch=(azimuth+numChannels.reciprocal*(numChannels/2))%numChannels;
		^SelectXFocus.ar(ch, in, focus, true)
	}
}