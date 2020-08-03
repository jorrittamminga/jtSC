/*
descriptors: [\onsets, \amplitude, \loudness, \specflatness, \speccentroid, \specpcile, \spectralentropy, \sensorydissonance, \keytrack, \peakfollower, \tartini, \tartinicps \tartinimidi, \pitch, \pitchcps, \pitchmidi, \mfcc, \fftflux]

- bij window close krijg je nog een error..... (eerst de synth weg en dan pas gui closen denk ik....)
- hopsizes moet er nog bij

- maak een offline versie (voor b.v. concat, met normalized outputs)
- maak een apart makeSynthDef (zodat je verschillende makeSynth opties kunt maken, voor bv NRT
- maak het mogelijk om zelf dingen toe te voegen (in een aparte file) qua descriptors
*/
AnalyzerJT : AnalyzerSystemJT {

	*new {arg inBus
		, target
		, descriptors=[\onsets, \loudness]
		, settings=()
		, metadataSpecs=()
		, outFlag=true
		, sendreplyFlag=true
		, outFFTFlag=true
		, fftsizes=()
		, hopsizes=()
		, updateFreq
		, normalized=false
		;

		^super.new.init(inBus, target, descriptors, settings, metadataSpecs
			, outFlag, sendreplyFlag, outFFTFlag, fftsizes, hopsizes, updateFreq, normalized);
	}


	init {arg arginBus, argtarget, argdescriptors, argsettings=()
		, argmetadataSpecs
		, argoutFlag, argsendreplyFlag, argoutFFTFlag
		, argfftsizes=(), arghopsizes=(), argupdateFreq, argnormalized;

		inBus=arginBus;
		target=argtarget;
		descriptors=argdescriptors;
		settings=argsettings??{()};
		metadataSpecs=argmetadataSpecs??{()};
		outFlag=argoutFlag;
		sendreplyFlag=argsendreplyFlag;
		outFFTFlag=argoutFFTFlag;
		fftSizes=argfftsizes;
		//hopsizes=arghopsizes??{()};
		updateFreq=argupdateFreq;
		normalized=argnormalized;
		bypass=false;
		bypassFunc={};
		id=UniqueID.next;
		if (target.class==Server, {server=target},{
			server=target.server;
		});
		if (inBus.class!=Bus, {inBus=inBus.asBus(\audio, inBus.asArray.size, server)});
		this.initAll;
		if (normalized, {this.makeNormalizers});
		if (threaded, {
			this.initBusses;
			this.makeSynthDef((\Analyzer++id).asSymbol); server.sync;
			this.makeSynth;
		},{{
			this.initBusses;
			this.makeSynthDef((\Analyzer++id).asSymbol); server.sync;
			this.makeSynth;
		}.fork}
		)

	}

	free {
		{
			synth.free;
			1.0.wait;
			if (outBus!=nil, {outBus.free});
			if (outBusT!=nil, {outBusT.free});
			if (outBusFFT!=nil, {outBusFFT.free});
			if (gui!=nil, {gui.close});
		}.fork(AppClock)
	}

	close {this.free}


	makeSynth {
		synth=Synth(synthDef, [], target, if (target.class==Synth, {\addAfter}
			, {\addToTail})).register;
		server.sync;
	}

	makeGUI {arg parent, bounds, descriptors, freeOnClose=false
		, margin=4@4, gap=4@4, font, thresholds, thresholdFuncs, gaterJT;

		gui=AnalyzerJTGUI(this, parent, descriptors, bounds, freeOnClose
			, margin, gap, font, thresholds, thresholdFuncs, gaterJT);
		^gui
	}
}