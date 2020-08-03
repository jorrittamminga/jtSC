AnalyzerJTNormalizer : AnalyzerSystemJT {
	var replace;

	*new {arg analyzer, descriptors, replace=false, oscFunc;

		^super.new.init(analyzer, descriptors, replace, oscFunc);
	}

	init {arg arganalyzer, argdescriptors, argreplace, argoscFunc;
		id=UniqueID.next;
		analyzer=arganalyzer;
		server=analyzer.server;
		descriptorsWithoutOnsets=argdescriptors??{analyzer.descriptorsWithoutOnsets};
		controlSpecs=analyzer.controlSpecs;
		if (argoscFunc!=nil, {
			this.initCmdNames;
			this.addNormalizedOSCFuncs(argoscFunc);
		});
		this.makeNormalizers;
		this.initThreaded;
		if (threaded, {
			this.makeBusses;
			this.makeSynthDefNormalizer; server.sync;
			this.makeSynth; server.sync;
		},{{
			this.makeBusses;
			this.makeSynthDefNormalizer; server.sync;
			this.makeSynth; server.sync;
		}.fork}
		)
	}

	free {
		synth.free;
		if (replace, {outBusperDescriptor.keysValuesDo{|key,bus| bus.free}});
		if (osc!=nil, {osc.keysValuesDo{|key,o| o.free}});
	}

	close {}

	makeBusses {
		if (replace.not, {
			outBusperDescriptor=();
			descriptorsWithoutOnsets.do{|key|
				outBusperDescriptor[key]=Bus.audio(server, totalNumberOfOutputs);
				server.sync;
			};
		},{
			outBusperDescriptor=analyzer.outBusperDescriptor;
		});
	}

	makeSynthDefNormalizer {
		synthDef=(\Normalizer++id).asSymbol;
		SynthDef(\Normalizer, {//arg inBus, outBus;
			var in;
			in=descriptorsWithoutOnsets.collect{|key|
				var in=In.kr(analyzer.outBusperDescriptor[key].index
					, analyzer.outBusperDescriptor[key].numChannels);
				in=normalizers[key].value(in);
				if (replace, {
					ReplaceOut.kr(outBusperDescriptor[key].index, in)
				},{
					Out.kr(outBusperDescriptor[key].index, in)
				})
			};
		}).add;
	}

	makeSynth {
		synth=Synth.after(analyzer.synth, synthDef).register;
	}

	addOSCFuncN {arg addFunc;//={arg key, value, time; };
		this.addNormalizedOSCFuncs()
	}
}