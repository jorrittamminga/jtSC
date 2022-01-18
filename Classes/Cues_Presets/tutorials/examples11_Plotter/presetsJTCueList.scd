(
var w=Window("Plotter", Rect(0,0,1200,400)).front;
var views=(), table=0!512, presets, pathName=thisProcess.nowExecutingPath.dirname++"/presets/";
w.addFlowLayout;
w.alwaysOnTop_(true);
views[\plotter]=Plotter("test", Rect(0,0,1000,350), w).value_(table).specs_([-20, 20].asSpec)
.domainSpecs_([1,table.size-1,\exp].asSpec)
//.domainSpecs_(\freq.asSpec)
.editMode_(true).editFunc_{arg plotter, plotIndex, index, val;
	table[index]=val;
}.findSpecs_(false);
presets=PresetsJT(views, pathName).makeGui(w, 350@20);
)