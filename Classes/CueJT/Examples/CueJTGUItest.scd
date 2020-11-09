(
var views=(), presetPath=thisProcess.nowExecutingPath.dirname++"/presets/", cuePath=thisProcess.nowExecutingPath.dirname++"/cues/";
var pc;
c=();

w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);

[\freq, \amp, \rq].do{|key| views[key]=EZSlider(w, 350@20, key, key.asSpec, {|ez| }) };

p=PresetJT(views, presetPath);
p.makeGui(w);
n=PresetJTNeuralNet(p, 3).makeGui(w, 350@20);
//pc=PresetJTCollectionBlender(p,nil, 'wrapAt', 'depth').makeGui(w, 350@20);
/*
c[\testSlave]=CueJT([p,pc], "testSlave");
c[\testSlave].makeGui(w);
*/
c[\testSlave]=CueJT(n, "testSlave");
c[\testSlave].makeGui(w);
w.view.decorator.shift(0,20);
m=CueJTMaster(cuePath, c).makeGui(w);

)


restore PresetCollection
initNN /Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/presets/NeuralNets/0.scmirZ
load pathNN
views[listNN] action 0
presetJT.presetJT.restore 0`
in views[mode] 1

restore PresetCollection
initNN /Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/presets/NeuralNets/0.scmirZ
load pathNN
presetJT.func restore
presetJT.func[restore]
views[listNN] action 0
presetJT.presetJT.restore 0



restore PresetCollection
initNN /Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/presets/NeuralNets/0.scmirZ
load pathNN
presetJT.func restore
presetJT.func[restore]
views[listNN] action 0
presetJT.presetJT.restore 0
