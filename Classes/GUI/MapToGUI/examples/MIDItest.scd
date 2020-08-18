MIDIClient.init
MIDIIn.connectAll
MIDIClient.destinations

e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln})
e.mapToMIDI(\cc, 1, midiOutPort:5)
e.controlSpec_(ControlSpec(400,4000,\exp))
e.midiMap.setUnmap(ControlSpec(500,700))
e.midiMap
e.midi
e.isMIDImapped

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep], [\kak], [\stront] ]).action_{|b| b.value.postln};
b.mapToMIDI(\cc, 12, 1, mode:\toggle, midiOutPort:5, midiOutChan:1, midiThru:true);
)


(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln});
e.mapToMIDI(\cc, 1)
)
e.midiMap[0]
e.controlSpec_(ControlSpec(700,1400,\exp))

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep] ]).action_{|b| b.value.postln};
b.mapToMIDI(\noteOn, 63, mode:\trigger);
)
b.midiMap

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep], [\kak], [\stront] ]).action_{|b| b.value.postln};
b.mapToMIDI(\noteOn, 1, mode:\toggle);
)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=ListView(w, 350@100).items_( (0..5)).action_{|l| l.value.postln};
b.mapToMIDI(\noteOn, 1, mode:\toggle);
)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=ListView(w, 350@100).items_( (0..5)).action_{|l| l.value.postln};
b.mapToMIDI(\noteOn, [1,11], mode:\plusminus);
)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=PopUpMenu(w, 350@100).items_( (0..5)).action_{|l| l.value.postln};
b.mapToMIDI(\noteOn, [1,11], mode:\togglefold);
)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@100).states_([[0],[1],[2],[3],[4],[5],[6]]).action_{|l| l.value.postln};
b.mapToMIDI(\noteOn,1, mode:\togglefold);
)



(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@100).states_([ [0],[1],[2],[3],[4]]).action_{|l| l.value.postln};
b.mapToMIDI(\noteOn, [1,11], mode:\plusminus);
)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep], [\kak], [\stront], [\kots] ]).action_{|b| b.value.postln};
b.mapToMIDI(\noteOn, [1,11], mode:\plusminus);
)

e=EZSlider(nil, 350@20, \amp, \amp.asSpec, {|ez| ez.value.postln});
e.mapToMIDI(\noteOn, 1, mode: \toggle)

e.midiMap

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep] ]).action_{|b| b.value.postln};
b.mapToMIDI(\noteOn, 1, mode:\trigger);
)
b.onClose_({"lal".postln})

test wat efficienter is:

f=func in func //serie van functies
of
f=(switch(blablabla))


(
f={arg x; x*10}; f={arg func; {arg x; func.value(x)+2.5;}}.value(f);
f={arg func; {arg x; func.value(x)+12.5;}}.value(f);
{10000.do{f.value(2)}}.bench
)

i=1;f={arg x; x*(if (i<0, {10},{20}))+if (i<0, {10},{20})+if (i<0, {10},{20})}; {10000.do{ f.value(2) }}.bench


MIDIIn.connectAll
e=EZSlider(nil, 350@20, \oct, ControlSpec(-48, 48, 0, 12), {|ez| ez.value.postln});
e.mapToMIDI(\noteOn, [1, 11], mode:\plusminus)

c=ControlSpec(0.0, 100.0, 4.0, 0.1); {10000.do{c.map(0.2).round(0.1)}}.bench
c=ControlSpec(0.0, 100.0, 4.0, 0.1); {10000.do{c.map(0.2)}}.bench

e=EZSlider(nil, 350@20, \test, ControlSpec(0.0, 100.0, 4.0), {|ez| ez.value.postln})
EZRanger
MIDIIn.connectAll
(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep], [\kak] ]).action_{|b| b.value.postln};
b.mapToMIDI(\noteOn, 1, mode:\toggle);

)
b.states=[["test"],["test1"],["test2"],["test3"]];
b.action=b.action.addFunc({|b| "poepjes".postln});
b.valueAction_(1)

(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 350@20).states_([ [\poep], [\kak] ]).action_{|b| b.value.postln};
//b.mapToMIDI(\noteOn, 1);

)

MIDIFunc.noteOn({arg msg;
	{b.valueAction_(1)}.defer;
}, 1);



MIDIIn.connectAll
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln})
e.mapToMIDI(\cc, 1, method: \dynamic)
e.controlSpec=ControlSpec(10.0, 100.0, \exp)


