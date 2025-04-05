//start writing ALL incoming OSC data to file
//s.boot;

OSCRecorderJT {
	var <path, <port, <maxFileSize, <>excludePaths;
	var <dirname, <fileName;
	var string, i, tmp, w, file, fileName, tmpFileName, newWindow, now, f;
	var <window, <recorder, <isRecording;


	*new {arg path, port=57120, maxFileSize=5000, excludePaths=['/status.reply'];
		^super.newCopyArgs(path, port, maxFileSize, excludePaths).init
	}

	init {
		//--------------------------------------------------------------------------
		i=0;
		now=Main.elapsedTime;
		fileName="";
		isRecording=false;
		//------------------------------------------------------
		path=path??{"~/Desktop/".absolutePath};
		dirname=PathName(path).fullPath;
		if (File.exists(dirname).not) {File.mkdir(dirname)};
		maxFileSize=maxFileSize??{5000};
		this.maxFileSize_(maxFileSize);

		//------------------------------------------------------
		f = { |msg, time, replyAddr, recvPort|
			if (recvPort==port) {
				if(msg[0] != '/status.reply') {
					if (file.isOpen) {
						var size=msg.size-1;
						file.write(  ((Main.elapsedTime-now).asString)++"," );
						msg.do{|val,i|
							file.write(val.asString(65536) );
							if (i<size) {file.write(",")};
						};
						file.write("\n");
					};
				};
			};
			if (file.isOpen) {
				if (file.length>maxFileSize) {
					i=i+1;
					file.close;
					file=File(dirname++tmpFileName++"_"++i++".txt", "w");
				};
			};
		};
	}

	maxFileSize_ {arg fileSize;
		maxFileSize=fileSize*1000*1000;
	}

	startRecording {
		if (isRecording.not) {
			isRecording=true;
			tmpFileName=fileName??{""};
			tmpFileName=tmpFileName++"_"++(Date.localtime.stamp);
			file=File(dirname++tmpFileName++"_"++i++".txt", "w");
			now=Main.elapsedTime;
			thisProcess.addOSCRecvFunc(f);
			"OSCRecorder is recording".postln;
		} {
			"OSCRecorder is already recording".postln;
		}
	}

	stopRecording {
		thisProcess.removeOSCRecvFunc(f);
		file.close;
		isRecording=false;
		"OSCRecorder stopped recording".postln;
	}

	//pauseRecording {}
	//resumeRecording {}

	gui {
		w=Window("OSC recorder", Rect(400,400,160,30)).front;
		w.addFlowLayout; w.alwaysOnTop_(true);
		w.onClose_{"close window".postln};

		w.onClose=w.onClose.addFunc({
			//recorder=false;
			thisProcess.removeOSCRecvFunc(f);
			if (file!=nil) {
				file.close;
			};
		});
		Button(w, 40@20).states_([ [\rec],[\REC,Color.black,Color.red] ]).action_{|b|
			if (b.value==1) {
				this.startRecording;
			}{
				this.stopRecording;
			}
		};
		TextField(w, 100@20).string_(fileName).action_{|ez| fileName=ez.string.postln};
	}
}
