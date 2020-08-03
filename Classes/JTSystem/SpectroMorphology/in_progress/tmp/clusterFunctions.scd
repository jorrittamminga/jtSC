/*
target en bus inbouwen
TODO:
- reverse evt inbouwen
- eventueel: stretch / round laten verlopen met een envelope oid binnen de synthdef
- ook een non-realtime versie maken!!!! dan maakt die hele 'foutcorrectie' mbt rate ook niet meer uit. De emmer dus. Met als variabele de Buffer en evt startFrame
*/
var clusterTrigTime=0.5;
var spectrum=~bellSpectrum.deepCopy;
var ratiosSorted=~bellSpectrum.deepCopy[\ratios].sort;
var cs=(clusterTrigTime:ControlSpec(0.01, 10.0, \exp), transpose: ControlSpec(-48, 48, 0, 12), startTimes:ControlSpec(0.0, -1.0), db: ControlSpec(-60.0, 0, \lin, 1), az: \bipolar.asSpec, delayTime: ControlSpec(0.001, 10.0, \exp), dur:ControlSpec(0.01, 10.0), wet:\amp.asSpec, stretch: ControlSpec(0.01, 10.0, \exp), range: ControlSpec(0, spectrum[\ratios].size-1, 0, 1), fundamental: ControlSpec(0, spectrum[\ratios].size-1, 2, 1), ampPow: ControlSpec(0.0, 2.0), round: ControlSpec(0.0, 12.0, 0, 1), sync: ControlSpec(0.0, 1.0), ratePow: ControlSpec(0.0, 1.0), attack: ControlSpec(0.0, 1.0), latency: ControlSpec(0.0, 10.0));
var p=(transpose: 0, startTimes:[0.0, 0.0], db: [0.0, -10.0], az: [-1.0, 1.0], delayTime: [0.001, 0.001], dur:[1.0,2.0], wet:[0,0], stretch:1.0, range: [0, 16], fundamental: 0, ampPow: 1.0, round: 0.0, sync: 0.0, ratePow: 1.0, attack: 0.0, latency:0.0, clusterTrigTime:0.5);
var init=true;
	var guis=();