MIDIIn.connectAll
e=EZRanger(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln})
e.mapToMIDI(\cc, [1,2], method: \dynamic)


\freq.asSpec.isKindOf(Spec)


(
var func;
func={arg val; (val*10)};
func={arg func; {arg val; func.value(val)+2.5}}.value(func);
func={arg func; {arg val; func.value(val)*12.5}}.value(func);
//func.value(5);
{10000.do{func.value(2.5)}}.bench
)

(
var func;
func={arg val; val*10+2.5*12.5};
{10000.do{func.value(2.5)}}.bench
)


(
var func;
func={arg val; (val*10).postln};
func={arg func; {arg val; func.value(val)+2.5}}.value(func);
func={arg func; {arg val; func.value(val)*12.5}}.value(func);
//func.value(5);
func.value(2.5)
)


FunctionChain

(
var func, ffunc;
func={arg val; val*10};
ffunc=func.deepCopy;
func={arg val; ffunc.value(val)+12.5};
func.value(10)
)


(
var f;
f={arg val; val*2};
f=f.addFunc{arg val; val+2};

f.value(10)
)

MIDIIn.connectAll
s.boot;
Server.killAll
(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToOSC('/1/fader1')
)


(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToOSC('/1/fader1', netAddr: NetAddr("192.168.178.13", 9000))
)

n=NetAddr("192.168.178.13", 9000);//TouchOSC: Local IP adres, port(incoming)
n.sendMsg('/1/fader1', 0.5)


(
w=Window.new.front; w.addFlowLayout;
e=MultiSliderView(w, 350@120).value_(0!5).action_{|ez| ez.value.postln};
e.mapToOSC(['/1/multifader1/1','/1/multifader1/2','/1/multifader1/3','/1/multifader1/4','/1/multifader1/5'])
)

(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToMIDI(\cc, 1, midiOutPort:5, midiOutChan: 0)
)

(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToMIDI(\cc, 1, controlSpec: ControlSpec(100, 1400), midiOutPort:5, midiOutChan: 0)
)

(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToMIDI(\cc, 1, controlSpec: ControlSpec(100, 1400), midiOutPort:5)
)

MIDIFunc.cc({arg ...args; args.postln})

(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToMIDI(\cc, 1, 0, controlSpec: ControlSpec(1400, 100),show:true)
)


10* -1

(\freq.asSpec.unmap(20)-0.23299000144534)*127


(
w=Window.new.front; w.addFlowLayout;
e=Slider(w, 350@20).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, 1, controlSpec: ControlSpec(0.2, 0.4), mode:\continuous)
)

(
w=Window.new.front; w.addFlowLayout;
e=Slider(w, 350@20).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, 1, mode:\continuous)
)



e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.class.asSymbol.class
x=(AAAA: 123);
x[\AAAA]

c=\freq.asSpec
c=c.warp;
c.isKindOf(Warp)

c=c.warp;
c.map(0.5)


(
var incs=ControlSpec(200,4000, \exp);
var cs=ControlSpec(20,20000, \exp);
var min,max, mul, add;

min=cs.unmap(incs.minval);
max=cs.unmap(incs.maxval);

/*
mul=(max-min)*(1/127);
add=min;
*/

mul=(max-min)*(1/127).neg;
add=max;


[mul,add].postln;

[0,127].collect{|v|
	cs.map( v*mul+add )
}
)

val=val*0.36824511684+31.342020521923

(v*((max-min)*(1/127)) + min)





v=(v*(max-min) + (min*127));
^(value - spec.minval) / spec.range

v=(v - 0) / (127-0);//unmap



(
var incs=ControlSpec(1100,4000, \exp);
var cs=ControlSpec(20,20000, \exp);
var min,max;

min=cs.unmap(incs.minval);
max=cs.unmap(incs.maxval);

[0,127].collect{|v|
	cs.map(
		ControlSpec(0,127).unmap(
			(v*(max-min) + (min*127))
		)
	)
}
)

(v/127*(max-min)*127 + (min*127))//map


value=value * (max-min) + min;//map
ControlSpec(20,100).map(0)
0*(100-20)+20


(
var incs=ControlSpec(200,4000, \exp);
var cs=ControlSpec(20,20000, \exp);
var min,max;

min=cs.unmap(incs.minval);
max=cs.unmap(incs.maxval);

[0,127].collect{|v|
	cs.map(
		ControlSpec(0,127).unmap(
			ControlSpec(min*127, max*127).map(v).postln
		)
	)
}
)



value=(value - 0) / (127);//unmap



