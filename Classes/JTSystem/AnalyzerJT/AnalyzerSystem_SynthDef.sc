/*
change wintype in FFT's to 1 instead of 0???
*/
+ AnalyzerSystemJT {

	makeSynthDef {arg synthDefName;
		synthDef=synthDefName??{\Analyzer};

		SynthDef(synthDef, {arg updateF;//inBus
			var in=Array.newClear(5);
			var onsets;
			var analysis=[], pars=();//=[[],[],[],[],[]], pars=();

			in[0]=In.ar(inBus);
			if (inputsFlag[1], {in[1]=FFT(LocalBuf(512), in[0], wintype:1)});
			if (inputsFlag[2], {in[2]=FFT(LocalBuf(1024), in[0], wintype:1)});
			if (inputsFlag[3], {in[3]=FFT(LocalBuf(2048), in[0], wintype:1)});
			if (inputsFlag[4], {in[4]=FFT(LocalBuf(4096), in[0], wintype:1)});
			//maybe some extra with different hopsizes
			//in[0]=in, in[1]=fft512, in[2]=fft1024, etc

			descriptors.do{|key|
				var parList=[], inIndex=inputs[key], outp;
				if (key.asString.contains("HPZ"), {
					parList=parList.add(analysis[hpzInputs[key]]);
					if(inIndex!=nil, {
						parList=parList.add(in[inIndex]);
					});
				},{
					if (inIndex>=0, {parList=parList.add(in[inIndex]);});
					//if (inIndex<0, {parList=parList.add(analysis[inIndex.abs])});
				});
				pars[key]=();
				settings[key].sortedKeysValuesDo{|key2, value|
					var name=(key++"_"++key2).asSymbol;
					if (metadataSpecs[key]!=nil, {
						pars[key][key2]=metadataSpecs.kr(name, value);
					},{
						pars[key][key2]=value;
					});
					parList=parList.add(pars[key][key2]);
				};
				if (key==\onsets, {
					onsets=makeUGens[key].value(*parList)
				},{
					outp=makeUGens[key].value(*parList);
					//LET OP!!!
					//bij normalized gaat nu normalized via OUT maar normal via SendReply!!!
					if (sendreplyFlag, {
						SendReply.kr(
							if (updateFreq!=nil, {
								Impulse.kr(updateF)},{
								in[inIndex.max(minInput)]
							});
							, cmdNames[key]
							, outp);
					});
					if (normalized, {
						outp=normalizers[key].value(outp);
					});
					analysis=analysis.add(outp);
				})
			};
			if (hasOnsets, {
				if (outFlag, {
					Out.kr(outBusT.index, onsets);
				});
				if (sendreplyFlag, {
					SendReply.kr(onsets, cmdNames[\onsets], 1);
				})
			});
			if (outFlag, {
				Out.kr(outBus.index, analysis.flat);
			});
			if (outFFTs.size>0, {
				outFFTs.do{arg i, k;
					Out.kr(outBusFFT.index+k, in[i])
				}
			});
		}, metadata: (specs:metadataSpecs)).add;
	}
}