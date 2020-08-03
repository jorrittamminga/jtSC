+ ClusterJT {

	*initClass {

		addActions = (
			addToHead: 0,
			addToTail: 1,
			addBefore: 2,
			addAfter: 3,
			addReplace: 4,
			h: 0,
			t: 1,
			// valid action numbers should stay the same
			0: 0, 1: 1, 2: 2, 3: 3, 4: 4
		);

		initArgsEvent=(
			voices: 16
			, sync: 0.0
			, loop: 1
			, delayTime: [0.0, 1.0]
			, attackTime:0.0
			, releaseTime:0.0
			, rate: [0.5, 2.0]
			, dur: -1.0//<0 is fraction of length of the buffer, >0 is absolute length in s.
			, durRateScale: -1.0
			, ampRateScale: 0.0
			, curveRelease: -4.0
			, curveAttack: 0.0
			, azimuth: [-1.0, 1.0]
			, numChannels: 2
			, outBus: 0
			, startFrame: 0//startFrame between 0 and 1 is fraction >1 is real frames
			, reverse: 0//factor tussen 0 (no reverse) and 1 (100% reverse)
			, synthDef: \PlayBufJT
			, sampleFormat: "int24"
			, headerFormat: "AIFF"
		);

		initControlSpecs=(
			voices: ControlSpec(1, 1024, \exp, 1)
			, sync: ControlSpec(0.0, 1.0)
			, loop: ControlSpec(0.0, 1.0, 0, 1)
			, delayTime: ControlSpec(0.0, 10.0, 6)
			, attackTime:ControlSpec(0.0, 1.0)
			, releaseTime:ControlSpec(0.0, 1.0)
			, rate: ControlSpec(1/16, 128, \exp)
			, dur: ControlSpec(-10.0, 100, 0)
			, durRateScale: ControlSpec(-2.0, 2.0)
			, ampRateScale: ControlSpec(-2.0, 2.0)
			, curveRelease: ControlSpec(-8.0, 0)
			, curveAttack: ControlSpec(-8.0, 8)
			, azimuth: \bipolar.asSpec
			, startFrame: ControlSpec(0.0, 1.0)
			, reverse: ControlSpec(0.0, 1.0)
		);

		initFuncsEvent=(
			rate: {arg x, p; exprand(x[0],x[1])}
			,azimuth: {arg x, p; rrand(x[0],x[1])}
			,delayTime: {arg x, p; rrand(x[0],x[1])}
		);

		initSynthdef=[1,2].collect{|n|
			SynthDef((\PlayBufJT++n).asSymbol, {arg outBus=0, rate=1.0, amp=0.5
				, azimuth=0.0
				, buffer=0, startFrame=0, attackTime=0, sustainTime=1.0, releaseTime=0.0
				, loop=1, curveAttack=0, curveRelease= -4.0;
				var env=EnvGen.kr(Env.linen(attackTime, sustainTime, releaseTime, amp
					, [curveAttack, 0, curveRelease]), doneAction:2);
				var out=PlayBuf.ar(n, buffer, BufRateScale.ir(buffer)*rate, 1, startFrame
					, loop)*env;
				if (n==1, {
					Out.ar(outBus, Pan2.ar(out, azimuth))
				},{
					Out.ar(outBus, Pan2.ar( XFade2.ar(out[0], out[1], azimuth), azimuth))
				});
			});
		};

		clusterDiskInSynthDef=[1,2,3,4].collect{|ch|
			SynthDef((\clusterDiskInSynthDef++ch).asSymbol, {arg bufnum, outBus=0, amp=1.0
				, rate=1.0;
				var out;
				out=VDiskIn.ar(ch, bufnum, rate);
				FreeSelfWhenDone.kr(out);
				Out.ar(outBus, out*amp)
			});
		};

		clusterPlayBufSynthDef=[1,2,3,4].collect{|ch|
			SynthDef((\clusterPlayBufSynthDef++ch).asSymbol, {arg bufnum, outBus, amp=1.0
				, rate=1.0;
				var out;
				out=PlayBuf.ar(ch, bufnum, rate, 1, 0, 0, 2);
				Out.ar(outBus, out*amp)
			})
		};


		//initFunction=this.makeInitFunction;
	}
}