/*
- zorg dat freezeTrigTime > (fadeIn of fadeOut), dat geeft de mooiste morphs)
- PitchShift inbouwen
*/
var freezeTrigTime=0.5;//minimal trig time between triggers
var name=\Freezer;
var synth=(), guis=();
var parameters=(az:[0.0,0.0], amp:1.0, bins:10, wet:0.0, fadeIn:1.0, fadeOut:5.0, delayFrames: 0.0, rate:1.0, gapDepth:0.0, gapSpeed: [5.0,25.0], gapWidth:5.0, gapSpeedModSpeed: [0.1, 1.0], shiftDepth:0.0);
var controlSpecs=(amp: \amp.asSpec, fadeIn: ControlSpec(0.0, 10.0, 4.0), fadeOut: ControlSpec(0.0, 10.0, 4.0), bins: ControlSpec(1, 2044, \exp, 1), wet: \amp.asSpec, delayFrames: ControlSpec(0, s.sampleRate, 0, 1), az: \bipolar.asSpec, gapSpeed: ControlSpec(0.01, 100.0, \exp), gapSpeedModSpeed: ControlSpec(0.01, 10.0, \exp), fadeOut: ControlSpec(0.0, 10.0, 4.0), gapWidth: ControlSpec(0.2, 7.0, \exp), rate: ControlSpec(0.125, 4.0, \exp), shiftDepth: \amp.asSpec);
var time=Main.elapsedTime;
var flipflop=0, idGUI=0;
//------------------------------------------------------------ SYNTHDEFS
~bus[\FreezeIn]=Bus.audio(s, 1);
~bus[\FreezePeak]=Bus.control(s, 2);
~bus[\Freezer]=Bus.audio(s, ~noc);

SynthDef(\PlayBufFreeze, {arg bufnum, outBus, fadeIn=0.1, sustainTime=1.0, fadeOut=0.1, startFrame=0;
	var in, env=EnvGen.kr(Env.linen(fadeIn, 1, fadeOut), doneAction:2);
	in=PlayBuf.ar(1, bufnum, 1, 1, startFrame, 1)*env;
	Out.ar(outBus, in)
}).add;

SynthDef(\Freezer, {arg inBus, outBus, gate=1, fadeIn=1.0, fadeOut=3.0, fftSize=4096, hop=0.125, az=#[-1.0, 1.0], amp=1.0, rate=1.0, bins=10, wet=0.0, wetBus, t_freeze, t_unfreeze, doneAction=1, gapDepth=0.0, gapSpeed=#[5.0,25.0], gapWidth=5.0, gapSpeedModSpeed=#[0.1, 1.0], freezeIn=0.01, freezeOut=5.0, busPeakOut, busPeakIn, shiftDepth=0, amp2=1.0;
	var chain,in,out,freq,ampl, hasFreq, gapper, gapperLFO, freeze, shift, chain2;
	var env=EnvGen.kr(Env.dadsr(fftSize/SampleRate.ir,fadeIn,0,1,fadeOut,1),gate,doneAction:doneAction), shiftEnv;
	in=In.ar(inBus,1);
	freeze=EnvGen.kr(Env.dadsr(fftSize/SampleRate.ir*2.0, freezeIn, 0, 1, freezeOut)
		, SetResetFF.kr(TDelay.kr(t_freeze, 0.001), t_unfreeze+t_freeze) );
	//---------------------------------------------
	chain=FFT(LocalBuf(fftSize),in,hop);
	chain=PV_Freeze(chain, (freeze-0.01).ceil);
	#freq,ampl=FFTPeak.kr(chain);
	chain=PV_MaxMagN(chain,bins);
	out=IFFT(chain)*hop.sqrt*0.45;
	Latch.kr(freq,chain);
	//---------------------------------------------
	shiftEnv=EnvGen.kr(Env.dadsr(fftSize/SampleRate.ir, fadeIn,0, 1, fadeOut, 1
		, \sin),gate);
	shift=In.kr(busPeakIn);
	out=FreqShift.ar(out
		,((shift-freq).lag(0.1)*(1.0-shiftEnv)).clip(-1000,1000)*shiftDepth);
	//---------------------------------------------
	out=XFade2.ar(out, PitchShift.ar(out, 0.2, rate, 0.0, 1.0)
		, ((rate-1).abs>0.0*2-1).lag(0.1));
	gapperLFO=LFDNoise1.kr(LFDNoise1.kr(gapSpeedModSpeed[0])
		.exprange(gapSpeedModSpeed[0],gapSpeedModSpeed[1]));
	gapper=gapperLFO.exprange(gapSpeed[0], gapSpeed[1]);
	out=Impulse.kr(gapper, 0, gapDepth*1.5, 1-gapDepth)
	.lag(0.001, gapper.reciprocal*gapWidth)*out;
	//---------------------------------------------
	out=freeze*out*amp.lag(0.1)*env;
	az=gapperLFO.range(az[0],az[1]);
	out=PanAz.ar(~noc, out, az.lag(1.0), amp2.lag(0.2));
	wet=wet.lag(0.1);
	Out.kr(busPeakOut, freq);
	Out.ar(outBus, (1-wet).sqrt*out);
	Out.ar(wetBus, (wet).sqrt*out);
}, metadata: (specs: (
	amp: \amp.asSpec, rate: ControlSpec(1/4, 4, \exp)
	, fadeIn: ControlSpec(0.0, 10.0, 4.0), gapDepth: \amp.asSpec
	, gapSpeed: ControlSpec(0.01, 100.0, \exp)
	, gapSpeedModSpeed: ControlSpec(0.01, 10.0, \exp)
	, fadeOut: ControlSpec(0.0, 10.0, 4.0), gapWidth: ControlSpec(0.2, 5.0, \exp)
	, bins: ControlSpec(1, 2044, \exp, 1)//, wet: \amp.asSpec
	, az: \bipolar.asSpec, t_freeze: ControlSpec(0,1), t_unfreeze: ControlSpec(0,1)
	, freezeIn: ControlSpec(0.001, 5.0, \exp), freezeOut: ControlSpec(0.001, 5.0, \exp)
))).add;

