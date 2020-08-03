/*
inBusA kan zijn: 1: Bus, 2: [Bus, Bus, Bus], 3: [0,1,5,6,9,10], 4: [1,3,5,[6,7,8,9]]
*/
ConcatJTAnalyzer : ConcatJTSystem {

	var <player, <recorder;

	*new {arg analyzer, descriptors, path;
		^super.new.init(analyzer, descriptors, path);
	}

	init {arg arganalyzer, argdescriptors, argpath;
		id=UniqueID.next;
		analyzer=arganalyzer;
		descriptors=argdescriptors??{analyzer.descriptorsWithoutOnsets};
		path=argpath;

		inBusFFT=descriptors.collect{|i| analyzer.outBusFFTperDescriptor}
		.asSet.asArray.sort{|a,b| a.index<b.index}[0];

		if (analyzer.normalized.not, {
			analyzer=AnalyzerJTNormalizer(analyzer, descriptors, false);
		});
		target=analyzer.synth;
		inBusA=descriptors.collect{|key| analyzer.outBusperDescriptor[key]};
		//this.convertinBusA;
	}

	addPlayer {arg outBus, synthDef, specs;
		player=ConcatJTPlayer(inBusA, outBus, target, path, synthDef, specs);
		if (recorder!=nil, {recorder.concatJTplayer=player});
	}

	addRecorder {arg inBus, bufRecording, maxRecTime=30;
		recorder=ConcatJTRecorder(inBus, inBusA, inBusFFT, bufRecording
			, target, path, maxRecTime);
		if (player!=nil, {player.concatJTrecorder=recorder});
	}

}