//---------------------------------------------------------------------------- SYNTHDEF
SynthDef(\PlayBufCluster, {arg bufnum, startFrame=0, rate=1.0, at=0.001, rt=0.2, az=0.0, amp=1.0, outBus=0, wet=0.0, wetBus;
	var env=EnvGen.ar(Env.perc(at, rt, 1.0, [4.0, -4.0]), doneAction:2);
	var out;
	out=PlayBuf.ar(1, bufnum, rate, 1, startFrame, 1)*env;
	out=PanAz.ar(~noc, out, az, amp);
	Out.ar(outBus, (1-wet).sqrt*out);
	Out.ar(wetBus, (wet).sqrt*out);
}).add;
//s.sync;
//------------------------------------------------------------------------------ FUNC
~func[\clusterScore]={arg par, frames=0, buffer=~buf[\Rec]
	, target=~synth[\ClusterMixer], outBus=~bus[\Cluster], wetBus=~bus[\ClusterVerb];
	var score=List[], r, numFrames, voices;
	var rate, dur, rates, durs, delayTimes, offsets;
	par=par??{~par[\Cluster]};
	numFrames=buffer.numFrames;
	rates=(spectrum[\ratios].deepCopy.copyRange(par[\range][0].asInteger, par[\range][1].asInteger)/ratiosSorted[par[\fundamental]]).pow(par[\stretch]);
	rates=rates*par[\transpose].midiratio;
	rates=rates.ratiomidi.round(par[\round]).midiratio;
	durs=rates.collect{|rate| exprand(par[\dur][0],par[\dur][1])*(rate.pow(par[\ratePow].neg) )};
	delayTimes=durs.collect{|dur, i|
		((durs.maxItem-dur)*par[\sync])//.max( (rates[i].abs-1).max(0)*dur )
	};
	offsets=delayTimes.collect{|time, i|
		(((rates[i].abs-1).max(0)*durs[i]) - time).max(0)
	};
	rates.do{|rate, i|
		var dur=durs[i];
		score.add([
			(exprand(par[\delayTime][0],par[\delayTime][1])-par[\delayTime][0]
				//+latency par[\latency]
				+delayTimes[i]
			)
			, [\s_new, \PlayBufCluster
				//, -1, 1, 1
				, -1, 2,
				~synth[\ClusterMixer].nodeID//target.nodeID
				, \bufnum, buffer.bufnum
				, \rate, rate//=rrand(par[\pitch][0],par[\pitch][1]).midiratio
				, \amp, rrand(par[\db][0],par[\db][1]).dbamp
				, \dur, durs[i]//dur=exprand(par[\dur][0],par[\dur][1])*(rate.reciprocal)
				, \az, rrand(par[\az][0],par[\az][1])
				, \startFrame,
				(
					(
						rrand(par[\startTimes][0],par[\startTimes][1])
						- offsets[i]
						//- ((rate.abs-1).max(0)*durs[i])
					)*s.sampleRate
					+frames
				).wrap(0, numFrames)
				, \at, dur*par[\attack]+0.001
				, \rt, dur*(1-par[\attack])+0.001
				, \wet, rrand(par[\wet][0],par[\wet][1])
				//, \outBus, ~bus[\Out].index, \wetBus, ~bus[\GVerb].index
				, \outBus, ~bus[\Cluster].index
				, \wetBus, ~bus[\ClusterVerb].index
		]]);
	};
	score
};
~func[\cluster]={arg par, frames, buffer=~buf[\Rec];
	var score=~func[\clusterScore].value(par, frames, buffer);
	"CLUSTER time".postln;
	Score.play(score, s);
};
//------------------------------------------------------------------------------ GUI
~func[\clusterGUI]={arg name=\Cluster, buffer;
	var w=Window("Cluster_"++name, Rect(400,400,400,500));
	~par[name]=~par[name]??{p.deepCopy};
	w.addFlowLayout;
	w.alwaysOnTop_(true);
	~par[name].sortedKeysValuesDo{|key,val|
		var c=cs[key];
		var slider=EZSlider;
		if (val.asArray.size>1, {slider=EZRanger});
		guis[key]=slider.new(w, 350@20, key, c, {|ez| ~par[name][key]=ez.value}, val, false, 100);
	};
	//-------------------------------------------------------------- EXTRA
	guis[\clusterTrig]=Button(w, 100@20).states_([ ["Cluster trig"],["Cluster trig",Color.black, Color.green]  ]).action_{|b|
		if (b.value==1, {
			"gui cluster trig".postln;
			~func[\clusterTrig].value(name, buffer);
			{"zet maar op 0".postln; guis[\clusterTrig].value_(0)}.defer;
		})
	};
	guis[\clusterLive]=Button(w, 100@20).states_([ ["Cluster live!"],
		["Cluster live!", Color.black, Color.green]
	]).action_{|b|
		if (b.value==1,
			{
				~func[\clusterLive].value(name, buffer)
			},{
				~osc[(\clusterLive++name).asSymbol].free;
		})
	};
	guis[\clusterLiveOneShot]=Button(w, 100@20).states_([ ["Cluster, once!"],["Cluster, once!",Color.black, Color.green]])
	.action_{|b|
		if (b.value==1, {
		"gui cluster oneshot".postln;
		~func[\clusterLiveOneShot].value(name, buffer)
		});
	};

	guis[\clusterBufnum]=PopUpMenu(w, 100@20).items_(["bufRec", "rec0", "rec1", "rec2", "rec3"]).action_{|b|
		buffer=[~buf[\Rec], ~buf[\RecOverdub][0], ~buf[\RecOverdub][1],~buf[\RecOverdub][2], ~buf[\RecOverdub][3]][b.value]
	};

	/*
	guis[\minTrigTime]=EZSlider(w, 350@20, \minTrigTime, ControlSpec(0.01, 10.0, \exp)
	, {|ez|
	clusterTrigTime=ez.value;
	}, clusterTrigTime, false, 100).round2_(0.0001);
	*/
	//-------------------------------------------------------------- EXTRA
	w.view.decorator.nextLine;
	~win[name]=w;
	~gui[name]=guis;
	w.minimize;
};

//------------------------------------------------------------------------------ OSC
~func[\clusterLive]={arg name=\Cluster, buffer;//~func[\clusterLive].value(\name)
	var ttime=0;
	var oscname=(\clusterLive++name).asSymbol;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg, time;
		var frames=msg[3];
		"cluster live".postln;
		if (time-ttime>(~par[name][\clusterTrigTime]??{0.5}), {
			~func[\cluster].value(~par[name], frames, buffer);
		});
		ttime=time;
	}, '/phase_onset');
};

~func[\clusterLiveOneShot]={arg name=\Cluster, buffer;
	var oscname=(\clusterLiveOneShot++name).asSymbol;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg;
		var frames=msg[3];
		"cluster oneshot".postln;
		~func[\cluster].value(~par[name], frames, buffer);
		{guis[\clusterLiveOneShot].value_(0)}.defer;
	}, '/phase_onset').oneShot;
};

~func[\clusterTrig]={arg name=\Cluster, buffer;
	var oscname=(\clusterTrig++name).asSymbol;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg;
		var frames=msg[3];
		"cluster trig phase request".postln;
		~func[\cluster].value(~par[name], frames, buffer);
	}, '/phase_request').oneShot;
	~synth[\RecWithInformation].set(\t_trig, 1);
};
//------------------------------------------------------------------------------