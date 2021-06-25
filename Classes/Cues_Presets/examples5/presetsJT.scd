(
var w, keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
w=Window("Presets", Rect(400,400,400,400)).front; w.addFlowLayout; w.alwaysOnTop_(true);
keys.do{|key|
	views[key]=EZSlider(w, 350@20, key, key.asSpec)
};
views[\range]=EZRanger(w, 350@40, \freq2, \freq.asSpec);
views[\but]=Button(w, 350@20).states_([ [\a],[\b],[\c] ]);
p=PresetsJT(views, path++"presets/").makeGui(w, 350@20);

w.view.decorator.nextLine;
["+","-","s","r", "add"].collect{|n,i| Button(w, 20@20).states_([ [n] ])};
Button(w, 100@20).states_([ [\calculate,Color.black,Color.green], [\learn,Color.white,Color.red] ]);

w.view.decorator.nextLine;
EZMultiSlider(w, 200@150, \nn, [0,1.0], {}, [0,0,0], true, 10, 20, 0, 20, \horz, 0@0, 0@0);
ListView(w, 180@150).items_([\test1, \test2]);
)
p.controlSpecs