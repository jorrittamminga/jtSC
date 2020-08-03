//rtm: 1=beat, -1=rest, 1.0=tie
//rtm-bar [rhythm, meter]
//rtm-rhythm [rhythm, times]

//O JA, TIE IS NIET ECHT OFFICIEEL RTM..... IK HEB EEN TIE OP DE NOOT GEZET DIE GAAT TIEEN (IPV DIE GETIED IS) MAAR DAT MOET TOCH WELLICHT WEER TERUG NAAR ORIGINEEL RTM

//MAATSOORT BEREKENEN KAN NOG WAT ELEGANTER....

+ Array {

	rtm2{


	}

	rtmMake {
		var bars;
		bars=if (this.rank<3, {this.bubble(0, 3-this.rank)},{this});
		^bars
		}


	rtm {arg unit=4, round=0.5;
		var bar,bars;
		if (unit.size==0, {unit=[unit]});
		//rank=1: beats+divisions, rank=2: bar, rank=3: bars
		bars=this.rtmMake;
		^bars.collect({|bar,i| var count=0, meter, beats, divisions=Array.new, sums, fracs;
			fracs=bar.collect({|i|
				if (i[0]>0.0001, {
					if (i[0].frac>0, {i[0].frac},{1})
				},{-1})   }).asSet.asArray.sort.removeEvery([0, -1]);
			fracs=fracs.minItem.reciprocal.asInteger;
			meter=(bar.collect({|i| if (i[0]>0.0001, {i[0]},{0})   }).sum/unit.clipAt(i)).asFraction;
			if (fracs.isPowerOfTwo.not, {fracs=1});
			if (meter[1]<unit.clipAt(i), {meter=meter*(unit.clipAt(i)/meter[1])});
			meter=meter*fracs;
			divisions=bar.collect({|i| i[1]});
			beats=bar.collect({|i| if (i[0]==0, {1},{i[0]})});
			sums=divisions.collect({|i| i.sumRhythmDivisions});

//			DIT KLOPT NIET ALTIJD!
//			if ( (beats.asSet.size==1) && (beats.size>1) && (sums.asSet.size!=sums.size), {
//				bar=[[beats.sum, divisions.flatten]];//GAAT DAT FLATTEN ALTIJD OP?????
//				});

			[bar.collect({|rhythm|
				rhythm.rtmRhythm(unit.clipAt(i))
				}), meter];

			});
		}


	rtmInfo {arg unit=4;
		var info=(meters: List.new, notes: List.new, rests: List.new, ties: List.new);
		var bars;
		bars=this.rtm(unit);
		bars.do({|bar, i_bar|
			var meter=bar.last;
			var notes=0, rests=0, ties=0;
			info.meters.add(meter);
			bar[0].do({|rhythms, i_rhythm|
				rhythms.do({|rhythm|
					#notes, rests, ties=rhythm.rhythmCounter(notes, rests, ties);
					});
				});
			info.notes.add(notes);
			info.rests.add(rests);
			info.ties.add(ties);
		});
		info[\beats]=this.rtmListOfBeats;
		info.totalBeats=[info.meters.collect({|meter| meter[0]/meter[1]}).sum*unit, 1];
		^info
		}


	rhythmCounter {arg notes=0, rests=0, ties=0;
		var times;
		if (this.size<2, {times=[1,1]});
		this[0].do({|val|
			if (val.size==0, {
				if (val<0, {rests=rests+1});
				if (val.class==Float, {ties=ties+1});
				if ((val.class==Integer) && (val>0), {notes=notes+1});
				},{
				#notes, rests, ties=val.rhythmCounter(notes, rests, ties)
				});
			});
		^[notes, rests, ties]
		}


	rtmRhythm {arg unit=4;
		var list, tie;
		list = List.new;
		tie = this.onertm(list, 0.0, 1.0, unit);
		if (tie > 0.0, {list.add(tie) });  // check for tie at end of rhythm
		^list
	}


	onertm { arg list, tie = 0.0, stretch = 1.0, unit=4, fractionPrev=1.0;
		var beats, divisions, repeats;
		var fraction,llist=List.new, rhythmVal, timesFraction, sum, float, grace=1.0;

		#beats, divisions, repeats = this;
		//repeats = repeats ? 1;
		repeats=1;
		divisions=divisions.rhythmDivisionsSimplify;

	//CHECK FOR MULTIPLE RESTS
		if (divisions.sum.size==0, {
			if (divisions.sumRhythmDivisions + divisions.sum == 0, {
				divisions= [-1]
				})
			});

		sum=divisions.sumRhythmDivisions;
		stretch = stretch * beats.abs / sum;


	//DIT MOET NOG WAT ELEGANTER KUNNEN AUB GVD BVD
		timesFraction=if ( (((beats.abs/sum).log/2.log).frac<0.001) || (divisions.size<2), {
			[1,1]
			},{
			if ((beats.abs.log/2.log).frac<0.0001, {
				[2.pow((sum.log/2.log).asInteger), sum];
				},{
				[beats.abs.asFraction[0], sum]
				})
			});

		if (beats<0.0001, {
			grace= -1.0;
			//timesFraction=[-1, 1];
			});

		list.add([llist, grace*timesFraction]);

		repeats.do({var extra;
			divisions.do { |val| var tmp, floating=false;
				if (val.isSequenceableCollection) {
					tie = val.onertm(llist, tie, stretch, unit, fractionPrev*(timesFraction[0]/timesFraction[1]));
				}{
					floating=val.isFloat;
					if (val==0, {val=1});
					val = val * stretch;

					rhythmVal=timesFraction[0]/timesFraction[1]*fractionPrev*val.reciprocal*unit;//WAT EEN SCHOONHEID! mmm, toch niet zo....
					//rhythmVal=rhythmVal.round(1.0);

					rhythmVal=

						if ((rhythmVal.abs-rhythmVal.round(1.0).asInteger.abs).abs<0.0001, {//OF MET OVERBINDINGEN OF GEWOON KNALLEN!
							if (floating, {rhythmVal.asFloat},{rhythmVal.round(1.0).asInteger});
						},{
							tmp=val.asFraction;
							tmp=(fractionPrev*(timesFraction[0]/timesFraction[1])).reciprocal;
							(unit.reciprocal*val*tmp).asFraction.rhythmTie
						});

					if (val.abs > 0.0) {//dit is onzin volgens mij.... gecopieerd uit convertToRhythm
						//rhythmVal=if (floating, {rhythmVal.asFloat},{rhythmVal.asInteger});
						llist.add(rhythmVal);
						tie = 0.0;
					}{
						tie = tie - rhythmVal
					};
				};
			};
		});
		^tie
	}


	rhythmTie {arg notation=List.new;
		var rhythm,tmp,diff,float,sign=this[0].sign;
		float=this[0].isFloat;
		tmp=2.pow((this[0].abs.log/2.log).asInteger);
		rhythm=this[1].abs/tmp;
		if (rhythm<1, {rhythm=1; tmp=this[1].abs;});
		diff=this[0].abs-tmp;
		if (diff>0, {
			notation.add(rhythm.asFloat);
			diff=if (float, {diff.asFloat},{diff.asInteger});
			[diff,this[1].abs].rhythmTie(notation);
			},{
			rhythm=if (float, {rhythm.asFloat},{rhythm.asInteger});
			notation.add(rhythm);
			});
		^[notation*sign, [1,1]]//MMMM, niet zo fraai.....
		}


//KAN DIT NIET SLIMMER?????
	rhythmDivisionsSimplify {
		var rhythm,gcd,float, fractions;
		float=this.collect({|i| i.isFloat});
		rhythm=this.deepCopy;
		rhythm=rhythm.collect({|i| if (i.size==0, {if (i==0, {1},{i})},{if (i[0]==0, {1},{i[0]})})});
		rhythm=(rhythm.asFraction.collect({|i| i[1]}).maxItem*rhythm).asInteger;
		rhythm=rhythm/rhythm.gcd(rhythm.minItem.abs.max(1)).minItem.max(1);
		rhythm=rhythm.collect({|i,j| if (float[j], {i.asFloat},{i.asInteger}) });
		rhythm=this.collect({|i,j| var tmp=i.deepCopy; if (i.size==0, {rhythm[j]},{tmp[0]=rhythm[j]; tmp})});

		^rhythm
		}


	rtmListOfBeats {
		var beats=List.new, bars=this.rtmMake;
		bars.do({|bar|
			bar.do({|rhythm|
				beats.add(rhythm[0]);
				});
			});
		^beats.asArray
		}


	rtmBeatStretch {arg stretch=[1], round=0.25, unit=4;
		var bars=this.rtmMake, count=0, tmp;
		var beats=List.new, add;
		var tmpAdd, tmpBeats, tmpRoundedBeats;//

		bars.collect({|bar,i_bar| var count=0, meter;
			bar.collect({|rhythm, i_rhythm|
				beats.add(rhythm[0]);
				rhythm
				});
			});
		tmpBeats=beats.asArray*stretch;

		tmp=(stretch/round).asInteger*round;
		add=(stretch-tmp)*unit;
		stretch=tmp;

		beats=beats.asArray;
		beats=beats*stretch;

	//ONDERSTAANDE 4 REGELS MOGEN ER WELLICHT UIT!
	//----------------------------------
		tmpRoundedBeats=tmpBeats.collect({|i| (i/round).asInteger*round});
		tmpAdd=(tmpBeats.sum-tmpRoundedBeats.sum);
		//tmpRoundedBeats.removeEvery([0]);

		beats=tmpRoundedBeats;
		add=tmpAdd;
	//----------------------------------

		bars=bars.collect({|bar,i_bar| var meter;//moet hier die count op 0 worden gezet????
			bar.collect({|rhythm, i_rhythm|
				var beat=beats.wrapAt(count);
				count=count+1;
				[beat,rhythm[1]]
				});
			});
		if (add>0, {bars=bars.rtmAddBeat(add, [1000,1000], -1) });
		^bars
		}


//DIT GAAT MIS BIJ GENESTE DIVISIONS.... EN VERDER IS DIT EEN ZOOITJE!!!
	rtmRhythmDivisionsSplit {arg factor=0.5, round=0.25;
		var rhythm=this.collect({|i| if (i.size>0, {i[0]},{i})});//DIT MOET NOG BETER!!! HIERDOOR VERLIES JE GENESTE TROEP
		var rhythms;
		var integrated=rhythm.integrate;
		var split=rhythm.sumRhythmDivisions*factor;
		var index=integrated.indexInBetween(split);
		var w,r;
		if ((index.asInteger-index).abs>0.0001, {
			w=index.ceil.asInteger;
			r=(index.asInteger-index).abs*rhythm[w];
			rhythm[w]=rhythm[w]-r;
			rhythms=[rhythm.copyRange(0,index.floor.asInteger)++r,rhythm.copyRange(index.ceil.asInteger,rhythm.size-1)];
			},{
			if (index==0, {
				index=(split-rhythm[0]).abs;
				if (index==0, {
					rhythms=[rhythm.copyRange(0,index.asInteger),rhythm.copyRange(index.asInteger+1,rhythm.size-1)];
					},{
					rhythms=[[split],[(rhythm[0]-split)]++rhythm.copyRange(index.asInteger+1,rhythm.size-1)];
					})
				},{
				rhythms=[rhythm.copyRange(0,index.asInteger),rhythm.copyRange(index.asInteger+1,rhythm.size-1)];
				})
			});
		rhythms=[rhythms[0].rhythmDivisionsSimplify.removeEvery([0.0]),rhythms[1].rhythmDivisionsSimplify.removeEvery([0.0])];
		^rhythms
		}


	//DEZE FUNCTIE IS HIER EN DAAR NOG WAT CORRUPT..... WIL NOG WEL EENS EEN BEAT=0 DOORGEVEN.....
	rtmRhythmSplit {arg factor=0.5, round=0.125;
		var beat, division, beats, divisions, rhythm;
		#beat,division=this;
		divisions=division.rtmRhythmDivisionsSplit(factor);
		beats=[beat*factor, beat*(1-factor)];
		rhythm=[ [beats[0], divisions[0]], [beats[1], divisions[1]]];
		^rhythm
		}


	rtmSplit {arg that=[[[2,[1,2,1]]],[[2,[1,1,1]]]], unit=4;
		var barsSource=this.rtmMake, barsTarget=that.rtmMake;
		var rhythms=List.new, rhythms2=List.new, meters=barsTarget.rtmInfo.meters, barCount=0, prev=0, bars={Array.new}!meters.size;
		barsSource.do({|bar,i_bar| var count=0, meter;
			bar.do({|rhythm, i_rhythm|
				rhythms.add(rhythm);
				});
			});

		rhythms.do({|rhythm|
			var factor,tmp;
			factor=((meters[barCount][0]-prev)/meters[barCount][1])*(unit/(rhythm[0]));

			if (factor<1, {
				rhythm.rtmRhythmSplit(factor).do({|i,j|
					barCount=barCount+j;
					if ((i[0]>0)&&(i[1].size>0), { bars[barCount]=bars[barCount].add(i)})//dit is omdat die rtmRhythmSplit toch nog niet helemaal correct werkt.....
					});
				prev=0;
				},{
				prev=prev+rhythm[0];
				bars[barCount]=bars[barCount].add(rhythm);
				})
			});
		^bars.asArray;
		}


	rtmAddBeat {arg beat=1, position=[0,0], division= -1;
		var bars=this.rtmMake,tmp;
		if (position[0]>=0, {
			//tmp=bars.deepCopy;
			bars.put(position[0].asInteger.min(bars.size-1), bars[position[0].asInteger.min(bars.size-1)].insert(position[1].asInteger, [beat,[division]]));
			},{
			bars.insert(position[0].abs.asInteger, [[beat,[division]]])
			});

		^bars
		}


	rtmFit {arg that=[ [ [2,[1,1,1]]], [[2,[1,1]]]], round=0.25, unit=4;
		var barsSource=this.rtmMake, barsTarget=that.rtmMake, out;
		var infoSource=barsSource.rtmInfo(unit),infoTarget=barsTarget.rtmInfo(unit);
		var meterSumSource=infoSource.totalBeats, meterSumTarget=infoTarget.totalBeats;
		var factor=((meterSumTarget[0]/meterSumTarget[1])/(meterSumSource[0]/meterSumSource[1]));

		out=if (factor==1.0, {
			barsSource.rtmBeatStretch(factor,round,unit).rtmSplit(barsTarget,unit);
			},{
			barsSource.rtmSplit(barsTarget,unit);
			});
		^out
		}

	rtmRhythmSplit2 {arg that, factor, out;
		var rhythm=this, meters=that, beat=rhythm[0], rest=beat-(factor*beat);
		out.add(factor*beat);
		if (rest/4>(meters[1][0]/meters[1][1]), {
			meters.removeAt(0);
			factor=(meters[0][0]/meters[0][1])/(rhythm[0]/4)
			[rest,[1]].rtmRhythmSplit2(meters, factor, out)
			},{
			nil
			});
		^[out, meters]
		}

	rtmSplit2 {arg that, unit=4;
		var meters=that.rtmMake.rtmInfo.meters, rhythms=this, out=Array.new, barCount=0;
		rhythms.do({|rhythm| var factor=0.0;
			factor=(meters[barCount][0]/meters[barCount][1])/(rhythm[0]/unit);
			if (factor<1.0, {
				#out, meters=out.add(rhythm.rtmRhythmSplit2(meters, factor, out));
				});
			})
		^out
		}
}