/*
target en bus inbouwen

~func[\bouncesTrig].value(parameters, settings)
~func[\bouncesLiveOneShot].value(parameters, settings)
~func[\bouncesLive].value(parameters, settings)
~osc[\bouncesLive].free

- per trigger door states/presets van bounces laten lopen. soort FSM
- goede beveiling inbouwen dmv min en max waarden van de deltatimes
- houd de gemiddelde deltatime tussen trigger bij zodat je de boucecurve kunt bijstellen
*/
//{arg id=0;
var bounceTrigTime=0.5;//minimal trig time between triggers
var bounceLatency=0.01;
var parameters=(bounces:[16,17], startT:[0.05,0.04], grow:[1.1,1.15], delayTime: [0.0,0.0], roundPitch: [0.0, 0.0], roundRhythm:[0,0], db: [[0.0,0.0], [-20,-10.0]], pitch:[[0,0],[0,0]], legato: [[1.0,1.0], [1.0,1.0]], az: [[-1.0,1.0], [1.0,1.0]], start: [[0.0,0.0], [0.0,0.0]], wet:[[0,0],[0,1.0]]);
var controlSpecs=(bounces: ControlSpec(4, 128, \exp, 1), startT: ControlSpec(0.01, 5.0, \exp), grow: ControlSpec(0.7, 1.3, \exp), db: ControlSpec(-60.0, 0, \lin, 1), pitch: ControlSpec(-60, 60, \cos, 0.01), legato: ControlSpec(0.01, 20.0, \exp), az: \bipolar.asSpec, start: ControlSpec(-1.0, 1.0), wet: ControlSpec(0.0, 1.0), delayTime: ControlSpec(0.0, 20.0, 4.0), roundPitch: ControlSpec(0.0, 12.0, 0, 0.5), roundRhythm: ControlSpec(0.0, 1.0));
var settings=(flipflop:0, overlap:false, repeatLast:false, mirror:false, bounceTrigTime: 0.5, latency: 0.0, reactOnPrev:true
	, onStartFunction:
	[{arg startFrame, latency, pars, sets;//par, frames=0, buffer=~buf[\Rec];
		~func[\clusterScore].value(nil, startFrame, ~buf[\Rec])},
	{arg startFrame, latency, pars, sets;
		nil}]
	, onEndFunction:{});
var fromToKeys=parameters.keys.asArray;
var scorePlayer=(), prev=(), startTimeOfScore=();
var guis=();
//-----------------------------------------------------------------------------
SynthDef(\PlayBufBounce, {arg bufnum, startFrame=0, rate=1.0, dur=0.2, az=0.0, amp=1.0, outBus=0, wet=0.0, wetBus;
	var env=EnvGen.ar(Env.perc(0.001, dur), doneAction:2);
	var out;
	out=PlayBuf.ar(1, bufnum, rate, 1, startFrame, 1)*env;
	out=PanAz.ar(~noc, out, az, amp);
	Out.ar(outBus, (1-wet).sqrt*out);
	Out.ar(wetBus, (wet).sqrt*out);
}).add;
//s.sync;

