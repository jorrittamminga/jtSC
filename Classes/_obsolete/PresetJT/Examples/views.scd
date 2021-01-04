(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presets/";
w.addFlowLayout;
w.alwaysOnTop_(true);

v=();
[\amp, \freq, \rq].do{|k| v[k]=EZSlider(w, 350@20, k, k.asSpec, {|ez| })};
v[\ms]=MultiSliderView(w, 350@100).value_([0.0, 0.2, 0.5, 0.3]).elasticMode_(1);

p=PresetJT(v, path).makeGui(w, 350@20);
//c=PresetJTCollection(p, [0,1]).makeGui(w, 350@20);
//c=PresetJTCollectionBlender(p, [0,1], 'wrapAt', 'depth').makeGui(w, 350@20);
n=PresetJTNeuralNet(p, 3).makeGui(w, 350@20)
)

n.values
n.presetJT.objects.sortedClumps
[0,10,20,30,0.5, 20000].clumps([1,3,1,1]).collect(_.unbubble)

[1,3,1,1,1,1].includes({|i| i>1})

[0,1,5].indexInBetween(2).ceil.asInteger


x=["aap", "courgette", "mies", "noot"];
x[2]="beer"
x.order

Array.fill(6, 0)