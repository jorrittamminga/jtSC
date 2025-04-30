/*
+Synth {
	mapJT { arg ... args;
		//var mapInOut=args.clump(2).flop;
		//^MapperInOutJT(mapInOut[0], mapInOut[1]).makeSynth(this, \addBefore)
	}
}
*/
MapperInOutJT {
	var <>mapperIn, <>mapperOut;
	var <uGen, <uGeni, <curvei, <mapFunc;
	var <mapMethod;
	var <synthDef, <synth, <target, <addAction, <inBus, <outBus;

	*new {arg mapperIn, mapperOut;
		if ( (mapperIn.isArray) || (mapperOut.isArray)) {
			^(mapperIn.size.max(mapperOut.size).collect{|i| super.new.initArray(mapperIn.wrapAt(i), mapperOut.wrapAt(i) )})
		} {
			^super.newCopyArgs(mapperIn, mapperOut).init
		}
	}
	initArray {arg argIn, argOut;
		mapperIn=argIn;
		mapperOut=argOut;
		this.init;
	}
	init {
		uGeni=(in: \lin, out: \lin);
		curvei=(in: 0, out: 0);
		[mapperIn,mapperOut].do{|mapper,i|
			//if (mapper.class!=Array) {mapper=[mapper]};
			mapper.addDependant(this);
			this.initFunc(mapper, [\in,\out][i])
		};
	}
	update {arg changer, what ...moreArgs;
		[mapperIn,mapperOut].do{|mapper,i|
			this.initFunc(mapper, [\in,\out][i])
		};
	}
	initFunc {arg mapper, type;
		var warpName;
		warpName=mapper.warp.asSpecifier;
		if (warpName.isKindOf(SimpleNumber)) {
			curvei[type]=warpName.copy;
			warpName=\curve;
		} {
			if ([\linear, \exponential].includesEqual(warpName)) {
				warpName=(linear:\lin, exponential: \exp)[warpName];
			}
		};
		uGeni[type]=warpName;
		uGen=(uGeni[\in]++uGeni[\out]).asSymbol;
		mapMethod=uGen.copy;
		if (MapperMethodJT.mappingMethods.keys.includesEqual(mapMethod)) {
			mapMethod=MapperMethodJT.mappingMethods.at(mapMethod).new(mapperIn,mapperOut);
		} {
			mapMethod=MapperMethodJT.new(mapperIn,mapperOut);
		};
		if (UGen.findRespondingMethodFor(uGen)==nil) {
			"uGen method ".post; uGen.post; " does not exist....".postln;
		};
	}
	map {arg value;
		^mapMethod.map(value);
	}
	mapFast {arg value;
		^mapperOut.mapNoClip(mapperIn.unmap(value))
	}
	makeMapperSynthJT {arg target, addAction=\addBefore;
		target = target.asTarget;
		//server = target.server;

		synth=SynthDef(\blablanewName, {
			var in, out;
			/*
			in[\pitch]=In.kr(bus_pitch, bus_numChannels);
			if (in_lag) { };
			out[\freq]=\freq.kr.linexp( \inmin, \inmax, \outmin, \outmax);
			if (out_lag) {out[\freq]=out[\freq].lag(out_lagU, out_lagD)};
			Out.kr(out_bus, out[\freq])
			*/
		}).add.play;
	}
	//synth
	namedControls {arg rate=\control, synthDef;
		var uGens=();
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
		^uGens
	}
}
//Warp
MapperMethodJT {
	classvar <>mappingMethods;
	//var <>mapperInOut;
	var <>mapperIn, <>mapperOut;
	//var <inMin, <inMax, <outMin, <outMax, <curve;
	*initClass {
		// support Symbol-asWarp
		mappingMethods = IdentityDictionary[
			\linlin -> MapperLinLinJT
			, \linexp -> MapperLinExpJT
			, \explin -> MapperExpLinJT
			, \expexp -> MapperExpExpJT
			, \lincurve -> MapperLinCurveJT
			, \curvelin -> MapperCurveLinJT
		];
	}
	*new {arg mapperIn, mapperOut;
		^super.newCopyArgs(mapperIn, mapperOut);
	}
	map { arg value; ^mapperOut.mapNoClip(mapperIn.unmap(value)) }
}
MapperLinLinJT : MapperMethodJT {
	map {arg value;
		^value.linlin(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, \minmax)
	}
}
MapperLinExpJT : MapperMethodJT {
	map {arg value;
		^value.linexp(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, \minmax)
		//^value.linexp(inMin, inMax, outMin, outMax, \minmax)
	}
}
MapperExpLinJT : MapperMethodJT {
	map {arg value;
		^value.explin(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, \minmax)
	}
}
MapperExpExpJT : MapperMethodJT {
	map {arg value;
		^value.expexp(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, \minmax)
	}
}
MapperLinCurveJT : MapperMethodJT {
	map {arg value;
		^value.lincurve(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, mapperOut.curve
			, \minmax)
	}
}
MapperCurveLinJT : MapperMethodJT {
	map {arg value;
		^value.expexp(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, mapperIn.curve
			, \minmax)
	}
}