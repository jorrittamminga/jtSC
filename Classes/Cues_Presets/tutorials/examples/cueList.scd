(
var w=Window("cueList", Rect(400,400,400,830)).front;
var path=thisProcess.nowExecutingPath.dirname++"/cues/";
p=();
w.addFlowLayout;
w.alwaysOnTop_(true);

c=CueListJT(path).makeGui(w, 350@20);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| [\n_set, 1000, k, ez.value]  })};
v[\slider]=Slider(w, 350@20);
v[\ms]=MultiSliderView(w, 350@250).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);
v[\bang]=Button(w, 350@20).states_([ [\BANG] ]).action_{"BANG!".postln};
v[\bang2]=Button(w, 350@20).states_([ [\BANG2],[\BANG2,Color.black,Color.green] ]).action_{|b| if (b.value==1, {"BANG2!".postln})};

//p=PresetsJT(v, \test).makeGui(w, 350@20).addToCueList(c);
p=PresetsJT(v, \test).addToCueList(c);
"poepen".postln;
p.makeGui(w, 350@20)
)