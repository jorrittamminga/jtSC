/*
Audacity v3, Spectral analysis output, works good for fftsize=4096

*/
SpectralFileA3 {
	var <pairs, <path, <peaks, <peaksN, <partials, <masked;

	*new {arg path, threshold= -120, peakThreshold= -80, freqLow=20, freqHigh=20000, minRatioDif=0.45, minInterval=0.5, maskdB= -6;
		^super.new.init(path, threshold, peakThreshold, freqLow, freqHigh, minRatioDif, minInterval, maskdB);
	}

	init {arg argpath, threshold= -120, peakThreshold, freqLow, freqHigh, minRatioDif, minInterval, maskdB;
		var file, string;
		var prevdB = -2000, freq, db, pair, peak, peakFound=false, root, indices, tmp;
		path=argpath;

		peaks=[];
		peaksN=[];
		masked=[];
		"reading Audacity spectral data, please wait....".postln;
		file=File(path, "r");
		string=file.getLine(65536);//header

		while {file.pos<file.length} {
			string=file.getLine(65536);
			string="["++string++"]";
			string=string.replace("\t",",");
			pair=string.interpret;
			freq=pair[0];
			db=pair[1];
			if (db>prevdB) {
				prevdB=db;
				peak=pair.copy;
				//peaks=peaks.add(peak);
				peakFound=false;
			} {
				if (peakFound.not) {
					if (root==nil) {
						root=peak[0];
						peak=peak.add(1.0);
					} {
						peak=peak.add(peak[0]/root);
					};
					if ( (db>threshold) && (freq>freqLow) && (freq<freqHigh)) {
						peaks=peaks.add(peak);
						//peak.postln;
					}
				};
				peakFound=true;
				prevdB=db;
			}
			//string.postln;
		};
		//pairs=file.readAllString;
		file.close;

		this.reducePeaks(minRatioDif, minInterval);

		peaksN=peaks.deepCopy;
		peaksN=peaks.flop;
		peaksN[1]=peaksN[1]-peaksN[1].maxItem;
		indices=peaksN[1].selectIndices({|db| db>=peakThreshold});
		peaksN=peaksN.collect{|peak|
			indices.collect{|i| peak[i]}
		};

		this.masking(maskdB);

		partials=();
		partials[\freq]=peaksN[0];
		partials[\db]=peaksN[1];
		partials[\ratio]=partials[\freq]/partials[\freq].minItem;

		"READY".postln;

	}


	reducePeaks {arg minRatioDif=0.45, minInterval=0.5;
		var prevdb= -200, prevIndex= -1, prevFreq=0, prevNote=0, prevRatio=0;
		var removeIndices=[], freqDev, interval, ratio, note, db, indices;

		this.peaks.do{|peak,i|
			note=peak[0].cpsmidi;

			freqDev=peak[0]-prevFreq;
			interval=note-prevNote;
			ratio=peak[2]-prevRatio;
			db=peak[1];

			if ((ratio<minRatioDif) && (interval<minInterval)) {
				if (db < prevdb) {
					removeIndices=removeIndices.add(i);
				} {
					removeIndices=removeIndices.add(i-1);
				}
			};
			prevFreq=peak[0];
			prevNote=note;
			prevdb=db;
			prevRatio=peak[2]
		};
		removeIndices=removeIndices.asSet.asArray.sort;
		//removeIndices.postln;

		indices=(0..this.peaks.size-1);
		removeIndices.do{|i| indices.remove(i)};

		peaks=indices.collect{|i| peaks[i]};
		//"ready".postln;
	}

	masking {arg dbThreshold= -6;
		var masked=[], indices;
		(peaksN[0].size-2).do{|i|
			var erb, dbdiv, freqdiv, mask;

			//erb=[peaksN[0][i].erbhalf[1], peaksN[0][i+2].erbhalf[0].abs];
			//dbdiv= [peaksN[1][i+1]-peaksN[1][i], peaksN[1][i+1]-peaksN[1][i+2]];
			//freqdiv=[peaksN[0][i+1]-peaksN[0][i], peaksN[0][i+2]-peaksN[0][i+1]];
			//(0..2).collect{|k| peaksN[0][k+i]}.post; (0..2).collect{|k| peaksN[1][k+i]}.postln;

			mask=[0,2].collect{|j|
				peaksN[1][i+1] -
				(BBandPass.magResponse( [ peaksN[0][i+1] ], 44100, peaksN[0][i+j], peaksN[0][i+j].erb/peaksN[0][i+j]).ampdb + peaksN[1][i+j])
			}.flat;

			/*
			if ( (mask[0]<0) || (mask[1]<0)) {
				"je zou het kunnen maskeren hoor!".postln;
			};
			*/

			if ( (mask[0]<0) && (mask[1]<0)) {
			//	"maskeren die handel!\n".postln;

			//if ( (dbdiv[0]<dbThreshold) && (dbdiv[1]<dbThreshold)  ) {
				//if ( (freqdiv[0]<erb[0]) || (freqdiv[1]<erb[1]) ) {
				//	[peaksN[0][i],peaksN[0][i+1],peaksN[0][i+2]].postln;
				//	[peaksN[1][i],peaksN[1][i+1],peaksN[1][i+2]].postln;
				//	dbdiv.postln;
				//	erb.post; freqdiv.postln;

					masked=masked.add(i+1);
			//	}
			}
		};

		masked=masked.asSet.asArray.sort;
		//peaksN.postln;
		if (masked.size>0) {
			//"masked out ".post; masked.postln;
			peaksN=peaksN.flop;
			indices=(0..this.peaksN.size-1);
			masked.do{|i| indices.remove(i)};
			//indices.postln;
			peaksN=indices.collect{|i| peaksN[i]};
			peaksN=peaksN.flop;
			//peaksN.postln;
		}
	}

}