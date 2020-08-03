//BUG1: 71.4 wordt ceh' (ipv ceh'' !!!)
//ADD1: zorg dat 71.0 ook ces kan worden!, en 64 ook fes)

+ Nil {

	midilily {arg type="s";
		^type
	}
}

+ Symbol {

	ly { arg type, extra;
		^LilyPondString.new(this.asString, type.asSymbol, extra)
	}

	asLilyString{
		^("\\"++this.asString)
	}

	clefLily {
		^this.asString.clefLily
	}

	staffLily {
		^this.asString.staffLily
	}

	ottavaLily {
		^this.asString.ottavaLily
	}

	noteHeadLily {arg change=true;
		^this.asString.noteHeadLily(change)
	}
}

+ String {
	ly {arg type, extra;
		^LilyPondString.new(this, type.asSymbol, extra)
	}

	asLilyString {
		^("\\"++this)
	}

	clefLily {
		^(" \\clef \\" ++ this ++ " ")
	}

	staffLily {
		^(" \\change Staff=\"" ++ this ++ "\" ")
	}

	ottavaLily {
		^(" \\ottava #" ++ this ++ " ")
	}

	noteHeadLily {arg change=true;
		var out;

		out=if (change, {
			(" \\override NoteHead #'style = #'" ++ this ++ " ");
		},{
			(" \\revert NoteHead #'style ");
		})
		^out
	}

}

+ SimpleNumber {

	ly { arg type, extra;
		var string=(LilyPondString.parameters[type.asSymbol]);
		var t1,t2,tmp, that;

		that=this;

		if (type==\tremolo, {tmp=string.keys.asArray.sort; that=tmp[tmp.indexInBetween(this).round(1.0)] });

		string=if (string==nil, {that.asString},{

			if (type==\dynamic, {
				t1=(that/100).asInteger*100;
				t2=((that/100).frac*100).round(1.0).asInteger;
				tmp=((t1>0).binaryValue*2)+((t2>0).binaryValue);
				switch(tmp,0,{string[t2]},1,{string[t2]},2,{string[t1]},3,{string[t2]++" "++string[t1]})
			},{
				string[that]
			})
		});
		if (string==nil, {string=that.asString});
		^LilyPondString.new(string, type, extra)
	}

	clefLily {arg ottava=0, clefs=(0:\bass, 1:"treble", 2:"tenor", 3:"alto");
		var out=clefs[this], ot="";
		if (ottava!=0, {out=out++["_","^"][ottava.isPositive.binaryValue]++ottava.abs.asString});
		^out.clefLily
	}

	staffLily {arg staves=(0:"lower", 1:"upper");
		^staves[this].staffLily
	}

	ottavaLily {
		^this.asString.ottavaLily
	}

	noteHeadLily {arg noteHeads=(1:"cross");
		var change=noteHeads.keys.includes(this);
		var that;
		that=if (change.not, {""},{noteHeads[this]});
		^that.asString.noteHeadLily(change)
	}

	midilily {arg negativeString="r";//
		//60.0=c'(default), 60.5=cih (default), 60.4=deseh (flats), 60.6=cih (sharps), 60.06(sharps-sharper)=bis, 60.04(default-flatter)=deses, 59.04(default-flatter)=ces of 59.1=ces en 59.9=bis
		var string="", notes=this, list, biscorr, cescorr;
		//		var default=[\c, \cis, \d, \es, \e, \f, \fis, \g, \as, \a, \bes, \b];
		//		var sharps=[\c, \cis, \d, \dis, \e, \f, \fis, \g, \gis, \a, \ais, \b];
		//		var flats=[\c, \des, \d, \es, \e, \f, \ges, \g, \as, \a, \bes, \b];

		var default=[\c, \cih, \cis, \cisih, \d, \eeseh, \es, \eeh, \e, \feh, \f, \fih, \fis, \fisih, \g, \aeseh, \as, \aeh, \a, \beseh, \bes, \beh, \b, \ceh];
		var sharps=[\bis, \cih, \cis, \cisih, \d, \dih, \dis, \disih, \e, \eih, \eis, \fih, \fis, \fisih, \g, \gih, \gis, \gisih, \a, \aih, \ais, \aisih, \b, \bih];
		var flats=[\c, \deseh, \des, \deh, \d, \eeseh, \es, \eeh, \fes, \feh, \f, \geseh, \ges, \geh, \g, \aeseh, \as, \aeh, \a, \beseh, \bes, \beh, \ces, \ceh];

		var octave=[",,,,", ",,,", ",,", ",", "", "'", "''", "'''", "''''", "'''''"];
		list=default;
		if (this!=nil, {
			if (this-this.round(0.5)<0.0, {list=flats});
			if (this-this.round(0.5)>0.0, {list=sharps});

			biscorr=if (list==sharps, {-0.9},{0.0});

			string=list[this.round(0.5)*2%24];
			cescorr=if (string.asString.contains("ces"), {1},{0});
			string=(string ++ octave.clipAt(( (this+0.2+biscorr).round(1.0)/12).asInteger+cescorr));
		},{
			string=(string ++ "s");
		}
		);
		if ((this.isNegative) && (negativeString.size>0), {string=negativeString});
		^string
	}

	harpmidi{arg pedals=[0,0,0,0,0,0,0], harp=0, scordatura=[0,2,4,5,7,9,11], range=[24,103];
		^scordatura[this%7]+((this/7).asInteger*12)+24+(pedals[this%7]*1.1)

	}


}


