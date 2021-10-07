/*
values van de input slider updaten niet in visuals (wel in de rest hoor!)
zou door de EZMultiSlider of .makeSliders te maken hebben
*/
(
var win=(), keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
var v;
v=Window("CueList", Rect(0,0,400,400)).front; v.addFlowLayout; v.alwaysOnTop_(true); v.onClose_{win.do(_.close)};
c=CueListJT(path++"cues/").makeGui(v);

//------------------------------------------------------------------------------------------------------------------------ PRESETS/CUES COMBI
win[\one]=Window("Presets", Rect(400,400,400,400)).front; win[\one].addFlowLayout; win[\one].alwaysOnTop_(true);
keys.do{|key|
	views[key]=EZSlider(win[\one], 350@20, key, key.asSpec, {|ez| [key, ez.value]})
};
views[\range]=EZRanger(win[\one], 350@40, \freq2, \freq.asSpec);
views[\but]=Button(win[\one], 350@20).states_([ [\a],[\b],[\c] ]);
views[\ms]=EZMultiSlider(win[\one], 350@200, \freqs, \freq, {|ez| });

p=PresetsJT(views, path++"presets/").makeGui(win[\one], 350@20).addToCueList(c, \cueOne);
win[\one].rebounds;

)