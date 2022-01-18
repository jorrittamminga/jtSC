(
w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);
c=CueListJT(thisProcess.nowExecutingPath.dirname++"/cues3/");
c.makeGui(w, 350@200);
s.waitForBoot{
	SynthDef(\Test, {arg freq=1000, amp=0.0;
		Out.ar(0, SinOsc.ar(freq, 0, amp)!2)
	}, metadata: (specs: (amp: \amp, freq: \freq))).add;
	s.sync;
	x=Synth(\Test);
	s.sync;
	v=x.makeGui(w, 350@20, waitTime: 0.01);
	{
		var views=v.views;
		views.postln;
		p=PresetsJT(views, \sine).makeGui(w, 350@20).addToCueList(c);
	}.defer
}
)