(
var w;
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
g=();
[\freq, \amp, \rq].do{|key| g[key]=EZSlider(w, 350@40, key, key.asSpec, {|ez| })};

k=g.asEventJT

)
k
k.arrayUnmappedSorted
k.specs
g.specs
k.objects
k.values
k.actions
k.specs
g[\freq].controlSpec=ControlSpec(200,4000,\exp)
k.getSpecs_
k.specs[\freq].map(1)

keys
ListView.superclass
ItemViewBase
.items

k.valuesActions_( (freq:560, amp:0.1, rq: 0.01))
k.keyValueAction_(\freq, 1660)
k.valuesActionsTransition( (freq:560, amp:0.1, rq: 0.01), 3, \sin)
k.valuesActionsTransition( (freq:1560, amp:1.0, rq: 0.1), 3, \sin)