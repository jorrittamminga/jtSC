x[\mic1].plugins[\Compressor].gui.addPresetSystem(folderName: "/Compressor/mic1");

(
var parent;
var width=30, faderWidth=20, margin=2, gap=1;
var meterHeight=150, height=15;
var controlSpecFader=ControlSpec(-inf, 6.0, 'db', 1, -inf, " dB");
var w=Window.new.front, d;
var c=();
var faderHeight=height*2+meterHeight+(2*margin);
var numberOfMeters=3, numberOfUtils=2;
var maxPlugins=2;
var maxHeight=(), maxWidth=();
var showFaders=false, showMasterFader=true;
var fontsize=width.min(height*1.5);
fontsize.postln;

w.alwaysOnTop_(true);
w.addFlowLayout(4@4, margin@margin);

parent=w;

maxHeight[\meters]=meterHeight+((maxPlugins+3)*(height+gap))+(2*margin);
maxHeight[\utils]=if (numberOfUtils<=numberOfMeters, {1},{numberOfUtils})*height+gap;
maxWidth[\total]=numberOfMeters*((2*margin+width+(showFaders.binaryValue*faderWidth))) + (showMasterFader.binaryValue*(2*margin+width));

c[\total]=CompositeView(parent, maxWidth[\total]@(maxHeight[\meters]+maxHeight[\utils]));
c[\total].addFlowLayout(0@0, 0@0);
c[\total].background_(Color.white);

numberOfMeters.do{|i|
	if (showFaders, {
		d=CompositeView(c[\total], faderWidth@maxHeight[\meters]);
		d.addFlowLayout(0@0, gap@gap);
		d.decorator.shift(0, height+margin);
		d.background_(Color.blue);
		EZSlider(d, faderWidth@faderHeight, "", controlSpecFader
			, {|ez| }
			, labelHeight: height, layout: 'vert')
		.font_(Font(Font.defaultMonoFace, fontsize*0.5));
	});
	d=CompositeView(c[\total], (2*margin+width)@maxHeight[\meters]);
	d.addFlowLayout(margin@margin, gap@gap);
	d.background_(Color.blue);
	StaticText(d, width@height).string_("piano").align_(\center).stringColor_(Color.white)
	.font_(Font(Font.defaultMonoFace, fontsize*0.5));
	NumberBox(d, width@height).font_(Font(Font.defaultMonoFace, fontsize*0.5));
	LevelIndicator(d, width@meterHeight);
	Button(d, width@height).states_([["m"],["M",Color.black, Color.yellow]])
	.font_(Font(Font.defaultMonoFace, fontsize*0.5));
	Button(d, width@height).states_([["compressor"]]).font_(Font(Font.defaultMonoFace, fontsize*0.3));
	Button(d, width@height).states_([["EQ"]]).font_(Font(Font.defaultMonoFace, fontsize*0.3));
	//d.rebounds;
};
if (showMasterFader, {
	d=CompositeView(c[\total], (2*margin+width)@maxHeight[\meters]);
	d.addFlowLayout(0@0, gap@gap);
	d.decorator.shift(0, height+margin);
	d.background_(Color.blue);
	EZSlider(d, width@faderHeight, "", controlSpecFader
		, {|ez| }
		, labelHeight: height, layout: 'vert')
	.font_(Font(Font.defaultMonoFace, fontsize*0.5));
	Button(d, width@height).states_([["m"],["M",Color.black, Color.yellow]])
	.font_(Font(Font.defaultMonoFace, fontsize*0.5));
});

c[\total].decorator.nextLine;
Button(c[\total], width@height).states_([[\player]]).font_(Font(Font.defaultMonoFace, fontsize*0.3));
Button(c[\total], width@height).states_([[\recorder]]).font_(Font(Font.defaultMonoFace, fontsize*0.3));

w.rebounds
)
