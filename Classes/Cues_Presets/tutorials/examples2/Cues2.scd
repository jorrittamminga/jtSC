(
var views=(), presetPath=thisProcess.nowExecutingPath.dirname++"/presets2/", cuePath=thisProcess.nowExecutingPath.dirname++"/cues2/";
var pc;
c=();

w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);

[\freq, \amp, \rq].do{|key| views[key]=EZSlider(w, 350@20, key, key.asSpec, {|ez| }) };

p=PresetJT(views, presetPath).makeGui(w);

c[\testSlave]=CueJT(p, "testSlave");
c[\testSlave].makeGui(w);
w.view.decorator.shift(0,20);
m=CueJTMaster(cuePath, c).makeGui(w);
)



c[\testSlave]


p=PathSystemJT(thisProcess.nowExecutingPath.dirname++"/cues2/")
c=CueJTMaster(thisProcess.nowExecutingPath.dirname++"/cues2/", nil, "master");
c.pathSystem.paths

p.pathStructure;
p.paths;
p.pathsRelative;


pathSystem.paths[ /Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/cues2/0000_Init/ ]
/Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/cues2/0000_Init/
entries[ PathName(/Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/cues2/0000_Init/master.scd) ]
fileName master
i==0
key testSlave
cueJT a CueJT
path /Users/jorrit/Dropbox/GitHub/jtSC/Classes/CueJT/Examples/cues2/0000_Init/

(
w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);

p=PathSystemJT(thisProcess.nowExecutingPath.dirname++"/cues2/");
p.makeGui(w)
)