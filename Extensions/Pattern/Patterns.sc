/*
w=noise color. 0.0=whitenoise, 1=pinknoise, 2=brownnoise
*/
Pnoise : Pattern {
	var <>lo, <>hi, <>w, <>bits, <>length;
	*new { arg lo=0.0, hi=1.0, w=0.0, bits=6, length=inf;
		^super.newCopyArgs(lo, hi, w, bits, length)
	}
	storeArgs { ^[lo,hi,w,bits,length] }
	embedInStream { arg inval;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var bitsStr = bits.asStream;
		var wStr = w.asStream;
		var hiVal, loVal, bitVal, wVal;
		var rand=0!bitsStr, pot=bitsStr.collect({|k| 2.pow(k)}), sum=pot.sum;
		var i=0;
		var rrand;
		var len=length.value(inval), pott;

		while{i<len} {
			hiVal = hiStr.next(inval);
			loVal = loStr.next(inval);
			bitVal = bitsStr.next(inval);
			wVal = wStr.next(inval);

			if(hiVal.isNil or: { loVal.isNil }) { ^inval };

			pott=pot.pow(wVal);
			sum=pott.sum;

			rand.size.collect({|j|
				if (i%pot[j]==0, {rand[j]=1.0.rand*(pot[j].pow(wVal))});
			});
			i=i+1;
			inval = (rand.sum).linlin(0, sum, loVal, hiVal).yield
		};

		^inval;
	}
}

Pnoise2 : Pattern {
	var <>lo, <>hi, <>w, <>bits, <>noisePattern, <>length;
	*new { arg lo=0.0, hi=1.0, w=0.0, bits=6, noisePattern=Pwhite(0.0, 1.0, inf), length=inf;
		^super.newCopyArgs(lo, hi, w, bits, noisePattern, length)
	}
	storeArgs { ^[lo,hi,w,bits,noisePattern,length] }
	embedInStream { arg inval;
		var loStr = lo.asStream;
		var hiStr = hi.asStream;
		var bitsStr = bits.asStream;
		var wStr = w.asStream;
		var npStr = noisePattern.asStream;
		var hiVal, loVal, bitVal, wVal, npVal;
		var rand=0!bitsStr, pot=bitsStr.collect({|k| 2.pow(k)}), sum=pot.sum;
		var i=0;
		var rrand;
		var len=length.value(inval), pott;

		while{i<len} {
			hiVal = hiStr.next(inval);
			loVal = loStr.next(inval);
			bitVal = bitsStr.next(inval);
			wVal = wStr.next(inval);
			npVal = npStr.next(inval);

			if(hiVal.isNil or: { loVal.isNil }) { ^inval };

			pott=pot.pow(wVal);
			sum=pott.sum;

			rand.size.collect({|j|
				if (i%pot[j]==0, {rand[j]=npVal*(pot[j].pow(wVal))});
			});
			i=i+1;
			inval = (rand.sum).linlin(0, sum, loVal, hiVal).yield
		};
		^inval;
	}
}

Pbrown2 : Pattern {
	var <>lo, <>hi, <>step, <>length, <>start;

	*new { arg lo=0.0, hi=1.0, step=0.125, length=inf, start=0.5;
		^super.newCopyArgs(lo, hi, step, length, start)
	}

	storeArgs { ^[lo,hi,step,length, start] }

	embedInStream { arg inval;
		var cur;
		var loStr = lo.asStream, loVal;
		var hiStr = hi.asStream, hiVal;
		var stepStr = step.asStream, stepVal;

		loVal = loStr.next(inval);
		hiVal = hiStr.next(inval);
		stepVal = stepStr.next(inval);
		cur = start; // rrand(loVal, hiVal);
		if(loVal.isNil or: { hiVal.isNil } or: { stepVal.isNil }) { ^inval };

		length.value(inval).do {
			loVal = loStr.next(inval);
			hiVal = hiStr.next(inval);
			stepVal = stepStr.next(inval);
			if(loVal.isNil or: { hiVal.isNil } or: { stepVal.isNil }) { ^inval };
			cur = this.calcNext(cur, stepVal).fold(loVal, hiVal);
			inval = cur.yield;
		};

		^inval;
	}

	calcNext { arg cur, step;
		^cur + step.xrand2
	}
}
/*
({Pwhite(0.0, 1.0).asStream.next}!100).plot
({Pseries(0.0, 2.0, 10).asStream.next}!10)

x=Pnoise(0.0, 1.0, 1.0, 6, inf).asStream; (1000.collect({x.next})).plot
x=Pnoise2(0.0, 1.0, 1.0, 6, Pexprand(0.001, 1.0), inf).asStream; (1000.collect({x.next})).plot
x=Pnoise2(0.0, 1.0, 0.5, 8, Pwhite(0.0, 1.0), inf).asStream; (1000.collect({x.next})).plot

Pbind(\dur, 1.0, \midinote, Pseries(0, 1)+60).play
Pbind(\dur, Pnoise(0.1, 0.5, 0.0, 7), \midinote, Pclump(Pnoise(1, 7, 0.0, 7), Pnoise(32, 96, 0.0, 6).round(1.0))).play

*/