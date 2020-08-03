(
var path=thisProcess.nowExecutingPath.dirname;
w=Window.new.front;
w.addFlowLayout;
w.alwaysOnTop_(true);
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	s.waitForBoot{
		x=InJT([0], [[s,t]], [\mic1]);
		x.addPlugin(\Meter);

//		x.addPlugin(\Recorder);
		x.addPlugin(\Player, ["/Users/jorrittamminga/Music/Samples/kraan1.aif"]);
//		x[\mic1].addPlugin(\Compressor);
//		x[\mic1].addPlugin(\EQ);
//		x[\mic1].plugins[\EQ].addPresetSystem(path++"/presets/", "EQ");

		x.makeGUI(w, bounds: 30@15);//20@10
		z=OutJT([[0,1]], [[s,t]]);
		z.addPlugin(\Compressor, [\CompanderC,(limiter:true)]);
		//z.addPlugin(\MasterFader);
		z.addPlugin(\Meter);
		z.makeGUI(w, bounds:30@15);

//		y={In.ar(x[\mic1].bus[0].index, 1)}.play
	}
}
)

z.plugins[\Compressor].gui.window.userCanClose_(true)
x.plugins[\Player].makeGUI
s.meter
z.plugins[\MasterFader]

p=PlayerJT(0, [s,t], "/Users/jorrittamminga/Music/Samples/kraan1.aif");
p.makeGUI


(
var path=thisProcess.nowExecutingPath.dirname;
w=Window.new.front;
w.addFlowLayout;
w.alwaysOnTop_(true);
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	s.waitForBoot{
		//y={Out.ar(32, SinOsc.ar(1000, 0, 0.5)}.play
		x=InJT([0], [[s,t]], [\mic1]);
		x.addPlugin(\Meter);
		x.makeGUI(w, bounds: 30@15);//20@10
	}
}
)



x.plugins[\Player].makeGUI
s.meter


Server.killAll

156-8/4


(
w=Window("test", Rect(300,300,300,300)).front;
w.addFlowLayout;
w.alwaysOnTop_(true);
c=CompositeView(w, 300@220);
c.addFlowLayout;
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	a={SinOsc.ar([440,550,660,770], 0, 0.2)}.play(t);
	t.sync;
	s.waitForBoot{
		b={SinOsc.ar([440,550,660,770]+1, 0, 0.2)}.play;
		s.sync;
		x=OutJT([[0,1,2,3]], [s,t]);
		x.addPlugin(\Meter);
		x.addPlugin(\MasterFader);
		x.addPlugin(\Splay);
		x.makeGUI(c, 30@15, 0@0, 0@0)
}})


x.plugins[\Splay].bypass_(true)
s.queryAllNodes
t.queryAllNodes
t.meter
s.meter


(
var path=thisProcess.nowExecutingPath.dirname;
w=Window.new.front;
w.addFlowLayout;
w.alwaysOnTop_(true);
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	s.waitForBoot{
		x=InJT([0], [[s,t]], [\mic1]);
		x.addPlugin(\Meter);

		x.addPlugin(\Recorder);
		x.addPlugin(\Player, ["/Users/jorrittamminga/Music/Samples/kraan1.aif"]);
		x[\mic1].addPlugin(\Compressor);
		x[\mic1].addPlugin(\EQ);
		x[\mic1].plugins[\EQ].addPresetSystem(path++"/presets/", "EQ");
		x.makeGUI(w, bounds: 30@15);//20@10

		z=OutJT([[0,1]], [[s,t]]);
		z.addPlugin(\Meter);
		z.makeGUI(w, bounds:30@15);

		//y={In.ar(x[\mic1].bus[0].index, 1)}.play
	}
}
)
y.free


p=PlayerJT(0, [s,t], "/Users/jorrittamminga/Music/Samples/kraan1.aif");
p.makeGUI


[\Compressor, \EQ].do{|plugin|
	var gui;
	p[\in][p[\instrument].asSymbol].plugins[plugin].addPresetSystem(p.settingspath
		, plugin);
	//gui.addPresetSystem(gui.viewsPreset, p.settingspath, plugin);
};

