(
var cs=(amp:\amp, freq:\freq, freq2:\freq, amp2:\amp);
var w,gui=(), ps;
w=Window("test", Rect(400,400,400,400)).front;
w.addFlowLayout;
w.alwaysOnTop_(true);
cs.sortedKeysValuesDo{|key,val|
	gui[key]=EZSlider(w, 350@20, key, cs[key].asSpec, {|ez|
		key.post; " ".post; ez.value.postln;
	})
};
ps=PresetSystem(gui, thisProcess.nowExecutingPath.dirname++"/presets/");
x=ps;
ps.gui(w);
p=PresetKD(ps);
p.makeGUI(w, 350@20)
)