+Array {
	deepCollectWithoutEvents { | depth = 1, function, index = 0, rank = 0 |
		if(depth.isNil) {
			rank = rank + 1;
			^this.collect { |item, i|
				item.deepCollectWithoutEvents(depth, function, i, rank) }
		};
		if (depth <= 0) {
			^function.value(this, index, rank)
		};
		depth = depth - 1;
		rank = rank + 1;
		^this.collect { |item, i|
			if (item.class==Event, {
				item=item.keys.asArray.sort.collect{|key| item[key]}
			},{
				item.deepCollectWithoutEvents(depth, function, i, rank)
			})
		}
	}


	/*
	deepCollect { | depth = 1, function, index = 0, rank = 0 |
	if(depth.isNil) {
	rank = rank + 1;
	^this.collect { |item, i| item.deepCollect(depth, function, i, rank) }
	};
	if (depth <= 0) {
	^function.value(this, index, rank)
	};
	depth = depth - 1;
	rank = rank + 1;
	^this.collect { |item, i| item.deepCollect(depth, function, i, rank) }
	}
	*/

	indexOfNextNotNil {arg i=0;
		var tmpI=i.copy;
		i=i+1;
		while({ (this[i]==nil) && (i>0) && (i<this.size) },{
			i=i+1
		});
		^if (this[i]!=nil, { i},{tmpI})
	}
	indexOfPrevNotNil {arg i=1;
		var tmpI=i.copy;
		i=i-1;
		while({ (this[i]==nil) && (i>0) && (i<this.size) },{
			i=i-1
		});
		^if (this[i]!=nil, { i},{tmpI})
	}
	nextNotNil {arg i=0;
		var tmpI=i.copy;
		i=this.indexOfNextNotNil(i);
		^if (i!=tmpI, {this[i]},{nil});
	}
	prevNotNil {arg i=1;
		var tmpI=i.copy;
		i=this.indexOfPrevNotNil(i);
		^if (i!=tmpI, {this[i]},{nil});
	}

	indispensability {
		var d=(1: [0], 2: [1,0], 3:[2,0,1], 4: [3,0,2,1], 5: [4,0,2,1,3], 7: [6,1,2,3,5,0,4]);
		var l=[];
		var f=1, x;
		this.do{|i,j|
			l=l.add((d[i]*f).rotate(-1));
			f=f*i;
		};
		x=l[0].copy;
		(this.size-1).do{|j| x=x.collect{|i,k| i+l[j+1]}.flat};
		^x.rotate(1)
	}

	binaryIterations {arg iterations=1;
		var that=this.copy;
		iterations.do{
			that=that++(that+1%2);
			that=that.flatten;
		};
		^that
	}

	asSetSerie {
		var set=this.copy.asSet.asArray.sort;
		^(set.collect{|i| this.indexOf(i)}).sort.collect{|i| this[i]}
	}


	copyRangeWrap {arg start, end;
		var tmp;
		^if (start>end, {
			this.copyToEnd(start)++this.copyRange(0, end)

		},{
			this.copyRange(start, end)
		})
	}

	map {arg value;
		^this.collect{|spec| spec.map(value)}
	}

	maxRank {
		^this.collect{|r| r.rank+1}.maxItem
	}

	clumpsrepetitions {
		^this.clumps(this.asSet.asArray.sort.collect{|i| this.occurrencesOf(i)})
	}

	*geomasEnv{arg size=4, start=1.0, grow=1.1;
		var a=Array.geom(size, start, grow);
		^Env([start, a.last], [size-1], grow.log*16)
	}

	*fib2 {arg size, a=0.0, b=1.0;
		^([a]++this.fib(size-1,a,b))
	}

	cpsbin {arg bufferSize=2048;
		^this.collect(_.cpsbin(bufferSize))
	}

	*sine3 {arg size=1024, freqs=[1], amps=[1], phases=[0pi];

		^(
			freqs.collect{|freq,i|
				((0..(size-1)).normalize*2pi*freq+phases.wrapAt(i)).sin*amps.wrapAt(i)
		}.sum.normalize*2-1)

	}


	*fillwchoose {arg size=1024, values=[0.0,1.0], weights=[0.5,0.5];
		var value=(weights.normalizeSum*size).collect{|i, j| values[j]!i}.flatten(1);
		^value.copyRange(0, size-1)
	}

	*fillBouncy {
		arg start=0.25, end=1.0, sum=4.0, curve=\exp;
		//var curve=(end/start).log*10;
		var size, array;
		/*
		size=if (curve==\exp, {
		0.5.linexp(0.0, 1.0, start, end)
		},{
		0.5.lincurve(0.0, 1.0, start, end, curve)
		}).reciprocal.round(1.0)*sum;
		*/
		size=(if (curve==\exp, {
			0.5.linexp(0.0, 1.0, start, end)
		},{
			0.5.lincurve(0.0, 1.0, start, end, curve)
		}).reciprocal*sum).round(1.0);

		array=[sum,sum];

		while({array.sum>sum}, {
			array=if (curve==\exp, {
				(0..(size-1)).normalize.linexp(0,1,start,end)
			},{
				(0..(size-1)).normalize.lincurve(0,1,start,end, curve)
			});
			size=size-1;
		});
		array=array.copyRange(1, array.size-2).normalizeSum*(sum-array.first-array.last);
		^([start]++array++[end])
	}

	*fillBouncyI {arg start=0.25, end=1.0, sum=4.0, curve=\exp;
		var array=Array.fillBouncy(start, end, sum, curve).integrate;
		array.removeAt(array.size-1);
		^([0]++array)
	}

	fillRandomTable { arg size=1024, array;
		var out;
		if (array==nil, {array=(0..(this.size-1))});
		out=(this.normalizeSum*size).round(1.0).collect{|i,j| array.wrapAt(j)!i}.flat;
		^out
	}


	*fillRandomTable { arg size=1024, array;
		var out;
		if (array==nil, {array=(0..(this.size-1))});
		out=(this.normalizeSum*size).round(1.0).collect{|i,j| array.wrapAt(j)!i}.flat;
		^out
	}

	inverse {
		^(this[0].asArray ++ ((this.differentiate*1.neg).copyToEnd(1))).integrate

	}

	makeMatrix {arg modulo;
		modulo=modulo??{this.size};
		^(this.inverse%modulo).collect{|t| this+t%modulo}
	}

	//direction: 0=clockwise, 1=counterclockwise
	//reverse: 0=outside-in, 1=inside-out
	spiral {arg direction=0, reverse=0;
		var spiral;
		spiral=(0..this[0].size/2-1).asInteger.collect{|c|
			if (direction==0, {
				[
					this[c].copyRange(c, this[c].size-1-c-1),
					(this.size-c).collect{|i| this[i][this.size-1-c]}.copyRange(c, this.size-1-c-1),
					this[this.size-1-c].reverse.copyRange(c, this[c].size-1-c-1),
					(this.size-c).collect{|i| this[i+c][c]}.reverse.copyRange(c, this.size-2-c)
				]
			},{

				[
					(this.size-c).collect{|i| this[i+c][c]}.copyRange(0, this.size-(2*c)-2),
					this[this.size-1-c].copyRange(c, this[c].size-1-c-1),
					(this.size-c).collect{|i| this[i][this.size-1-c]}.reverse.copyRange(0, this.size-(2*c)-2),
					this[c].reverse.copyRange(c, this[c].size-1-c-1)
				]

			})
		};
		^if (reverse==0, {spiral},{spiral.reverse.collect{|i| i.reverse}})
	}




	posttab {
		this.deepCollect((this.rank-1).clip(0,1), {|i|
			i.do{|j,k|
				j.post;
				if (k<(i.size-1), {"\t".post},{"".postln});
			};

		})
	}

	/*
	see also Pnoise
	Array.noise(8).plot;

	Array.noise(64, 0.0, 1.0, 0, 1.0, rand: {exprand(0.001, 1.0)}).plot;

	Array.noise(64, rand: {Pbeta(0.0, 1.0, 0.02, 0.01).asStream.next}).plot;

	({rrand(1.0, 100.0)}!100).stdev

	Array.fillBouncy(7.5, 1.875, 30).sum
	Array.fillBouncy(7.5, 1.0, 40)


	0.5.linexp(0.0, 1.0, 1.875, 7.5)
	0.5.linexp(0.0, 1.0, 7.5, 1.875)

	*/



	getControlSpec {
		var minval=this.minItem, maxval=this.maxItem, curve;
		var diff;
		diff=this.deepCopy.asSet.asArray.sort.resamp1(this.size/2).differentiate.copyToEnd(1);
		curve=(diff.size-1).collect{|i|
			//[diff[i+1], diff[i], diff[i+1]/diff[i]].round(0.01);
			diff[i+1]/diff[i]
		}.mean;
		curve=(curve.log*10)
		^ControlSpec(minval,maxval, curve)
	}


	normalizeWithCurve {
		var that=this.deepCopy;
		var controlSpec=that.getControlSpec;
		^controlSpec.unmap(that)
	}

	interpolatecs {arg division=10, type='sine', loop=true, extra, close=false, controlSpec, key=\testing;
		var array=this.asArray.resampnil, cs, min, max, warp, that, round=0.0;
		if (controlSpec==nil, {
			controlSpec=ControlSpec.specs[key];
			if (controlSpec==nil, {controlSpec=ControlSpec(array.flat.minItem, array.flat.maxItem)});
		},{
			round=controlSpec.step;
		});

		min=controlSpec.minval;
		max=controlSpec.maxval;
		warp=controlSpec.warp.asSpecifier;

		if (array.asSet.asArray.size<2, {
			that=array.resamp0(division*array.size);
		},{
			/*
			array=controlSpec.unmap(array);
			array=array.interpolate(division, type);
			array=controlSpec.map(array);
			that=array.copyRange(0, array.size-division);
			*/
			that=array.collect({|ar,i| if (ar.size>0, {ar.flat},{ar})});
			controlSpec=that.flop.collect({|a,i|
				if ((a.minItem<=0) && (warp=='exp'), {warp=0});
				if (a.minItem<min, {min=a.minItem});
				if (a.maxItem>max, {max=a.maxItem});
				ControlSpec(min, max, warp)
			});
			that=that.flop.collect({|a,k| controlSpec[k].map(controlSpec[k].unmap(a).interpolate(division, type))  }).flop.collect({|a| a.reshapeLike(array[0])});
			that=that.copyRange(0, that.size-division);
		});
		//value[key]=value[key].asArray.resampnil.interpolate(argn, argshape)//.copyRange(0, value[key].size-1*argn)

		//if (controlSpec!=nil, {if (controlSpec.step>0.0, {that=that.round(controlSpec.step)})});

		^that.round(round)


	}



	//----------------------------------------------------------------------------- RHYTHM
	sumList {arg beats=4;
		var integrate=[0], array=[this[0]];
		var sum=array.abs.sum, count=0, diff;

		while( {sum<beats}, {
			integrate=integrate.add(integrate.last+this.wrapAt(count).abs);
			count=count+1;
			array=array++this.wrapAt(count);
			sum=array.abs.sum;
		});

		diff=array.abs.sum-beats;
		array[array.size-1]=(array[array.size-1].abs-diff)*array[array.size-1].sign;

		^array
	}


	//----------------------------------------------------------------------------- VARIOUS
	maxRank {
		^if ((this.size>0) && (this.class!=String), {this.collect({|i| i.rank+1}).maxItem},{0})
	}

	asEnv {
		var levels, times, curves='lin', releaseNode, loopNode;
		levels=this[0].asArray;
		times=this[1].asArray;
		curves=this[2]??{curves};

		^Env(levels, times, curves, releaseNode, loopNode)
	}
	/*
	asEvent {//bestaat dit eigenlijk al?
	var event=();
	this.clumps([2]).do{arg i;
	event[i[0]]=i[1];
	};
	^event
	}
	*/
	*noise { arg size=32, minVal=0.0, maxVal=1.0, bits=0, w=1.0, maxBits=8, rand={1.0.rand};
		var p, o;
		size=size.round(1.0).asInteger;
		bits=bits.clip(0, maxBits);
		p=(size.log/2.log).ceil.asInteger+1;
		if (bits<=0, {bits=p.clip(0, maxBits)});
		o=p-bits;
		^(bits).collect({|i|
			var x, y, weight;
			x=2.pow(i+o);
			y=2.pow(bits-i-1);
			weight=y.pow(w);
			(rand!x).stutter(y.asInteger)*weight
		}).sum.copyRange(0,size-1).normalize.linlin(0.0, 1.0, minVal, maxVal)
	}

	*primes {arg size=12, prime=2;
		var primes=[];
		prime=prime.nextPrime;
		size.collect{ primes=primes.add(prime); prime=(prime+1).nextPrime};
		^primes
	}

	windex2 {arg minval=0.0, maxval=1.0, curve=0.0;
		var size=this.size;
		var that, jitter=1.0.rand2;
		that=(this.windex+jitter)/size;
		^that.lincurve(0.0, 1.0, minval, maxval, curve);
	}

	tableRand2 {arg minval=0.0, maxval=1.0, curve=0.0;
		var that;
		that=this.tableRand;
		^that.lincurve(0.0, 1.0, minval, maxval, curve);
	}

	stutters {arg n;
		//var that=this.deepCopy;
		^if (n.size<1, {
			this.stutter(n)},{
			this.collect{|a,i| a.asArray.stutter(n.wrapAt(i))}
		});

	}

	//[-1,0,2,4].degreeToKey([0,2,4,5,7,9,11]);


	powerset2 {
		var powerset=this.deepCopy.powerset;
		powerset.removeAllSuchThat{|i| i.size==0};

		^powerset
	}

	powersetPermutations {arg minSize=1;
		var powerset=this.deepCopy.powerset;
		powerset.removeAllSuchThat{|i| i.size<minSize};

		^powerset.collect{|p| p.allPermutations}.flatten(1)
	}

	allPermutationsSubfactorial {
		var result=[], m;
		m=this.size.collect{|i| i};
		this.size.factorial.do{|i| if ( (m-m.permute(i)).includes(0).not, {result=result.add(this.permute(i))}) };
		^result
	}

	allPermutations {arg flatten=0;
		^this.size.factorial.collect{|i| this.permute(i)}.flatten(flatten)
	}

	resampnil {
		var that, repeats=List[];
		that=this.deepCopy;
		this.removeAllSuchThat({|i| i==nil});

		that.do({|i,k|
			if (i==nil, {
				if (repeats.size==0, {repeats.add(1)});
				repeats[repeats.size-1]=repeats.last+1;
			},{
				repeats.add(1);
			});
		});
		^this.collect({|i,k| i!repeats[k]}).flatten

	}

	clumpIntegrate {
		var y=0;
		^this.integrate.collect({|i| var t=[y,i-1]; y=i; t});
	}

	split{arg splitPoints=[60], forceSplit=true, forceValue;
		^this.deepCollect(this.rank-1, {|i|
			i.splitter(splitPoints, forceSplit, forceValue)
		})
	}


	splitter {arg splitPoints=[60];
		var lists, ranges;
		//		if (splitPoints.size>1, {splitPoints=splitPoints++[10000000]});
		splitPoints=[-100000]++splitPoints++[10000000];
		lists={List[]}!(splitPoints.size-1);
		ranges=(splitPoints.size-1).collect{|i|
			//[splitPoints[i],splitPoints[i+1]-1 ]
			[splitPoints[i],splitPoints[i+1] ]
		};

		this.collect{|n|
			ranges.do{|range, i|
				if ((n>=range[0]) && (n<range[1]), {
					lists[i].add(n)
				});
			}
		};
		^lists.collect{|array|
			if (array.size<1, {
				nil
			},{
				array.asArray
			})
		}
	}

	occurrencesOfMax{arg val=[0,2];
		var that;

		val=val.asArray.sort;
		that=val.collect({|i| this.sort.occurrencesOf(i)});
		^val[that.indexOf(that.maxItem)]
	}

	occurrencesOfMaxAll{arg val=[0,2];
		var that;

		val=val.asArray.sort;
		that=val.collect({|i| this.sort.occurrencesOf(i)});
		^val[that.indicesOfEqual(that.maxItem)]
	}


	midiclef{

	}

	midisplit{arg splitPoints=[60], forceSplit=true, forceValue= -1;
		var out;
		var splitPoint=splitPoints.asArray.deepCopy;
		out=this.collect({|i| if (i.size>0, {
			i.split(splitPoints, forceSplit, forceValue)
		},{
			([i]++(forceValue!((splitPoint.size).max(0)))).rotate( (splitPoint++splitPoint.maxItem).indexInBetween(i).ceil.asInteger)
		})
		});
		^out.flop
	}

	asArrayDeep{arg that;
		that=this.collect({|i|
			if (i.size>0, {i.asArray.asArrayDeep(that)},{i});
		});
		^that
	}

	blendfold {arg that, blend=0.0, min=0, max=4, round=1.0;
		var array;
		that=((((this-that).abs>(max*0.5)).collect({|i| i.binaryValue})*(max-that))*((this-that).sign)).collect({|i,j| if (i!=0, {i}, {that[j]})});
		array=([this, that].blendAt(blend).round(round)%max);
		^array
	}

	stepblend {arg that=[0,0,0,0];
		var a,z;
		if (that.size!=this.size, {that=that.resamp0(this.size)});
		a=this.deepCopy; z=List[this]; (this-that).abs.do({|i,j| if (i!=0, {  ((1..i)/i).do({|k| a.put(j, this[j].blend(that[j],k) ); z.add(a.copy) })  });  });
		^z.asArray
	}

	stepblendfold {arg that=[0,0,0,0], min=0, max=4, round=1.0;
		var a,z;
		if (that.size!=this.size, {that=that.resamp0(this.size)});
		that=((((this-that).abs>(max*0.5)).collect({|i| i.binaryValue})*(max-that))*((this-that).sign)).collect({|i,j| if (i!=0, {i}, {that[j]})});
		a=this.deepCopy; z=List[this]; (this-that).abs.do({|i,j| if (i!=0, {  ((1..i)/i).do({|k| a.put(j, this[j].blend(that[j],k)%max ); z.add(a.copy) })  });  });
		^z.asArray
	}

	stutterArray {arg n=1;
		^if (n.size==0,
			{this.stutter(n)},
			{this.collect{|i,j| i.bubble.stutter(n.wrapAt(j))}.flatten(1) }
		)
	}

	/*
	[ [ 2, 3, 4, 5 ], [ 0, 2, 4, 5, 8 ], [1,2] ].resamp3(12, 1.0).size
	[[10,20,30,40],[1,2,3,4,5,6,7,8]].resamp3(11,1.0)
	[[1,2,3,4,5,6,7,8],[10,20,30,40]].resamp3(11,1.0)
	[[1,2,3,4,5,6,7,8],[10,20,30,40]].resamp3(11,1.0)
	[ [ 100, 200 ], [ 67, 134, 67.666666666667, 134.66666666667, 68.333333333333, 135.33333333333, 69, 136 ], [ 34, 68, 35.333333333333, 69.333333333333, 36.666666666667, 70.666666666667, 38, 72 ], [ 1, 2, 3, 4, 5, 6, 7, 8 ] ].copyRange(1,5)
	*/

	resamp3 {arg newSize=10, round=1.0;
		//var sizes=this.collect{|i| i.size};
		//var steps;

		var that=this.deepCopy;
		var sizes=(newSize/(that.size-1)).round(1.0).asInteger!that.size;
		sizes[sizes.size-1]=sizes.last+(newSize-sizes.sum);

		^(that.size-1).collect{|i| var thisSize=sizes[i];
			var resize=that[i].size.max(that[i+1].size);
			var counter=[that[i].size, that[i+1].size].resamp1(thisSize).copyRange(1,thisSize-1);
			[that[i].resize(resize), that[i+1].resize(resize)].resamp1(thisSize).copyRange(1, thisSize-1).collect{|l,j|
				l.resize(counter[j])
			}.addFirst(that[i]).round(round)
		}.flatten

		/*
		^(that.size-1).collect{|i|
		var size=that[i].size.max(that[i+1].size);
		[that[i].resize(size), that[i+1].resize(size)].resamp1(newSize/(this.size-1)).copyRange(1,size-1).collect{|a| a.round(round).removerepetitions}.addFirst(that[i])
		}.flatten
		*/
	}



	resamp2 {arg newSize, removerepetitions=false;
		var size=this.collect{|i| i.size};
		var that, indices;
		size=size.resamp1(newSize).round(1.0).asInteger;
		indices=(0..newSize-1).resamp1(this.size).round(1.0).asInteger;
		that=this.resamp1(newSize);
		indices.do{|i,j| that[i]=this[j]};
		^that.collect{|i,j| var tmp;
			if (i.size>1, {
				tmp=i.resamp1(size[j]);
				if (removerepetitions, {tmp=tmp.removerepetitions});
				tmp
			},{
				i
			})
		};
	}

	blendAtcs { arg index, controlSpec, method='clipAt';

		^if (controlSpec!=nil, {
			controlSpec.map(controlSpec.unmap(this.copyRange(index.asInteger, index.asInteger+1)).blendAt(index.frac))
		},{
			this.blendAt(index, method)
		})

	}

	/*
	blendAt { arg index, method='clipAt';
	var iMin = index.roundUp.asInteger - 1;
	^blend(this.perform(method, iMin), this.perform(method, iMin+1), absdif(index, iMin));
	}
	*/
	fill1 {arg newSize, method='wrapAt';
		^newSize.collect{|i| this.perform(method, i)}
	}

	//	[[0,1],[2,3,4,5],[10]].resamp2(10)
	// x=[1,2,3]; 7.collect{|i| x.wrapAt(i)};
	//[1,2,3].fill1(7, 'foldAt')


	uniqueIndices {
		var indices=List[], uniques=List[];
		this.do{|chord|
			var i=this.indicesOfEqual(chord).sort;
			if (i.size>1, {
				indices.add(i.copyToEnd(1))
			})
		};
		indices=indices.flatten.asSet.asArray.sort;
		uniques=(0..this.size-1);
		indices.do{|i| uniques.remove(i)};
		^uniques
	}


	removerepetitions {
		var array=this.deepCopy;
		var c=0;
		array.size.do({|i| if (array[c+1]==array[c], {array.removeAt(c+1)},{c=c+1})});
		^array
	}

	replacerepetitions {arg val= -1;
		var that;
		that=this.differentiate.collect({|i,k| if (i==0, {val},{this[k]})}); that[0]=this[0];
		^that
	}

	unbubblerepetitions{
		^this.collect{|i| if (i.size<2, {i.unbubble},{i=i.asSet.asArray.sort; if(i.size==1, {i[0]},{i});}) }
	}

	pyramidm {arg patternType=1, mirrorType=0;
		/*
		switch (mirrorType,
		0, {^this.pyramidg(patternType).collect{|i| i.mirror}.flatten},
		1, {^this.pyramidg(patternType).collect{|i|
		var mirror=i.mirror1;
		if (mirror.size==0, {i},{mirror})
		}.flatten},
		2, {^this.pyramidg(patternType).collect{|i| i.mirror2}.flatten}
		)
		*/
		^(this.pyramidgm(patternType, mirrorType).flatten)
	}


	pyramidgm {arg patternType=1, mirrorType=0;
		switch (mirrorType,
			0, {^this.pyramidg(patternType).collect{|i| i.mirror}},
			1, {^this.pyramidg(patternType).collect{|i|
				var mirror=i.mirror1;
				if (mirror.size==0, {i},{mirror})
			}},
			2, {^this.pyramidg(patternType).collect{|i| i.mirror2}}
		)
	}


	permuterepetitions {
		var array=this.deepCopy;
		var elements=this.asSet.asArray.sort;
		array.size.do({|i| var old, new;
			if (array[i+1]==array[i], {
				old=array[i];
				new=elements.foldAt(elements.indexOf(old)+1);
				if (i+2<(array.size-1),{
					if (new==array[i+2], {
						new=elements.foldAt(elements.indexOf(old)-1)
					});
				});
				array[i+1]=new;
			})
		});
		^array
	}

	powersetn {arg n=1;//make more effecient!!!!
		var tmp;
		^this.powerset.select({|i| i.size==n});
	}



	selectRange {arg min=0, max=100;
		this.removeAllSuchThat({|i| (i<min) || (i>max) });
		^this
	}

	sectInScale {arg scale, stepsPerOctave=12;
		var that=List[];
		this.do{|i,j|
			if (scale.includes(i%stepsPerOctave), {that.add(i)})
		};
		^that.asArray
	}

}