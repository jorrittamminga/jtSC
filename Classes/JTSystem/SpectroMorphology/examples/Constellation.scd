//----------------------------------------------------------------------------- FLOWS
/*
- mixer inbouwen die ook naar andere fx te sturen dan de mainoutput
-
- soort cluster maar meer sustain, lange grains wellicht, die RecOverdubs spelen
- deze flarden kunnen ook alle kanten om geboetseerd worden (glissandi, filtering, etc)
- vloeibaar
- constellatie van 'sterren', deze Constellation kun je roteren, omdraaien, etc
- parameters: rate, amp, az, (eventueel bins)
//rotate/tilt/etc, zoom, shift / modulation (nieuw akkoord/spectrum). lfo, line, lfo/line
*/
var maxPartials=16, x;
SynthDef(\PlayBufCF, {arg bufnum, fadeIn=1.0, fadeOut=3.0, gate=1, xfadeTime=1.0, az=0.0, amp=1.0, rate=1.0, startPos=0, outBus, azDev=0.0, rateDev=0.0, ampDev=0.0, tDev=0.0;
	var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut),gate, doneAction:2);
	var out, trigger;
	rate=rate.lag(0.1);
	//trigger=TDuty.kr( (BufDur.ir(bufnum)*rate).reciprocal*Dwhite(0.25, 1.0), 0, tDev );
	trigger=Dust.kr( BufDur.ir(bufnum).reciprocal*rate, tDev );
	out=PlayBufCF.ar(1, bufnum, rate, trigger, startPos, 1, xfadeTime)*env;
	//PlayBufAlt
	Out.ar(outBus, PanAz.ar(~noc, out, (az+azDev).wrap(-1.0, 1.0), amp))
}).add;
s.sync;
"turn on".postln;
x=Synth(\PlayBufCF, [\amp, 0.0]);
s.sync;
x.set(\fadeOut, 0, \gate, 0);
"turn off".postln;

~task[\Constellation]=(ratios:1, azs:1, amps:1);
~synth[\Constellation]=();
~bus[\Constellation]=();
~bus[\Constellation][\outBus]=Bus.audio(s, ~noc);
~synth[\ConstellationMixer]=Synth.before(~synth[\GVerb], \SubMixer2Bins, [
	\inBus, ~bus[\Constellation][\outBus], \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb]
	, \amp, 1.0, \lpfWet, 4000, \wet, 0.0]).register;

~func[\MakeConstellation]={arg buffer, args, name=\Constellation;
	{
		var n, ratios, amps, azs;
		var pars;
		~func[\StopConstellation].value(0, name);
		~bus[name]=~bus[name]??{()};
		~bus[name][\outBus]=~bus[name][\outBus]??{var b=Bus.audio(s, ~noc); s.sync; b};
		[\rate, \az, \amp, \ratelfo, \azlfo, \amplfo].do{|p,i|
			var channels=[16,16,16,1,1,1][i];
			~bus[name][p]=~bus[name][p]??{var b=Bus.control(s, channels);//maxPartials
				s.sync; b.set(0); b}};
		if (args!=nil, {
			if (args[\outBus]==nil, {

				~synth[\ConstellationMixer]=~synth[\ConstellationMixer]??{
					var syn;
									"make a new constellation mixer".postln;
					syn=Synth.before(~synth[\GVerb], \SubMixer2Bins, [
						\inBus, ~bus[\Constellation][\outBus], \outBus, ~bus[\Out]
						, \wetBus, ~bus[\GVerb]
						, \amp, 1.0, \lpfWet, 4000, \wet, 0.0]).register;
					s.sync;
					syn;
				};
		})});
		pars=(outBus: ~bus[\Constellation][\outBus]
			, rate:~bellSpectrum[\ratios].copyRange(0, 7)
			, amp: ~bellSpectrum[\dbs].copyRange(0, 7).dbamp
			, az: Array.rand(8, -1.0, 1.0).normalize.range(-1.0, 1.0)
			, stretch:1.0, shift:1.0, ampPow:1.0, azMean:0.0, azSpread: 1.0
			, fadeIn:1, fadeOut:3.0, gate:1, xfadeTime:1.0, startPos:0, tDev: 0.0
			, target:~synth[\ConstellationMixer]);
		if (args!=nil, {
			args.keysValuesDo{|key,val| pars[key]=val};
		});
		~par[name]=pars;
		n=pars[\rate].size;
		ratios=pars[\rate].pow(pars[\stretch])*pars[\shift];
		amps=pars[\amp].pow(pars[\ampPow]);
		azs=pars[\az]*pars[\azSpread]+pars[\azMean];
		~bus[name][\rate].setn(ratios);
		~bus[name][\amp].setn(amps);
		~bus[name][\az].setn(azs);

		~synth[name][\Cluster]=n.collect{|i|
			var synth=Synth.before(pars[\target], \PlayBufCF, [
				\rate, ratios[i]
				, \amp, amps[i]
				, \az, azs[i]
				, \bufnum, buffer
				, \startPos, buffer.numFrames.rand
			]
			++pars.asKeyValuePairs
			).register;
			s.sync;
			synth.map(\rate, ~bus[name][\rate].index+i,
				\amp, ~bus[name][\amp].index+i,
				\az, ~bus[name][\az].index+i,
				\ratelfo, ~bus[name][\ratelfo].index,
				\amplfo, ~bus[name][\amplfo].index,
				\azDev, ~bus[name][\azlfo].index
			);
			synth
		};

	}.fork
};

