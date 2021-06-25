/*
- stand van de faders opslaan of niet, misschien ook wel los van de NeuralNet
- welke onderdelen opslaan voor een CueList?
- CueF
*/
(
var w, keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
w=Window("Presets", Rect(400,400,400,400)).front; w.addFlowLayout; w.alwaysOnTop_(true);
keys.do{|key|
	views[key]=EZSlider(w, 350@20, key, key.asSpec)
};
views[\range]=EZRanger(w, 350@40, \freq2, \freq.asSpec);
views[\but]=Button(w, 350@20).states_([ [\a],[\b],[\c] ]);
views[\ms]=EZMultiSlider(w, 350@200, \freqs, \freq, {|ez| });
//p=PresetsJT(views, path++"presets/").makeGui(w, 350@20);
//b=PresetsNNJT(p).makeGui(w, 350@20);
p=PresetsJT(views, path++"presets/").addNN.makeGui(w, 350@20);
w.rebounds;
)