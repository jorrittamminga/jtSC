/*
Spear
export format: Text - partials

f=SpectralFile("/Users/jorrit/Dropbox/Henderickx-Tamminga/Koningin_Zonder_Land/soundfiles/framedrum1.txt");
f.partials
f.fundamental
1/16
*/

SpectralFile2 {
	var <>fundamental, <>partials, <>data, <path;

	*new {arg pathName, ratio=true, frequency=true, normalize=true, round=0.001, startTimeTr=0.2, durTr=0.01, dBtr= -30, bandwidthTr=1.0, durRatioTr=0.125, post=false;
		^super.new.init(pathName, ratio, frequency, normalize, round, startTimeTr, durTr, dBtr, bandwidthTr, durRatioTr, post);
	}

	init {arg pathName, ratio=true, frequency=true, normalize=true, round=0.001, startTimeTr=0.01, durTr=0.01, dBtr, bandwidthTr, durRatioTr, post;

		var file, b, n;
		var freqs, amps, durs, loudest, tmpdata;
		file=File(pathName, "r");
		partials=file.readAllString;
		file.close;

		"reading spectral data, please wait....".postln;

		partials=partials.copyToEnd(partials.find("partials-data\n")+14);
		partials=partials.replace("\n", "],[");
		2.do{partials.removeAt(partials.size-1)};
		partials=partials.addFirst("[[");
		partials=partials++"]";
		partials=partials.replace(" ", ", ");
		partials=partials.interpret;


		n=partials.size/2;

		//freqs={}!n;
		//amps={}!n;
		//durs={}!n;
		data=[];

		n.do{|i|
			var par, dataa, dur;
			par=partials[i*2+1].clumps([3]).flop;//
			//dur=
			//startTimeTr, durTr

			if (par[0][0] < startTimeTr, {
				dataa=[0,0,0,1,par[0][0]];//freq, amp, dur, attackTime, startTime
				dataa[2]=partials[i*2][3]-partials[i*2][2];
				if (dataa[2]>durTr, {
					//par.postln;
					dataa[0]=par[1].mean;
					dataa[1]=par[2].maxItem.ampdb;
					dataa[3]=(par[0][par[2].indexOf(par[2].maxItem)]-par[0][0]);
					data=data.add(dataa);
				})
				//dataa.postln;
			})
		};

		data=data.sort({|a,b| a[0]<b[0]});
		partials=();
		tmpdata=[data[0]];

		if (post, {data[0].postln});

		(1..(data.size-1)).do{|i|
			var dataa=data[i];
			var interval, db, durRatio;
			interval=(dataa[0].cpsmidi - tmpdata.last[0].cpsmidi);
			db=dataa[1]-tmpdata.last[1];
			durRatio=dataa[2]/tmpdata.last[2];
			if (post, {
				[dataa[0], dataa[1], dataa[2], interval, db, durRatio, tmpdata.last[0], tmpdata.last[2]].round(0.01).postln;
			});
			if (db > dBtr, {
				if (interval<bandwidthTr, {
					if (db>0.0, {
						if (durRatio > durRatioTr, {
							if (post, {"replace1".postln});
							tmpdata[tmpdata.size-1]=dataa;
						})
					})
				},{
					if (durRatio > durRatioTr.reciprocal, {
						if (post, {"replace2".postln});
						tmpdata[tmpdata.size-1]=dataa;
					},{
						if (durRatio > durRatioTr, {
							if (post, {"add".postln});
							tmpdata=tmpdata.add(dataa);
						})
					})
				})
			})
		};


		data=tmpdata;

		partials[\freq]=data.flop[0];
		partials[\db]=data.flop[1];
		partials[\db]=partials[\db]-partials[\db].maxItem;
		partials[\dur]=data.flop[2];
		partials[\attackTime]=data.flop[3];
		partials[\decayTime]=partials[\dur]-partials[\attackTime];
		partials[\startTime]=data.flop[4];
		fundamental=partials[\freq][0];
		partials[\fundamental]=fundamental;
		partials[\ratio]=partials[\freq]/fundamental;

		/*
		fundamental=data[0][0];
		loudest=data.collect{|i| i[2]}.maxItem;

		partials=data.collect{|data|
		data[1]=data[0]/fundamental;
		data[2]=(data[2]/loudest).ampdb;
		if (frequency.not, {data.removeAt(0)});
		if (ratio.not, {
		data.removeAt(frequency.binaryValue)
		});
		data.round(round).postln;
		data
		};

		partials.removeAllSuchThat{|p| (p[2]== -inf) || (p[3]==0.0)};
		*/
		"ready".postln;
	}

	/*
	write {arg path;



	}
	*/
}