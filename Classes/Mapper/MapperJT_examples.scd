//benchmark tests
b=MapperControlSpecJT(20, 20000, \exp);
c=\freq.asSpec;

{1000000.do{v=0.5; b.map(v)}}.bench//a bit faster
{1000000.do{v=0.5; b.warp.map(v.clip(0,1.0))}}.bench//much faster than MapperControlSpecJT
{1000000.do{v=0.5; b.warp.map(if (v>1.0) {v.clip(0,1.0)}{v})}}.bench//not much improvement...

{1000000.do{v=0.5; c.map(v)}}.bench;
{1000000.do{v=0.5; c.warp.map(v)}}.bench;



e=Env([20, 20000],[1.0],\exp);
{1000000.do{v=0.5; e.at(v)}}.bench;



(//mapper, name, default, lag, smoothSize
a=[[0,1],\loudness, 0, nil, 2].asMapperJT;
b=[\freq, \pitch, [440,1000,5000], nil, 4].asMapperJT;
c=[[0, 1.0,4],\amp, 0, [0.0, 0.1]].asMapperJT;
d=[[220,1200,\exp],\freq, 220, [0.0, 0.1]].asMapperJT;
)
(
w=Window.new.front; w.addFlowLayout; w.onClose_{}; w.alwaysOnTop_(true);
[a,b,c,d].do{|map| map.makeView(w)};
)





a=MapperJT(Env([20, 20000],[1.0],\exp), \freq, 440)
a.map(0.5)
a.minval_(440)
a.map(0.5)
a.curve_([\exp])
a.minval




EnvelopeView

a.edit
a.makeGui
//a.editor ipv a.makeGui
//a.makeGui is a EZSlider/EZwhatever plus een editor denk ik

(
w=Window.new.front; w.addFlowLayout; w.onClose_{}; w.alwaysOnTop_(true);
e=EZSlider(w, 350@40, \loudness, [0, 1.0, 4.0], {});
m=e.addMapperJT;
/*
a.makeGui(w.view, 350@20);
b.makeGui(w.view, 350@20);
c.makeGui(w.view, 350@20);
d.makeGui(w.view, 350@20);
*/
)

m.warp.asSpecifier

a.map(0.0)

EZSlider.class.isKindOfClass(EZGui)

m=MapperInOutJT([a,b],[c,d])
e=MapperIOJT(a,c);
e.mapMethod
e.map(0.5)


f=MapperIOJT(b,d);
f.map(440)

(
u=[0,1].asSpec;
v=[20,20000].asSpec.asSpec.warp;
w=[0, 1.0, 4.0].asSpec.warp;
x=[220,1200,\exp].asSpec
)

e.map(0.5)
f.map(440)


//{100000.do{[0.5,440].collect{|v,i| m[i].map(v)}}}.bench
{100000.do{[e.map(0.5),f.map(440)]}}.bench
{100000.do{[w.map(u.unmap(0.5)), x.map(v.unmap(440))]}}.bench


a.map(0.5)
m.array[0].map(0.5)





m=MapperIOJT(b,d)
m.map(2000)


s.boot
(
a=\amp.asSpec.asMapperJT(\loudness, 0).makeBus;
b=[20,20000].asSpec.asMapperJT(\pitch, 440).makeBus;
c=[0, 1.0, 4.0].asSpec.asMapperJT(\amp, 0.0).makeBus;
d=[220,1200].asSpec.asMapperJT(\freq, 1000, [2, 4.0]).makeBus;
)


Array.newFrom([1, 2, 3, 4]);


e={arg out=400, outBus; var sig=SinOsc.kr(100, 0, 0.000001, out);  Out.kr(outBus, sig)}.play(args: [\out, 1000, \outBus, b.bus.index]);
f={arg freq=440; freq.poll(1, \freq)}.play(e, addAction: \addAfter);

//method 1
m=f.mapJT(d, b)
m.synth
e.set(\out, 20)
m.synth.set(\freq_minval, 400, \freq_lag, [0.0,10,0])

//method 2
m=MapperInOutJT(b,d);
m.mapSynth(f)
m.synth
e.set(\out, 20)
m.synth.set(\freq_minval, 40, \freq_lag, [0.0,10,0])


//method 3
m=MapperInOutJT([a,b],[c,d]);
m.mapSynth(f)

