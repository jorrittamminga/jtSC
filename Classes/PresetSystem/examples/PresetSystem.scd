(
var guis=(), guis2=(), guis3=();
var w,v;
w=Window.new.front;
w.addFlowLayout;
w.alwaysOnTop_(true);
v=Window.new.front;
v.addFlowLayout;
v.alwaysOnTop_(true);
w.onClose_{v.close};
guis[\amp]=EZSlider(w, 350@20, \amp, \amp.asSpec, {|ez| });
guis[\freq]=EZSlider(w, 350@20, \freq, \freq.asSpec, {|ez| ez.value;});
guis[\slider]=Slider(w, 350@20);//.action_{|sl| sl.value.postln};
guis[\button]=Button(w, 350@20).states_([ [\a],[\b],[\c] ]).action_{arg b;
	//[b.value, b.states].postln;
};
guis[\text]=TextField(w, 350@20).string_("poep").action_{|t|
	"textfield".postln;
	t.value
};

guis2[\amp]=EZSlider(v, 350@20, \amp, \amp.asSpec, {|ez| });
guis3[\amp]=EZSlider(w, 350@20, \amp3, \amp.asSpec, {|ez| });
guis2[\amp2]=EZMultiSlider(v, 350@100, \amps, \amp.asSpec, {|ez| }, [1.0,0.0,0.0,0.0,0.0]);
//guis[\test]=12345;
~test=EZSlider(v, 350@20, \poep, \freq.asSpec);

guis2[\button]=Button(v, 350@20).states_([ ["test"],["test2"]]);

x=PresetSystem(guis);//guis
x.addInterpolation;//(argslaves:false);
x.makeGUI(w,350@20, 1, false);

y=x.addSlave(guis2, "ampy");
y.addInterpolation;
y.makeGUI(v, includeSlaves:false);

p=PresetMorph.polar2D(x).gui;

x.addToSlave((poep: ~test), 0);

w.onClose=w.onClose.addFunc({p.parent.close});
)

x.presets
.restoreI
x.canInterpolate
x.slaves[0].canInterpolate
x.interpolate
x.slaves[0].interpolate




NeuralNet

a=PresetNN(x);

a.train(0.0001, 1000000)

a.makeGUI
a.normalizePreset
a.presetNormalized
a.normalizePresets
a.controlSpecsList
a.presetsList
a.path
a.neuralNet.isTraining

x.extra
x.addToInterpolation([\button]);


\button.asArray.do{|i| i.class.postln}

x.presets

x.normalize
x.presetsNormalized

x.localpath
x.entries
x.slaves[0].presetsNormalized

p.array[1][0][\amp2].levels
p.array[1][0][\amp2].times
p.array[1][0][\amp].times


[ [ 1, 0, 0, 0, 0 ], [ 0, 0, 0, 0, 1 ], [ 0.41463413834572, 0.5, 0.36768293380738, 0.59268295764923, 1 ], [ 1, 0, 0, 0, 0 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ], 1, 0, 0, 0, 0 ]


Env
levels, <times

x.slaves[0].presets
x.slaves[0].entries


x.shiftfiles(1, 1);
x.update;

p=PresetMorph2(x, [0,1,2,3], \clipAt).gui;


{800.do{p.azimuth_(4.0.rand)}}.bench
time to run: 0.19854337699996 seconds.//met function
time to run: 0.19854337699996 seconds.//met if


x.slaves.do{|y| y.presets.postln};"";
x.presets
x.guis[\name].canFocus_(false)

x.entries
x.slaves[0].entries
x.slaves[0].presets
x.scripts
p.dimensions
p.keysNoCS.class
p.array
x.canScript
x.windows[\scriptEditorGUI]
p.controlSpecs
p.indices_([[0,1,2],[3]])

x.presets
x.canInterpolate
x.slaves[0].canInterpolate
x.scripts
x.scriptEditorGUI
x.guis[\ScriptEditor].resize_(5)
x.interpolateOnce(\amp, 1, 0, 0)
x.presets;
x.slaves[0].presets

x.addScript
x.restoreS(2)
x.openScript

x.prev
x.slaves[0].store
x.slaves[0].index
x.slaves[0].index_(2)
x.slaves[0].newFileName
x.slaves[0].fullPath
x.fileNameWithoutExtension
x.store
x.extra
100.collect{{x.restore(0)}.bench}.mean
x.restoreI(0)
x.storeI(2, 3)
x.restore(1)
x.rename(1, "test")
x.store
x.fileNameWithoutExtension
x.fullPath
x.add
x.delete
x.add
x.fullPath
x.index
x.actions

time to run: 0.0015493600003538 seconds.
-> 0.0015493600003538
0.0014774620693788


a=EZSlider(w, 350@20, \freq, \freq.asSpec, {|ez| });
b=Slider.new
a.isKindOf(EZGui)
b.class
b.isMemberOf(View)
b.isMemberOf(QAbstractStepValue)
b.class.superclassesDo{|i| i.postln}
EZSmoothSlider.class.superclassesDo{|i| i.postln}


x=TesterJT.new
x.interpolate_(1)
x.restore(10)


(ar++ar[0]).postln;

x=[ 0, 0, 0.44651162790698, 0.54418604651163, 1, 1 ];
y=0;
x.add(y);

[ 0, 0, 0.44651162790698, 0.54418604651163, 1, 1, 0 ]


x=[ [ 1, 0, 0, 0, 0 ], [ 0, 0, 0, 0, 1 ], [ 0.41463413834572, 0.5, 0.36768293380738, 0.59268295764923, 1 ], [ 1, 0, 0, 0, 0 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ] ];
y=[ 1, 0, 0, 0, 0 ];
x.add(y);

[ [ 1, 0, 0, 0, 0 ], [ 0, 0, 0, 0, 1 ], [ 0.41463413834572, 0.5, 0.36768293380738, 0.59268295764923, 1 ], [ 1, 0, 0, 0, 0 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ], [ 0.26829269528389, 0.67073172330857, 0.36550527811051, 0.34487807750702, 0.69512194395065 ], 1, 0, 0, 0, 0 ]



EZSlider