map { arg value;
	// maps a value from [0..1] to spec range
	^value * spec.range + spec.minval
}
unmap { arg value;
	// maps a value from spec range to [0..1]
	^(value - spec.minval) / spec.range
}




(
var min, max;
a=\freq.asSpec.unmap(1900);//0.65924120176295
b=\freq.asSpec.unmap(20000);//1.0
min=(b-a).reciprocal*127;
\freq.asSpec.map(ControlSpec(127-min,127).unmap(0));
)


(
var min, max, f1=120, f2=2000;
a=\freq.asSpec.unmap(f1);
b=\freq.asSpec.unmap(f2);
min=(1-((b-a).reciprocal))*127*b;
max=127/(b);
[f1,f2,a,b,min,max].postln;
[0,127].collect{|v| \freq.asSpec.map(ControlSpec(min,max).unmap(v))};
)

[ 100, 2000, 0.23299000144534, 0.66666666666667, -165.84490078613, 190.5 ]
[100,2000] => * 0.348995
[ 200, 2000, 0.33333333333333, 0.66666666666667, -95.25, 190.5 ]
[200,2000] => *0.375 en *
[20,2000]=> * 0
[100,20000]=> * 1




\freq.asSpec.map( \freq.asSpec.unmap(110)  )

(
w=Window.new.front; w.addFlowLayout;
e=Slider(w, 350@20).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, 1, controlSpec: ControlSpec(-20, 140), mode:\continuous)
)


\freq.asSpec.map(ControlSpec(-40, 140).unmap(0)  )

(
w=Window.new.front; w.addFlowLayout;
e=Slider2D(w, 350@350).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, [1,2], mode:\continuous)
)

e.valueAction_([0.2,0.2])
(
w=Window.new.front; w.addFlowLayout;
e=Slider(w, 350@20).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, 1, mode:\continuous)
)


(
w=Window.new.front; w.addFlowLayout;
e=MultiSliderView(w, 350@120).value_(0!9).action_{|ez| ez.value.postln};
e.mapToMIDI(\cc, (1..9), mode:\continuous)
)

(
w=Window.new.front; w.addFlowLayout;
e=EZMultiSlider(w, 350@120, \amps, \freq.asSpec, {|ez| ez.value.postln}, 20!9);
e.mapToMIDI(\cc, (1..9), mode:\continuous)
)

(
w=Window.new.front; w.addFlowLayout;
e=Button(w, 350@20).action_{|ez| ez.value.postln}.states_([ [\in],[\out], [\io] ]);
e.mapToMIDI(\noteOn, 1, mode:\toggle)
)

(
w=Window.new.front; w.addFlowLayout;
e=Button(w, 350@20).action_{|ez| ez.value.postln}.states_( {|i| [i]}!16);
e.mapToMIDI(\noteOn, [1,11], mode:\plusminus)
)

(
e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln}, unitWidth:40);
e.mapToMIDI(\cc, 1, controlSpec: ControlSpec(60, 120),show:true)
)


{arg guiObject, functionList, guiFunctionList, value;var controlSpecOut=FreqSpec(20,20000,'exp',0.0,440," Hz").warp; {arg val, number;val=val*0.016666666666667-1.0; val=controlSpecOut.map(val);functionList.value(val); { guiFunctionList.value(val) }.defer;}}


var val=127; val=val*0.016666666666667-1.0;


e.unmapToMIDI

Array.new()
[1,2].size
nil.asArray[2]
(
e=EZRanger(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln});
//e.valueAction_([20,20000]);
e.mapToMIDIU(\cc, [1,2])
)



{
	arg guiObject, functionList, guiFunctionList, value;
	var controlSpecOut=FreqSpec(20,20000,'exp',0.0,440," Hz").warp;
	{arg val, number;
		val=val*0.0078740157480315;
		val=controlSpecOut.map(val);
		value.put(0,val);
		val=value;
		functionList.value(val);
		{ guiFunctionList.value(val) }.defer;
	}
}

{
	arg guiObject, functionList, guiFunctionList, value;
	var controlSpecOut=FreqSpec(20,20000,'exp',0.0,440," Hz").warp;
	{arg val, number;
		val=val*0.0078740157480315;
		val=controlSpecOut.map(val);
		value.put(1,val);
		val=value;
		functionList.value(val);
		{ guiFunctionList.value(val) }.defer;
	}
}



e.lo_(40)
e.loBox.valueAction_(40);""

e.learnMIDIU

m=MIDIFunc.cc({arg val,num; [val,num].postln});
m.asArray.do(_.free)


m.learn