~func[\StopConstellation]={arg fadeOut=10.0, name=\Constellation;
	fadeOut=fadeOut.asArray;
	~synth[name][\Cluster].do{|synth, i| if (synth.isPlaying, {
		synth.set(\fadeOut, fadeOut.wrapAt(i), \gate, 0)})};
};

~func[\TransformConstellation]={arg parameter=\rate, value=[1.0, 1.0], time=10.0, constellation=\Constellation;
	var values, synthDef=\SXLine;
	switch(parameter, \rate, {
		~par[\stretch]=value[0];
		~par[\shift]=value[1];
		values=~par[constellation][\rate].deepCopy.pow(~par[\stretch])*~par[\shift];
		synthDef=\SXLine;
	}, \amp, {
		~par[\ampPow]=value;
		values=~par[constellation][\amp].deepCopy.pow(~par[\ampPow]);

		values=values*(values.sum.sqrt.reciprocal.postln);

		synthDef=\SXLine;
	}, \az, {
		~par[\azSpread]=value[0];
		~par[\azMean]=value[1];
		values=~par[constellation][\az].deepCopy*~par[\azSpread] + ~par[\azMean];
		synthDef=\SLine;
	});
	~func[\TransformConstellationLine].value(parameter, values, time, constellation, synthDef);
};

~func[\SetConstellation]={arg parameter=\rate, values=[1.0, 1.0], time=10.0, constellation=\Constellation, method='wrapAt';
	var synthDef=\SXLine;
	values=~synth[constellation][\Cluster].size.collect{|i| values.wrapAt(i)};
	~par[constellation][parameter]=values;
	switch(parameter, \rate, {
		synthDef=\SXLine;
	}, \amp, {
		synthDef=\SXLine;
	}, \az, {
		synthDef=\SLine;
	});
	~func[\TransformConstellationLine].value(parameter, values, time, constellation, synthDef);
};

~func[\ModulateConstellation]={arg par=\azlfo, freq=0.25, synthDef=\Saw, constellation=\Constellation;
	~func[\StopModulateConstellation].value(par, 1.0, constellation);
	~synth[constellation][par]=Synth.head(s, synthDef, [\outBus, ~bus[constellation][par], \freq, freq]).register;
};

~func[\StopModulateConstellation]={arg par=\azlfo, fadeOut=1.0, constellation=\Constellation;
	if (~synth[constellation][par]!=nil, {
		~synth[constellation][par].do{|synth|
			if (synth.class==Synth, {
				if (synth.isPlaying, {synth.set(\fadeOut, fadeOut, \gate, 0)})
			})
		};
	});
};

~func[\TransformConstellationLine]={arg par=\amp, values, time=1.0, constellation=\Constellation, synthDef=\SXLine;
	time=time.asArray;
	if (~synth[constellation][par]!=nil, {
		~synth[constellation][par].do{|synth|
			if (synth.class==Synth, {
				if (synth.isPlaying, {synth.free})
			})
		};
	});
	~bus[constellation][par].getn(~synth[constellation][\Cluster].size, {
		arg current;
		var bus=~bus[constellation][par].index;
		~synth[constellation][par]=~synth[constellation][\Cluster].collect{|synth,i|
			var c;
			if ((current[i]-values[i]).abs>0.000001, {
				c=Synth.before(synth
					, synthDef, [\outBus, bus+i, \start, current[i]
						, \end, values[i], \time, time.wrapAt(i)]).register
			})
		};
	});
};

~func[\constellationGUI]={
	var guis=();
	var w=Window("Constellation", Rect(400,400,400,200)).front;
	w.addFlowLayout;
	guis=~synth[\ConstellationMixer].makeGUI(w);
	w.alwaysOnTop_(true);
	~gui[\Constellation]=guis;
	~win[\Constellation]=w;
};

{~func[\constellationGUI].value}.defer;

/*

rate:~bellSpectrum[\ratios].copyRange(0, 7)
			, amp: ~bellSpectrum[\dbs].copyRange(0, 7).dbamp
			, az: Array.rand(8, -1.0, 1.0).normalize.range(-1.0, 1.0)
			, stretch:1.0, shift:1.0, ampPow:1.0, azMean:0.0, azSpread: 1.0
			, fadeIn:1, fadeOut:3.0, gate:1, xfadeTime:1.0, startPos:0, tDev: 0.0

~func[\MakeConstellation].value(~buf[\RecOverdub][0], (rate: {[0,-7,7,-12].choose.midiratio}!16, amp: 0.5!8, tDev: 1.0))

~func[\SetConstellation].value(\rate, {[0, 12,-12,-24].choose.midiratio}!16, 2);

~func[\MakeConstellation].value(~buf[\RecOverdub][0], (tDev: 1.0))

~func[\TransformConstellation].value(\rate, [0.01, 0.5], Array.rand(16, 5.0, 10.0));
~func[\TransformConstellation].value(\amp, 0.1, 1);
~func[\TransformConstellation].value(\az, [1.0, 0.5], 4);
~func[\ModulateConstellation].value(\azlfo, 0.25, \Saw);
~func[\StopModulateConstellation].value(\azlfo, 2.0)


~func[\SetConstellation].value(\rate, {[0,7,14].choose.midiratio}!16, Array.rand(8,5.0,7.0));
~func[\SetConstellation].value(\rate, {[-12].choose.midiratio}!16, 10);
~func[\StopConstellation].value(Array.rand(8, 12.0, 20.0))


~func[\StopConstellation].value(Array.rand(16, 1.0, 5.0))

~bus[\Constellation]
~win[\Constellation].alwaysOnTop_(true)
*/