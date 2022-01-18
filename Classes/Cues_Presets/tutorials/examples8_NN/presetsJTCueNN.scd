/*
values van de input slider updaten niet in visuals (wel in de rest hoor!)
zou door de EZMultiSlider of .makeSliders te maken hebben
*/
(
var w, keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
var v;
v=Window("CueList", Rect(0,0,400,400)).front; v.addFlowLayout; v.alwaysOnTop_(true); v.onClose_{w.close};
c=CueListJT(path++"cues/").makeGui(v);
w=Window("Presets", Rect(400,400,400,400)).front; w.addFlowLayout; w.alwaysOnTop_(true);
keys.do{|key|
	views[key]=EZSlider(w, 350@20, key, key.asSpec, {|ez| [key, ez.value]})
};
views[\range]=EZRanger(w, 350@40, \freq2, \freq.asSpec);
views[\but]=Button(w, 350@20).states_([ [\a],[\b],[\c] ]);
views[\ms]=EZMultiSlider(w, 350@200, \freqs, \freq, {|ez| });
p=PresetsJT(views, path++"presets/").addNN.makeGui(w, 350@20).neuralNet.addToCueList(c, \nn1);
w.rebounds;
)