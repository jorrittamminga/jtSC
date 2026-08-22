//based on wslib 2010
XFadeCombLP {
	*ar { arg in = 0.0, gate = 1.0, maxdelaytime = 0.2, delaytime = 0.2, decaytime = 1.0, coef = 0.5, mul = 1.0, add = 0.0, fadeTime = 0.1;

		var ins = in.asArray;
		var dts = delaytime.asArray;
		var n   = ins.size.max(dts.size);

		^n.collect { |i|
			var chan     = ins.wrapAt(i);
			var thisTime = dts.wrapAt(i);
			var dChange, dTime, delayed;
			dChange = Trig.kr( HPZ1.kr( thisTime ).abs, fadeTime );
			dChange = ToggleFF.kr( dChange );
			dTime   = Latch.kr( thisTime, [ 1 - dChange, dChange ] );
			delayed = dTime.collect { |dt|
				CombLP.ar( chan, gate, maxdelaytime, dt, decaytime, coef )
			};
			XFade2.ar( delayed[0], delayed[1],
				Delay1.kr( Slew.kr( dChange, 1/fadeTime, 1/fadeTime ) ).linlin(0, 1, -1, 1) )
			.madd( mul, add );
		}.unbubble
	}
}