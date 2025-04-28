SmoothArrayJT {
	var <size;
	var <array, <index, <funcMean, <funcMedian, <funcAdd, <lastValue;

	*new {arg size;
		^super.newCopyArgs(size).init
	}
	init {
		this.makeArray;
		funcAdd={arg value;
			array[index]=value;
			index=index+1%size;
		};
		funcMean={array.mean};
	}
	makeArray {
		array=Array.fill(size, {0.0});
		index=0;
	}
	size_ {arg value;
		if (size!=value) {
			size=value.max(1);
			if (size>1) {
				array=array.asArray.lace(size);
				index=index%size;
				funcAdd={arg value;
					array[index]=value;
					index=index+1%size;
				};
				funcMean={array.mean};
				funcMedian={array.median};
			} {
				array=array.wrapAt(index-1);
				index=0;
				funcAdd={arg value;array=value};
				funcMean={array};
				funcMedian={array};
			}
		}
	}
	add {arg value;
		/*
		array[index]=value;
		index=index+1%size;
		*/
		funcAdd.value(value)
	}
	mean {^funcMean.value}
	median{^funcMedian.value}
	smooth{arg value, method='mean';
		^this.add(value).mean
	}
}
//RunningSum