//-----------------------------------------------------------------------------
fromToKeys.removeAllSuchThat{|key|
	parameters[key].asArray.flat.size<=2;
};
//-----------------------------------------------------------------------------
~func[\bounceGUI]={arg name=\Bounce, buffer=~buf[\Rec];//arg parameters, controlSpecs;
	var keys1=List[], keys2=List[];
	var w, feedback=true;
	~par[name]=parameters.deepCopy;
	~setting[name]=settings.deepCopy;
	keys2=parameters.keys;
	keys2.removeAllSuchThat{|key|
		var flag=parameters[key].asArray.flat.size<=2;
		if (flag, {keys1.add(key)});
		flag
	};
	w=Window("Bounce", Rect(795, 179, 398, 610)).front;
	w.addFlowLayout;
	w.alwaysOnTop_(true);

	keys1.sort.do{|key|
		var val=~par[name][key];
		guis[key]=EZRanger(w, 350@20, key, controlSpecs[key], {|ez|
			~par[name][key]=ez.value
		}, val, false, 80).round2_(0.0001)
	};
	keys2.asArray.sort.do{|key|
		var val=~par[name][key];
		val.asArray.do{|val, i|
			var keyName=(key++i).asSymbol;
			guis[keyName]=EZRanger(w, 350@20, key++["_from","_to"][i], controlSpecs[key], {|ez|
				~par[name][key][i]=ez.value}, val, false, 100).round2_(0.0001)
	}};
	guis[\bouncesTrig]=Button(w, 100@20).states_([ ["Bounce trig"], ["Bounce trig", Color.black, Color.green] ]).action_{|b|
		if (b.value==1, {
			~func[\bouncesTrig].value(name, buffer);
			{b.value_(0)}.defer;
		});
	};
	guis[\bouncesLive]=Button(w, 100@20).states_([ ["Bounce live!"],
		["Bounce live!", Color.black, Color.green]
	]).action_{|b|
		if (b.value==1,
			{
				"MAKE FUNCTION BOUNCE LIVE".postln;
				~func[\bouncesLive].value(name, buffer)
			},{
				~osc[(\bouncesLive++name).asSymbol].free;
		})
	};
	guis[\bouncesLiveOneShot]=Button(w, 100@20).states_([["Bounce, once!"],["Bounce, once!",Color.black,Color.green]]).action_{|b|
		if (b.value==1, {
			~func[\bouncesLiveOneShot].value(name, buffer);
		});
	};
	guis[\minTrigTime]=EZSlider(w, 350@20, \minTrigTime, ControlSpec(0.01, 10.0, \exp), {|ez|
		~setting[name][\bounceTrigTime]=ez.value;
	}, ~setting[name][\bounceTrigTime], false, 100).round2_(0.0001);

	[\mirror, \overlap, \repeatLast, \reactOnPrev].do{|p|
		guis[p]=Button(w, 85@20).states_([ [p], [p, Color.black, Color.green] ])
		.action_{|b|
			~setting[name][p]=(b.value==1);
		}.value_(~setting[name][p].binaryValue);
	};
	//w.minimize;

	w.view.decorator.nextLine;
	~win[name]=w;
	~gui[name]=guis;
};

//-----------------------------------------------------------------------------
~func[\bouncesInits]={arg pars, sets;
	parameters.keys.asArray.difference(pars.keys.asArray).do{|key|
		pars[key]=parameters[key].deepCopy
	};
	settings.keys.asArray.difference(sets.keys.asArray).do{|key|
		sets[key]=settings[key].deepCopy
	};
	[pars, sets]
};
~func[\bouncesLive]={arg name=\Bounce, buffer;
	var ttime=0;
	var oscname=(\bouncesLive++name).asSymbol;
	"FUNCTION BOUNCE LIVE".postln;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg, time;
		var frames=msg[3];
		if (time-ttime>(~setting[name][\bounceTrigTime]??{bounceTrigTime}), {
			~func[\bounces].value(~par[name], ~setting[name], frames, buffer, name);
		});
		ttime=time;
	}, '/phase_onset');
};
~func[\bouncesLiveOneShot]={arg name=\Bounce, buffer;
	var oscname=(\bouncesLiveOneShot++name).asSymbol;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg;
		var frames=msg[3];
		~func[\bounces].value(~par[name], ~setting[name], frames, buffer, name);
		{guis[\bouncesLiveOneShot].value_(0)}.defer;
	}, '/phase_onset').oneShot;
};
~func[\bouncesTrig]={arg name=\Bounce, buffer;
	var oscname=(\bouncesLiveOneShot++name).asSymbol;
	if (~osc[oscname].class==OSCFunc, {~osc[oscname].free});
	~osc[oscname]=OSCFunc({arg msg;
		var frames=msg[3];
		~func[\bounces].value(~par[name], ~setting[name], frames, buffer, name);
	}, '/phase_request').oneShot;
	~synth[\RecWithInformation].set(\t_trig, 1);
};

