/*
Spear
export format: Text - partials

f=SpectralFile("/Users/jorrit/Dropbox/Henderickx-Tamminga/Koningin_Zonder_Land/soundfiles/framedrum1.txt");
f.partials
f.fundamental
1/16
*/

SpectralFile4 {
	var <>fundamental, <>partials, <>data, <path;

	*new {arg pathName, startTimeTr=0.2, durTr=0.01
		/*
		, ratio=true, frequency=true, normalize=true
		, round=0.001

		, dBtr= -30
		, bandwidthTr=1.0
		, durRatioTr=0.125
		, post=false
		*/
		;
		^super.new.init(
			pathName, startTimeTr, durTr
			//, ratio, frequency, normalize, round, dBtr, bandwidthTr, durRatioTr, post
		);
	}

	init {arg
		pathName, startTimeTr=0.01, durTr=0.01
		//, ratio=true, frequency=true, normalize=true, round=0.001, dBtr, bandwidthTr, durRatioTr, post
		;

		var file, b, n, string;
		var freqs, amps, durs, loudest, tmpdata;
		"reading Spear spectral data, please wait....".postln;
		file=File(pathName, "r");

		partials=[];
		4.do{string=file.getLine(65536)};//header
		while {file.pos<file.length} {
			string=file.getLine(16777216);
			string="["++string++"]";
			string=string.replace(" ",",");
			string=string.interpret;
			partials=partials.add(string);
		};
		file.close;
		n=partials.size/2;
		data=[];

		n.do{|i|
			var par, dataa, dur;
			par=partials[i*2+1].clumps([3]).flop;//
			if (par[0][0] < startTimeTr, {
				dataa=[0,0,0,1,par[0][0]];//freq, amp, dur, attackTime, startTime
				dataa[2]=partials[i*2][3]-partials[i*2][2];
				if (dataa[2]>durTr, {
					dataa[0]=par[1].mean;

					//[dataa[0], par[2].maxItem, par[2].maxItem.ampdb].post; par[2].postcs;

					dataa[1]=par[2].maxItem.ampdb;
					dataa[3]=(par[0][par[2].indexOf(par[2].maxItem)]-par[0][0]);
					data=data.add(dataa);
				})
			})
		};
		data=data.sort({|a,b| a[0]<b[0]});

		"ready".postln;
	}
}