s.sync;
//------------------------------------------------------------ FX

~synth[\FreezerMixer]=Synth.before(~synth[\GVerb], \SubMixer2, [
	\inBus, ~bus[\Freezer], \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb]
	, \amp, 1.0, \lpfWet, 14000, \wet, 0.0]).register;


/*
~synth[name]=Synth.after(~synth[\In], name, [\fftSize, 4096, \hop, 0.125, \inBus, ~bus[\In], \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb], \t_freeze, 0 ]);
s.sync;
~func[\makeGUI].value(name);
*/
//------------------------------------------------------------ FUNCS
~func[\FreezeBuf]={arg frame= -1, pars, id=0;
	var prevflipflop=flipflop, previd=id+prevflipflop;
	var inBus=frame.isPositive.binaryValue;
	flipflop=flipflop+1%2;
	id=id+flipflop;
	pars=pars??{parameters};
	if (synth[previd]!=nil, {
		if (synth[previd].class==Synth, {synth[previd].set(\gate, 0)});
	});
	if (synth[id]!=nil, {
		if (synth[id].class==Synth, {synth[id].set(\gate, 0)});
	});
	if (inBus==1, {
		synth[\PlayBuf]=Synth(\PlayBufFreeze, [\bufnum, ~buf[\Rec]
			, \outBus, ~bus[\FreezeIn]
			, \fadeIn, 0.01, \sustainTime, 2.0, \fadeOut, 1.0
			, \startFrame, (frame-parameters[\delayFrames]).wrap(0,~buf[\Rec].numFrames) ]);
	});
	{guis[\Freeze].value_(1)}.defer;
	//~bus[\FreezePeak].getn(2, {|thi| thi.postln});//moet dit echt?
	idGUI=id;
	synth[id]=Synth.after([~synth[\In], synth[\PlayBuf]][inBus], \Freezer, [
		\inBus, [~bus[\In], ~bus[\FreezeIn]][inBus]
		, \fftSize, 4096, \hop, 0.125
		, \busPeakOut, ~bus[\FreezePeak].index+flipflop
		, \busPeakIn, ~bus[\FreezePeak].index+prevflipflop
		//, \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb]
		, \outBus, ~bus[\Freezer]
		, \doneAction, 2, \t_freeze, 1]
	++pars.asKeyValuePairs
	).register;
};
~func[\UnFreezeBuf]={arg shiftDepth;
	var id=0;//er wordt niets met id gedaan....
	id=id+flipflop;
	if (synth[id]!=nil, {
		if (synth[id].class==Synth, {
			if (synth[id].isPlaying, {
				"turn off".postln;
				if (shiftDepth==nil, {
					synth[id].set(\gate, 0);
				},{
					synth[id].set(\gate, 0, \shiftDepth, shiftDepth);
				})
			});
			synth.removeAt(id)
		});
	});
	{
		guis[\Freeze].value_(0);
		if (guis[\freezeLiveOneShotNext].value==1, {
			guis[\freezeLiveOneShotNext].value_(0)
		});
	}.defer;
};
//------------------------------------------------------------ OSC funcs
~func[\freezeLiveOneShot]={arg pars;
	pars=pars??{parameters};
	if (~osc[\freezeLiveOneShot].class==OSCFunc, {~osc[\freezeLiveOneShot].free});
	~osc[\freezeLiveOneShot]=OSCFunc({arg msg, t;
		var frame=msg[3];
		~func[\FreezeBuf].value(frame);
		{
			guis[\freezeLiveOneShot].value_(0);
			//guis[\Freeze].value_(1);
		}.defer;
		time=t;
	}, '/phase_onset').oneShot;
};
~func[\unfreezeLiveOneShot]={arg pars;
	if (~osc[\unfreezeLiveOneShot].class==OSCFunc, {~osc[\unfreezeLiveOneShot].free});
	~osc[\unfreezeLiveOneShot]=OSCFunc({arg msg, t;
		//		if (t-time>freezeTrigTime, {
		~func[\UnFreezeBuf].value(0);
		{
			guis[\unfreezeLiveOneShot].value_(0);
			//guis[\Freeze].value_(0);
		}.defer;
		//		});
		time=t;
	}, '/phase_onset').oneShot;
};
~func[\freezeLiveOneShotNext]={arg pars;
	pars=pars??{parameters};
	if (~osc[\freezeLiveOneShotNext].class==OSCFunc, {~osc[\freezeLiveOneShotNext].free});
	~osc[\freezeLiveOneShotNext]=OSCFunc({arg msg, t;
		var frame=msg[3];
		~func[\FreezeBuf].value(frame);
		~func[\unfreezeLive].value;
		time=t;
	}, '/phase_onset').oneShot;
};
~func[\unfreezeLive]={arg pars;
	if (~osc[\unfreezeLive].class==OSCFunc, {~osc[\unfreezeLive].free});
	~osc[\unfreezeLive]=OSCFunc({arg msg, t;
		if ((t-time)>freezeTrigTime, {
			~func[\UnFreezeBuf].value(0);
			{
				guis[\unfreezeLiveOneShot].value_(0);
				//guis[\Freeze].value_(0);
			}.defer;
			~osc[\unfreezeLive].free;//this.free
			time=t;
		});
		time=t;//kan hier evt ook weg
	}, '/phase_onset');
};
~func[\autoFreeze]={arg pars;
	pars=pars??{parameters};
	if (~osc[\autoFreeze].class==OSCFunc, {~osc[\autoFreeze].free});
	~osc[\autoFreeze]=OSCFunc({arg msg, t;
		var frame=msg[3];
		if (t-time>freezeTrigTime, {
			~func[\UnFreezeBuf].value;//gebeurt dit al niet bij FreezeBuf?
			~func[\FreezeBuf].value(frame);
			time=t;
		});
	}, '/phase_onset');
};
//------------------------------------------------------------ GUI
~func[\freezeGUI]={arg id=0;//arg parameters, controlSpecs;
	var w, guis2;
	w=Window("Freeze functions", Rect(400,400,400,400)).front;
	w.addFlowLayout;
	w.alwaysOnTop_(true);
	w.front;
	parameters.sortedKeysValuesDo{|key,val|
		var slider=EZSlider;
		if (val.asArray.size>1, {slider=EZRanger});
		guis[key]=slider.new(w, 350@20, key, controlSpecs[key], {|ez|
			parameters[key]=ez.value;
			if (synth[idGUI]!=nil, {
				if (synth[idGUI].class==Synth, {
					if (synth[idGUI].isPlaying, {
						synth[idGUI].set(key, ez.value)
			})})});
		}, val);
	};
	[\freezeLiveOneShot, \unfreezeLiveOneShot, \freezeLiveOneShotNext].do{|i, k|
		var name=[\freezeTrig, \unfreezeTrig, \freezeunfreezeTrig][k];
		guis[i]=Button(w, 100@20).states_([[name],[name,Color.black, Color.yellow]]).action_{|b|
			if (b.value==1, {
				~func[i].value
			},{
				~osc[i].free
			})
		};
	};
	guis[\Freeze]=Button(w, 350@20).states_([ [\freeze],[\Freeze, Color.black, Color.red]]).action_{|b|
		var pars=parameters.deepCopy;
		if (b.value==0, {
			~func[\UnFreezeBuf].value(0)
		},{
			//frame= -1, pars, id=0;
			pars[\shiftDepth]=0;
			~func[\FreezeBuf].value(-1, pars)
		})
	};
	guis[\autoFreeze]=Button(w, 350@20).states_([ [\autoFreeze],[\autoFreeze, Color.black, Color.green]]).action_{|b|
		if (b.value==0, {
			//~func[\UnFreezeBuf].value;
			~osc[\autoFreeze].free;
		},{
			~func[\autoFreeze].value
		})
	};
	guis[\freezeTrigTime]=EZSlider(w, 350@20, \freezeTrigTime, ControlSpec(0.01, 10.0, \exp), {|ez| freezeTrigTime=ez.value}, freezeTrigTime);
	~win[\Freeze]=w;
	guis2=~synth[\FreezerMixer].makeGUI(w);
	guis2.keysValuesDo{|key,gui|
		guis[(\mixer++key).asSymbol]=gui
	};
	~gui[\Freeze]=guis;
};

{~func[\freezeGUI].value}.defer;

//------------------------------------------------------------ INIT
~func[\FreezeBuf].value;
1.0.wait;
~func[\UnFreezeBuf].value;//gebeurt dit al niet bij FreezeBuf?
