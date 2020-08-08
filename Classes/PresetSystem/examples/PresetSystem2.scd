(
var path=thisProcess.nowExecutingPath.dirname++"/presets1/";
var acts=PathName(path).entries.collect{|p| p.folderName.asString}++["act3"], act;
var w1, w2, w3, w4, cv;
var views1=(), views2=(), views3=(), views4=();
var ps1, ps2, ps3, pm2, pm4, ps4;
w1=Window("Main", Rect(400,1000,400,400)).front; w1.addFlowLayout; w1.alwaysOnTop_(true); w1.onClose_{w2.close; w3.close; w4.close};
w2=Window.new.front; w2.addFlowLayout; w2.alwaysOnTop_(true);
w3=Window.new.front; w3.addFlowLayout; w3.alwaysOnTop_(true);
w4=Window.new.front; w4.addFlowLayout; w4.alwaysOnTop_(true);

act=acts[0];

[\amp,\freq,\rq].do{|key| views1[key]=EZSlider(w1, 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views2[key]=EZSlider(w2, 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views3[key]=EZSlider(w3, 350@20, key, key, {|ez| })};
[\amp,\freq,\rq, \db].do{|key| views4[key]=EZSlider(w4, 350@20, key, key, {|ez| })};

ps1=PresetSystem(views1, path++act++"/", "master");
ps1.makeGUI(w1, 350@20, 1);

ps2=PresetSystem(views2, path++act++"/", "morph");
ps2.makeGUI(w2, 350@20, 1);
pm2=PresetMorph(ps2, method: 'clipAt', guiType:'presets');
pm2.makeGUI(w2);

ps4=PresetSystem(views4, path++act++"/", "neuralnet");
ps4.makeGUI(w4, 350@20, 1);
pm4=PresetNN(ps4, 3
	, path: path++act++"/neuralnets/"
);
pm4.makeGUI(w4);

ps1.addPresetSystem(ps2, \subfolder);
ps1.addSlave(views3, "slave").makeGUI(w3, 350@20);

PopUpMenu(w1, 350@20).items_( acts ).action_{|p|
	ps1.path_((path++acts[p.value]++"/"));
};
)