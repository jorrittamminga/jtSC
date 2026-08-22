OSCRecorderJT {
	var <path, <port, <>excludePaths, <batchDuration;
	var <dirname, <fileName;
	var file, tmpFileName, startTime, recvFunc;
	var <window, <recorder, <isRecording;
	var <pathName;
	var <tmpFilePath, batch, batchRoutine;

	*new {arg path, port=57120, excludePaths=['/status.reply'], batchDuration=0.25;
		^super.newCopyArgs(path, port, excludePaths, batchDuration).init
	}

	init {
		//--------------------------------------------------------------------------
		fileName="";
		isRecording=false;
		batch=List[];
		batchDuration=batchDuration??{0.25};
		//------------------------------------------------------
		path=path??{"~/Desktop/".absolutePath};
		dirname=PathName(path).fullPath;
		if (File.exists(dirname).not) {File.mkdir(dirname)};
		//------------------------------------------------------
		recvFunc = { |msg, time, replyAddr, recvPort|
			if (recvPort==port) {
				if(excludePaths.includes(msg[0]).not) {
					batch.add([Main.elapsedTime, msg]);
				};
			};
		};
	}

	startRecording {
		if (isRecording.not) {
			isRecording=true;
			startTime=Main.elapsedTime;
			tmpFileName=fileName??{""};
			tmpFileName=tmpFileName++"_"++(Date.localtime.stamp);
			file=File( pathName=(dirname +/+ tmpFileName++".txt"), "w");
			thisProcess.addOSCRecvFunc(recvFunc);
			batchRoutine={
				inf.do{
					batchDuration.wait;
					this.deserialize;
				}
			}.fork;
			"OSCRecorder is recording in ".post;pathName.postln;
		} {
			"OSCRecorder is already recording".postln;
		}
	}

	deserialize {
		var msgString="", batchCopy=batch, lines;
		batch=List[];
		if (batchCopy.size>0) {
			lines=batchCopy.collect{|totalMsg|
				var time, msg;
				#time, msg=totalMsg;
				(time - startTime).asString ++ "," ++ msg.collect(_.asString(65536)).join(",")
			};
			if (file.notNil and: { file.isOpen }) {
				file.write(lines.join("\n") ++ "\n");
			};
		}
	}

	stopRecording {
		thisProcess.removeOSCRecvFunc(recvFunc);
		if (batchRoutine!=nil) {batchRoutine.stop};
		if (file.notNil) {
			this.deserialize;
			file.close };
		isRecording=false;
		file=nil;
		"OSCRecorder stopped recording, file written ".post; pathName.postln;
	}

	close {
		if (isRecording) {
			this.stopRecording
		}
	}

	free {
		this.close
	}
	//pauseRecording {}
	//resumeRecording {}

	gui {
		var w;
		w=Window("OSC recorder", Rect(400,400,160,30)).front;
		w.addFlowLayout; w.alwaysOnTop_(true);
		w.onClose_{
			this.close
		};
		Button(w, 40@20).states_([ [\rec],[\REC,Color.black,Color.red] ]).action_{|b|
			if (b.value==1) {
				this.startRecording;
			}{
				this.stopRecording;
			}
		};
		TextField(w, 100@20).string_(fileName).action_{|ez| fileName=ez.string};
		window=w;
	}
}