+Array {

	midiToNotes {arg numberOfStaves=2, availableClefs=[0,1], clefChange=false, staffChange=true, clefRange=6, repeats=true, staffValue=0, replace=10.neg, clefsAndRanges=(bass: [39,60], tenor:[48,69], alto:[54,75], treble:[60,81]), clefsNames=(0: \bass, 1: \treble, 2: \alto, 3: \tenor);

		var notes=this.deepCopy;
		var ranges, rangesWide, c=1, clefs, clefs2, clefsAll, ottavaRanges, notes2;
		var out={|i| (midi:replace!notes.size, clef:replace!notes.size, staff:(staffValue+i)!notes.size, ottava:0!notes.size)}!numberOfStaves;

		if (numberOfStaves<2, {staffChange=false});

		ranges=availableClefs.collect({|clef| if (clef.class==Integer, {clef=clefsNames[clef]}); clefsAndRanges[clef]});
		//ranges=ranges.sort({|a,b| a[0]<=b[0]});
		ottavaRanges=ranges.deepCopy;
		ranges[0][0]=0; ranges[ranges.size-1][1]=128;
		rangesWide=ranges.collect({|range| range+[clefRange.neg,clefRange]});
		ottavaRanges=ottavaRanges.collect({|range| range+[clefRange.neg,clefRange]});//is dit niet gewoon rangesWide???

		notes2=notes.collect({|note|
			var chord;
			if (note.size>0, {
				chord=note.splitchord;
				if (chord[0].size==0, {
					chord.reverse
				},{
					chord
				})
			},{
				[note,[]]
			})
		}).flop.collect({|notes,staff|
			notes.collect({|note|
				note=note.unbubble;
				if (note==nil, {note=replace});
				note
			})
		});
		notes2;

		clefs=notes.deepCopy.collect({|note|
			note=note.deepCopy.asArray.mean;
			while({((note>rangesWide[c].minItem)&&(note<rangesWide[c].maxItem)).not},{
				if (note<=rangesWide[c].minItem, {c=c-1}, {c=c+1});
				c
			});
			c
		});
		clefs;
		clefs2=notes2.deepCopy.collect({|notes, staff|
			var c=1;
			c=availableClefs.sort.clipAt(staff);
			notes.collect({|note|
				note=note.deepCopy.asArray.mean;
				if (note>=0, {
					while({((note>rangesWide[c].minItem)&&(note<rangesWide[c].maxItem)).not},{
						if (note<=rangesWide[c].minItem, {c=c-1}, {c=c+1});
						c
					});
				});
				c
			})
		});

		clefsAll=clefs.deepCopy;
		clefsAll=clefsAll.asSet.asArray.sort; clefsAll=clefsAll.clumps([(clefsAll.size/numberOfStaves).ceil.max(1)]);

		if(staffChange, {
			//out[0][\staff]=clefs.deepCopy;
			out[0][\staff]=clefs2[0].deepCopy;

			(0..(numberOfStaves-1)).do({|staff|
				var cl;

				out[staff][\midi]=notes2[staff].deepCopy;

				if (clefChange, {
					out[staff][\clef]=clefs2.deepCopy;
				},{
					//	numberOfStaves.do({|i|
					cl=clefsAll.wrapAt(staff);
					cl=clefs2[staff].deepCopy.occurrencesOfMax(cl);
					out[staff][\clef]=cl!notes.size
					//	});
				});
			});
			/*
			[24,[36,61,82,94,95],68,72].midiToNotes(staffChange:true);
			ottava moet de vorige ottava waarde ook meenemen!
			*/
			(0..(numberOfStaves-1)).do({|staff|
				notes2[staff].deepCopy.collect({|note,k|
					var range=(ottavaRanges.deepCopy[out[out[staff][\staff][k]][\clef][k]]).deepCopy, o= 0;
					note=note.deepCopy.asArray.mean;
					if (note>replace, {
						[note,range,ottavaRanges, out[out[staff][\staff][k]][\clef][k], out[out[staff][\staff][k]][\clef][k] ];
						while( {((note>range.minItem) && (note<range.maxItem)).not}, {if(note<=range.minItem,{o=o-1; range[0]=range[0]-12; },{o=o+1; range[1]=range[1]+12}); });
						o=o.clip(-2,2);
					},{o=replace});
					out[staff][\ottava][k]=o
				});
				//(1..(numberOfStaves-1)).do({|staff| out[staff][\midi]=notes2[staff]});//hier ook nog ottava's e.d inbouwen!!!!
			})
		},{
			/*
			[24,[36,61,82,94,95],68,72].midiToNotes(staffChange:false)
			*/
			numberOfStaves.do({|staff|
				out[staff][\clef]=(clefs.deepCopy.occurrencesOfMax(clefsAll[staff]))!notes.size;
				notes.deepCopy.do({|note, i|
					var cl=replace;
					//out[staff][\midi][i]=if (clefsAll.wrapAt(staff).includes(clefs[i]), {cl=clefs[i]; note},{replace});//OLD CODE
					out[staff][\midi][i]=
					if (clefsAll.wrapAt(staff).includes(clefs[i]),
						{
							cl=clefs[i]; notes2[staff][i]
						},{
							if (staff==0, {replace},{
								if (notes2[staff][i]==replace, {replace}, {notes2[staff][i]})
							})
					});

					if (clefChange, {
						out[staff][\clef][i]=cl;
					});
				});
				out[staff][\midi].deepCopy.collect({|note,k|
					var range=(ottavaRanges.deepCopy[out[staff][\clef][k]]).deepCopy, o=0;
					note=note.deepCopy.asArray.mean;
					if (note>=0, {
						while( {((note>range.minItem) && (note<range.maxItem)).not}, {if(note<=range.minItem,{o=o-1; range[0]=range[0]-12; },{o=o+1; range[1]=range[1]+12}); });
					},{nil});
					out[staff][\ottava][k]=o.clip(-2,2)
				});
			})
		});

		if (repeats.not, {
			numberOfStaves.do({|i|
				[\staff, \clef, \ottava].do({|par|
					out[i][par]=out[i][par].replacerepetitions(replace)
				})
			})
		});
		if (numberOfStaves<2, {out=out.unbubble});
		^out
	}


}