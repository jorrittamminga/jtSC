+ AnalyzerSystemJT {

	initAll {
		this.initdefaultFFTsizes;
		this.initDescriptors;
		this.initSettings;
		this.initFFTsizes;
		this.initInputs;
		this.initNumberOfOutputs;
		this.initHPZ;
		this.initCmdNames;
		this.initFFTouts;
		this.isThreaded;
	}

	initdefaultFFTsizes {
		var extradefaultfftsizes=(
			mfcc:(server.sampleRate>50000).binaryValue+1*1024,
			loudness:(server.sampleRate>50000).binaryValue+1*1024,
			keytrack: (server.sampleRate>50000).binaryValue+1*4096
		);
		descriptors.do{|key|
			if ((defaultfftsizes[key]==nil) && (fftSizes[key]==nil)
				&& (extradefaultfftsizes[key]!=nil), {
					defaultfftsizes[key]=extradefaultfftsizes[key]
			})
		}
	}

	initDescriptors {
		hasOnsets=descriptors.includes(\onsets);
		descriptors.do{|key,i| if (makeUGens[key]==nil, {descriptors.removeAt(i)})};
		descriptorsWithoutOnsets=descriptors.deepCopy;
		if (hasOnsets, {descriptorsWithoutOnsets.remove(\onsets)});
	}

	initSettings {
		settings=settings??{()};
		/*
		if (descriptors.includes(\mfccd) && descriptors.includes(\mfcc).not
		, {descriptors=descriptors.insert(descriptors.indexOf(\mfccd), \mfcc)});
		*/
		descriptors.do{|key|
			var tmpsettings=defaultsettings[key].deepCopy;
			if (settings[key]==nil, {settings[key]=tmpsettings},{
				tmpsettings.keysValuesDo{|key2,val|
					if (settings[key][key2]==nil, {settings[key][key2]=val})
				}
			});
		};
		if (descriptors.includes(\spectralentropy), {
			settings[\spectralentropy][\fftsize]=defaultfftsizes[\spectralentropy];
		});
		controlSpecs=();
		descriptorsWithoutOnsets.do{|key| controlSpecs[key]=defaultcontrolSpecs[key]};
	}

	initFFTsizes {
		fftSizes=fftSizes??{()};
		descriptors.do{|key|
			if (fftSizes[key]==nil, {fftSizes[key]=defaultfftsizes[key]});
		};
	}

	initInputs {
		//0=in, 1=fft512, 2=fft1024, 3=fft2048, 4=fft4096
		inputs=();
		inputsFlag={false}!6;//inputsFlag, to activate FFT or not
		inputsFlag[0]=true;//the In.ar(inBus) is inputs[0], is always true;
		fftSizes.keysValuesDo{|key,val|
			var in;
			in=((val.log/2.log)-8).round(1.0).asInteger;
			inputs[key]=in;
		};

		descriptors.do{|key|
			inputs[key]=inputs[key]??{defaultInputs[key]};
		};
		inputs.keysValuesDo{|key,val| inputsFlag[val]=true};

		minInput=nil;
		(1..4).do{|i|
			if (inputsFlag[i], {
				minInput=minInput??{i};
			})
		};

	}

	initNumberOfOutputs {
		numberOfOutputs=();
		descriptors.do{|key|
			numberOfOutputs[key]=1;
			if ((key==\pitch)||(key==\tartini)||(key==\fftpeak), {
				numberOfOutputs[key]=2
			});
			if (key==\mfcc, {
				numberOfOutputs[key]=settings[\mfcc][\numcoeff];
			});
			if (key==\spectralentropy, {
				numberOfOutputs[key]=settings[\spectralentropy][\numbands];
			});
		};
		totalNumberOfOutputs=0;
		descriptorsWithoutOnsets.do{|key|
			totalNumberOfOutputs=totalNumberOfOutputs+(numberOfOutputs[key]??{1});
		};
	}

	initHPZ {
		hpzInputs=();
		descriptors.do{|key,i|
			var key1, key2, key3, split;
			if (key.asString.contains("HPZ"), {
				split=key.deepCopy.asString.split($H);
				key1=split[0].asSymbol;
				key2=("H"++split[1]).asSymbol;
				if (descriptors.includes(key1).not, {
					descriptors.insert(i, key1);
				});
				hpzInputs[key]=descriptors.indexOf(key1);
				if (makeUGens[key1].argNames.includes(\fft), {
					key2=(key2++\T).asSymbol;
					inputs[key]=inputs[key1].deepCopy;
				});
				makeUGens[key]=switch(key2
					, \HPZ, {{arg in; HPZ1Latch.kr(in)} }
					, \HPZT, {{arg in, trig; HPZ1LatchT.kr(in, trig)} }
					, \HPZa, {{arg in; HPZ1Latch.kr(in).abs} }
					, \HPZaT, {{arg in, trig; HPZ1LatchT.kr(in, trig).abs} }
				);
				controlSpecs[key]=if (key2.asString.contains("a"), {
					ControlSpec(0, defaultcontrolSpecs[key1].maxval, 8.0);
				},{
					ControlSpec(defaultcontrolSpecs[key1].maxval.neg
						, defaultcontrolSpecs[key1].maxval)
				});
				numberOfOutputs[key]=numberOfOutputs[key1];
				settings[key]=();
			});
		};
	}

	initCmdNames {
		cmdNames=(512: ("/fft512"++id), 1024: ("/fft1024"++id)
			, 2048:("/fft2048"++id).asSymbol, 4096:("/fft4096"++id)
			//, onsets: ("/onsets"++id)
		);
		descriptors.do{|key| cmdNames[key]=("/"++key++id).asSymbol};
	}

	initFFTouts {
		//var fftOut=descriptors.collect{|key| inputs[key]};
		//fftOut=fftOut.asSet.asArray.sort;
		outFFTs=[];
		outFFTperDescriptor=();
		if (outFFTFlag, {
			inputsFlag.do{|flag,i|
				if (i>0, {
					if (flag, {
						outFFTs=outFFTs.add(i)
					});
				});
			};
			descriptors.do{|key|
				outFFTperDescriptor[key] = outFFTs.indexOf(inputs[key])
			};
		});
	}

}