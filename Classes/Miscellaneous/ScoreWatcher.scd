~sw.turnOffs


x=ScoreWatcher.new;

x.register(0, [\s_new, \Sine, 1000, 1, 0, \freq, 100, \dur, 2.0])
x.register(1, [\s_new, \Sine, 1001, 1, 0, \freq, 105, \dur, 2.0])
x.register(1.9, [\s_new, \Sine, 1002, 1, 0, \freq, 115, \dur, 2.0])

x.freqs
x.turnOffs

x.checkFreq(1.9, 302)



(
var p=(freq: 50, freqDev: 20, harm: 1);
var cs=(amp: \amp, freq: \freq, freqDev: [0, 200, 7.0]);
var w=Window.new.front;
w.alwaysOnTop_(true);
w.onClose_{s.freeAll};
w.addFlowLayout;
EZSlider(w, 350@20, \freq, \freq.asSpec, {|ez|
	p[\freq]=ez.value;
	p[\cof]=(p[\freq]*p[\harm]).min(19999);
	x.set(\freq, p[\freq], \cof, p[\cof]);
	y.set(\freq, p[\freq]+p[\freqDev], \cof, ((p[\freq]+p[\freqDev])*p[\harm]).min(19999) );
}, p[\freq]);
EZSlider(w, 350@20, \freqDev, cs[\freqDev], {|ez|
	p[\freqDev]=ez.value;
	y.set(\freq, p[\freq]+p[\freqDev], \cof, ((p[\freq]+p[\freqDev])*p[\harm]).min(19999));
}, p[\freqDev]);
EZSlider(w, 350@20, \amp, \amp.asSpec, {|ez| y.set(\amp, ez.value)}, 0);
EZSlider(w, 350@20, \harm, ControlSpec(1.0, 32, \exp), {|ez|
	p[\harm]=ez.value;
	p[\cof]=(p[\freq]*p[\harm]).min(19999);
	x.set(\cof, p[\cof]);
	y.set(\cof, ((p[\freq]+p[\freqDev])*p[\harm]).min(19999) );
}, 0);
s.waitForBoot{
	x={arg freq=50, cof=50; BLowPass4.ar(LFTri.ar(freq, 0, 0.5), cof.lag(0.1))!2}.play;
	y={arg amp=0.0, freq=70, cof=70; BLowPass4.ar(LFTri.ar(freq, 0, amp), cof.lag(0.1))!2}.play;
})

freq    min     max
20      0.0
40      0.0
80      0.0
160     1.0
320     16.0    40.0
640

a=5; (a<0)||(a>20)