x=e.midiMapU
e.controlSpec=ControlSpec(100, 4000, \exp);
x.update



MIDIFunc.noteOn({arg vel,val;[vel,val].postln});
MIDIFunc.noteOff({arg vel,val;[vel,val].postln});
MIDIFunc.cc({arg val,num; [val,num].postln})


{arg guiObject, functionList, guiFunctionList;
	{arg val, number;
		functionList.value(val);
		{ guiFunctionList.value(val) }.defer;
	}
}

MIDIFunc.cc({arg val, num; [val,num].postln})
MIDIFunc.noteOn({arg val,num; num.postln})


f={arg mul=10, add=0.5;
	{arg value;
		value*mul+add
	}
}.value;

f.value(3);




e.controlSpec.class.instVarNames

f=(e.controlSpec.asCompileStringJT++".warp");
f.interpret.map(0.5)

e.controlSpec.asCompileStringJT

(
var thiz;
var class, string, args;
thiz=e.controlSpec;
class=thiz.class;
string=class.asString;
string=string++"(";
args=thiz.storeArgs;
args.do{|i| string=string++i.asCompileString++","};
string.removeAt(t.size-1);
string=string++")";
string;
string.interpret.map(0.5)
)

e.controlSpec.class.classVarNames
Class

e.controlSpec.args

format("%(%,%,%).warp",e.controlSpec.class.asString,e.controlSpec.minval,e.controlSpec.minval,e.controlSpec.warp.asSpecifier.asSymbol)

.asString.split($ )[1]


{arg val, number;var guiVal;val=val*mul+add; val=cs.map(val);guiVal=val;functionList.value(val); { guiFunctionList.value(guiVal) }.defer;}


{arg val, number;
	var guiVal;
	val=val*mul+add;
	val=cs.map(val)guiVal=val;
	functionList.value(val);
	{ guiFunctionList.value(guiVal) }.defer;
}


MIDIIn.connectAll
(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
b=Button(w, 200@200).states_([ [\test],[\test,Color.black,Color.yellow]]);
)

b.mapToMIDI(\noteOn, 1)
b.isMIDImapped
x=b.learnMIDI(action:{|msg| y=msg.postln})
b.unmapToMIDI

x.msg


f=EZSlider(nil, 350@20, \test, \freq.asSpec, {|ez| ez.value.postln})
f.mapToMIDI(\cc, 1)
f.unmapToMIDI
f.learnMIDI



f=EZRanger(nil, 350@20, \test, \freq.asSpec, {|ez| ez.value.postln})
f.mapToMIDI(\cc, [1,2])
f.alwaysOnTop.responderFunc
f.unmapToMIDI
f.learnMIDI
f.midiMap
f.responderFunc


(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
f=Slider2D(w, 350@350);
f.mapToMIDI(\cc, [1,2])
)





m=MIDItoGUIlearn(f)


(
var ch=true, srcID=true;
m=[\noteOn, \noteOff, \control, \polytouch, \touch, \program, \bend, \sysex].collect{|type|
	var type2=type.copy;
	if (type2==\control, {type2=\cc});
	MIDIFunc({arg ...msg;
		([type]++msg).postln;
		{
			if (f.isMIDImapped.not, {
				f.mapToMIDI(
					type2, msg[1], if (ch, {msg[2]},{nil}), if (srcID, {msg[3]},{nil})
				)
			});
		}.defer
	}, msgType:type)
}
)
m.do{|m| m.free}

f.mapToMIDI(\cc, 1)
f.unmapToMIDI
f.alwaysOnTop
f.isMIDImapped

MIDIFunc.cc({arg ...msg; msg.postln})
//MIDI source uid

[\noteOn, \noteOff, \control, \polytouch, \touch, \program, \bend, \sysex].do({|type|
	MIDIIn.addFuncTo(type, traceFuncs[type]);
});
.trace



-276310766

199497762


m=MIDIFunc.cc({arg ...msg; msg.postln}, srcID: 199497762)
m.free

.mapToMIDI

MIDIOut
127*0.01+10

MIDIIn -> GUI
0-127 => val * mul + add (b.v. *0.01 + 10) => gui value (10-11.27)
GUI -> MIDIOut
gui value (10-11.27) => (val-add)*mul.reciprocal => MIDIOut

MIDIIn -> GUI
0-127 => val=val * mul; val=\freq.asSpec.map(val) => gui value (20-20000)
GUI -> MIDIOut
gui value (20-20000) => val=\freq.asSpec.unmap(val); val=(val-add)*mul.reciprocal => MIDIOut


