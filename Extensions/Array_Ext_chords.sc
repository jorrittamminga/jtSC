+Array {



//--------------------------------------------------------------- CHORD
	sortchord {arg octave=12;
		var out=this.copy;

		out=out.differentiate;
		out=out.collect{|i|
			if (i<0, {
				var add=(i.abs/octave).ceil*octave;
				i+add
			},{i})
		}.integrate;
		^out
	}

	rotatechord {arg rotation = 1, fixedBase=false, octave=12, sort=true;
		//var out=this.rotate(rotation.neg);
		//(out.size-1).do{|i| while ({out[i]>out[i+1]}, {out[i+1]=out[i+1]+octave}) };
		//^out.sortchord(octave)
		var tmp, base=this[0];
		tmp=this.rotate(rotation.neg);
		if (sort, {tmp=tmp.sortchord(octave)});
		if (fixedBase,{tmp=tmp-(tmp[0]-base)});
		^tmp

	}

	permutechord {arg nthPermutation=1, fixedBase=false;
		var base=this[0],chord;
		chord=this.permute(nthPermutation);
		(chord.size-1).do({|i|
			if (chord[i+1]<chord[i], {
				chord[i+1]=(chord[i]-chord[i+1]/12).ceil*12+chord[i+1]
			});
		});
		if (fixedBase, {chord=chord-chord[0]+base});
		chord=chord.sortchord;
		^chord
	}

	allintervals {
		^this.copyRange(0,this.size-2).collect{|note,i| this.copyToEnd(i+1)-note}.flat.abs
	}

	allintervals2 {
		^this.copyRange(0,this.size-2).collect{|note,i| this.copyToEnd(i+1)-note}.flat
	}

	chordValue {arg weight=[1];
		var list=this.deepCopy.sort.differentiate.copyToEnd(1);
		list=list.collect{|i,j| i*weight.clipAt(j)};
		^list.sum
	}

	//fb=0

	combinationtoneschord {|round=1.0, add=false, fb=0|
		^this.ringmodulatechord(nil, round, add, fb, true)
	}

	ringmodulatechord {|chord2=nil, round=1.0, add=false, fb=0, removelow=true|
		var chord1, sum, out, removeOctaves=false;
		//if (chord2==nil, {chord2=this.deepCopy;});
		chord1=this.asArray.asSet.asArray.sort.midicps;

		if (chord2==nil, {
			sum=chord1.powersetn(2).collect{|i|
				[[1,1],[1,-1]].collect{|r| (r*i).sum.abs};
			}.flat.asSet.asArray.sort;
		},{
			chord2=chord2.asArray.asSet.asArray.sort.midicps;
			sum=((chord1 +.x chord2) ++ (chord1 -.x chord2));
			sum=sum.asSet.asArray.abs;
		});
		/*
		*/
		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		out=sum.cpsmidi.round(round).asSet.asArray.sort;
		if (fb>0, {out=out.ringmodulatechord(nil, round, false, fb-1)});
		out=out.asSet.asArray.sort;

		if (add, {out=(out++this).sort.round(round)});
		^out
	}

	frequencymodulatechord {arg chord2=nil, index=1, round=1.0, add=false, removelow=true, combinationTones=true;
		var chord1, sum, diff, out, addition, removeOctaves=false;
		if (chord2==nil, {chord2=this.deepCopy;});
		addition=chord2.deepCopy.asSet.asArray.sort;
		chord1=this.asArray.asSet.asArray.sort.midicps;
		chord2=chord2.asArray.asSet.asArray.sort.midicps;
		sum=(index+1).collect{|i| (chord1 +.x (chord2*i))}.flat;
		diff=(index+1).collect{|i| (chord1 -.x (chord2*i))}.flat;
		sum=diff ++ sum;
		sum=sum.flat.asSet.asArray.abs;
		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		out=sum.cpsmidi.round(round).asSet.asArray.sort;
		if (add, {out=(out++addition)});
		out=out.asSet.asArray.sort;
		^out
	}

	sumchord {|chord2, round=1.0, order=1, add=false, removelow=true|
		var chord1, sum;
		if (chord2!=nil, {chord1=chord1++chord2});
		chord1=this.asSet.asArray.sort.midicps;
		order=order.asArray.abs;

		sum=order.collect{|o|
			(chord1.size-1).collect{|j|
				(chord1.size-j-1).collect{|i|
					if (o>1, {
						o*chord1[j]+((o-1)*chord1[j+i+1])
					},{
						chord1[j+i+1]+chord1[j]
					})
				}
		}}.flat.abs.asSet.asArray.sort;

		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		sum=sum.cpsmidi;
		if (add, {sum=(sum++this)});
		^sum.round(round).asSet.asArray.sort
	}

	diffchord {|chord2, round=1.0, order=1, add=false, removelow=true|
		var chord1, sum;
		if (chord2!=nil, {chord1=chord1++chord2});
		chord1=this.asSet.asArray.sort.midicps;
		order=order.asArray.abs;

		sum=order.collect{|o|
			(chord1.size-1).collect{|j|
				(chord1.size-j-1).collect{|i|
					if (o>1, {
						o*chord1[j]-((o-1)*chord1[j+i+1])
					},{
						chord1[j+i+1]-chord1[j]
					})
				}
		}}.flat.abs.asSet.asArray.sort;
		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		sum=sum.cpsmidi;
		if (add, {sum=(sum++this)});
		^sum.round(round).asSet.asArray.sort
	}

	freqshiftchord {|freq, round=1.0, fixedBase=false|
		var sum, chord, base;
		chord=this.asArray.sort;
		base=chord[0];
		chord=chord.midicps;
		sum=(chord + freq);
		sum=sum.asSet.asArray.abs;
		//		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		chord=sum.cpsmidi.round(round).asSet.asArray.sort;
		if (fixedBase, {chord=chord-chord[0]+base});
		^chord
	}

	distortchord {|distortion=1.0, round=1.0, base=0|
		var chord, sum, partials;
		chord=this.asArray.sort.midicps;
		if (base==nil, {base=0});
		base=chord.clipAt(base);
		chord=(chord/base).collect({|i| (i.pow(distortion))*base});
		//		if (removelow, {sum.removeAllSuchThat({|i| i<=0.midicps})});
		^chord.cpsmidi.round(round).asSet.asArray.sort
	}

	stretchchord {|stretch=1.0, round=1.0, baseIndex=0|
		var chord, base, intervals;
		//chord=if (this[0].class==String, {chord=this.namemidi.sort},{this.sort});
		chord=this.sort;//chord=
		base=chord[0];
		intervals=chord.differentiate;
		intervals[0]=0;
		chord=(base+(intervals*stretch).integrate.round(round));
		if (baseIndex>0, {chord=chord-(chord.blendAt(baseIndex)-this.blendAt(baseIndex))});
		^chord.round(round)
	}

	splitchord {arg numberOfStaves=2, removeDoubles=true;
		var a, that;
		that=if (removeDoubles, {that=this.asSet.asArray.sort},{this.sort});
		if ((numberOfStaves>1), {
			if (that.size>1, {
				a=(that.differentiate.copyToEnd(1).order.reverse+1).collect({|i|
					that[i]
				});
				a.swap(0, a.indexOfGreaterThan(that.mean));
				^that.split(a.copyRange(0,numberOfStaves-2).sort)
			},{
				^that
			})
		},{
			^[that]
		})
		/*
		arg simplify=false, hands=2, fingers=4, staves=2;
		var test, octave, splitPoints, tmp, alternativeSplitPoint, ambitus=this.maxItem-this.minItem;
		octave=(this.mean/12).round(1.0).asInteger;
		if (ambitus<30, {if (octave.even, {octave=octave-1})});
		splitPoints=octave*12;

		tmp=this.differentiate;
		tmp[0]=0;
		alternativeSplitPoint=this[tmp.indexOf(tmp.maxItem)];
		if ((alternativeSplitPoint-splitPoints).abs<7, {splitPoints=alternativeSplitPoint});
		if (splitPoints-this.minItem<7, {splitPoints=this.minItem});

		test=this.split(splitPoints);
		if ((test[0].size==1), {
		if (test[0][0]>(splitPoints-5), {
		test[1]=test[1].addFirst(test[0][0]); test[0]=[]
		})
		});
		if (test[0].size==0, {if (test[1].mean<60, { test=test.reverse   })});
		^test
		*/
	}
	/*
	[12,24,48,60].splitchord(2)
	[56,57,62,73,74,76, 85, 99,110].split([60,80])
	[56,57,62,73,74,76, 85, 99,110].splitchord(3)
	[56,57,62,73,74,76].splitchord
	[12,24,48,88].splitchord(3)
	[46,47,59,62,63].splitchord
	[24,59,68,72].midiToNotes(staffChange:false)

	x=[59,61,72,74,75];x.splitchord
	x=[59,61,71,74,75]; x.minItem; x.maxItem; (x.maxItem-x.minItem); x.mean; y=x.differentiate; y.removeAt(0); x.split(x[y.order.last+1])
	x=[49,59,71,74,75]; x.minItem; x.maxItem; (x.maxItem-x.minItem); x.mean; y=x.differentiate; y.removeAt(0); x.split(x[y.order.last+1])
	x=[48,59,61,72]; x.minItem; x.maxItem; (x.maxItem-x.minItem); x.mean; y=x.differentiate; y.removeAt(0); x.split(x[y.order.last+1])
	x=[48,59,61,66,72]; x.minItem; x.maxItem; (x.maxItem-x.minItem); x.mean; y=x.differentiate; y.removeAt(0); x.split(x[y.order.last+1])

	[24,[36,61,72,74,75],68,72].midiToNotes(2,staffChange:false, staffValue:1)
	[24,[36,61,72,74,75],68,72].midiToNotes(1,staffChange:false, staffValue:1)

	*/


}