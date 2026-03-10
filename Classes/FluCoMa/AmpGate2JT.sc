FluidBufAmpGate2JT {
	var audioBuffer, ampFeaturesBuffer, ampSlicesBuffer, threshold, includeSilences, minSliceFrames;
	var <indices, <pairs, <startFrames, <numFrames, <silenceIndices;

	* new {arg audioBuffer, ampFeaturesBuffer, ampSlicesBuffer, threshold= -0.001, includeSilences=true, minSliceFrames=10;
		^super.newCopyArgs(audioBuffer, ampFeaturesBuffer, ampSlicesBuffer, threshold, includeSilences, minSliceFrames).init
	}

	init {
		var ampSliceFeatures;
		silenceIndices=[];
		ampFeaturesBuffer.loadToFloatArray(0, -1, {|ar| ampSliceFeatures=ar.as(Array)});//load the AmpSliceFeatures in an array
		if (ampSlicesBuffer.class==Buffer) {
			ampSlicesBuffer.loadToFloatArray(0, -1, {|ar| indices=ar.as(Array)});//load the indices of the onsets in an array
		} {
			indices=ampSlicesBuffer;
		};
		indices=indices.collect{|val,i| [val, indices[i+1]??{audioBuffer.numFrames}].asInteger};//make [new, next] startFrames
		if (indices.last[1]<(audioBuffer.numFrames)) {indices=indices.add([indices.last[1],audioBuffer.numFrames])};
		if (includeSilences) {
			var tmp=[];
			indices.do{|frames,i|
				var index, amps=ampSliceFeatures.copyRange(frames[0],frames[1]);
				//i.post; frames.postln;
				index=amps.detectIndex({|val| val<= threshold});
				if (index!=nil) {
					if (index>=minSliceFrames) {
						silenceIndices=silenceIndices.add(index+frames[0]);
						tmp=tmp.add([frames[0], index+frames[0]]);
						tmp=tmp.add([index+frames[0], frames[1]]);
					} {
						tmp=tmp.add(frames)
					}
				} {
					tmp=tmp.add(frames)
				}
			};
			indices=tmp;
			tmp=nil;
		} {
			indices.do{|frames,i|
				var indexOffset, index=0, amps=ampSliceFeatures.copyRange(frames[0],frames[1]);
				indexOffset=amps.detectIndex({|val| val< 0 });
				index=amps.copyToEnd(indexOffset).detectIndex({|val| val>=threshold});
				index=index+indexOffset;
				//i.post; frames.post; index.postln;
				if (index!=nil) {
					if (index>=minSliceFrames) {
						silenceIndices=silenceIndices.add(index+frames[0]);
						indices[i]=[frames[0], index+frames[0]];
					}
				}
			};
		};
		pairs=indices.deepCopy;//[ [startPos, endPos], [startPos, endPos], etc]
		pairs=pairs.collect{|val| [val[0],val[1]-val[0]]};//[[startFrame0, numFrames0], [startFrame1, numFrames1], .....]
		#startFrames, numFrames=pairs.flop;
		indices=indices.flat;//for buffer
		[ampSliceFeatures].do{|i| i=nil};//free large array
	}

}