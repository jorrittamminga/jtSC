MapperInOutJTArray : MapperIOJT {
	var <>array;
	*new {arg mapperIn, mapperOut;
		^super.newCopyArgs(mapperIn, mapperOut).init;
	}
	init {
		array=mapperIn.size.max(mapperOut.size).collect{|i|
			MapperIOJT.new(mapperIn.wrapAt(i), mapperOut.wrapAt(i) )
		};
	}
	map {arg value;
		^value.collect{|value,i|
			[\map, i, value, array[i]].postln;
			[array[i].mapMethod,array[i].mapMethod.mapperIn, array[i].mapMethod.mapperOut].postln;
			[
				array[i].mapMethod,array[i].mapMethod.mapperIn.minval, array[i].mapMethod.mapperIn.maxval,
				array[i].mapMethod,array[i].mapMethod.mapperOut.minval, array[i].mapMethod.mapperOut.maxval
			].postln;
			array[i].mapMethod.map(value)
		};
	}
	mapFast {arg value;
		^array.collect{|m,i| m.mapperOut.mapNoClip(m.mapperIn.unmap(value[i]))}
	}
/*
	mapSynth {arg target, addAction;
		var mapMsg;
		mapMsg=this.collect{|m| [m.mapperOut.name, m.mapperOut.bus.index]}.flat;
		target.map(*mapMsg);
		this.collect{|m|
			m.makeMapperSynthJT(target, \addBefore);
		};
	}
	update {arg changer, what ...moreArgs;
		this.do{|m|
			[m.mapperIn,m.mapperOut].do{|mapper,i|
				m.initFunc(mapper, [\in,\out][i])
			};
			if (m.synth!=nil) {
				if (m.target!=nil) {
					m.makeMapperSynthJT(m.target, m.addAction)
				}
			}
		}
	}
	makeMapperSynthJT {arg target, addAction=\addBefore;
		var synthDef;
		target = target.asTarget;
		synthDef=SystemSynthDefs.generateTempName;
		^SynthDef(synthDef, {
			this.do{|m|
				var in,out, vals=();
				var inLagger, outLagger;
				var array;
				in=In.kr(m.mapperIn.bus.index, m.mapperIn.bus.numChannels);
				//in.poll(1, \in);
				if (m.mapperIn.lag!=nil) {
					var lagName=(m.mapperIn.name++\_lag).asSymbol, lagger;
					lagger=NamedControl(lagName, m.mapperIn.lag, \control);
					in=in.lag(*lagger);
				};
				array=[m.mapperIn, m.mapperOut].collect{|mapper,i|
					var key=[\in,\out][i];
					[\minval, \maxval].collect{|key,i|
						var kee=(mapper.name++"_"++key).asSymbol;
						vals[kee]=NamedControl(kee, [mapper.minval,mapper.maxval][i], \control, nil, spec:mapper.asSpec);
						vals[kee]
					};
				}.flat;
				out=in.performList(m.uGen, array);
				if (m.mapperOut.lag!=nil) {
					var lagName=(m.mapperOut.name++\_lag).asSymbol, lagger;
					lagger=NamedControl(lagName, m.mapperOut.lag, \control);
					out=out.lag(*lagger);
				};
				//out.poll(1, \out);
				Out.kr(m.mapperOut.bus, out)
			}
		}).add.play(target, nil, addAction);
	}
	*/
}