//-------------------------------------------
a=\amp.asSpec.asMapperJT(\loudness, 0).makeBus;
b=[20,20000,\exp].asSpec.asMapperJT(\pitch, 440).makeBus;
c=[0, 1.0, 4.0].asSpec.asMapperJT(\amp, 0.0).makeBus;
d=[220,1200,\exp].asSpec.asMapperJT(\freq, 1000, [2, 4.0]).makeBus;


m=MapperInOutJT(b,d);
m.map(440)

m=MapperInOutJT(a,c);
m.map(0.5)

m=MapperInOutJT([a,b],[c,d]);
m.class
m.map([0.5, 440])

m=MapperInOutJT.testArray


//-------------------------------------------
a=[0,1,\amp].asSpec.asMapperJT(\amp, 0.5, [0.1, 2.0])
b=[220,660, \exp].asSpec.asMapperJT(\freq, 440, [4.0, 4.0]);

f={NamedControl.kr(\freq, 440, 4.0)};
SynthDef(\Test, { b.uGenGraphFunc.value.value; \freq.kr.poll(10,\freq)}).add;
SynthDef(\Test, { f.value; \freq.kr.poll(10, \freq)}).add
SynthDef(\Test, { NamedControl.kr(\freq, 440, 4.0); \freq.kr.poll(10, \freq)}).add

//SynthDef and Synth
x=SynthDef(\Test, { var freq=b.uGenGraphFunc.value; freq.poll(10,\freq)}).add.play;

x.set(\freq, 660, \freq_lag, [0,4])

x=SynthDef(\Test, {NamedControl.kr(\freq, 440, 4.0); \freq.kr.poll(10, \freq)}).add.play



b=[220,660, \exp].asSpec.asMapperJT(\freq, 440, [0, 4.0]);
x=SynthDef(\Test, {var freq=b.kr; freq.poll(10, \freq); }).add.play
x=SynthDef(\Test, {b.kr;\freq.kr.inspect}).add.play
x=SynthDef(\Test, {b.kr; \freq.kr.poll(10, \freq)}).add.play
//-------------------------------------------


[ [ a MapperControlSpecJT(220, 1200, 'linear', 0, 1000, "") ], [ a MapperControlSpecJT(20, 20000, 'linear', 0, 440, "") ] ]
mapMsg [ freq, Bus(control, 1, 1, localhost) ]


Synth


(
// use shift-click to keep a node selected
w = Window("envelope", Rect(150 , Window.screenBounds.height - 250, 250, 100)).front;
w.view.decorator = FlowLayout(w.view.bounds);

b = EnvelopeView(w, Rect(0, 0, 230, 80))
    .drawLines_(true)
    .selectionColor_(Color.red)
    .drawRects_(true)
    .resize_(5)
    .step_(0.05)
    .action_({arg b; [b.index, b.value].postln})
    .thumbSize_(5)
    .value_([[0.0, 0.1, 0.5, 1.0],[0.1,1.0,0.8,0.0]]);
w.front;
)


(
n=4;
w = Window.new.front;
m = MultiSliderView(w,Rect(10,10,100,n*13+2)); //default thumbWidth is 13
m.gap_(0);
m.indexIsHorizontal_(false);
m.isFilled_(true);
m.value=Array.fill(n, {|v| v*0.05}); // size is set automatically when you set the value
m.action = { arg q;
    q.value.postln;
};
)


pitch_minval
pitch_maxval
pitch_curve
pitch_warp
-> an EZSlider
-> ( 'pitch_minval': an EZSlider, 'pitch_maxval': an EZSlider, 'pitch_warp': a PopUpMenu, 'pitch_editor': a Button,
  'pitch_curve': an EZSlider )



[ 1, a MapperControlSpecJT(0, 1, 'linear', 0, 0, ""), loudness ]
[ 2, a MapperControlSpecJT(0, 1, 'linear', 0, 0, ""), loudness ]
[ 3, a MapperControlSpecJT(0, 1, 'linear', 0, 0, ""), loudness ]
[ 4, a MapperControlSpecJT(0, 1, 'linear', 0, 0, ""), loudness ]
[ 5, a MapperControlSpecJT(0.0044843049327354, 1, 'linear', 0, 0, ""), nil ]
