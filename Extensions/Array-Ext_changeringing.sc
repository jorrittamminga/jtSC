+Array {
	swapPair {}
	swapOdd {arg skip=0;
		var pairs, size, offset=0;
		size=if(
			this.size.even, {
				//offset=0;
				this.size-1;
			},{
				//offset=0;
				this.size-2;
		}).max(0);
		pairs=(offset..size+offset).clump(2);
		pairs=pairs.copyToEnd(skip);
		pairs.do{|pair| this.swap(pair[0],pair[1])}
	}

	swapEven {arg skip=0;
		var pairs, size, offset=1;
		size=if(
			this.size.even, {
				//offset=1;
				this.size-2;
			},{
				//offset=1;
				this.size-1;
		}).max(0);
		pairs=(offset..size).clump(2);
		pairs=pairs.copyToEnd(skip);
		pairs.do{|pair| this.swap(pair[0],pair[1])}
	}

	swapPairs {arg pairs=[1];
		var tmpPairs=pairs.deepCopy;
		tmpPairs=tmpPairs.select({|i| i<(this.size)});
		tmpPairs=tmpPairs.select({|i| i>0});
		tmpPairs=tmpPairs.collect{|i| [i-1, i]};
		tmpPairs.do{|pair| this.swap(pair[0],pair[1])};
	}
}