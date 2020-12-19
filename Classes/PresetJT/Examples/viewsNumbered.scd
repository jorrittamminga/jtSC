(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presetsNumbered/";
w.addFlowLayout;
w.alwaysOnTop_(true);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| })};
v[\ms]=MultiSliderView(w, 350@100).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);

p=PresetJT(v, path).makeGui(w, 350@20);
)

addje
index0
target/Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd
addAction addBefore
index0
save maria[ /Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd, addBefore ]
[ abra201115_153417, /Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd, addBefore, 4 ]
directoryPathName(/Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/)
entries[ PathName(/Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd) ]
index 0
/Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd /Users/jorrit/Dropbox/GitHub/jtSC/Classes/PresetJT/Examples/presetsNumbered/0000_abra.scd
ready rename
writeAsCompileString
