/*
Weighted arithmetic mean

freqs.meanWeighted(freqs.differentiate)

*/
SpectralAnalysisJT {
	var <partials, <rawData;
	var <buffer, <bufferMono, <freqs, <amps, <times;
	var <bufFreqs, <bufMags;
	var bufPitch, bufConfidence, bufPitchMean;
	var <bufFreqsArray, <bufMagsArray, <root, <pitch;

	*new {arg server=Server.default, path,
		numPeaks=16,//number of peaks
		windowSize=2048,//window size
		overlap=2,//overlap
		detectionThreshold= -80,//minimum db
		freqMin=50,
		partialBw=0.5,
		maskingBw=0,
		maxDurTransient=0.2,
		freeBuffers=true,
		post=true,
		cond=Condition.new
		;

		^super.new.init(server, path,numPeaks,windowSize,overlap,detectionThreshold,freqMin,partialBw,maskingBw,maxDurTransient, freeBuffers, post, cond)
	}

	close { }
	free {

	}

	init {arg server, path,numPeaks,windowSize,overlap,detectionThreshold,freqMin,partialBw,maskingBw,maxDurTransient, freeBuffers, post, cond;
		var s=server;
		var maxDiff, maxFramesTransient, freeMono=false;

		s.waitForBoot{

			maxFramesTransient=((windowSize/overlap/48000).reciprocal*maxDurTransient).round(1.0).asInteger;

			//------------------------------------------------------------------- STEP 1
			{
				buffer=Buffer.read(s, path);
				s.sync;
				if (buffer.numChannels>1) {
					freeMono=true;
					bufferMono=Buffer(s); s.sync;
					buffer.numChannels.do{|i|
						FluidBufCompose.processBlocking(s, buffer, 0, -1, i, 1, buffer.numChannels.sqrt.reciprocal, bufferMono).wait
					};
				} {
					bufferMono=buffer;
				};

				bufferMono.normalize; s.sync;

				//make destination buffers
				bufFreqs = Buffer(s); s.sync;
				bufMags = Buffer(s); s.sync;

				bufPitch = Buffer(s); s.sync;
				bufConfidence = Buffer(s); s.sync;
				bufPitchMean = Buffer(s); s.sync;

				//process
				FluidBufPitch.process(s, bufferMono, 0, -1, 0, -1, bufPitch, [\pitch, \confidence], 2, 20, 4000, 0, 4096).wait;
				FluidBufCompose.process(s, bufPitch, 0, -1, 1, 1, 1, bufConfidence).wait;
				FluidBufStats.process(s, bufPitch, 0, -1, 0, -1, bufPitchMean, [\mean], 0, 0, 50, 100, 1, bufConfidence).wait;
				bufPitchMean.get(0, {|p|
					if (post) {"pitch is ".post; p.postln};
					pitch=p
				});

				//process
				FluidBufSineFeature.process(s, bufferMono, 0, -1, 0, 1, bufFreqs, bufMags, numPeaks, detectionThreshold, 1, 0, 1
					, windowSize, windowSize/overlap, action: {
						if (post) {"".postln}
				}).wait;

				if (post) {"READY with FluidBufSineFeature".postln};

				//------------------------------------------------------------------- STEP 2
				bufFreqs.loadToFloatArray(0, -1, {|x|
					bufFreqsArray=x;
					bufFreqsArray=bufFreqsArray.clump(bufFreqs.numChannels);
					bufMags.loadToFloatArray(0, -1, {|x|
						var lastFreqs;
						var order, indices, bool;
						bufMagsArray=x;
						bufMagsArray=bufMagsArray-bufMagsArray.maxItem;
						bufMagsArray=bufMagsArray.clump(bufMags.numChannels);
						if (post) {"READY loading the bufferdata in arrays".postln};

						//------------------------------------------------------------------- STEP 3

						maxDiff=(s.sampleRate/windowSize)*partialBw;//partialBw

						//maxDiff=maxDiff*0.5;
						lastFreqs=bufFreqsArray[0];
						freqs=lastFreqs.collect{|i| [i]};
						amps=bufMagsArray[0].collect{|i| [i]};
						times={[0]}!freqs.size;

						//y.copyRange(1,30)
						bufFreqsArray.copyToEnd(1).do{|freqz,i|
							var tmp=[], indices=(0..freqz.size-1), indicesS=[], pairs=[];

							//i.post; freqz.postln;
							freqz.selectIndices({|f| f<freqMin}).do{|i| indices.remove(i)};
							bufMagsArray[i].selectIndices({|a| a< detectionThreshold}).do{|i| indices.remove(i)};

							freqz=indices.collect{|i| freqz[i]};
							indices=[];
							//i.post; freqz.postln;

							freqz.do{|freq1,j|
								var count=0, flag=false, freq2, diff;
								//you could also make a list of remaining indices to check
								while { (count<(lastFreqs.size-1)) && (flag.not) } {
									if (indicesS.includes(count).not) {
										freq2=lastFreqs[count];
										diff=(freq1-freq2).abs;
										//[freq1, freq2, diff, i, count].postln;
										flag=diff<maxDiff;
										if (flag) {
											indices=indices.add(count);
											pairs=pairs.add([j,count, freq1, freq2, diff]);
										};
									};
									count=count+1;
								};
								if (flag.not) {
									pairs=pairs.add([j, -1, freq1]);//
									//check here if there were previous freqs
								};
								indicesS=indices.copy;
								indicesS.sort;
								//indicesS.postln;
							};
							pairs.do{|values|
								var index=values[1], index2=values[0];
								if (values[1]>=0) {
									freqs[values[1]]=freqs[values[1]].add(values[2]);
									amps[index]=amps[index].add(bufMagsArray[i][index2]);
									times[index]=times[index].add(i);
								}{
									//values.postln;
									freqs=freqs.add([values[2]]);
									amps=amps.add([bufMagsArray[i][index2]]);
									times=times.add([i]);
								}
							};

							//pairs.postln;

							lastFreqs=freqs.collect{|freqz| freqz.last};

						};
						if (post) {"READY with phase 3".postln};

						//-------------------------------------------------------------------

						order=freqs.order({|a,b| a.mean<b.mean});
						amps=order.collect{|i| amps[i]};
						times=order.collect{|i| times[i]};
						freqs.sort({|a,b| a.mean<b.mean});
						//freqs.do({|freq,i| [freq.mean, amps[i].maxItem, times[i].minItem].post;" ".post; freq.size.postln});
						if (post) {"READY with phase 4".postln};

						//-------------------------------------------------------------------
						indices=(0..(freqs.size-1));
						freqs.do({|freq,i|
							//[freq.mean, amps[i].maxItem, times[i].minItem, times[i].maxItem, times[i].differentiate.mean].post;
							//" ".post; freq.size.postln;
							if (freq.mean<20) {
								indices.remove(i);
							} {
								if (times[i].minItem>maxFramesTransient) {//make this dependant of the windowsize!
									indices.remove(i);
								} {
									if (freq.size==1) {//make this dependant of the windowsize!
										indices.remove(i)
									} {
										if (times[i].differentiate.mean>3.5) {//was 3.5, make this an argument!
											indices.remove(i)
										}
									}
								}
							}
						});
						freqs=indices.collect{|i| freqs[i]};
						amps=indices.collect{|i| amps[i]};
						times=indices.collect{|i| times[i]};
						times=times*(windowSize/overlap/s.sampleRate);
						if (post) {"READY with phase 5".postln};

						//-------------------------------------------------------------------
						amps.do{|amps,i|
							var indices;
							indices=amps.selectIndices({|a| a<120.neg});
							if (indices.size>0) {
								indices.do{|j|
									if (j>0) {
										if (j<(amps.size-1)) {
											amps[j]=[amps[j-1],amps[j+1]].mean;
										}{
											amps[j]=amps[j-1]
										}
									}{
										if (amps.size>1) {
											amps[0]=amps[1]
										}
									}
								}
							};
						};
						if (post) {"READY with 5b, amps repair".postln};

						//-------------------------------------------------------------------
						//var bw=1.0;//1.0 or 0.5?
						//if (post) {"maskingBw ".post; maskingBw.postln};

						if (maskingBw>0.0000001) {
							bool=true;
							while {bool} {
								indices=(0..(freqs.size-1));
								bool=false;
								freqs.do({|freq,i|
									var boolean;
									//"freqs ".post; i.post; freq.postln;
									//"one".post; [freq.mean, ~amps[i].maxItem, ~times[i].minItem, ~times[i].size].postln;
									if (i>0) {
										if (i<(freqs.size-1)) {
											//"mean normal".postln;
											boolean=[-1,1].collect{|j|
												//	[i, j, i+j].post; ~freqs[i+j].postln;
												freqs[i].mean.masking(freqs[i+j].mean, amps[i].maxItem, amps[i+j].maxItem, maskingBw)
											};
											boolean=boolean[0]||boolean[1];
											//boolean.postln
										} {
											//"mean only -1".postln;
											boolean=freqs[i].mean.masking(freqs[i-1].mean, amps[i].maxItem, amps[i-1].maxItem, maskingBw)
										}
									} {
										//"mean only +1".postln;
										boolean=freqs[i].mean.masking(freqs[i+1].mean, amps[i].maxItem, amps[i+1].maxItem, maskingBw)
									};
									if (boolean) {bool=true; indices.remove(i)};
								});
								freqs=indices.collect{|i| freqs[i]};
								amps=indices.collect{|i| amps[i]};
								times=indices.collect{|i| times[i]};
							};

							if (post) {freqs.postln};
							if (post) {amps.postln};
							if (post) {times.postln};

						};
						if (post) {"READY with masking".postln};



						//-------------------------------------------------------------------
						root=freqs[0].mean;

						partials=(basefreq: root, duration: times.collect(_.maxItem).maxItem, root: root, number_of_partials: freqs.size);
						partials[\freqs]=freqs.collect(_.mean);
						partials[\ratios]=partials[\freqs]/partials[\freqs][0];
						partials[\dbs]=amps.collect(_.maxItem);
						partials[\times]=times.collect(_.maxItem);
						partials[\timesNormalized]=partials[\times].normalize2;
						partials[\pitch]=pitch;

						//-------------------------------------- COMPATIBILITY with other analysis classes
						//partials[\ratio]=partials[\ratios];
						//partials[\db]=partials[\dbs];
						//partials[\freq]=partials[\freqs];

						if (post) {
							freqs.do({|freq,i|
								[
									freq.mean, freq.mean/root, amps[i].maxItem, times[i].minItem, times[i].maxItem
									, times[i].differentiate.mean

								].post;" ".post; freq.size.postln;
							});
						};
						//-------------------------------------------------------------------

						if (freeBuffers) {
							buffer.free;
							if (freeMono) {bufferMono.free};
							bufFreqs.free;
							bufMags.free;
							bufPitch.free;
							bufConfidence.free;
							bufPitchMean.free;
						};
						"READY with analysis ".post; path.postln;
						cond.unhang;
					});
				});
			}.fork
		}
	}
}