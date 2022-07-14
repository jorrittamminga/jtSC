/*
values van de input slider updaten niet in visuals (wel in de rest hoor!)
zou door de EZMultiSlider of .makeSliders te maken hebben
*/
(
var win=(), keys=[\freq, \rq, \boostcut, \beats], views=(), path=thisProcess.nowExecutingPath.dirname++"/";
var v;
v=Window("CueList", Rect(400,400,400,600)).front; v.addFlowLayout; v.alwaysOnTop_(true); v.onClose_{win.do(_.close)};
c=CueListJT(path++"cues/").makeGui(v);
//------------------------------------------------------------------------------------------------------------------------
keys.do{|key| views[key]=EZSlider(v, 350@20, key, key.asSpec, {|ez| })};
CuesJT(views, \test).addToCueList(c).makeGui(v);

)