//-----------------------------------------------------------------------------
~func[\bouncesScore]={arg argpars, argsets, frames=0, buffer=~buf[\Rec], name=\Bounce, target=~synth[\BounceMixer], outBus=~bus[\Bounce], wetBus=~bus[\BounceVerb];
	var pars, sets;
	var p=(), score=List[], time=Main.elapsedTime, bounceArray, numFrames;
	var delta, lastIndex, startVal=(), rangeCopy;
	var bounces, startT, grow, delayTime,latency, startFrame, endFrame;
	var initDelayTime=0.0;
	//------------------------------------------------------------------------
	#pars, sets=~func[\bouncesInits].value(argpars.copy, argsets.copy);
	bounces=pars[\bounces];
	startT=pars[\startT];
	grow=pars[\grow];
	delayTime=pars[\delayTime];
	latency=sets[\latency]??{bounceLatency};
	//------------------------------------------------------------------------
	bounces=rrand(bounces[0],bounces[1]).round(1.0).asInteger;
	startT=rrand(startT[0],startT[1]);
	grow=rrand(grow[0],grow[1]);
	delayTime=rrand(delayTime[0],delayTime[1]);
	//------------------------------------------------------------------------
	if (sets[\reactOnPrev], {
		if (prev[name]!=nil, {
			delta=(Main.elapsedTime-startTimeOfScore[name]);
			//scorePlayer.isPlaying2.post;
			lastIndex=prev[name][\startTimes].indexInBetween(delta).ceil;
			pars.keysDo{|key|
				if(prev[name][key].size>0, {
					startVal[key]=prev[name][key].clipAt(lastIndex)
				});
			};
			startT=(prev[name][\startTimes].clipAt(lastIndex)
				-
				prev[name][\startTimes].clipAt(lastIndex-1)).clip(0.05, 1.0);
			if ( (prev[name][\grow]>1.0)==(grow>1), {
				grow=grow.reciprocal;
			});
			if ((startT<0.1)&&(grow<1.0), {grow=grow.reciprocal});
			if ((startT>0.5)&&(grow>1.0), {grow=grow.reciprocal});
			sets[\flipflop]=sets[\flipflop]+1%2;
			if (sets[\flipflop]==1, {
				rangeCopy=rangeCopy.collect{|v| v.reverse};
			});
		});
	});
	//------------------------------------------------------------------------
	if (sets[\overlap].not, {
		if (scorePlayer[name].class==Score, {
			if (scorePlayer[name].isPlaying2, {
				scorePlayer[name].stop
			});
		})
	});
	//------------------------------------------------------------------------
	pars[\deltaTimes]=Array.geom(bounces, startT, grow).clip(0.05, 2.0);
	if (sets[\mirror]==1, {pars[\deltaTimes]=pars[\deltaTimes].mirror});//mirror kan ook random zijn!
	bounceArray=Array.geom(bounces, 1, grow).integrate.normalize;
	//------------------------------------------------------------------------
	fromToKeys.do{arg key;
		var val;
		val=pars[key];
		val=val.collect{|v| rrand(v[0],v[1])};
		val[0]=startVal[key]??{val[0]};
		val=bounceArray.range(val[0], val[1]);
		//if(sets[\mirror]==1, {val=val.mirror});
		pars[key]=val;
	};
	//------------------------------------------------------------------------
	if (sets[\repeatLast] && sets[\reactOnPrev], {
		if (pars[\deltaTimes].last>0.05, {
			pars=pars.collect{|array|
				array=array++array.last.dup(40)
			}
		})
	});
	//------------------------------------------------------------------------
	pars[\startTimes]=(pars[\deltaTimes].integrate)+latency-pars[\deltaTimes][0];

	initDelayTime=pars[\deltaTimes][0].postln;

	pars[\grow]=grow;
	prev[name]=pars.deepCopy;
	//------------------------------------------------------------------------

	numFrames=buffer.numFrames;

	pars[\startTimes].size.do{|i|
		var frame;
		score.add([
			pars[\startTimes][i]+initDelayTime
			, [\s_new, \PlayBufBounce
			, -1, 2,
			~synth[\BounceMixer].nodeID
			, \bufnum, buffer.bufnum
			, \rate, pars[\pitch][i].midiratio//.round(p[\roundPitch][0])
			, \amp, pars[\db][i].dbamp
			, \dur, pars[\legato][i]*pars[\deltaTimes][i]
			, \az, pars[\az][i]
			, \startFrame, frame=((delayTime*s.sampleRate)
				+frames+
				(pars[\start][i]*s.sampleRate*pars[\startTimes][i])).wrap(0,numFrames)
			, \wet, pars[\wet][i]
			, \outBus, ~bus[\Bounce].index
			, \wetBus, ~bus[\BounceVerb].index
		]]);
		if (i==0, {startFrame=frame});
		if (i==(pars[\startTimes].size-1), {endFrame=frame});
	};
	/*
	if (sets[\onStartFunction]!=nil, {
	sets[\onStartFunction].asArray.do{|func|
	var extraScore=func.value(
	startFrame, pars[\startTimes][0], pars, sets);
	if (extraScore.class==List, {
	extraScore.do{|sc|
	score.add(sc)
	}
	})
	};
	});
	*/
	score
};
//pars, sets, frames=0, buffer=~buf[\Rec], name=\Bounce;
~func[\bounces]={arg pars, sets, frames=0, buffer=~buf[\Rec], name=\Bounce;
	var score;
	score=~func[\bouncesScore].value(pars, sets, frames, buffer, name);
	//------------------------------------------------------------------------
	score = score.sort({ arg a, b; b[0] >= a[0] });
	scorePlayer[name]=Score.play(score, s);
	startTimeOfScore[name]=Main.elapsedTime;
	//------------------------------------------------------------------------
	//[pars.deepCopy, scorePlayer, startTimeOfScore].post;
};