+ Collection {

	deepCollectKeys { | depth = 1, function, index = 0, rank = 0, keys |
		if(depth.isNil) {
			rank = rank + 1;
			^this.collect { |item, i|
				item.deepCollectKeys(depth, function, i, rank, keys) }
		};
		if (depth <= 0) {
			^function.value(this, index, rank)
		};
		depth = depth - 1;
		rank = rank + 1;
		^this.collect { |item, i|
			var tmp;
			if (item.class==Event, {
				if (keys==nil, {

				},{
					tmp=();
					keys.do{|key| tmp[key]=item[key]};
					item=tmp//item.keys.asArray.sort.collect{|key| item[key]}
				});
			});
			item.deepCollectKeys(depth, function, i, rank, keys)
		}
	}


	blendAtIndices {arg indices, method='clipAt';
		var a=this.deepCopy; indices.do{arg i; a=a.blendAt(i, method) }; ^a
	}

	removeAndShift {arg i;
		var y,z;
		y=this.deepCopy.asString;
		y=y.replace(i.asString, "");
		y=y.replace(", , ", ", ");
		y=y.replace("[ , ", "[ ");
		y=y.replace(",  ]", " ]");
		y=y.interpret;
		z=y.deepCopy.flat;
		z=z.collect{|k| if (k>i, {k-1},{k})};
		z=z.reshapeLike(y);
		^z
	}

	deepDoo{arg function={|i| i.postln};
		this.do({|that|
			if (that.class==this.class, {that.deepDoo(function)},{that.postln})
		})
	}

	includesCollection { arg that;
		var flags;
		flags=this.collect({|i| i==that});
		^flags.includes(true)

	}

}
