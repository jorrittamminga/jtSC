(
s.options.numInputBusChannels=16;
s.meter;
s.waitForBoot{

	x={Out.ar(100, SoundIn.ar(0))}.play; s.sync;
	c=BufWrJT(100, x, 60).startRecording;
	a=AnalyzerJT(100, x, [\onsets]);
	/*
	b=BounceLive(x, \addAfter, 100, s, \addToHead, 0, 2);
	b.makeGui(nil, 220@15);
	*/
}
)

OSCFunc({arg ...msg; msg.postln}, c.cmdNameT)

c.synth.set(\inBusT, a.outBusT.index);
c.synth.set(\inBusT, 2000000);

s.options.numControlBusChannels





a.outBusT
b.close

c.synth.run(true)


s.queryAllNodes