(
var w=Window.new.front;
var path=thisProcess.nowExecutingPath.dirname++"/presets/";
w.addFlowLayout;
w.alwaysOnTop_(true);
{
	5.do{Server.killAll; 0.1.wait};
	t=Server.internalNew("live",makeWindow:false);
	t.waitForBoot{
		s.waitForBoot{
			x=InJT([0,1], [s,t]!2, [\mic1,\mic2]);
			x.addPlugin(\Meter);
			x.addPlugin(\Recorder);
			x.addPlugin(\Player, ["/Users/jorrittamminga/Music/Samples/kraan1.aif"]);
			x[\mic1].addPlugin(\Compressor);
			x[\mic2].addPlugin(\EQ);

			x[\mic1].plugins[\Compressor].addPresetSystem(path,"Compressor");
			x[\mic2].plugins[\EQ].addPresetSystem(path,"EQ");

			x.makeGUI(w, bounds: 20@10);
			y=OutJT([[0,1]],[[s,t]]);
			y.addPlugin(\Meter);
			y.addPlugin(\MasterFader);
			y.addPlugin(\EQ);
			y.makeGUI(w, bounds: 20@10)
	}}
}.fork

)

{{WhiteNoise.ar(0.2)}!2}.play
t.queryAllNodes

y.gui.views[\MasterFader].value.max(-80)+2

CompressorJTGUI
x.bus
y.bus
x.busIndexPerServer
y.busIndexPerServer
x.busPerServer

x[\mic1].plugins[\Compressor].synth


~test={ {WhiteNoise.ar(Dust2.kr(10).lag(0, 1.0))}!2 }.play(t)
~test.free
z={{WhiteNoise.ar(0.01)}!2}.play
z.free

w
x.free

x.bus

x.labels
EQJT

(
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	s.waitForBoot{
		x=InJT(0, [s,t], \mic1)

}})



s.queryAllNodes
t.queryAllNodes
x
x.bus
x[\mic]

x.addPlugin(\Meter)
x.plugins[\Meter].makeGUI
x.free
x.busForMeter

x.busPerFlatIndex
x.synthPerFlatIndex



Server.killAll
(
s.waitForBoot{
	b=Bus.audio(s, 1); s.sync;
	x={Out.ar(b.index, SoundIn.ar(0))}.play;
	s.sync;
	e=EQJT(b, x, \fiveBand);
	e.makeGUI(bounds: 350@20, showScope:true, onClose:true);
}
)

MasterEQ(2,s)








(
s.waitForBoot{

	SynthDef(\Test, {arg freq=1000, gate=1, amp=0.2, amps=#[1,0,0,0], t_trig=1, pan=0;
		var out, env;
		env=EnvGen.kr(Env.asr(1, 1, 1, -4.0), gate, doneAction:1);
		out=SinOsc.ar(freq.lag(0.1)*[1,2,3,4],0,amp.lag(0.1)*amps.lag(0.1)).sum;
		Out.ar(0, Pan2.ar(out*env, pan))
	}, metadata: (specs: (freq: \freq.asSpec, amp: \amp.asSpec, amps: \amp.asSpec
		, pan: \bipolar.asSpec))).add;
	s.sync;
	x=Synth(\Test);
	a=x.makeGui(labelWidth:\auto);
}
)


(
t=Server.internalNew("live",makeWindow:false);
t.waitForBoot{
	s.waitForBoot{
		SynthDef(\Test, {arg freq=1000, gate=1, amp=0.2, amps=#[1,0,0,0], t_trig=1
			, pan=0;
			var out, env;
			env=EnvGen.kr(Env.asr(1, 1, 1, -4.0), gate, doneAction:1);
			out=SinOsc.ar(freq.lag(0.1)*[1,2,3,4],0,amp.lag(0.1)*amps.lag(0.1)).sum;
			Out.ar(0, Pan2.ar(out*env, pan))
		}, metadata: (specs: (freq: \freq.asSpec, amp: \amp.asSpec, amps: \amp.asSpec
			, pan: \bipolar.asSpec))).add;
		s.sync; t.sync;
		x=Synth(\Test);
		y=Synth(\Test, target:t);
		a=[x,y].makeGui(labelWidth:\auto);
}}
)

