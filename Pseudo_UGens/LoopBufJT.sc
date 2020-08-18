LoopBufJT {
	*ar{arg numChannels, bufnum, rate=1.0, trigger=1.0, startPos=0.0, endPos= -1, interpolation=2, fadeTime=0.1;
		//arg fadeTime=0.0, startPos=0, endPos= -1, bufnum=0, rate=1.0;
		var phase, env, gate, trig, out, ff;
		var masterPhasor, maxPhase;
		var numFrames, duration;
		rate=rate*BufRateScale.kr(bufnum);
		endPos=((endPos<0)*BufFrames.kr(bufnum)+1)+endPos;
		numFrames=endPos-startPos;
		duration=numFrames*BufSampleRate.kr(bufnum);
		fadeTime=fadeTime.min(duration*rate.abs.reciprocal*0.5);
		masterPhasor=Phasor.ar(trigger, rate, 0, numFrames-(fadeTime*BufSampleRate.kr(bufnum)), 0);
		trig=Trig1.ar(masterPhasor-1, 0.001);
		ff=ToggleFF.ar(trig);
		ff=[ff, 1-ff];
		phase=Phasor.ar(ff, rate, startPos, BufFrames.ir(bufnum), startPos);
		//env=EnvGen.ar(Env.asr(fadeTime, 1, fadeTime, [2.0,2.0]), ff);
		fadeTime=fadeTime.reciprocal;
		env=Slew.ar(ff, fadeTime, fadeTime).sqrt;
		out=BufRd.ar(numChannels, bufnum, phase, 1, 4)*env;
		^out.sum
	}
}