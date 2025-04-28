+String {
	/*
	45.asAscii -> -
	46.asAscii -> .
	47.asAscii -> /
	48.asAscii- 57.asAscii
	*/
	/*
	interpretSafeJT {
	var ascii, allNumbers=true, numberOfDots;
	var asciiNumbers=[45,46]++(48..57);
	ascii=this.ascii;
	allNumbers=ascii.collect{|val| asciiNumbers.includes(val).binaryValue}.sum==this.size;
	^if ( ascii[0]==47) {
	this.asSymbol
	} {
	if (allNumbers) {
	numberOfDots=ascii.occurrencesOf(46);
	if (numberOfDots>1) {
	this
	} {
	this.interpret
	}
	} {
	this
	}
	}
	}

	interpretSafeJT {
	var firstChar = this[0];

	// Quick check for Symbol (begins with '/')
	if (firstChar == $/) {
	^this.asSymbol
	};

	// Quick check for possible number (starts with digit or minus)
	if (firstChar.isDecDigit or: { firstChar == $- }) {
	var dotCount = 0;
	var isNumeric = true;

	// Single pass through the string to check if it's a valid number
	this.do { |char, i|
	if (i > 0 or: { char != $- }) { // Skip checking the first char if it's minus
	if (char == $.) {
	dotCount = dotCount + 1;
	} {
	if (char.isDecDigit.not) {
	isNumeric = false;
	// Break early once we know it's not a number
	^this
	};
	};
	};
	};

	// If it's a valid number
	if (isNumeric) {
	// Check for too many decimal points
	if (dotCount > 1) {
	^this
	};

	// Faster than interpret for simple cases
	if (dotCount == 0) {
	^this.asInteger
	} {
	^this.asFloat
	};
	};
	};

	// Default case - return the string itself
	^this
	}
	*/

	interpretSafeJT {
		var firstChar = this[0];
		// Quick check for Symbol (begins with '/')
		if (firstChar == $/) {
			^this.asSymbol
		};
		// Quick check for possible number (starts with digit or minus)
		if (firstChar.isDecDigit or: { firstChar == $- }) {
			var dotCount = 0;
			var eCount = 0;
			var isNumeric = true;
			var hasSeenE = false;

			// Single pass through the string to check if it's a valid number
			this.do { |char, i|
				if (i > 0 or: { char != $- }) { // Skip checking the first char if it's minus
					if (char == $.) {
						// Decimal point is not valid after 'e'
						if (hasSeenE) {
							isNumeric = false;
							^this
						};
						dotCount = dotCount + 1;
					} {
						if (char == $e or: { char == $E }) {
							hasSeenE = true;
							eCount = eCount + 1;
						} {
							if (char == $- or: { char == $+ }) {
								// Minus or plus only valid right after 'e'
								if (i > 0 and: { (this[i-1] == $e) or: { this[i-1] == $E } }) {
									// Valid
								} {
									isNumeric = false;
									^this
								};
							} {
								if (char.isDecDigit.not) {
									isNumeric = false;
									// Break early once we know it's not a number
									^this
								};
							};
						};
					};
				};
			};

			// If it's a valid number
			if (isNumeric) {
				// Check for too many decimal points or 'e's
				if ((dotCount > 1) or: { eCount > 1 }) {
					^this
				};

				// For scientific notation or any float, use asFloat
				if ((dotCount > 0) or: { eCount > 0 }) {
					^this.asFloat
				} {
					^this.asInteger
				};
			};
		};
		// Default case - return the string itself
		^this
	}


	smpteSecs {arg frameRate=25, subFrames=80, offset=3600;//subFrames in Logic is 80
		^this.split($:).collect{|i| i.interpret}.collect{|v,i| if (i<3) {v*[3600,60,1][i]} { ((v.frac*100/subFrames)+v.asInteger)/frameRate} }.sum-offset
	}

	moveDir {arg depth=1;
		/*
		var folders=this.allFolders;
		^PathName("/"++folders.copyRange(0, (folders.size-1-depth).max(0)).join($/)++"/")
		*/
		^PathName(this).moveDir(depth).fullPath
	}

	//calculate fontSize for Font.monospace
	fontSize {arg bounds=100@20, factor=0.608;
		^(this.size.fontSize(bounds, factor))
		//^if (this.size*factor>(bounds.x/bounds.y), {bounds.x/(this.size*factor)},{bounds.y})
	}

	movedirlevel {arg levels=1;
		var array, in=this.copy;
		if (in.last==$/, {in=in.copyRange(0, in.size-2)});
		array=in.split($/);
		^array.copyRange(0, array.size-1-levels).join($/)++"/"
	}

	asciiProduct {
		var x=1;
		this.ascii.do{|val|
			if ([val.log/2.log, x.log/2.log].sum>=30, {
				x=x.mod(x.log/2.log).round(1.0).asInteger;
			});
			x=x*val.max(1);
			x.abs
		};
		^x
	}

	unixPath {
		^(this.replace(" ","\\ ").replace("(","\\(").replace(")","\\)").replace("&","\\&").replace("!","\\!").replace("'","\\'")).replace("[", "\\[").replace("]", "\\]").replace(",", "\\,").replace(":", "\\:")
	}

	deunixPath {
		^(this.replace("\\","")	)
	}

	hexToInt {
		var v=[];
		this.do{|i| v=v.add(i.digit)};
		^v.convertDigits(16)
	}

	hexdec {
		var array=List[];
		this.do{|i|
			array.add((\0:0,\1:\1,\2:2,\3:3,\4:4,\5:5,\6:6,\7:7,\8:8,\9:9,\a:10,\b:11,\c:12,\d:13,\e:14,\f:15)[i.asSymbol])
		};
		^array.convertDigits(16)
	}

	save {arg val;
		var file;
		file=File(this, "r");
		file.write(val.asCompileString);
		file.close;
	}

	play{arg server, startFrame=0, numFrames= -1, action, bufnum=nil, loop = false, mul = 1;
		var buf;
		server=server??{Server.default};
		{
			buf=Buffer.read(server, this, startFrame, numFrames, action, bufnum);
			server.sync;
			buf.play(loop, mul);
		}.fork
	}

	lispArray {
		^this.replace("(", "[").replace(")", "]").replace(" ", ",").interpret
	}


	aceToEvent {
		var f, x, l;
		var header=true, data=List[], features=List[], paths=List[], index= -1, number=0, featureName, featureListReady= -1;

		f=File(this, "r");
		x=f.readAllString;
		f.close;

		x=x.split($\n);
		l=();
		x.do{|string|
			if (string.contains("<data_set_id>"), {
				index=index+1;
				featureListReady=featureListReady+1;
				string=string.copyToEnd(string.find("<")).replace("<data_set_id>", "");
				string=string.replace("</data_set_id>", "");
				string=string.replace(" ", "");
				paths.add(string);
				data.add(List[]);
				//string.post
			});
			if (string.contains("<v>"), {
				string=string.replace("<v>", "");
				string=string.replace("</v>", "");
				string=string.replace("NaN", "0");
				data[index].add(string.interpret);

				if (featureListReady<1, {
					features.add(featureName++number);
				});
				number=number+1;
			});
			if (string.contains("<name>"), {
				number=0;
				string=string.copyToEnd(string.find("<")).replace("<name>", "").replace("</name>", "");
				featureName=string;
			});
			if (string=="<feature_vector_file>", {header=false;});
			string;
		};
		^(features: features, paths: paths, data: data);

	}

}