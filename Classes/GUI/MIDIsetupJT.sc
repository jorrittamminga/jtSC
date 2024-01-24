MIDISetupWindowJT {
	var <views, <window;
	var <inports, <outports, <>path, <>dict, <midiOut, <fontSize;
	//midiIn: MIDIEndPoint("iCON iControl V1.01", "Port 1"),
	//midiOut: MIDIEndPoint("IAC Driver", "Bus 1"),
	//MIDIEndPoint
	*new {arg inports, outports, path, fontSize=12;
		^super.new.init(inports, outports, path, fontSize)
	}
	init {arg arginports, argoutports, argpath, argFontSize, reinit=false;
		var file, tmp, width, height;
		views=();
		dict=();

		MIDIClient.init;
		fontSize=argFontSize??{12};
		width=((21*fontSize+8+4));
		height=(MIDIClient.externalSources.size+MIDIClient.externalDestinations.size+3)*(fontSize+4)+8;

		window=Window("MIDI setup", Rect(
			(Window.screenBounds.width-width)/2
			,(Window.screenBounds.height-height)/2
			,width,400), false).front;
		window.addFlowLayout;
		window.alwaysOnTop_(true);

		inports=arginports.asArray;
		outports=argoutports.asArray;
		path=argpath??{"~/Desktop/".standardizePath};
		path=path++"midisetup.scd";
		dict[\inports]=inports;
		dict[\outports]=outports;

		if (File.exists(path), {
			file=path.load;
			file.keysValuesDo{|key,val| dict[key]=val};
		},{
			this.write;
		});

		//---------- MIDI IN
		StaticText(window, (21*fontSize+4)@(fontSize*1.5)).string_(" MIDI in").font_(Font(Font.defaultMonoFace, fontSize)).align_(\left).background_(Color.black).stringColor_(Color.white);
		MIDIClient.externalSources.do{|src,i|
			var but;
			StaticText(window, (20*fontSize)@fontSize).string_(src.device++" ("++src.name++")").font_(Font(Font.defaultMonoFace, 0.8*fontSize)).align_(\right);
			but=Button(window, fontSize@fontSize).states_( [ [],['X', Color.black, Color.green] ]).action_{|b|
				if (b.value==1) {
					MIDIIn.connect(i, src);
					dict[\inports]=dict[\inports].add([src.device,src.name]);
					dict[\inports]=dict[\inports].asSet.asArray;
				}{
					MIDIIn.disconnect(i, src);
					dict[\inports].removeAt(dict[\inports].indexOfEqual([src.device, src.name]));
				};
				this.write;
			}.font_(Font(Font.defaultMonoFace, 0.8*fontSize));
			dict[\inports].do{|midi|
				if ((src.device==midi[0])&&(src.name==midi[1])) {but.valueAction_(1)};
			};
		};

		//---------- MIDI OUT
		StaticText(window, (21*fontSize+4)@(fontSize*1.5)).string_(" MIDI out").font_(Font(Font.defaultMonoFace, fontSize)).align_(\left).background_(Color.black).stringColor_(Color.white);
		MIDIClient.externalDestinations.do{|src,i|
			var but;
			StaticText(window, (20*fontSize)@fontSize).string_(src.device++" ("++src.name++")").font_(Font(Font.defaultMonoFace, 0.8*fontSize)).align_(\right);
			but=Button(window, fontSize@fontSize).states_( [ [],['X', Color.black, Color.green] ]).action_{|b|
				if (b.value==1) {
					if (midiOut.size==0, {
						midiOut=MIDIOut.newByName(src.device, src.name);
						midiOut.latency=0.0;

						dict[\outports]=dict[\outports].add([src.device,src.name]);
						dict[\outports]=dict[\outports].asSet.asArray;

					},{

					})
				}{
					MIDIOut.disconnect(i, src);
					dict[\outports].removeAt(dict[\outports].indexOfEqual([src.device, src.name]));
					//midiOut=nil//???
				};
				this.write;
			}.font_(Font(Font.defaultMonoFace, 0.8*fontSize));
			dict[\outports].do{|midi|
				if ((src.device==midi[0])&&(src.name==midi[1])) {but.valueAction_(1)};
			};
		};

		/*
		Button(window, (21*fontSize+4)@(fontSize*1.5)).states_([ ["init MIDI"] ]).font_(Font(Font.defaultMonoFace, fontSize)).action_{
			this.init(argpath:path, argFontSize:fontSize, reinit:true);
		};
		*/
		window.rebounds;
	}
	write {
		var file;
		file=File(path, "w");
		file.write( dict.asCompileString );
		file.close;
	}
}