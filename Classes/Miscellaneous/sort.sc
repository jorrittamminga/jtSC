QuasiKDTree2 {
	var <sortedList, <order, <list, <min, <max, <range, normalized;
	var <controlSpecs;

	*new { arg array, normalize=false;
		^super.new.init(array.deepCopy, normalize)
	}

	init {arg array, normalize;
		controlSpecs=List[];
		normalized=normalize;

		list=if (normalize, {
			array.flop.collect{|val,i|
			controlSpecs.add(ControlSpec(val.minItem,val.maxItem));
			val.normalize
		}.flop;
			},{
				array
		})
	}

	nearest {arg that, k=1;
		var thatN;
		thatN=if (normalized, {that.collect{|v,i| controlSpecs[i].unmap(v)}},{that});
		^(list.order{|a,b| (a-thatN).mean.abs<(b-thatN).mean.abs}.copyRange(0, (k-1).max(1).min(list.size-1)))
	}
}


QuasiKDTree {
	var <sortedList, <order, <list, <min, <max, <range;

	*new { arg array;
		^super.new.init(array.deepCopy)
	}

	init {arg array;
		list=array;
		sortedList=array.deepCopy.flop.collect(_.sort);//in subclass gebeurt dit al
		order=array.deepCopy.flop.collect(_.order);//in subclass gebeurt dit al
	}


	nearest {arg that;
		var i;

		i=that.collect{|v,i|
			var index, indices, floor, ceil;
			index=sortedList[i].indexInBetween(v);
			index=index.round(1.0).asInteger;
			order[i][index.asInteger]
		};

		^i.asSet.asArray.collect{|k| [k,i.occurrencesOf(k)]}.sort{|a,b| a[1]>b[1]};
	}

	nearestInRange {arg min, max, k=1;
		var i, maxOccurrences;

		i=min.size.collect{|i|
			var indexMin, indexMax;
			var minI, maxI;

			#minI, maxI=[min[i],max[i]].sort;

			indexMin=sortedList[i].indexInBetween(minI);
			indexMin=indexMin.round(1.0).asInteger;

			indexMax=sortedList[i].indexInBetween(maxI);
			indexMax=indexMax.round(1.0).asInteger;

			order[i].copyRange(indexMin,indexMax)
		}.flat;

		i=i.asSet.asArray.collect{|k| [k,i.occurrencesOf(k)]}.sort{|a,b| a[1]>b[1]};
		maxOccurrences=i.flop[1].maxItem;
		i=i.select{|j| j[1]>(maxOccurrences-k).max(1)};

		^i

	}

}

+ Array {

	sortedIndicesInBetween {arg that;
		var sortedList, order, i;

		sortedList=this.deepCopy.flop.collect(_.sort);//in subclass gebeurt dit al
		order=this.deepCopy.flop.collect(_.order);//in subclass gebeurt dit al

		i=that.collect{|v,i|
			var index, indices, floor, ceil;
			index=sortedList[i].indexInBetween(v);
			index=index.round(1.0).asInteger;
			order[i][index.asInteger]
		};

		^i.asSet.asArray.collect{|k| [k,i.occurrencesOf(k)]}.sort{|a,b| a[1]>b[1]};

	}
}