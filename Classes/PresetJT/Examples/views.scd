(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presets/";
w.addFlowLayout;
w.alwaysOnTop_(true);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| [k, ez.value].postln})};
v[\ms]=MultiSliderView(w, 350@100).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);

p=ViewsPresetJT(v, path).makeGui(w, 350@20);
c=ViewsPresetJTCollection(p, [0,1]).makeGui(w, 350@20);
e=ViewsPresetJTBlender(c).makeGui(w, 350@20, false);
)

e.mean