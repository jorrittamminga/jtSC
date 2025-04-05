SpectralFile {
	var <>fundamental, <>partials;

	*new {arg pathName, ratio=true, frequency=true, normalize=true, round=0.001;
		^super.new.init(pathName, ratio, frequency, normalize, round);
	}

	init {arg pathName, ratio=true, frequency=true, normalize=true, round=0.001;

		var file, b, n, string;
		var freqs, amps, durs, data, loudest;
		"reading spectral data, please wait....".postln;
		file=File(pathName, "r");

		partials=[];
		4.do{string=file.getLine(65536)};//header
		while {file.pos<file.length} {
			string=file.getLine(65536);//header
			string="["++string++"]";
			string=string.replace(" ",",");
			string=string.interpret;
			partials=partials.add(string);
		};
		file.close;
		/*
		partials=file.readAllString;
		file.close;
		partials=partials.copyToEnd(partials.find("partials-data\n")+14);
		partials=partials.replace("\n", "],[");
		2.do{partials.removeAt(partials.size-1)};
		partials=partials.addFirst("[[");
		partials=partials++"]";
		partials=partials.replace(" ", ", ");
		partials=partials.interpret;
		*/
		n=partials.size/2;

		freqs={}!n;
		amps={}!n;
		durs={}!n;
		data={}!n;

		n.do{|i|
			var par;
			data[i]=[0,1,0,0];//freq, ratio, amp, dur
			data[i][3]=partials[i*2][3]-partials[i*2][2];
			par=partials[i*2+1].clumps([3]).flop;
			data[i][0]=par[1].mean;
			data[i][2]=par[2].maxItem;
		};

		data=data.sort({|a,b| a[0]<b[0]});
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

		"ready".postln;
	}

	/*
	write {arg path;



	}
	*/
}

/*
f=SpectralFile("/Users/jorrit/Dropbox/Henderickx-Tamminga/Koningin_Zonder_Land/soundfiles/framedrum1.txt");
f.partials
f.fundamental
*/