+ Array {

	rhythmLily{arg dur=7/4, extra="", type="~", prev=100000000;//type "~", "r", "s"
		dur=dur.asArray;
		extra=extra.asArray;
		type=type.asArray;
		^this.collect{|n,i|
			n.rhythmLily(dur.wrapAt(i), extra.wrapAt(i), type.wrapAt(i))
		}
	}
	/*
	renderNotes {arg rhythm=[4], dynamics=[""], path="~/tmp".standardizePath, mode="w", version="2.13.40", pre="{", post="}", midi=false, book=true, beats, clef="treble";//, forceClef=false of numberOfStaves, a la open music
	var string, notename, ly="";
	rhythm=rhythm.asArray??{[4]};
	dynamics=dynamics.asArray.add("")?[""];
	notename=this.midilily;
	pre=pre++ "\\clef " ++ clef.asString ++ " ";//
	notename.do{|nn,i| ly=ly++nn++rhythm.wrapAt(i).asString++dynamics.clipAt(i)++" "};

	//
	//if (beats!=nil, {pre=pre++"\\time " ++ beats.asString ++ "/" ++ rhythm.asArray[0] ++ " "});
	ly.writeLily(path, mode, version, true, pre, post, midi, true, book);
	^ly
	}
	*/
	/*
	sommige noten gaan mis!!! 59.5 wordt niet vertoond b.v.

	*/


	renderNotes {arg rhythm=[1/4], dynamics=[""], path="~/tmp".standardizePath, mode="w", version="2.13.40", pre="{", post="}", midi=false, book=true, timeSignature, forceAccidentals=true, showRests=false, splitpoints=[35, 59, 85];//[35, 60, 85]
		var thiz, string, notename, ly=""
		, clefs=["\"treble^15\"", "treble", "bass", "\"bass_15\""]
		//, splitpoints=[35, 60, 85]
		;
		var splits=List[], clef, mean, range, array=this.deepCopy.flat, means=[0]++splitpoints, copyRange;

		range=array.maxItem-array.minItem;
		mean=[array.minItem, array.maxItem].mean;
		//if (splits==nil, { });
		splitpoints.do{|splitpoint|
			if ((array.flat.minItem<=splitpoint) && (array.flat.maxItem>splitpoint), {
				splits.add(splitpoint)
			});
		};

		clef=clefs.reverse[means.indexInBetween(mean).asInteger];
		splits.asSet.asArray.sort;

		if (forceAccidentals, {ly=ly++" \\accidentalStyle Score.forget "});

		rhythm=rhythm.asArray??{[1/4]};
		dynamics=dynamics.asArray.add("")?[""];

		if (timeSignature==nil, {
			ly=ly++"\\override Score.BarLine #'transparent = ##t \\override Score.TimeSignature #'transparent = ##t \\override Score.TimeSignature #'stencil = ##f ";
		}, {
			ly=ly++" \\time " ++ timeSignature[0] ++ "/" ++ timeSignature[1] ++ " ";
		});

		if (splits.size==0, {

			notename=this.midilily;
			ly=ly ++ "\\clef " ++ clef.asString ++ " ";
			notename.do{|nn,i|
				ly=ly++nn.rhythmLily(rhythm.wrapAt(i))++dynamics.clipAt(i)
				//ly=ly++nn++rhythm.wrapAt(i).asString++dynamics.clipAt(i)
				++" "};


			//this.renderNotes(rhythm, dynamics, path, mode, version, pre, post, midi, book, beats, clef:clef)
		}, {
			copyRange=splits.collect{|i| splitpoints.indexOf(i)};
			clefs=clefs.reverse.copyRange(copyRange.minItem, copyRange.maxItem+1).reverse;

			ly=ly++"<<\n";

			this.collect{|note|
				note.split(splits);
			}.flop.reverse.do{|notes,voice|
				notes.postln;
				ly=ly++"\\new Staff {\\clef "++ clefs[voice] ++ " ";
				notes.do{|note,i|
					note=if (note!=nil, {
						note.bubble.midilily.unbubble;
					},{
						if (showRests, {
							"r"
						},{
							"s"
						})
					});
					ly=ly++note.rhythmLily(rhythm.wrapAt(i))++dynamics.clipAt(i)
					//ly=ly++note++rhythm.wrapAt(i).asString++dynamics.clipAt(i)
					++" "
				};
				ly=ly++"}\n";
			};
			ly=ly++">>\n";

		});
		ly.writeLily(path, mode, version, true, pre, post, midi, true, book);
		^ly

	}

	playNotes {arg rhythm=[0.25], dynamics=[0.2], repeats=1, legato=1;
		rhythm=rhythm.asArray; dynamics=dynamics.asArray;
		rhythm=this.size.collect{|i| rhythm.wrapAt(i)};
		dynamics=this.size.collect{|i| dynamics.wrapAt(i)};
		Pbind(\dur, Pseq(rhythm, repeats), \midinote, Pseq(this, repeats), \amp, Pseq(dynamics, repeats), \sustain, Pseq(rhythm*legato, repeats)).play
	}

	pedals{ arg tuning=[0,2,4,5,7,9,11];
		var pedals=List[],flag,that;
		//pedals=(this%12)-[0,2,4,5,7,9,11];
		that=this%12;
		that=that.asSet.asArray.sort.resamp0(7);

		that.size.do({|i|
			var tmp;
			tmp=( that.rotate(i) - tuning ).collect({|i| if (i.abs>2, {i-12},{i})});
			flag=tmp.collect({|i| (i.abs>1).binaryValue}).sum==0;
			if (flag, {pedals.add(tmp.round(1.0).asInteger)});
		});

		pedals=pedals.asArray;
		pedals=pedals.asSet.asArray.sort({|a,b| a.sum<b.sum});
		if (pedals.size<2, {pedals=pedals.unbubble});

		^pedals

	}

	pedalslily {arg align;
		var string="";
		string=[1,0,6,2,3,4,5].collect{|i| (-1: "^", 0:"-", 1:"v")[this[i]]}.join("");
		string="{ \\harp-pedal #\"" ++ string.copyRange(0,2) ++ "|" ++ string.copyToEnd(3) ++ "\" }";
		if (align!=nil, {string="{ \\" ++ align++"-align " ++ string ++ "}" });
		^("_\\markup " ++ string)
	}

	harpmidi {arg pedals=[0,0,0,0,0,0,0], harp=0, scordatura=[0,2,4,5,7,9,11], range=[24,103];
		var that=this.deepCopy.flat;
		that=that.collect({|i| i.harpmidi(pedals,harp,scordatura,range)});
		^that.reshapeLike(this)

	}


	midilily {arg negativeString="r", asString=false;
		var string="";
		if (this.size>0, {
			string=this.collect({|note| string="";
				note=note.asArray;
				if (note.size>1, {
					string=string++"<";
					note.sort.collect({|n,i| string=string++n.midilily;
						if (i+1<note.size, {string=string++" "}) });
					string=string++">";
				},{
					string=string++note[0].midilily(negativeString);
				});
				string
			});
			if (this.size==1, {string=string[0]});
		});
		if ((string.class==String)&&(asString.not), {string=[string]});
		^string
	}

	lilyRhythm {arg unit, extra="", type=0, wrap;//0=unbeamed, 1=beamed
		var str="";
		if (unit.size==0, {unit=[unit]});
		if (extra.class==String, {extra=[extra]});
		if (this.size<2, {type=0});
		this.do({|note,i|
			var length=if((unit.size>1)||(i==0),{unit.wrapAt(i)},{""});
			var beam=if((i==0)&&(type==1),{"["},{""});
			var ex=if (extra.size==1, {if (i==0, {extra[0]},{""})},{extra.wrapAt(i)});
			str=str++note++length++ex++beam++" "
		});
		if (type==1, {str=str++"]"});
		^str
	}


	lilyGrace {arg unit=16, extra="", type=0;//[\grace, \acciaccatura, \appoggiatura, \afterGrace]
		var that,str=["\\grace", "\\acciaccatura", "\\appoggiatura", "\\afterGrace"].clipAt(type);
		if (unit.size==0, {unit=[unit]});
		that=if (this.size==0, {[this]},{this});
		str=str++" {"++that[0]++unit[0]++" ";
		if (that.size>1, {
			str=str++"[";
			that.copyRange(1,that.size-1).do({|note,i|
				var length=if (unit.size>1,{unit.wrapAt(i)},{""});
				str=str++note++length++" ";
			});
			str=str++"]";
		});
		str=str++"} ";
		^str
	}

	//bouw hier ook 'book' in
	rtmLily {arg unit=4, notes=["c'"], post=[""], pre=[""], showMeter=true, showTupletBracket=true, prevMeter=[0,0], skipRestsAndTies=true, render=false, midi=false, tempo=60, showTempo=true;
		var string="", count=[0,0,0];//[notes,rests,ties]
		var bars, tmp,info, prevTempo;
		if (tempo.size==0, {tempo=[tempo]});
		bars=this.rtm(unit);

		bars.do({|bar, i_bar|
			var meter=bar.last, newTempo=tempo.wrapAt(i_bar);

			if ( (prevTempo!=newTempo) && showTempo, {string=string++ "\\tempo " ++ meter[1] ++ " = " ++ newTempo ++ " "});

			if ((prevMeter!=meter) && showMeter, {string=string++" \\time " ++ meter[0] ++ "/" ++ meter[1] ++ " " });
			prevMeter=meter; prevTempo=newTempo;

			bar[0].do({|rhythms, i_rhythm|
				var beamStarted=false;
				var count2=[0,0,0];
				rhythms.do({|rhythm|
					var out;
					var totalCount;
					totalCount=this.rtmInfo;
					totalCount=totalCount[\notes].sum+totalCount[\rests].sum+totalCount[\ties].sum;

					out=rhythm.rtmLilyRhythm("", notes, count, post, pre, skipRestsAndTies=true, beamForce:true, totalCount: totalCount, count2:count2);

					count=out[1];
					string=string++out[0];
				})
			});
			if (showMeter, {string=string++" |\n "});
		});
		//if (string.contains("[") && string.contains("]").not, {string=string.replace("]", "")});
		if (render, {string.writeLily(render:true, midi:midi)});

		^string
	}


	rtmLilyRhythm {arg tmpString="", notes=["c'"], count=[0,0,0], post=[""], pre=[""], skipRestsAndTies=true, prev=[1,1,1], depth=0,counter=0, beamForce=false, totalCount, count2=[0,0,0];
		var tuplet=false, times=this.last, grace=false, beam="", number, beamStart=false;
		var fractionString=" \\once \\override TupletNumber #'text = #tuplet-number::calc-fraction-text ";

		if (this.size<2, {times=[1,1]});
		if ((times!=[1,1]) && (times[0]>=0.0), {
			tuplet=true;
			if ((times[0].log/2.log).frac!=0, {tmpString=tmpString++fractionString});
			if (tmpString.size==0, {beamStart=true},{beamStart=false});
			tmpString=tmpString++" \\times " ++ times[0] ++ "/" ++ times[1] ++ " { "
		});
		number=this[0].size;

		this[0].do({|val, n|
			var note="c'", tie="",out,before="",after="", rhythmVal, dot="";
			//MAKE DOTTED NOTES
			/*
			if ((val.size==2), {
			//[2,4]=2.  3=4., 6=8.
			"val ".post;val;
			if (val[0][1]/val[0][0]==2 && (val[0][0].class==Float), {
			//val=(val[0][0]*1.5).asInteger
			val=val[0][0].asInteger; dot=".";
			});
			});
			*/
			if (val.size==0, {
				if (val<0, {
					note="r";
					count[1]=count[1]+1; count2[1]=count2[1]+1;
				},{
					if (skipRestsAndTies && (prev[0].class==Integer), {before=pre.wrapAt(count[0]); after=post.wrapAt(count[0]); });
					note=notes.wrapAt(count[0])
				});
				if (val.class==Float, {
					count[2]=count[2]+1; count2[2]=count2[2]+1;
					tie="~";
				});
				if ((val>0) && (val.class==Integer), {
					count[0]=count[0]+1; count2[0]=count2[0]+1;
				},{
					if (skipRestsAndTies.not, {before=pre.wrapAt(count.sum); after=post.wrapAt(count.sum); });
				});
				prev[0]=val;//previous rhythmic value
				beam="";
				val=val.abs.asInteger;
				rhythmVal=if(val.isPowerOfTwo, {val.abs}, {"yo".post; ((val.abs*1.0).nextPowerOfTwo).asString++"."});
				rhythmVal=rhythmVal.asString++dot;

				//if (count2.sum==1, {beam="["});
				//if (totalCount==count2.sum, {beam="]"});
				tmpString=tmpString ++ " " ++ before ++ " " ++ note ++ rhythmVal ++ after ++ beam ++ tie;
			},{
				//if (val[0].size==2, {nil});//for dotted notes!!!!
				out=val.rtmLilyRhythm(tmpString, notes, count, post, pre, skipRestsAndTies=true, depth:depth+1, totalCount: totalCount, count2:count2);

				tmpString=out[0];
				count=out[1];
			});
		});
		if (tuplet || grace, {tmpString=tmpString++ " } "});
		^[tmpString,count]
	}
}


