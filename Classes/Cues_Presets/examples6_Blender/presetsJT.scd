(
var w, keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
w=Window("Presets", Rect(400,400,400,400)).front; w.addFlowLayout; w.alwaysOnTop_(true);
keys.do{|key|
	views[key]=EZSlider(w, 350@20, key, key.asSpec)
};
views[\range]=EZRanger(w, 350@40, \freq2, \freq.asSpec);
views[\but]=Button(w, 350@20).states_([ [\a],[\b],[\c] ]);

p=PresetsJT(views, path++"presets/").addBlender.makeGui(w, 350@20);
w.rebounds;
)