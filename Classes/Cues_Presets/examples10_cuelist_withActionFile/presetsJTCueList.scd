/*
values van de input slider updaten niet in visuals (wel in de rest hoor!)
zou door de EZMultiSlider of .makeSliders te maken hebben
*/
(
var win=(), keys=[\freq, \rq, \boostcut, \beats], views=(), presetsJT, path=thisProcess.nowExecutingPath.dirname++"/";
var v;
v=Window("CueList", Rect(0,0,400,400)).front; v.addFlowLayout; v.alwaysOnTop_(true); v.onClose_{win.do(_.close)};
c=CueListJT(path++"cues/").makeGui(v);
CueFunctionsJT((test:123), \action).addToCueList(c).makeGui(v);
)