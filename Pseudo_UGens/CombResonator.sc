ResonatorJT {

	*ar{ arg in = 0.0, minFreq=20, freq = 440, decaytime = 1.0, damp = 0.25, mul = 1.0, add = 0.0;
		var detune, o, coef;

		coef=freq.explin(20, SampleRate.ir*0.5, 1.0, 0)*damp;
		mul=freq.expexp(20, SampleRate.ir*0.5, 1.0, 0.2)*mul;

		o=(freq*SampleDur.ir)*pi;
		detune=(((coef.neg*o.sin)/(1-(o.cos*coef)))*freq*0.25);

		^Pluck.ar(in, in, minFreq.reciprocal, (freq-detune).reciprocal, decaytime, coef, mul, add)
		}
}


WaveGuideJT {

	*ar{ arg in = 0.0, minFreq=20, freq = 440, decaytime = 1.0, damp = 0.25, posIn=0.5, posOut=0.5, mul = 1.0, add = 0.0;
		var detune, o, coef;
		var in1, in2, delayTime, fb;
		var buf1, buf2, buf3;

		buf1=LocalBuf(minFreq.reciprocal*SampleRate.ir);
		buf2=LocalBuf(minFreq.reciprocal*SampleRate.ir);
		buf3=LocalBuf(minFreq.reciprocal*SampleRate.ir);

		coef=freq.explin(20, SampleRate.ir*0.5, 1.0, 0)*damp;
		mul=freq.expexp(20, SampleRate.ir*0.5, 1.0, 0.2)*mul;

		o=(freq*SampleDur.ir)*pi;
		detune=(((coef.neg*o.sin)/(1-(o.cos*coef)))*freq*0.25);

		delayTime=(freq-detune).reciprocal-ControlDur.ir;
		fb=0.001.pow(delayTime/decaytime);

		//in=OnePole.ar(in, coefIn);
		in=OnePole.ar(in, coef);
		in=BufDelayL.ar(buf1, in, posIn*delayTime, -1.0, in);

		//in=OnePole.ar(LocalIn.ar(1), coef)+in;
		in=LocalIn.ar(1)+in;
		in1=BufDelayL.ar(buf2, in, delayTime*posOut);
		in2=BufDelayL.ar(buf3, in1, delayTime*(1-posOut));
		in2=OnePole.ar(in2, coef);
		LocalOut.ar(in2*fb);

		^((in1+in2)*mul+add)
		}
}


WaveGuide2JT {

	*ar{ arg in = 0.0, minFreq=20, freq = 440, decaytime = 1.0, damp = 0.25, posIn=0.5, posOut=0.5, apFactor=0.5, inharmonicity=0.0, mul = 1.0, add = 0.0;
		var detune, o, coef;
		var in1, in2, delayTime, delayTime1, delayTime2, fb;
		var buf1, buf2, buf3, buf4;

		buf1=LocalBuf(minFreq.reciprocal*SampleRate.ir);
		buf2=LocalBuf(minFreq.reciprocal*SampleRate.ir);
		buf3=LocalBuf(minFreq.reciprocal*SampleRate.ir);
		buf4=LocalBuf(minFreq.reciprocal*SampleRate.ir);

		coef=freq.explin(20, SampleRate.ir*0.5, 1.0, 0)*damp;
		mul=freq.expexp(20, SampleRate.ir*0.5, 1.0, 0.2)*mul;

		o=(freq*SampleDur.ir)*pi;
		detune=(((coef.neg*o.sin)/(1-(o.cos*coef)))*freq*0.25);

		delayTime=(freq-detune).reciprocal-ControlDur.ir;
		fb=0.001.pow(delayTime/decaytime);

		delayTime1=(1-apFactor)*delayTime;
		delayTime2=delayTime-delayTime1;


		//in=OnePole.ar(in, coefIn);
		in=OnePole.ar(in, coef);
		in=BufDelayL.ar(buf1, in, posIn*delayTime1, -1.0, in);

		//in=OnePole.ar(LocalIn.ar(1), coef)+in;
		in=LocalIn.ar(1)+in;
		in1=BufDelayL.ar(buf2, in, delayTime1*posOut);
		in2=BufDelayL.ar(buf3, in1, delayTime1*(1-posOut));
		in2=OnePole.ar(in2, coef);
		in2=BufAllpassL.ar(buf4, in2, delayTime2, inharmonicity*delayTime2);
		LocalOut.ar(in2*fb);

		^((in1+in2)*mul+add)
		}
}