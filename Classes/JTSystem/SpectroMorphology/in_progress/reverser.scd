//~osc[\Reverser].enabled
var isRunning=false, ttime=0;
var gui=(), gui2=(), w;
var minTrigTime=0.05;//minimal trig time between triggers

var p=(delayTime: [1.0, 1.0], fadeOut: [0.02, 0.1], sustainTime: [1.0, 1.0], fadeIn: [0.45, 0.5]);
var cs=(delayTime: ControlSpec(0.25, 4.0), fadeOut: ControlSpec(0.0, 2.0), fadeIn: ControlSpec(0.1, 4.0), sustainTime: ControlSpec(0.0, 1.0));
SynthDef(\ReverseMaster, {arg inBus, outBus, wetBus, amp=1.0, wet=0.0, amp2=1.0;
	var out=In.ar(inBus, ~noc)*amp.lag(0.1)*amp2.lag(0.2);
	wet=wet.lag(0.1);
	Out.ar(outBus, (1-wet).sqrt*out);
	Out.ar(wetBus, (wet).sqrt*out);
}, metadata: (specs: (
	amp:\amp.asSpec, wet: \amp.asSpec))).add;
~bus[\ReverserOut]=Bus.audio(s, ~noc);
SynthDef(\PlayBufReverser, {arg bufnum, startPos, dur=0.1, fadeIn=0.01, fadeOut=0.1, az=1.0, amp=1.0, outBus=0.0, wetBus, wet=0.0;
	var env=EnvGen.ar(Env.linen(fadeIn, dur, fadeOut, amp, -4.0), doneAction:2);
	var out;
	out=PlayBuf.ar(1, bufnum, -1, 1, startPos, 1)*env;
	Out.ar(outBus, PanAz.ar(~noc, out, az));
}).add;
s.sync;
~synth[\ReverserMaster]=Synth.before(~synth[\GVerb], \ReverseMaster, [\inBus, ~bus[\ReverserOut], \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb]]).register;
s.sync;
~osc[\Reverser]=OSCFunc({arg msg, time; }, '/test');
~osc[\Reverser].free;

~func[\Reverse]={arg flag=true, oneShot=false;
	if (flag, {
		if (~osc[\Reverser].enabled.not, {
			~osc[\Reverser]=OSCFunc({arg msg, time;
				var delayTime=exprand(p[\delayTime][0], p[\delayTime][1]);
				var fadeIn=rrand(p[\fadeIn][0], p[\fadeIn][1]);
				var fadeOut=rrand(p[\fadeOut][0], p[\fadeOut][1]);
				var sustain=rrand(p[\sustainTime][0], p[\sustainTime][1]);
				if (time-ttime>minTrigTime, {
					s.sendBundle(delayTime, [\s_new, \PlayBufReverser, -1, 1, 1
						, \bufnum, ~buf[\Rec].bufnum, \outBus, ~bus[\ReverserOut].index
						, \startPos, msg[3]+(s.sampleRate*delayTime)
						, \dur, delayTime*sustain-fadeIn, \fadeIn, fadeIn
						, \fadeOut, fadeOut, \az, 1.0.rand2, \amp, 0.5]);
				});
				ttime=time;
				if (oneShot, {{gui[\oneShot].value_(0)}.defer});
			}, '/phase_onset');
		})
	},{
		~osc[\Reverser].free;
	});
	if (oneShot, {
		~osc[\Reverser].oneShot;
	});
	{
		gui[\on].value_(flag.binaryValue-oneShot.binaryValue);
	}.defer;
};

~func[\ReverseOneShot]={
	~func[\Reverse].value(true, true)
};

{
	var synth=(), buf=();
	w=Window("Reverser");
	w.addFlowLayout;
	w.onClose_{o.free; s.quit};
	w.alwaysOnTop_(true);
	gui[\on]=Button(w, 160@20).states_([ [\on], [\ON, Color.black, Color.green] ]).action_{|b|
		~func[\Reverse].value(b.value.asBoolean)
	};
	gui[\oneShot]=Button(w, 160@20).states_([ ["oneShot"], ["oneShot",Color.black,Color.green]]).action_{|b|
		if (b.value==1, {
		~func[\ReverseOneShot].value
		})
	};
	p.sortedKeysValuesDo{|key,val|
		gui[key]=EZRanger(w, 350@20, key, cs[key], {|ez| p[key]=ez.value}, p[key])
	};

	gui[\minTrigTime]=EZSlider(w, 350@20, \minTrigTime, ControlSpec(0.01, 5.0, \exp), {|ez|
		minTrigTime=ez.value
	}, minTrigTime);
	~win[\Reverser]=w;
	~par[\Reverser]=p;
	~gui[\Reverser]=gui;
}.defer;

while({w==nil},{0.001.wait});
gui2=~synth[\ReverserMaster].makeGUI(w, willHang:true);
gui2.keysValuesDo{|key,view| gui[(key++\master).asSymbol]=view};
