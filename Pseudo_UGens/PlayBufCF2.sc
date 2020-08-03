PlayBufCF2 {
	// dual play buf which crosses from 1 to the other at trigger

	*ar { arg numChannels, bufnum=0, rate=1.0, trigger=1.0, startPos=0.0, loop = 0.0,
			lag = 0.1, n = 2; // alternative for safemode

		var index, method = \ar, on;

		switch ( trigger.rate,
			 \audio, {
				index = Stepper.ar( trigger, 0, 0, n-1 );
			 },
			 \control, {
				index = Stepper.kr( trigger, 0, 0, n-1 );
				method = \kr;
			},
			\demand, {
				trigger = TDuty.ar( trigger ); // audio rate precision for demand ugens
				index = Stepper.ar( trigger, 0, 0, n-1 );
			},
			{ ^PlayBuf.ar( numChannels, bufnum, rate, trigger, startPos, loop ); } // bypass
		);

		on = n.collect({ |i|
			//on = (index >= i) * (index <= i); // more optimized way?
			InRange.perform( method, index, i-0.5, i+0.5 );
		});
		switch ( rate.rate,
			\demand,  {
				rate = on.collect({ |on, i|
					Demand.perform( method, on, 0, rate );
				});
			},
			\control, {
				rate = on.collect({ |on, i|
					Gate.kr( rate, on ); // hold rate at crossfade
				});
			},
			\audio, {
				rate = on.collect({ |on, i|
					Gate.ar( rate, on );
				});
			},
			{
				rate = rate.asCollection;
			}
		);
		switch ( trigger.rate,
			\demand,  {
				bufnum = on.collect({ |on, i|
					Demand.perform( method, on, 0, bufnum );
				});
			},
			\control, {
				bufnum = on.collect({ |on, i|
					Latch.kr( bufnum, on ); // hold rate at crossfade
				});
			},
			\audio, {
				bufnum = on.collect({ |on, i|
					Latch.ar( K2A.ar(bufnum), on );
				});
			},
			{
				bufnum = bufnum.asCollection;
			}
		);
		if( startPos.rate == \demand ) {
			startPos = Demand.perform( method, trigger, 0, startPos )
		};

		lag = 1/lag.asArray.wrapExtend(2);

		^Mix(
			on.collect({ |on, i|
				PlayBuf.ar( numChannels
					, bufnum.wrapAt(i)
					, rate.wrapAt(i), on, startPos, loop )
					* Slew.perform( method, on, lag[0], lag[1] ).sqrt
			})
		);

	}
}
