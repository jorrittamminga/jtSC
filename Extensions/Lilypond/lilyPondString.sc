/*
1.ly(\articulation)
2.ly(\fermata)
3.ly(\articulationshort)

1.ly(\clef)

-1.ly(\ottava)
"lower".ly(\staff)
60.midilily++"pp".ly
LilyPondString.parameters[\staff]
*/
LilyPondString : String {
	classvar <>parameters, <>types;

	*new {arg string="", type=\other, extra;
		^super.new.init(string,type.asSymbol,extra)
	}

	init {arg argstring, argtype, argextra;
		var string, tmp;
		if (types.includes(argtype).not, {argtype=\other});
		if (argstring=="", {argtype=\nothing});
		^switch(argtype,
			\clef, {" \\clef " ++ argstring ++ " "},
			\staff, {" \\change Staff=\"" ++ argstring ++ "\" "},
			\ottava, {" \\ottava #" ++ argstring ++ " "},
			\noteHead, {if (argstring!="", {
				(" \\override NoteHead #'style = #'" ++ argstring ++ " ")
				},{
				(" \\revert NoteHead #'style ")
				})},
			\articulation, {
				argextra=if (argextra!=nil, {argextra=argextra.asString},{"-"});
				if (argstring.size>1, {argstring="\\" ++ argstring},{argextra++argstring});
				},
			\articulationshort, {
				argextra=if (argextra!=nil, {argextra=argextra.asString},{"-"});
				if (argstring.size>1, {argstring="\\" ++ argstring},{argextra++argstring});
				},
			\fermata, {
				argextra=if (argextra!=nil, {argextra=argextra.asString},{"-"});
				if (argstring.size>1, {argstring="\\" ++ argstring},{argextra++argstring});
				},
			\tremolo, {
				argstring
				},
			\dynamic, {
				if (argextra==nil, {argextra=""});
				tmp=argstring.split($ );
				argstring="";
				tmp.collect({|i| argstring=argstring++"\\"++i});
				argextra ++ argstring
				},
			\other, {"\\" ++ argstring},
			\nothing, {""}
		)
	}

	*initClass {
		parameters=(
			clef: (0:"bass", 1: "treble"),
			noteHead: (-1: "", 0: "default", 1: "altdefault", 2: "baroque", 3: "neomensural", 4: "mensural", 5: "petrucci", 6: "harmonic", 7: "harmonic-black", 8: "harmonic-mixed", 9: "diamond", 10: "cross", 11: "xcircle", 12: "triangle", 13: "slash"),
			staff: (0: "lower", 1: "upper"),
			dynamic: (0: "!", 1:"ppppp", 2:"pppp", 3:"ppp", 4:"pp", 5:"p", 6:"mp", 7:"mf", 8:"f", 9:"ff", 10:"fff", 11:"ffff", 12:"fffff", 13: "fp", 14:"sf", 15:"sff", 16:"sp", 17:"spp", 18:"sfz", 19:"rfz", 100:">", 200:"<"),
			ottava: (),
			articulation: (0:"", 1:"staccatissimo", 2:"staccato", 3: "portato", 4:"tenuto", 5:"accent", 6:"espressivo", 7:"marcato"),
			articulationshort: (0:"", 1:"|", 2:".", 3: "_", 4:"-", 5:">", 6:"", 7:"^"),
			ornament: (0:""),
			fermata: (0:"", 1:"shortfermata", 2:"fermata", 3:"longfermata", 4:"verylongfermata"),
			tremolo: (0:"", 4:":4",8:":8",16:":16",32:":32", 64:":64", 128:":128")
		);
		types=parameters.keys.asArray;
	}
}
/*
+ Symbol {

	ly {arg type=\midi;

		^LilyPondString.new(this, type)

	}

}

+ String {

	ly {arg type=\midi;

		^LilyPondString.new(this, type)

	}

}

+ SimpleNumber {

	ly {arg type=\clef, extra;

		^LilyPondString.new(this, type, extra)

	}

}

+ Array {

	ly {arg type=\midi;

		^LilyPondString.new(this, type)

	}

}
//midilily
.clefLily
String
ControlSpec
0.ly(type: \clef, extra, parameters)
*/