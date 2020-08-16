(
var path=thisProcess.nowExecutingPath.dirname++"/presets3/";
var cv;
var w=(), views=(), ps=(), pm=();

w[1]=Window("Main3", Rect(0,1000,400,400)).front; w[1].addFlowLayout; w[1].alwaysOnTop_(true); w[1].onClose_{w.keysValuesDo{|key,w| w.close}};
views[1]=();
(2..3).do{|i| views[i]=(); w[i]=Window.new.front; w[i].addFlowLayout; w[i].alwaysOnTop_(true)};

[\amp,\freq,\rq].do{|key| views[1][key]=EZSlider(w[1], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[2][key]=EZSlider(w[2], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[3][key]=EZSlider(w[3], 350@20, key, key, {|ez| })};

ps[1]=PresetSystem(views[1], path, "master");
ps[1].addInterpolation;
ps[1].makeGUI(w[1], 350@20, 1);

ps[2]=PresetSystem(views[2], path, "morph");
ps[2].addInterpolation;
ps[2].makeGUI(w[2], 350@20, 1);
pm[2]=PresetMorph(ps[2], method: 'clipAt', guiType:'presets');
pm[2].makeGUI(w[2]);

ps[1].addPresetSystem(ps[2]);
ps[3]=ps[1].addSlave(views[3], "slave" );
ps[3].makeGUI(w[3])
)