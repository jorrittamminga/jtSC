Server.killAll
s.waitForBoot{p=PlayerJT(10, s, "/Users/jorrittamminga/Music/Samples/kraan1.aif").addMonitor(0, 2, 1.0)};

p.makeGUI(bounds:200@20);
//p.gui.addPresetSystem(folderName:"player");

p.free;
p.monitor.set(\amp, 1)

t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{s.waitForBoot{p=PlayerJT(0, [s,t], "/Users/jorrittamminga/Music/Samples/kraan1.aif")}};

t.queryAllNodes

p.startPlaying
p.stopPlaying
p.pausePlaying
p.resumePlaying
p.servers
p.numChannels

p.free

(
s.waitForBoot{
	r=RecorderJT(0+s.options.numOutputBusChannels, s);
	p=PlayerJT(0, s, "/Users/jorrittamminga/Music/Samples/kraan1.aif");
	{
		p.postln;
		p.makeGUI(bounds:200@20);
		r.makeGUI(bounds:200@20);
		r.stopRecordingFunc=r.stopRecordingFunc.addFunc({arg r;
			//{
			//p.gui.pathsAndFilesNames;
			p.path_(r.path, true);
			//}.defer
		});
	}.defer
};
)

p.buffer
p.path_("/Users/jorrittamminga/Music/CD_tracks/05 Mortuos Plango, Vivos Voco.aif")
p.path_("/Users/jorrittamminga/Music/Samples/kraan1.aif")

OSCFunc({arg msg; msg.postln}, p.cmdName, p.servers[0].addr);


PathName("/Users/jorrittamminga/Music/CD_tracks/05 Mortuos Plango, Vivos Voco.aif".dirname).entries


(
w=Window.new.front;
w.alwaysOnTop_(true);
w.addFlowLayout;

EZNumber(w, 40@20, nil, ControlSpec(0.0, 5.0, 0, 0.5), {|ez| ez.value.postln})
)

a=RangeSlider


r=Task({inf.do{|i| i.postln; 0.1.wait;}});
r.play
r.isPlaying
r.pause
r.resume
r.stop

OSCFunc({arg msg; msg.postln}, p.cmdNameT)

Recorder