in=>MapperJT
out=>MapperJT

{
	ugenGraphFunc={
		var val=();
		val[\in]=();
		val[\out]=();
		val[\in][\sig]=In.kr(mapperIn.bus.index, mapperIn.bus.numChannels);
		if (mapperIn.lag!=nil) {
			val[\in][\lag]=NamedControl((mapperIn.name++"_lag").asSymbol, mapperIn.lag, \control);
			val[\in][\sig]=val[\in][\sig].lag(val[\in][\lag]);
		};
		[mapperIn, mapperOut].do{|mapper,i|
			var ding=[\in,\out][i];
			[\minval, \maxval].do{|key,j|
				val[ding][key]=NamedControl((mapper.name++"_"++key).asSymbol, mapper.default, \control);
			}
		};
		val[\out][\sig]=val[\in][\sig].performList(MapperInOutJT.uGen, [ val[\in][\minval], val[\in][\maxval], val[\out][\minval], val[\out][\maxval] ]);
		if (mapperOut.lag!=nil) {
			val[\out][\lag]=NamedControl((mapperOut.name++"_lag").asSymbol, mapperOut.lag, \control);
			val[\out][\sig]=val[\out][\sig].lag(in[\lag]);
		};
		Out.kr(mapperOut.bus, val[\out][\sig])
	};
}

SynthDef(

//------------------------------------------------------------------------------------------------------------------------------------------------

m[\loudness]=Mapper2JT([0, 120, \db].asSpec, \loudness).makeBus;
m[\amp]=Mapper2JT(\amp.asSpec, \amp).makeBus;
m[\freq]=Mapper2JT(\freq.asSpec, \freq).makeBus;
m[\pitch]=Mapper2JT(\freq.asSpec, \pitch).makeBus;


x=Synth(\Analyzer, [\outBusPitch, m[\pitch].bus, \outBusLoudness, m[\loudness].bus]);
y=Synth(\FX, [\amp, 0.0, \freq, 440]);

// mapper: target  source     target    source.... (to..from)
z=y.mapJT(m[\freq], m[\pitch], m[\amp], m[\loudness]);
//.mapJT outputs a MapperInOutJT (or an array)


MapperSynthJT {
	var <target, <addAction, <mapperIn, <mapperOut, <inBus, <outBus;
	var <server, <synthDef, <synth;

	*new{arg target, addAction=\addBefore, mapperIn, mapperOut, inBus, outBus;
		^super.newCopyArgs(target, addAction, mapperIn, mapperOut, inBus, outBus).init
	}
	init {
		target = target.asTarget;
		server = target.server;
		if (mapperIn.class!=Array) {mapperIn=[mapperIn]};
		if (mapperOut.class!=Array) {mapperOut=[mapperOut]};
		inBus=inBus??{inBus=(); this.makeBus(mapperIn, inBus);};
		outBus=outBus??{outBus=(); this.makeBus(mapperOut, outBus);};
		synth=this.makeSynth;
	}
	makeBus {arg mapperArray, bus;
		var numChannels;
		mapperArray.do{|mapper| bus[mapper.name]=Bus.control(server, mapper.default.size);};
		^bus
	}
	makeSynth {arg rate=\control;
		synthDef=SystemSynthDefs.generateTempName;
		^SynthDef(synthDef, {
			var uGens=();
			in=In.kr();
			[\in, \out].do{|key,i|
				var mapper=[mapperIn,mapperOut][i];
				var name=mapper.name;
				[\minval, \maxval].do{|key2|
					var rangeName=(name++key2).asSymbol;
					uGens[rangeName]=NamedControl(rangeName, mapper.perform(key2), rate);
				};
				if (i==0) {
					if (mapper.lag!=nil) {
						var lagName=(mapper.name++\Lag).asSymbol;
						uGens[lagName]=NamedControl(lagName, mapper.lag, rate);
						uGens[mapper.name]=NamedControl(mapper.name, mapper.default, rate, nil
							, spec:mapper.asSpec).lag(*uGens[lagName]);
					} {
						uGens[mapper.name]=NamedControl(mapper.name, mapper.default, rate, spec:mapper.asSpec);
					};
				} {
					uGens[mapper.name]=uGens[mapperIn.name].performList(uGen
						, [
							uGens[(mapperIn.name++\minval).asSymbol]
							, uGens[(mapperIn.name++\maxval).asSymbol]
							, uGens[(mapperOut.name++\minval).asSymbol]
							, uGens[(mapperOut.name++\maxval).asSymbol]
							, \minmax]);
					if (mapper.lag!=nil) {
						var lagName=(mapper.name++\Lag).asSymbol;
						uGens[lagName]=NamedControl(lagName, mapper.lag, rate);
						uGens[mapper.name]=uGens[mapper.name].lag(*uGens[lagName])
						//NamedControl(mapper.name, mapper.default, rate, nil
						//, spec:mapper.asSpec).lag(*uGens[lagName]);
					} {
						//uGens[mapper.name]=uGens[\out]
						//NamedControl(mapper.name, mapper.default, rate, spec:mapper.asSpec);
					};
				}
			};
			uGens;
			Out.kr();
		}).add.play(target, args, addAction)
	}
	update {arg changer, what ...moreArgs;
		synth.free;
		synth=this.makeSynth
	}
}


[ \freq, 100, \rq, 0.1, \amp, 0.0 ].clump(2).flop

f={arg ... args;

	args.postln;
}

f.value(\freq, 100, \rq, 0.1, \amp, 0.0)


x=[1,2,3]; y=1;
x.isArray || y.isArray