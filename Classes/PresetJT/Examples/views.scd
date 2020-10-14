(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presets/";
w.addFlowLayout;
w.alwaysOnTop_(true);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| [k, ez.value].postln})};
v[\ms]=MultiSliderView(w, 350@100).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);

p=PresetJT(v, path).makeGui(w, 350@20);
//c=PresetJTCollection(p, [0,1]).makeGui(w, 350@20);
c=PresetJTCollectionBlender(p, [0,1], 'wrapAt', 'depth').makeGui(w, 350@20);
)