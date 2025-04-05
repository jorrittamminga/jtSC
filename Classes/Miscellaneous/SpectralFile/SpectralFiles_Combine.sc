/*
Spear
export format: Text - partials

f=SpectralFile("/Users/jorrit/Dropbox/Henderickx-Tamminga/Koningin_Zonder_Land/soundfiles/framedrum1.txt");
f.partials
f.fundamental
1/16
*/

SpectralFilesCombine {
	var <partials, <rawData;

	*new {arg audacityFilePath, spearFilePath
		, threshold= -120, peakThreshold= -80, minRatioDif=0.45, minInterval=0.5
		, startTimeTr=0.2, durTr=0.01
		;

		^super.new.init(audacityFilePath, spearFilePath, threshold, peakThreshold, minRatioDif, minInterval
			, startTimeTr, durTr);
	}

	init {arg audacityFilePath, spearFilePath, threshold, peakThreshold, minRatioDif, minInterval
		, startTimeTr, durTr;
		var f, z, i, j, p, freqs, indices;
		var audacity, spear, data, peaksN;
		var removes=1;

		spear=SpectralFile4(spearFilePath, startTimeTr, durTr);
		data=spear.data;
		z=spear.data.deepCopy;
		z=z.flop;
		freqs=z[0];
		audacity=SpectralFileA3(audacityFilePath, threshold, peakThreshold, freqs.minItem*0.9, freqs.maxItem*1.1, minRatioDif, minInterval);
		peaksN=audacity.peaksN;

		freqs=peaksN[0];
		f=peaksN.flop;
		i=peaksN[0].collect{|freq|
			var index;
			index=z[0].indexInBetween(freq);
			index
		};
		indices=[];

		i.do{|index,i|
			var a, b, ind;
			a=data[index.floor.asInteger];
			b=data[index.ceil.asInteger];

			if ( (index.round(1.0)-index).abs<0.2 ) {
				ind=index.round(1.0).asInteger;
			} {
				if ( (a[2]>b[2]) && (a[3]>b[3])) {
					ind=index.floor.asInteger
				} {
					if ( (a[2]<=b[2]) && (a[3]<=b[3])) {
						ind=index.ceil.asInteger
					} {
						ind=index.floor.asInteger;
						if (indices.includes(ind)) {
							ind=index.ceil.asInteger
						};
					}
				}
			};
			indices=indices.add(ind)
		};
		indices=indices.asSet.asArray.sort;
		rawData=indices.collect{|i| data[i]};

		partials=();
		partials[\freq]=rawData.flop[0];
		partials[\db]=rawData.flop[1];
		partials[\db]=partials[\db]-partials[\db].maxItem;
		partials[\decayTime]=rawData.flop[2];
		partials[\ratio]=partials[\freq]/partials[\freq][0];



		//-------------------------------------------------------- REDUCE

		p=partials.deepCopy;
		while {removes>0}  {
			//"\nreduce!".postln;
			removes=0;
			j=(0..(p[\decayTime].size-1));
			p[\decayTime].size.do{|i|
				var ratio, freq, db, decayTime, flags=[0,0,0], out;
				[\freq, \ratio, \decayTime, \db].do{|key|
					var val=p[key][i], out;
					if (key==\decayTime) {
						out=[key, val, val/(p[key].clipAt(i-1)), val/(p[key].clipAt(i+1))]
					} {
						out=[key, val, val-(p[key].clipAt(i-1)), val-(p[key].clipAt(i+1))]
					};
					//out.postln;
					switch(key)
					//	{\freq} {}
					{\ratio} {  flags[0]=(out[2]<0.75) &&  (out[3].abs<0.75) }
					{\decayTime} {flags[1] = ( (out[2]<1.0) || (out[3]<1.0)) }
					{\db} {flags[2] = (( out[2]< 3.neg) || (out[3]<3.neg)) }
					;
				};
				//flags.postln;
				if ((flags.collect(_.binaryValue).sum==flags.size)) {
					//"remove ".post; i.postln;
					removes=removes+1;
					j.remove(i)
				};
			};
			//j.postln;
			p.keysDo{|key| p[key]=j.collect{|i| p[key][i]} };
		};

		partials=p.deepCopy;
	}
}