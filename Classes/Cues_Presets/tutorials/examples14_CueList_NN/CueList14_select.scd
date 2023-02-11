/*
bij deselect/select object bij PresetsJT wordt soms toch nog een geDEselecteerd object veranderd met Blender.
*/
(
var path=thisProcess.nowExecutingPath.dirname++"/";
w=Window("CueList_select", Rect(400,400,400,400)).front;
w.addFlowLayout;
w.alwaysOnTop_(true);
w.onClose_{v.close;};
c=CueListJT(path++"cues/").makeGui(w);
Button(w, 350@20).states_([ [\cues],[\Cues,Color.black,Color.yellow] ]).action_{|b|
	v.visible_(b.value>0)
};
Button(w, 350@20).states_([ [\presets],[\Presets,Color.black,Color.yellow] ]).action_{|b|
	u.visible_(b.value>0)
};
w.rebounds;

//---------------------------------------------------- CUES
v=Window("cues", Rect(0,0,400,400));
v.addFlowLayout;
v.alwaysOnTop_(true);
v.userCanClose_(false);
v.front;
v.visible_(false);
g=();
g[\isOn]=Button(v, 350@20).states_([ [\on],[\ON, Color.black, Color.green] ]);
[\amp, \freq, \rq].do{|key| g[key]=EZSlider(v, 350@20, key, key.asSpec, {})};
[\lofreq].do{|key| g[key]=EZRanger(v, 350@20, key, key.asSpec, {})};
[\midfreq].do{|key| g[key]=EZNumber(v, 350@20, key, key.asSpec, {})};
[\phase].do{|key| g[key]=EZKnob(v, 350@300, key, key.asSpec, {})};
g[\amps]=EZMultiSlider(v, 350@100, \amps, \amp.asSpec, {}, [1,0,0,0,0]);

~pr=PresetsJT(g, path++"/presets/").addNN.makeGui(v);//.addNN
i=g.deepCopy;//essential!!!
//i[\presetCollection]=~pr.blender.objects[\presets];//   \presets, \blender, \depth
~cues=CuesJT(i, \test).addToCueList(c).makeGui(v);
v.rebounds;
)