f={arg x; var y=x; y*10}; {10000.do{f.value(1.0.rand)}}.bench
var y; f={arg x; y=x; y*10}; {10000.do{f.value(1.0.rand)}}.bench



e=EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln});
e.action.class


e.action=e.action.addFuncFirst({"hal".postln});
e.action=e.action.addFuncFirst({"stront".postln});

e.action


Function


[1,2,3].flop

f = { |a, b| if(a > 0) { a + b } { -inf } }.flop;
f.value([-1, 2, 1, -3.0], [10, 1000]);
f.value(2, 3);

f={arg x; x*2}; f=f.addFunc({arg x; x+2}); f=f.flop; f.value(10)


a = nil;
a = a.addFunc { |x="", y=""| "this % is an % example\n".postf(x, y); 1 };
a.postln;
a = a.addFunc { |x="", y=""| "there is no % that is %\n".postf(x, y); 2 };
a.value;
a.value("text", "extraordinary well written")
a.valueArray(["x", "y"]);

// mathematics
a = nil;
a = a.addFunc({ 1.0.rand }).addFunc({ [0, 1].choose });
a = a.squared.linexp(0, 1, 1.0, 500);
a.value;

(
f={arg x; x*2};
f=f.addFunc({|x| x+2});
f=f.addFunc({|x| x*2});
f=f.addFunc({|x| x+2});
)
f.value(100)
f.array
f.do({arg f; f.value(100).postln})

{100000.do{x=100; f.value(x)}}.bench
{100000.do{x=100; f.do({|f| f.value(x)})}}.bench
{100000.do{x=100; f.array.copyToEnd(1).do({|f| f.value(x)})}}.bench

{10000.do{x=100; f.do{|f| x=f.value(x)};}}.bench





{10000.do{x=100; if (x<100, {x},{if (x>90, {x*2+2*2},{x})})}}.bench

(
f={arg x; x+2};
f={arg f; {arg x; x=f.value(x); x*2}}.value(f);
f={arg f; {arg x; x=f.value(x); x+4}}.value(f);
{100000.do{f.value(10)}}.bench
)

(
x=10; {100000.do{((x+2)*2)+4}}.bench
)

MIDIClient.init;
MIDIClient.destinations;
[
	MIDIEndPoint("IAC Driver", "Bus 1")
	, MIDIEndPoint("Babyface Pro (70784425)", "Port 1")
	, MIDIEndPoint("iCON iControl V1.01", "Port 1")
	, MIDIEndPoint("Launchkey 61", "Launchkey MIDI")
	, MIDIEndPoint("Launchkey 61", "Launchkey InControl")
	, MIDIEndPoint("Midi Fighter Twister", "Midi Fighter Twister") ]

MIDIIn.connectAll

MIDIOut(outPort).write(3, 16rB0, outChan.asInteger, num.asInteger, val);

MIDIOut(5).write(3, 16rB0, 1, 12, 127);


(
var g={arg x; x*2}, f;
var type=\noteOn;
f={arg val; val=g.value(val);
	switch(type, \noteOn, {val*2}, \noteOff, {val*2}, \polyTouch, {val*2}, \control, {val*2}
		, \program, {val*2}, \touch, {val*2}, \bend, {val*2});
};
{10000.do{f.value(10)}}.bench
)

(
var g={arg x; x*2}, f, h;
var type=\noteOn;
//	val=g.value(val);
f=switch(type, \noteOn, {{|val| val*2}}, \noteOff, {|val| val}, \polyTouch, {|val| val}
	, \control, {|val| val}
	, \program, {|val| val}, \touch, {|val| val}, \bend, {|val| val});
h={|ez| f.value(g.value(ez))};
{10000.do{f.value(g.value(10))}}.bench
)

MIDIClient.destinations[0].device

[1,2,3,4][nil]
MIDIFunc


device, <>name
MIDIClient.destinations.do{|mc, i| if ( (mc.device==MIDIClient.sources[6].device)&&(mc.name==MIDIClient.sources[6].name), {i.postln})};


c=\freq.asSpec; {100000.do{ c.map(1.0) }}.bench
c=\freq.asSpec.warp; {100000.do{ c.map(1.0) }}.bench
c=ControlSpec(1, 100.0, \exp).warp; {100000.do{ c.map(1.0) }}.bench

\freq.asSpec.warp.constrain(50)

EZSlider(nil, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln})
EZSlider(nil, 350@20, \freq, \freq.asSpec.warp, {|ez| ez.value.postln})

ControlSpec.default
Warp

makeGUI