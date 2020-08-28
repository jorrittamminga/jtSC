(
var path=thisProcess.nowExecutingPath.dirname++"/presets3/";
var parts=PathName(path).entries.collect{|path| if (path.isFolder, {path.folderName})}, part=parts[0];
var cv;
var w=(), views=(), ps=(), pm=(), but;

MIDIIn.connectAll;

w[1]=Window("Main3", Rect(0,1000,400,400)).front; w[1].addFlowLayout; w[1].alwaysOnTop_(true); w[1].onClose_{w.keysValuesDo{|key,w| w.close}};
views[1]=();
(2..3).do{|i| views[i]=(); w[i]=Window.new.front; w[i].addFlowLayout; w[i].alwaysOnTop_(true)};

ListView(w[1], 352@40).items_(parts).action_{arg l;
	ps[1].path_(path++parts[l.value]++"/")
};


[\amp,\freq,\rq].do{|key| views[1][key]=EZSlider(w[1], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[2][key]=EZSlider(w[2], 350@20, key, key, {|ez| })};
[\amp,\freq,\rq].do{|key| views[3][key]=EZSlider(w[3], 350@20, key, key, {|ez| })};

ps[1]=PresetSystem(views[1], path++part++"/", "master");
"masterpath ".post; ps[1].path.postln;
ps[1].addInterpolation;
ps[1].makeGUI(w[1], 350@20, 1);

ps[2]=PresetSystem(views[2], path++part++"/", "morph");
ps[2].addInterpolation;
ps[2].makeGUI(w[2], 350@20, 1);
pm[2]=PresetMorph(ps[2], method: 'clipAt', guiType:'presets');
pm[2].makeGUI(w[2]);
"morphpath ".post; ps[2].path.postln;

ps[1].addPresetSystem(ps[2]);
ps[3]=ps[1].addSlave(views[3], "slave" );
ps[3].makeGUI(w[3]);

"morphpath ".post; ps[2].path.postln;

but=Button(w[1], 40@20).states_([ [\next] ]).action_{
	//{
		ps[1].next(1, {
			"in de extra nextaction....".postln;
		/*
			if (part<parts.size, {
				p[\func][\setPart].value(part+1);
				"en hier staat toch echt een defer hoor".postln;
				{p[\views][\preset][\parts].value_(part)}.defer
			});
		*/
		});
	//}.defer
};
but.mapToMIDI(\noteOn, 63, mode:\trigger);

//MIDIFunc.noteOn({ {ps[1].next(1)}.defer}, 63);


)