+ AnalyzerSystemJT {

	*initClass {
		Class.initClassTree(ControlSpec);

		defaultcontrolSpecs=(
			amplitude: \amp.asSpec
			, loudness: ControlSpec(0, 64, 4.0)
			, specflatness: ControlSpec(0.0, 1.0, 4.0)
			, speccentroid: \freq.asSpec
			, specpcile: \freq.asSpec
			, spectralentropy: ControlSpec(0.0, 352, 6.0)
			, sensorydissonance: ControlSpec(0.0, 1.0, 10.0)
			, keytrack: ControlSpec(0, 23)
			, mfcc: ControlSpec(-0.1, 2.0, 2.0)
			, peakfollower: ControlSpec(0.0, 1.0, 4.0)
			, tartini: [\freq.asSpec
				, ControlSpec(0.0, 1.0, -8.0)]
			, pitch: [\freq.asSpec
				, ControlSpec(0.0, 1.0, -8.0)]
			, tartinicps: \freq.asSpec
			, tartinimidi: ControlSpec(0, 127.0)
			, pitchcps: \freq.asSpec
			, pitchmidi: ControlSpec(0, 127.0)
			, holdTime:ControlSpec(0.0, 5.0, 4.0)
			, minTime: ControlSpec(0.0, 5.0, 4.0)
			, lagU:ControlSpec(0.0, 5.0, 4.0)
			, lagD:ControlSpec(0.0, 5.0, 4.0)
			, fftflux: ControlSpec(0.0, 1.0, 4.0)
		);

		defaultsettings=(
			onsets: (threshold:0.5), amplitude: (at: 0.05, rt:0.3), mfcc: (numcoeff:13)
			, loudness: (), specflatness:(), speccentroid:()
			, specpcile:(fraction:0.9, interpolate: 0)
			, spectralentropy: (fftsize: 2048, numbands: 1)
			, sensorydissonance: (maxpeaks: 100, peakthreshold: 0.1)
			, keytrack: (akeydecay:2, chromaleak:0.5)
			, peakfollower: (decay: 0.999)
			, tartini: (athreshold:0.93, bn:1024, ck:0, overlap:512, smallCutoff:0.5)
			, tartinicps: (athreshold:0.93, bn:1024, ck:0, overlap:512, smallCutoff:0.5
				, tmin:50, umax:4000)
			, tartinimidi: (athreshold:0.93, bn:1024, ck:0, overlap:512, smallCutoff:0.5
				, tmin:0, umax:127)
			, pitch: (ainitFreq: 440.0, bminFreq: 60.0, cmaxFreq: 4000.0,
				dexecFreq: 100.0, emaxBinsPerOctave: 16, fmedian: 1,
				gampThreshold: 0.01, hpeakThreshold: 0.5, idownSample: 1, jclar:0)
			, fftflux: ()
		);

		defaultfftsizes=(
			onsets:512, specflatness: 2048, speccentroid: 2048, specpcile: 2048,
			spectralentropy:2048, sensorydissonance:2048, fftflux: 2048
		);

		defaulthopsizes=();

		defaultInputs=(amplitude: 0, peakfollower: 0, tartini: 0, pitch:0
			, tartinicps:0, tartinimidi:0);

		[\pitchcps, \pitchmidi].do{|key|
			defaultsettings[key]=defaultsettings[\pitch].copy;
			defaultInputs[key]=defaultInputs[\pitch].copy;
		};

		makeUGens=(
			onsets: {arg fft, tr=0.5; Onsets.kr(fft, tr)},
			amplitude: {arg in, at, rt;
				Amplitude.kr(in, at, rt);
			},
			loudness: {arg fft; Loudness.kr(fft)},
			specflatness: {arg fft; SpecFlatness.kr(fft)},
			speccentroid: {arg fft; SpecCentroid.kr(fft)},
			specpcile:  {arg fft, fraction=0.9, interpolate=0;
				SpecPcile.kr(fft, fraction, interpolate)},
			spectralentropy: {arg fft, fftsize=2048, numbands=1;
				SpectralEntropy.kr(fft,fftsize,numbands)},
			sensorydissonance: {arg fft, maxpeaks=100, peakthreshold=0.1;
				SensoryDissonance.kr(fft, maxpeaks, peakthreshold)},
			keytrack: {arg fft, akeydecay=2, chromaleak=0.5;
				KeyTrack.kr(fft, akeydecay, chromaleak)},
			peakfollower: {arg in, decay=0.999; A2K.kr(PeakFollower.ar(in, decay))},
			tartini: {arg in, athreshold=0.93, bn=1024, ck=0, overlap=512, smallCutoff=0.5;
				Tartini.kr(in, athreshold, bn, ck, overlap, smallCutoff)},
			tartinicps: {arg in, athreshold=0.93, bn=1024, ck=0, overlap=512
				, smallCutoff=0.5, tmin=50, umax=4000;
				Tartini.kr(in, athreshold, bn, ck, overlap, smallCutoff)[0].clip(tmin,umax)},
			tartinimidi: {arg in, athreshold=0.93, bn=1024, ck=0, overlap=512
				, smallCutoff=0.5
				, tmin=0, umax=127;
				Tartini.kr(in, athreshold, bn, ck, overlap, smallCutoff)[0]
				.cpsmidi.clip(tmin,umax)},
			pitch: {arg in = 0.0, ainitFreq = 440.0, bminFreq = 60.0, cmaxFreq = 4000.0,
				dexecFreq = 100.0, emaxBinsPerOctave = 16, fmedian = 1,
				gampThreshold = 0.01, hpeakThreshold = 0.5, idownSample = 1, jclar=0;
				Pitch.kr(in, ainitFreq, bminFreq, cmaxFreq, dexecFreq,emaxBinsPerOctave
					, fmedian, gampThreshold, hpeakThreshold, idownSample, jclar)},
			pitchcps: {arg in = 0.0, ainitFreq = 440.0, bminFreq = 60.0, cmaxFreq = 4000.0,
				dexecFreq = 100.0, emaxBinsPerOctave = 16, fmedian = 1,
				gampThreshold = 0.01, hpeakThreshold = 0.5, idownSample = 1, jclar=0;
				Pitch.kr(in, ainitFreq, bminFreq, cmaxFreq, dexecFreq,emaxBinsPerOctave
					, fmedian, gampThreshold, hpeakThreshold, idownSample, jclar)[0]},
			pitchmidi: {arg in = 0.0, ainitFreq = 440.0, bminFreq = 60.0, cmaxFreq = 4000.0,
				dexecFreq = 100.0, emaxBinsPerOctave = 16, fmedian = 1,
				gampThreshold = 0.01, hpeakThreshold = 0.5, idownSample = 1, jclar=0;
				Pitch.kr(in, ainitFreq, bminFreq, cmaxFreq, dexecFreq,emaxBinsPerOctave
					, fmedian, gampThreshold, hpeakThreshold, idownSample, jclar)
				[0].cpsmidi},
			mfcc: {arg fft, numcoeff=13; MFCC.kr(fft, numcoeff)},
			//mfccd: {arg mfcc; mfcc.differentiate.abs.copyToEnd(1).sum},
			fftflux: {arg fft; FFTFlux.kr(fft)}
		);
	}

}