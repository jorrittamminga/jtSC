StablePitch {
	*ar{arg in, stableTime=0.5, maxInterval=0.2, fadeIn=0.2, fadeOut=0.2, minFreq=100, maxFreq=1500, pitch, confidence, latency=0.0;
		var ff=0, ws=1024, stableOut, interval, gate, time, env, gateOut;
		//var pitch, confidence;
		//------------------------------------------------------------
		fadeIn=fadeIn.min(stableTime*0.5);
		fadeOut=fadeOut.min(stableTime*0.5);
		if (pitch==nil, {
			#pitch, confidence=FluidPitch.kr(in, 2, minFreq, maxFreq, 1, ws);
			latency=SampleDur.ir*ws;
		});
		interval=HPZ1.kr(pitch).abs;
		interval=Latch.kr(interval, Changed.kr(pitch));
		gate=interval<maxInterval;
		time=Sweep.kr(gate);
		gate=(time>stableTime)*gate*(confidence>0.5);
		gateOut=gate;
		ff=ToggleFF.kr(gate);
		gate=[ff, 1-ff]*gate;
		env=Env.new([0, 1.0, 1.0, 0],[fadeIn, (stableTime-fadeIn-fadeOut).max(0), fadeOut], [\sin, 0, 0, \sin], 1);
		env=EnvGen.kr(env, gate);
		stableOut=DelayN.ar(in, 5.0, stableTime+latency, env).sum;
		//time.poll(gateOut);
		^[stableOut, gate, time]
	}
}