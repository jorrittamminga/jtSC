+Array {

	//zorg ook dat er akkoorden mogelijk zijn met minder tonen dan de tune
	//zorg dat je ook een modus kunt ingeven zonder '0'
	//en dat verdubbelingen van akkoordtonen mogelijk zijn (maar dat kan ook door octaven toe te voegen aan de modus)
	//bij include kunnen ook andere noten uit de modus (b.v. er moet een kwint en terts in oid)
	//je kunt ook denken aan rotaties van de modus (dus geen transposities) inclusief een include
	allChordsGuitar {arg tune=[0,5,10,15,19,24], maxTransposition=12, minStrings, maxStrings, maxStretch=3, minOpenStrings=2, includeRoot=true;
		var modes, modes2=List[], chords=List[];
		var octaves, transpositions, fingerings=List[];
		var time=Main.elapsedTime;
		"calculating....".postln;

		modes=this.size.factorial.collect{|i|
			this.permute(i).copyRange(0, tune.size-1).sort }.asSet.asArray;
		if (includeRoot, {modes.do{|mo| if (mo.includes(0), {modes2.add(mo)})};
			modes=modes2;
		});
		modes=(0..11).collect{|i| (modes+i)%12}.flatten.asSet.asArray.collect(_.sort);
		octaves=(0..11).collect{|nn|
			[
				((nn-tune/12).neg.ceil*12),
				(((tune+maxTransposition)-nn/12).asInteger*12)
			].flop.collect({|p| p.asSet.asArray.sort.abs})
		};

		modes.do{|mode|
			mode.size.factorial.do{|permutation|
				var modeP, transpositions, numberOfOpenStrings;
				modeP=mode.permute(permutation);
				transpositions=modeP.collect{|nn, i| octaves[nn][i]}.allTuples;
				transpositions.do{|octave|
					var modeO=modeP+octave, fingering, stretch, fingering2;
					fingering=(modeO-tune);

					if ( (fingering.select{|i| i<0}.size==0) && (fingering.select{|i| i>11}.size==0), {
						numberOfOpenStrings=fingering.count{|i| i==0};

						if (numberOfOpenStrings >= minOpenStrings, {
							if (numberOfOpenStrings==tune.size, {
								chords.add(modeO);
								fingerings.add(fingering);
							},{
								fingering2=fingering.deepCopy;
								fingering2.removeAllSuchThat{|i| i==0};
								if ( (fingering2.maxItem-fingering2.minItem) <= maxStretch, {
									//wellicht hier nog wat extra voorwaarden die de gebruiker kan invoeren
									chords.add(modeO);
									fingerings.add(fingering);
								})
							})

						})
					})

				}

			}
		};
		//chords.size.post; " -> ".post;
		//chords=chords.asSet.asArray;

		transpositions=chords.collect{|i|
			var transpositions=List[];
			(0..11).do{|t|
				if ( (this+t%12).asInteger.sect((i%12).asInteger).size==tune.size, { //was 6
					transpositions.add(t)
				});
			};
			transpositions.asArray
		};
		"calculation time: ".post; (Main.elapsedTime-time).postln;
		chords.size.post; " chords found".postln;
		"output is [chords, transpositions, fingerings]".postln;
		^[chords, transpositions, fingerings]
	}

	fingeringGuitar {arg tuning=[0,5,10,15,19,24]+4+36, highestFret=12, maxStretch=2, minOpenStrings=2, maxInterval=12, sort=true, posting=false;
		var n=this.size, order;
		var strings=tuning.powersetn(n);
		var stringz=List[], fingerings=List[], chordz=List[], totalFingerings=List[];
		var score=List[];
		var allChords=List[], chord;
		if (posting, {"calculating fingering.....".postln});
		chord=(this%12).sort;
		allChords=chord.size.factorial.collect{|permute|
			chord.permute(permute);
		}.asSet.asArray;
		strings.do{|strings|
			allChords.do{|transposedChord|
				var flag=false;
				var intervals;
				var frets;
				var openstrings, frettedstrings, totalFingering= {-1}!tuning.size;
				transposedChord=((transposedChord-strings).neg.max(0)/12.0).ceil*12+transposedChord;
				frets=(transposedChord-strings);
				openstrings=frets.deepCopy;
				frettedstrings=frets.deepCopy;
				intervals=transposedChord.sort.differentiate.abs.copyToEnd(1);
				if (intervals.maxItem<=maxInterval, {
					openstrings.removeAllSuchThat{|i| i!=0};
					frettedstrings.removeAllSuchThat{|i| i==0};
					if (frettedstrings.size>0, {
						if (frettedstrings.collect{|i| (i>=0).binaryValue}.sum==frettedstrings.size, {
							if (frettedstrings.collect{|i|
								(i<=highestFret).binaryValue}.sum==frettedstrings.size
							, {
								if ( (frettedstrings.maxItem-frettedstrings.minItem) <= maxStretch, {
									if (openstrings.size>=minOpenStrings, {

										flag=if (frets.size>4, {
											if (frets.occurrencesOf(frets.minItem)>=2, {
												true

											},{
												false
											})
										},{
											true
										});

										//flag=true

									})
								})
							})
						})
					},{
						flag=true;
					});
					if (flag, {
						stringz.add(strings);//.post
						fingerings.add(frets);//.post
						chordz.add(transposedChord);//.post
						strings.do{|i,j| totalFingering[tuning.indexOf(i)]=frets[j] };
						totalFingerings.add(totalFingering);//.postln
					});
					flag=false;
				})
			}
		};

		if (sort, {
			order=fingerings.squared.collect(_.mean).order;
			chordz=order.collect{|i| chordz[i]};
			stringz=order.collect{|i| stringz[i]};
			fingerings=order.collect{|i| fingerings[i]};
			totalFingerings=order.collect{|i| totalFingerings[i]};


		});

		if (posting, {
			"READY!".postln;
			chordz.size.post; " chords founds".postln;
			"output is [chords, strings, fingerings, all fingerings]".postln;
		});
		^[chordz.asArray, stringz.asArray, fingerings.asArray, totalFingerings.asArray]
	}
}