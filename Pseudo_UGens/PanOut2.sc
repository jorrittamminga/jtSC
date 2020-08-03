// aanpassing op PanOut
// wslib 2006

PanOut2 {

	*pan { |in, pos = 0| ^Pan2.ar( in, ( pos.fold(0.0,1.0) - 0.5) * 2.0) }

	*switchBus { |bus = 0, in, wrap, offsetBus|  // in should be 2 channels

		if( wrap.isNil )
		{ ^[ Out.ar( bus.round(2) + offsetBus, in[0] * (bus >= -1 ).binaryValue  ),
			Out.ar(  ( (bus+1).round(2) - 1) + offsetBus, in[1] * (bus >= 0 ).binaryValue ) ] }
		{ ^[ Out.ar( bus.round(2).wrap(0,wrap) + offsetBus, in[0] ),
			Out.ar(  ( (bus+1).round(2) - 1).wrap(0,wrap)  + offsetBus, in[1] ) ]  };
	}

	*ar { |bus = 0, channel, wrap, offsetBus=0|
		^this.switchBus( bus, this.pan( channel, bus ), wrap, offsetBus ); }

}


PanOutPairs {

	*ar {arg numberOfSpeakers=8, bus=0, in, xfade=1.neg, shift=1.0;
		var shiftSpeakers=numberOfSpeakers*0.5;
		shiftSpeakers=[0, shiftSpeakers*shift];
		in=Pan2.ar(in, xfade);
		^2.collect{|i|
			PanOut.ar(shiftSpeakers[i]+bus, in[i], numberOfSpeakers);
		}
	}
}


PanOutXY{
	*ar	{arg numChans, bus=0, in, x=0, y=0, theta=0.0;
		var point, angle, pos, factor;

		point=Point(x,y).rotate(theta.neg);
		angle=point.y.asin;
		pos=[angle, pi-angle]+theta;

		factor=ControlSpec(pos[0], pos[1]).asSpec.unmap(Point(x,y).theta);
		//factor=(Point(x,y).theta-pos[0])/(pos[1]-pos[0]);

		pos=pos/pi.neg+0.5;	//convert to azimuthPan
		pos=(pos+numChans.reciprocal*(numChans/2))%numChans;
		//factor zou ook *2-1 kunnen worden om in XFade2 te gebruiken

		in=Pan2.ar(in, factor*2-1);
		//factor=[1-factor,factor].sqrt;

		//^2.collect{|i| PanOut.ar(pos[i], in*factor[i], numChans)}
		^2.collect{|i|
			PanOut.ar(pos[i]+bus, in[i])}
	}


}




PanOutXY2{
	*ar	{arg numChans, bus=0, in, x=0, y=0, theta=0.0;
		var angle1, angle2;


	}
}