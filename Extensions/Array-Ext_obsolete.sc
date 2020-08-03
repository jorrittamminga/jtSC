+Array {



	//----------------------------------------------------------------------------- OBSOLETE
	merge {arg seperator=""; //dit is dus eigenlijk join....
		var string="";
		this.do({arg str, i;
			string=string++str;
			if (i<(this.size-1), {
				string=string++seperator;
			})
		});
		^string
	}

	//is dus gewoon separate.....
	clumpsClusters {arg thresh=1.1;
		var groupSizeList=[];
		this.sort;
		(this.size-1).do{|i|
			if (this[i+1]/this[i]>thresh, {
				groupSizeList=groupSizeList.add(i+1);
		})};
		groupSizeList=groupSizeList.add(this.size);
		groupSizeList=groupSizeList.differentiate;
		^this.clumps(groupSizeList)

	}
	//is dus gewoon separate.....
	clumpsClustersDif {arg thresh=1.0;
		var groupSizeList=[];
		this.sort;
		(this.size-1).do{|i|
			if (this[i+1]-this[i]>thresh, {
				groupSizeList=groupSizeList.add(i+1);
		})};
		groupSizeList=groupSizeList.add(this.size);
		groupSizeList=groupSizeList.differentiate;
		^this.clumps(groupSizeList)

	}

}