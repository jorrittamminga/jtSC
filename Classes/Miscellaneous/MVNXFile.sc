/*
An XML element is everything from (including) the element's start tag to (including) the element's end tag.
*/
MNVXFile {

	var <frameRate, <segmentCount, <configuration, <userScenario, <processingQuality, <label, <torsoColor;
	var <segments, <segmentByID;
	var <orientation, <position;
	var <>pathName;
	var <index, <tc, <ms;
	var <file, <routine;
	var <test;
	var <filePos;

	*new {arg pathName;
		^super.new.pathName_(pathName).init
	}

	init {
		var line, tmp, count=0;
		var segmentLabel, pointLabel, id, type;
		segments=();
		segmentByID=();
		orientation=();
		position=();
		filePos=[];
		file=File(pathName, "r");

		//----------------------------- inits
		4.do{|i|
			file.getLine(65536);
		};

		//----------------------------- get subject
		line=file.getLine(65536);
		line=line.replace("\t", "").replace("<","").replace(">","");
		tmp=line.split($ );
		tmp=tmp.collect{|i| i.split($=)};
		tmp.do{|i|
			switch(i[0])
			{"label"}  {label=i[1].interpret.asString}
			{"frameRate"}  {
				frameRate=i[1].interpret.asInteger;
			}
			{"segmentCount"}  {segmentCount=i[1].interpret.asInteger}
			{"configuration"}  {configuration=i[1].interpret.asString}
			{"userScenario"}  {userScenario=i[1].interpret.asString}
			{"processingQuality"}  {processingQuality=i[1].interpret.asString}
			{"label"}  {label=i[1].interpret.asString}
			{"torsoColor"}  {torsoColor=i[1].interpret.asString}
		};

		line=file.getLine(65536);//comments

		//----------------------------- get segments
		while
		{line.contains("/segments").not}
		//{count<100}
		{
			line=line.replace("\t", "").replace("<","").replace(">","");
			//line.postln;
			if (line.contains("segment label=")) {
				line=line.split($ );
				line=line.collect{|i| i.split($=)};
				line.do{|i|
					switch (i[0])
					{"label"} {
						segmentLabel=i[1].interpret.asString;
						segments[segmentLabel]=(points:())
					}
					{"id"} {
						id=i[1].interpret.asInteger;
						segmentByID[id]=segmentLabel;
						segments[segmentLabel][\id]=id
					}
				}
			} {
				if (line.contains("point label=")) {
					line=line.split($ );
					line=line.collect{|i| i.split($=)};
					pointLabel=line[1][1].interpret.asString;
					segments[segmentLabel][\points][pointLabel]=();
				} {
					//<pos_b>-0.000023 0.000000 0.106693</pos_b>
					if (line.contains("pos_b")) {
						line=line.replace("pos_b","").replace("/");
						line=line.split($ );
						line=line.collect(_.interpret);
						segments[segmentLabel][\points][pointLabel]=line;
					}
				}
			};
			line=file.getLine(65536);
		};
		line=file.getLine(65536);

		//----------------------------- get the rest
		while
		{line.contains("<frames ").not}
		//{count<100}
		{
			line=file.getLine(65536);
		};

		//----------------------------- frames, calibration settings
		3.do{
			line=file.getLine(65536);
			line=line.replace("\t", "").replace("<","").replace(">","");
			line=line.split($ );
			line.do{|i| i=i.split($=);
				if (i[0]=="type") {type=i[1].interpret.asString}
			};

			//check parameters and amount ()
			2.do{|i|
				line=file.getLine(65536);
				line=line.replace("\t", "");
				line=line.split($<);
				line=line.collect{|i| i.split($>)}.flatten(1);
				line.removeAllSuchThat{|i| i.size==0};
				//line.do({|i| i.postln});
				[position, orientation][i][type]=line[1].split($ ).collect(_.interpret)
			};
			line=file.getLine(65536);
		};
		filePos=filePos.add(file.pos);
	}

	close {
		file.close
	}

	nextFrame {
		var line, type;

		//read frame information
		line=file.getLine(65536);
		line=line.replace("\t", "").replace("<","").replace(">","");
		line=line.split($ );
		line.do{|i|
			i=i.split($=);
			switch (i[0])
			{"type"} {type=i[1].interpret.asString}
			{"index"} {index=i[1].interpret.asInteger}
			{"ms"} {ms=i[1].interpret.asInteger}
			{"tc"} {tc=i[1].interpret.asString}
		};
		//[index, tc, ms, type].postln;

		//read position and orientation and others....
		//so not 2.do but while
		2.do{|i|
			line=file.getLine(65536);
			line=line.replace("\t", "");
			line=line.split($<);
			line=line.collect{|i| i.split($>)}.flatten(1);
			line.removeAllSuchThat{|i| i.size==0};
			//line.do({|i| i.postln});
			[position, orientation][i][type]=line[1].split($ ).collect(_.interpret)
		};
		line=file.getLine(65536);

		//close </frame>
		filePos=filePos.add(file.pos);
	}


play {


}

continu {

}

return {

}

stop {

}

pause {

}

loop {

}

}