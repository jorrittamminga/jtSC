(
var win=();
var views=();
var presets=();
var bounds=();
var path=thisProcess.nowExecutingPath.dirname++"/";
var cueList;

win[\Main]=Window("Main").front; win[\Main].addFlowLayout; win[\Main].alwaysOnTop_(true);
win[\Main].view.onMove_{bounds[\Main]=win[\Main].bounds};
win[\Main].onClose_{arg w;
	var file=File(path++"settings/views.scd", "w");
	win.removeAt(\Main);
	win.keysValuesDo{|key,w|
		if (w.view.enabled, {
			bounds[key]=w.bounds;
			w.close;
		});
	};
	file.write(bounds.asCompileString);
	file.close
};
cueList=CueListJT(path++"cues/").makeGui(win[\Main], 350@20);
win[\Synth]=Window("Synth").front; win[\Synth].addFlowLayout; win[\Synth].alwaysOnTop_(true);
views[\Synth]=();
[\freq, \amp, \rq].do{|key| views[\Synth][key]=EZSlider(win[\Synth], 350@20, key, key.asSpec)};
presets[\Synth]=PresetsJT(views[\Synth], path++"presets/Synth/").makeGui(win[\Synth]).addToCueList(cueList);
x=PresetsCollectionJT(presets[\Synth]).makeGui(win[\Synth]);
e=views[\Synth];

win[\FX]=Window("FX").front; win[\FX].addFlowLayout; win[\FX].alwaysOnTop_(true);
views[\FX]=();
[\freq, \amp,\rq, \db].do{|key| views[\FX][key]=EZSlider(win[\FX], 350@20, key, key.asSpec)};
presets[\FX]=PresetsJT(views[\FX], \fx).makeGui(win[\FX]).addToCueList(cueList);



/*
if (File.exists(path++"settings/views.scd"), {
bounds=(path++"settings/views.scd").load;
bounds.keysValuesDo{|key,bounds| win[key].bounds=bounds};
win[\Main].front;
})
*/
)
/*
x.presetsCollection
x.indices
x.array
x.actionArray
x.index
\poep > 0
*/
x.presetsCollection
[ ( 'rq': 0.001111887591665, 'amp': 0.0, 'freq': 120.89973376649 ), ( 'rq': 0.016915709995146, 'amp': 0.16752839372634, 'freq': 602.69801776804 ) ]
e
a=EventsArrayJT.fill(x.presetsCollection, e)
[\extras, \routinesJT, \method, \durations].do{|key| e.removeAt(key)}
a.array
a.blendAtIndex(0.25)

