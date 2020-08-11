(
var guis=();
var w=Window("Presets", Rect(0,0,420,200)).front; w.addFlowLayout;w.alwaysOnTop_(true);

guis[\freq]=EZSlider(w, 350@20, \freq, \freq.asSpec, {|ez| "freq ".post; ez.value.postln});
guis[\amp]=EZSlider(w, 350@20, \amp, \amp.asSpec);

g=GUIpreset(w, 350@90, guis, "/Users/jorrit/Desktop/test/", nil, true, (amp:0))//
)