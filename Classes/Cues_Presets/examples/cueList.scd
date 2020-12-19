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

p=PresetsJT(v, \test).makeGui(w, 350@20).addToCueList(c);
)

(
x=( 'ms': [ 0.56910568475723, 0.3414634168148, 0.27743899822235, 0.18292683362961 ], 'slider': 0.0, 'bang': 0, 'bang2': 1,
	'rq': 0.062571196936032, 'method': 1, 'durations': 2.4, 'freq': 1389.6020968787, 'extras': ( 'curves': 'sin' ),
	'amp': 0.0 );

v.collect(_.value).valuesActionsTransition(x, x.durations, x[\extras][\curves], specs: v.collect(_.controlSpec), actions: v.collect(_.action), nrt:true)[\routinesJT].keysValuesDo{
	arg key, func;
	//[key, 0, func, func.value(0)].postln;
}
)
.valuesActionsTransition