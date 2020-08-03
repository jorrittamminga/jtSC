+ FreqScopeView {

	showFreqs {
		var rect, font;
		var freqLabel, freqLabelDist, aligns;
		var window;
		var pad = [30, 48, 14, 10]; // l,r,t,b
		//var dbLabel, dbLabelDist;
		var setFreqLabelVals;
		//setDBLabelVals;
		var nyquistKHz;

		font = Font.monospace(8);
		rect=scope.bounds;
		window=scope.parent;

		freqLabel = Array.newClear(12);
		aligns=[\left, \center, \right].resamp0(freqLabel.size);
		freqLabelDist = (rect.width/(freqLabel.size-1)-window.decorator.gap.x).floor;
		//dbLabel = Array.newClear(17);
		//dbLabelDist = rect.height/(dbLabel.size-1);

		nyquistKHz = server.sampleRate;
		if( (nyquistKHz == 0) || nyquistKHz.isNil, {
			nyquistKHz = 22.05 // best guess?
		},{
			nyquistKHz = nyquistKHz * 0.0005;
		});

		setFreqLabelVals = { arg mode, bufsize;
			var kfreq, factor, halfSize;

			factor = 1/(freqLabel.size-1);
			halfSize = bufsize * 0.5;

			(freqLabel.size-1).do({ arg i;
				if(mode == 1, {
					kfreq = (halfSize.pow(i * factor) - 1)/(halfSize-1) * nyquistKHz;
				},{
					kfreq = i * factor * nyquistKHz;
				});

				if(kfreq > 1.0, {
					freqLabel[i].string_( kfreq.asString.keep(4)
						//++ "k"
					)
				},{
					freqLabel[i].string_( (kfreq*1000).asInteger.asString)
				});
			});
		};
		/*
		setDBLabelVals = { arg db;
		dbLabel.size.do({ arg i;
		dbLabel[i].string = (i * db/(dbLabel.size-1)).asInteger.neg.asString;
		});
		};

		window = Window("Freq Analyzer", rect.resizeBy(pad[0] + pad[1] + 4, pad[2] + pad[3] + 4), false);
		*/
		(freqLabel.size-1).do({ arg i;
			freqLabel[i] = StaticText(window,
				//Rect(pad[0] -(freqLabelDist*0.5) + (i*freqLabelDist),pad[2] - 10, freqLabelDist, 10)
				freqLabelDist@10
			).font_(font).align_(aligns[i]);
			/*
			StaticText(window,
				//Rect(pad[0] + (i*freqLabelDist), pad[2], 1, rect.height)
			)
			.string_("")
			;
			*/
		});
		/*
		dbLabel.size.do({ arg i;
		dbLabel[i] = StaticText(window, Rect(0, pad[2] + (i*dbLabelDist), pad[0], 10))
		.font_(font)
		.align_(\left)
		;
		StaticText(window, Rect(pad[0], dbLabel[i].bounds.top, rect.width, 1))
		.string_("")
		;
		});
		*/
		setFreqLabelVals.value(freqMode, 2048);
		//setDBLabelVals.value(scope.dbRange);
	}
}

