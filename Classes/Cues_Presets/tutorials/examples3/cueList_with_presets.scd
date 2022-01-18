//hee, dit moet kunnen, vind ik: een lijst met presets maken en hier dan presets van in een cuelist zetten
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
presets[\Synth]=PresetsJT(views[\Synth], path++"presets/Synth/").makeGui(win[\Synth]);
x=CuesJT(presets[\Synth], \Synth);//en kijk of je deze met .addToCueList iets handigs mee kunt doen
x.makeGui(win[\Synth]).addToCueList(cueList);

/*
win[\FX]=Window("FX").front; win[\FX].addFlowLayout; win[\FX].alwaysOnTop_(true);
views[\FX]=();
[\freq, \amp,\rq, \db].do{|key| views[\FX][key]=EZSlider(win[\FX], 350@20, key, key.asSpec)};
//presets[\FX]=PresetsJT(views[\FX], path++"presets/FX/").makeGui(win[\FX]);
presets[\FX]=PresetsJT(views[\FX], \fx).makeGui(win[\FX]).addToCueList(cueList);

if (File.exists(path++"settings/views.scd"), {
bounds=(path++"settings/views.scd").load;
bounds.keysValuesDo{|key,bounds| win[key].bounds=bounds};
win[\Main].front;
})
*/
)