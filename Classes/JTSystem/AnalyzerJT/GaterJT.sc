GaterJT : GaterSystemJT {

	*new {arg analyzer, descriptors, settings=()
		, outFlag=true, sendReplyFlag=true, functions={};
		^super.new.init(analyzer, descriptors, settings
			, outFlag, sendReplyFlag, functions)
	}

	init {arg arganalyzer, argdescriptors, argsettings, argoutFlag, argsendReplyFlag
		, argfunctions;
		var threadedFunc;
		analyzer=arganalyzer;
		descriptors=argdescriptors??{analyzer.descriptors};
		outFlag=argoutFlag;
		sendReplyFlag=argsendReplyFlag;
		settings=argsettings;
		functions=argfunctions??{{}};
		server=analyzer.server;
		id=UniqueID.next;

		this.initAll;
		threaded=thisProcess.mainThread.state>3;
		threadedFunc={
			this.makeBusses;
			this.makeSynthDef((\Gater++id).asSymbol); server.sync;
			this.makeSynth; server.sync;
		};

		if (threaded, {
			threadedFunc.value
		},{
			{
				threadedFunc.value
			}.fork
		});
	}

	free {
		if (outFlag, {outBus.free});
		if (synth!=nil, {synth.free});
		if (gui!=nil, {gui.close});
		if (oscFunc!=nil, {oscFunc.free});
	}

	close {
		this.free;
	}

	makeSynth {
		synth=Synth.after(analyzer.synth, synthDef).register;
	}

	makeGUI {arg parent, bounds, funcs, freeOnClose=false, makeCompositeView=true
		, margin=4@4, gap=4@4, font, analyzerJT;
		{
			gui=GaterJTGUI(this, parent, bounds, funcs, freeOnClose
				, makeCompositeView, margin, gap, font, analyzerJT)//, funcs
		}.defer
	}
}