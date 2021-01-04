(
var path=thisProcess.nowExecutingPath.dirname++"/presets1/";
var acts=PathName(path).entries.collect{|p| p.folderName.asString}++["act3"], act;
var cv;
var w=(), views=(), ps=(), pm=();

w[1]=Window("Main", Rect(0,1000,400,400)).front; w[1].addFlowLayout; w[1].alwaysOnTop_(true); w[1].onClose_{w.keysValuesDo{|key,w| w.close}};
views[1]=();
(2..5).do{|i| views[i]=(); w[i]=Window.new.front; w[i].addFlowLayout; w[i].alwaysOnTop_(true)};

act=acts[0];

[\amp,\freq,\rq].do{|key| views[1][key]=EZSlider(w[1], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[2][key]=EZSlider(w[2], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[3][key]=EZSlider(w[3], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq, \db].do{|key| views[4][key]=EZSlider(w[4], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq, \db, \phase, \boostcut].do{|key| views[5][key]=EZSlider(w[5], 350@20, key, key, {|ez| [key, ez.value].postln})};

ps[1]=PresetSystem(views[1], path++act++"/", "master");
ps[1].makeGUI(w[1], 350@20, 1);

ps[2]=PresetSystem(views[2], path++act++"/", "morph");
ps[2].makeGUI(w[2], 350@20, 1);
pm[2]=PresetMorph(ps[2], method: 'clipAt', guiType:'presets');
pm[2].makeGUI(w[2]);

ps[4]=PresetSystem(views[4], path++act++"/", "neuralnet");
ps[4].makeGUI(w[4], 350@20, 1);
pm[4]=PresetNN(ps[4], 2);
pm[4].makeGUI(w[4]);

ps[5]=PresetSystem(views[5], path++act++"/", "neuralnet2");
ps[5].makeGUI(w[5], 350@20, 1);
pm[5]=PresetNN(ps[5], 2);
pm[5].makeGUI(w[5]);

ps[1].addPresetSystem(ps[2], \subfolder);
ps[1].addPresetSystem(ps[4]);
ps[1].addPresetSystem(ps[5]);
ps[1].addSlave(views[3], "slave").makeGUI(w[3], 350@20);

PopUpMenu(w[1], 350@20).items_( acts ).action_{|p|
	ps[1].path_((path++acts[p.value]++"/"));
};
)