t.queryAllNodes
y.set(\freq, 1100)




threeBand: 0.77

e.synth.specs
e.synth.makeGui(nil, 350@20)
e.bus
s.queryAllNodes
e.makeGUI
e.gui.parent.bounds

e.scope.mouseDownAction={"poep".postln};


(
var width=40, height=20;
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
4.do{
	c=CompositeView(w, (width+4)@(width+2+height*3+0)); c.addFlowLayout(0@0, 2@2);
	3.do{
		e=EZKnob(c, width@(width+height), \amp, \amp.asSpec, {}, 0, false, height/2, nil, 0, height/2, 'vert2', 0@0, 0@0).font_(Font(Font.defaultMonoFace, height/2*0.8));
		e.numberView.align_(\center);
	};
};
w.view.decorator.nextLine;
Slider(w, width@20)
)
w.front
w.minimize


[0].size




1 nil
2 nil
3 nil
4 nil
5 [ [ Bus(audio, 64, 1, localhost), Bus(audio, 64, 1, live) ] ]
6 [ Bus(audio, 64, 1, localhost), Bus(audio, 64, 1, live) ]





1 nil
2 nil
3 nil
4 nil
5 [ [ Bus(audio, 64, 1, localhost), Bus(audio, 64, 1, live) ], [ Bus(audio, 65, 1, localhost), Bus(audio, 65, 1, live) ] ]
6 [ [ Bus(audio, 64, 1, localhost), Bus(audio, 64, 1, live) ], [ Bus(audio, 65, 1, localhost), Bus(audio, 65, 1, live) ] ]




(
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
c=CompositeView(w, 200@20); c.addFlowLayout;
c.resize_(5);
d=CompositeView(w, 200@20); d.addFlowLayout;
d.resize_(5);
c.background_(Color.yellow);
d.background_(Color.green);
//e=Knob(c, 200@40);
//e.resize_(5);
e=Slider(c, 200@20);
e.resize_(5);
f=NumberBox(d, 200@20);
f.resize_(5);
/*
e=EZKnob(c, 350@140, \freq, layout: 'vert2');
e.view.resize_(5);
e.knobView.resize_(5);
e.numberView.resize_(5);
e.labelView.resize_(5);
*/
)


(
var a;

a = { |i|
	var w, b, x,k,t,p;
	k=i;
	i = i + 1;
	w = Window("resize:"+i, Rect(10 + (k%3 * 220), Window.screenBounds.height- [250,460,670].at(k/3), 200, 180));
	b = w.view.bounds;
	x = CompositeView(w, w.view.bounds.insetBy(20,20))
	.background_(Color.rand)
	.resize_(i);

	y = CompositeView(x, x.bounds.moveTo(0,0).insetBy(20,20))
	.background_(Color.rand)
	.resize_(i);

	y.decorator = FlowLayout(y.bounds).gap_(0.0 @ 0.0);

	t = StaticText(y, Rect(0, 0, 40, 40))
	.background_(Color.rand.alpha_(0.8))
	.resize_(i)
	.string_(i)
	.font_(Font("Helvetica", 26));

	p=PopUpMenu(y,40@40).items_((1..9).collect(_.asString)).value_(i-1).resize_(i)
	.action_{|m| t.string_((m.value+1).asString); [p,t].do(_.resize_(m.value+1))};

	w.front;
	w.onClose = {a.do(_.close) };

} ! 9;
)



p=PresetSystem()
addSlave()


(
x=0.5;
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
e=EZSlider(w, 350@20, \amp, \amp.asSpec, {|ez| x=ez.value}, x);
w.userCanClose_(false);
)
w.front
e.value_(0.25)
w.close


x=();
x.removeAt(\a)


(
var showscope=false, showScope=true;

showscope??{showScope}

)



(
x=0.5;
w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
3.do{
	b=Button(w, 200@40).states_([[\c, Color.grey],[\C, Color.black, Color.green(1.8)]]);
}
)
