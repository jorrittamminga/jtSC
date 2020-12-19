(
var presetPath=thisProcess.nowExecutingPath.dirname++"/presets/", cuePath=thisProcess.nowExecutingPath.dirname++"/cues/";
var pc;
c=();
v=();
p=();
w=Window("Cue", Rect(700,400,700,600)).front;
w.addFlowLayout(4@4, 0@0);
w.alwaysOnTop_(true);
[\freq, \amp, \rq].do{|key| v[key]=EZSlider(w, 350@20, key, key.asSpec, {|ez| p[key]=ez.value }) };
v[\range]=EZRanger(w, 350@40, \range, \freq, {});
v[\test]=Slider(w, 350@20);
v[\but]=Button(w, 350@20).states_([ [\test],[\ok]]);
v[\list]=ListView(w, 350@60).items_( [\a,\b,\c] );
v[\pop]=PopUpMenu(w, 350@20).items_( [\a,\b,\c] );
v[\check]=CheckBox(w, 350@20);
v[\ms]=MultiSliderView(w, 350@40).value_(Array.rand(10, 0.0, 1.0));
v[\twoD]=Slider2D(w, 200@200);
)

nil.value
Button.value is default nil ipv 0! stom....

[1,2,3].rank
[[1,2],[3,2]].rank
FunctionList
CueList
PresetJT

Event.asViewEvent


(
var func;
files.do{arg path, i;
	var key=path.fileNameWithoutExtension.split($_).copyToEnd(1).join($_).asSymbol;
	var cue=cues[key];
	out=path.load;
	func=func.addFunc(
		case(
			cue.class==PresetJT, {
				switch(out.class
					, Array, {
						var preTime, postTime;
						//[method, value, durations, curves, delayTimes, specs, actions]
						//or
						//[[method, value, durations, curves, delayTimes, specs, actions], [preTime, postTime]]
						if (array.rank>1, {
							preTime=out[1][0]??{0};
							postTime=out[1][1]??{0};
							{arg e; preTime.wait; cue.performMsg(out[0]); postTime.wait; }
						},{
							{arg e; cue.performMsg(out)}
						});
					}, Event, {
						{arg e; cue.restore(out) }
					}, SimpleNumber, {
						{arg e; cue.restore(out) }
				})
			},
			cue.class==Event, {
				var sectKeys, event=();
				switch(out.class
					, Array, {
						var preTime, postTime;
						sectKeys=cue.objects.keys.asArray.sect(out[0].keys.asArray);
						//[method, event, durations, curves, delayTimes, specs, actions]
						//or
						//[[method, event, durations, curves, delayTimes, specs, actions], [preTime, postTime]]
						if (array.rank>1, {
							preTime=out[1][0]??{0};
							postTime=out[1][1]??{0};
							{arg e; preTime.wait; cue.performMsg(out[0]); postTime.wait; }
						},{
							{arg e; cue.performMsg(out)}
						});


						{arg e;
							preTime.wait;
							sectKeys.collect{|key| cue.objects[key].action.value(val); {obj.value_(val)}.defer};
							postTime.wait;
						}
					}, Event, {
						//sectKeys.do{|key| event[key]=out[key]};
						{arg e;
							sectKeys.collect{|key| cue.objects[key].action.value(val); {obj.value_(val)}.defer};
						}
					}, SimpleNumber, {
						{arg e; cue.restore(out) }
				})
			},
			//cue.class==Function,
			{
				switch(out.class, Array, {
					var preTime, postTime, func=out[0];
					//[{}, preTime, postTime, ....]
					preTime=out[1]??{0};
					postTime=out[2]??{0};
					{arg e; preTime.wait; func.value(e); postTime.wait; }
				}, Function, {
					out
				})
		})
	)
};
func
)


.valuesActionsTransition

v[\twoD].action
v[\ms].action
v[\but].action
v[\but].respondsTo('value')
v[\range].value;
v[\freq].value
v[\test].value
v[\check].value

e=();
e.actions=v.collect{|object| {arg val; object.action.value(val); {object.value_(val)}.defer;}};
e.valuez=(); v.keysValuesDo{|key,obj| [key,obj.value,obj].postln; e[\valuez][key]=obj.value??0}; e
e.controlSpecs=v.collect(_.controlSpec);

PresetSystem


v[\test].respondsTo('action')
().respondsTo('action')
v.collect{|i| i.respondsTo(\action)}//i.methods.collect(_.name).includesEqual(\action)}
v.collect(_.controlSpec)

{10000.do{v[\check].controlSpec.warp.map(0.5)}}.bench
{x=v[\check].controlSpec.warp;10000.do{x.map(0.5)}}.bench

{10000.do{v[\freq].controlSpec.map(0.5)}}.bench
{x=v[\freq].controlSpec.warp;10000.do{x.map(0.5)}}.bench


{x=[0.4,1.0].asSpec; 10000.do{x.map(0.5)}}.bench
{x=[0.4,1.0].asSpec.warp; 10000.do{x.map(0.5)}}.bench


v[\test].controlSpec
v[\amp].controlSpec_(v[\amp].controlSpec.asSpec)

{x=v[\freq].controlSpec.warp; 10000.do{x.map(0.25)}}.bench
{10000.do{v[\freq].controlSpec.warp.map(0.25)}}.bench
{10000.do{v[\freq].controlSpec.map(0.25)}}.bench


MultiSliderView
EnvelopeView
w=LinearWarp.new
w=Warp.new
w.map(0.2)

EventJT
specs[key]=ControlSpec(0.0, 1.0).warp;
clumps[key]=obj.value.size.max(1);
if (obj.class.superclass==ItemViewBase, {specs[key]=ControlSpec(0, obj.items.size-1, 0, 1)});
if ((obj.class==Button) || (obj.class==RoundButton), {specs[key]=ControlSpec(0, obj.states.size-1, 0, 1)});


v.values.collect{|i| i.post; i.isKindOf(View).postln; }
v.values.collect{|i| i.isKindOf(AbstractStepValue) }
v.values.collect{|i| i.post;i.class.superclasses.postln }
PopUpMenu.class.superclasses

Class


(
e=();
e[\objects]=v;
e[\controlSpecs]=e.objects.collect(_.controlSpec);
e[\actions]=e.objects.collect(_.action);
e[\valuez]=e.objects.collect(_.value);
e[\keyz]=e.objects.keys;
e[\sortedKeyz]=e.objects.sortedKeys;
e[\sortedControlSpecs]=e[\sortedKeyz].collect{|key| e[\controlSpecs][key]};
e[\sortedActions]=e[\sortedKeyz].collect{|key| e[\actions][key]};
e[\sortedValues]=e[\sortedKeyz].collect{|key| e[\valuez][key]};
e.clumps;
e.type=\Views;// \Views, \Values
e.objectsType = \Views, \Values;
)

View.methods.collect(_.name).includesEqual(\action)

EventJT

v[\amp].action={|ez| "amp".post; p[\amp]=ez.value};
e[\actions][\amp].value(0.5)
e[\sortedActions][0].value(0.5)

Event.addEventType(\eventJT, {}, ());

e.type


(
x={1.0.wait; "YO".postln};
y=Routine({
	x.value
});
)

y.play