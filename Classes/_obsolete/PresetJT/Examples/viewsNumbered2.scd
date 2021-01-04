(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presetsNumbered2/";
w.addFlowLayout;
w.alwaysOnTop_(true);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| })};
v[\ms]=MultiSliderView(w, 350@100).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);
v[\but]=Button(w, 350@20).states_([ [\a],[\b],[\c],[\d] ]);

p=PresetsJT(v, path).makeGui(w, 350@20);
//p=PresetsJT(v, \test).makeGui(w, 350@20);
)