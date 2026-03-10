//TODO: make a MapperInOutJTArray version
MapperIOJT {
	var <>mapperIn, <>mapperOut;
	var <uGen, <uGeni, <curvei, <mapFunc;
	var <mapMethod;
	var <synthDef, <>synth, <>target, <>addAction, <inBus, <outBus;

	*new {arg mapperIn, mapperOut;
		^if ( (mapperIn.isArray) || (mapperOut.isArray)) {
			//MapperInOutJTArray.new(mapperIn, mapperOut)
			"not implemented yet".postln;
		} {
			MapperInOutJT.new(mapperIn, mapperOut)
		}
	}
}
MapperInOutJT : MapperIOJT {
	*new {arg mapperIn, mapperOut;
		^super.newCopyArgs(mapperIn, mapperOut).init
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
		if (synth!=nil) {
			if (target!=nil) {
				this.makeMapperSynthJT(target, addAction)
			}
		}
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
	/*
	map {arg value;
	if(mapMethod.class == MapperLinLinJT) {
	^value.linlin(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, \minmax)
	} {
	^mapMethod.map(value);
	}
	}
	*/
	mapFast {arg value;
		^mapperOut.mapNoClip(mapperIn.unmap(value))
	}
	mapSynth {arg target, addAction;
		target.map(mapperOut.name, mapperOut.bus);
		this.makeMapperSynthJT(target, \addBefore);
	}
	makeMapperSynthJT {arg argtarget, argaddAction=\addBefore;
		target = argtarget.asTarget;
		addAction=argaddAction??{\addBefore};
		synthDef=SystemSynthDefs.generateTempName;
		synth=SynthDef(synthDef, {
			var in,out, vals=();
			var inLagger, outLagger;
			var array;
			in=In.kr(mapperIn.bus.index, mapperIn.bus.numChannels);
			//in.poll(1, \in);
			if (mapperIn.lag!=nil) {
				var lagName=(mapperIn.name++\_lag).asSymbol, lagger;
				lagger=NamedControl(lagName, mapperIn.lag, \control);
				in=in.lag(*lagger);
			};
			array=[mapperIn, mapperOut].collect{|mapper,i|
				var key=[\in,\out][i];
				[\minval, \maxval].collect{|key,i|
					var kee=(mapper.name++"_"++key).asSymbol;
					vals[kee]=NamedControl(kee, [mapper.minval,mapper.maxval][i], \control, nil, spec:mapper.asSpec);
					vals[kee]
				};
			}.flat;
			out=in.performList(uGen, array);
			if (mapperOut.lag!=nil) {
				var lagName=(mapperOut.name++\_lag).asSymbol, lagger;
				lagger=NamedControl(lagName, mapperOut.lag, \control);
				out=out.lag(*lagger);
			};
			//out.poll(1, \out);
			Out.kr(mapperOut.bus, out)
		}).add.play(target, nil, addAction);
		synth.register;
		^synth
	}
}
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
		^value.curvelin(mapperIn.minval, mapperIn.maxval, mapperOut.minval, mapperOut.maxval, mapperIn.curve
			, \minmax)
	}
}