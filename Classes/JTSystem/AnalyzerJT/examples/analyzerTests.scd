(
s.waitForBoot{
	x={Out.ar(100, SoundIn.ar(0))}.play;
	s.sync;
	a=AnalyzerJT(100.asBus(\audio, 1, s), x, [\onsets, \mfcc, \specflatness, \sensorydissonance, \spectralentropy]);
	g=GaterJT(a, [\specflatness]);

	a.makeGUI(nil, 350@20, freeOnClose:true
		, thresholds:(specflatness:0.2)

		, thresholdFuncs:(
			specflatness: {|ez|
				{g.gui.views[\threshold_specflatness].value_(ez.value)}.defer;
				g.synth.set(\threshold_specflatness, ez.value)}
		)

	);
	g.makeGUI(nil, 350@15, freeOnClose:true );
}
)

a.outBusperDescriptor
a.outBusT
a.outBusFFT
a.outBusFFTperDescriptor

(
g.gui.views[\threshold_specflatness].action=g.gui.views[\threshold_specflatness].action.addFunc({|ez| a.gui.views[\threshold_specflatness].value_(ez.value)});

p=PresetSystem(g.gui.views, thisProcess.nowExecutingPath.dirname++"/presets/", "gater");
p.gui(g.gui.window, 350@20);
g.gui.window.rebounds;

p=PresetSystem(a.gui.views, thisProcess.nowExecutingPath.dirname++"/presets/", "analyzer");
p.gui(a.gui.window, 350@20, 2);
a.gui.window.rebounds;

)