+ String {

	lilyGrace {arg unit=16, extra="", type=0;//[\grace, \acciaccatura, \appoggiatura, \afterGrace]
		^[this].lilyGrace(16,extra,type)
	}

	lilyRhythm{arg unit, extra="", type=0, wrap;//0=unbeamed, 1=beamed
		^[this].lilyRhythm(unit,extra,type,wrap)
	}

	rhythmLily {arg dur=7/4, extra="", type="~";//type "~", "r", "s"

		var fraction, f, rhythms, string="";
		var tie=(type=="~");

		fraction=dur.asFraction;

		if (extra.class==String, {extra=[extra]});
		if (this=="s", {tie=false; type="s"});

		if (fraction[1].isPowerOfTwo, {
			f={arg x, list=[];
				var a;
				a=2.pow((x[0].log/2.log).asInteger).min(x[1]);
				list=list.add(x[1]/a);
				if (list.size>1, {
					if (list.last/list[list.size-2]==2, {list.removeAt(list.size-1); list[list.size-1]=list.last+0.5})});
				a=x[0]-a;
				if (a>0, {f.value([a,x[1]], list)},{
					list.collect({|i| if (i.frac==0, {i.asString}, { (i.asInteger.asString++".")})});
				});
			};
			rhythms=f.value(fraction);
		},{
			rhythms=["1*"++fraction[0].asInteger++"/"++fraction[1].asInteger];
		});

		if (extra.size<rhythms.size, {extra=extra ++ ({""}!(rhythms.size-extra.size))  });

		rhythms.do({|r,i|
			var noteName=if ((i==0)||tie, {this},{type}), tmpExtra=if (i==0, {extra}, {""}), tmpTie=if ( (i<(rhythms.size-1)) && tie, {"~"},{" "});
			string=string++noteName++r.asInteger++extra[i]++tmpTie
		});

		^string
	}


	writeLily {arg path="~/tmp".standardizePath, mode="w", version="2.13.40", render=false, pre="{", post="}", midi=false, layout=true, book=false, raw=false;
		var string="";
		var file;
		//path="/Users/jorrittamminga/Dropbox/Current/Jam/poepen.ly";
		file=File(path++".ly", mode);
		if (raw==false, {
			if (version!=nil, {string=string++"\\version \"" ++ version ++ "\"\n"});
			if (book, {string=string++"\\include \"lilypond-book-preamble.ly\"\n"});

			//\include "lilypond-book-preamble.ly"
			if (layout&&midi, {pre="\n\\score{\n"++pre; post=post++"\n\\layout{}\n\\midi{}\n}"});
			if (layout.not&&midi, {pre="\n\\score{\n"++pre; post=post++"\n\\midi{}\n}"});

			string=string ++ pre ++
			//" \\override TupletBracket #'bracket-visibility = ##t " ++
			this ++ post;

			//string.postcs;
		},{
			string=this;
		});
		string.postln;
		file.write(string);

		file.close;
		if (render, {(path++".ly").renderLily(midi)});
	}

	renderLily {arg midi=false, lilypondPath="/Applications/LilyPond.app/Contents/Resources/bin/lilypond";
		var directory=PathName(this).pathOnly.unixPath;//dit kan mooier????
		var fileName=PathName(this).fileNameWithoutExtension.unixPath;
		var pdf=directory++fileName++".pdf";
		var midiFile=directory++fileName++".midi";
		var midiCmd="";

		if (midi, {midiCmd=" \nopen " ++ midiFile});

		/*
		if (lilypondPath==nil, {lilypondPath=
		Platform.case(
		\osx,       { "/Applications/LilyPond.app/Contents/Resources/bin/lilypond" },
		\linux,     { "lilypond"},
		\windows,   { "lilypond"}
		);		});
		*/

		/*
		//-dno-point-and-click can be added to lilypond function
		("#!/bin/sh
		/usr/bin/touch " ++ directory ++ " -m " ++ this ++ "
		" ++ lilypondPath ++ " -ddelete-intermediate-files -o " ++ directory ++ " " ++ this ++ "
		/usr/bin/find " ++ directory ++ " -newer " ++ this ++ " -name '*.pdf' -exec open {} \\;
		/usr/bin/find " ++ directory ++ " -newer " ++ this ++ " -name '*.midi' -exec open {} \\;
		").unixCmd;
		*/
		(lilypondPath ++ " -ddelete-intermediate-files=#t -o " ++ directory ++ " " ++ this.unixPath ++ " \nopen " ++ pdf ++ midiCmd).unixCmd//

	}

}