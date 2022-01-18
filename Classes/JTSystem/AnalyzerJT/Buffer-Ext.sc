+ Buffer {
	findOnsets {arg action;
		var tmpPath="~/tmppppp.aif".asAbsolutePath;
		this.write(tmpPath);
		this.server.sync;
		^tmpPath.findOnsets(s:this.server, fileDelete:true, action: action);
	}

	//mmm, this is loadCollection or sendCollection....
	allocSetn {arg values;
		//arg server, numFrames, numChannels = 1, completionMessage, bufnum;

		^this.alloc(this.server, values.size, this.numChannels, {|z| "go".postln; [z, values].postln; z.setnMsg(0, values)}, this.bufnum);

		//Buffer.alloc(s, 10, 1, {|z| z.postln; z.setnMsg(0, {1.0.rand}!10)}, 5);


		//setn { arg ... args;
		//server.sendMsg(*this.setnMsg(*args));
	}


	beatRoot {arg dirName=PathName("~/Desktop").fullPath++"/", saPath="/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator", bpmLimits=[60.0, 125.0], maken3file=true;
		var bpm, cmd;
		var fileName="tmp", cvsfile, file, string;
		var cond=Condition.new;
		//this.write
		this.write(dirName++fileName++".aiff");
		this.server.sync;
		//this.write(dirName++fileName++".aiff", completionMessage: {
		if (maken3file, {
			cmd=("/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator -s vamp:beatroot-vamp:beatroot:beats > "++dirName++"beatroot-vamp.n3");
			cmd.unixCmd({cond.unhang}, false);
			cond.hang;
			if (this.server.sampleRate!=44100, {
				file=File(dirName++"beatroot-vamp.n3", "r");
				string=file.readAllString;
				file.close;
				string=string.replace("vamp:step_size \"441\"^^xsd:int", "vamp:step_size \""++(this.server.sampleRate*0.01).round(1.0).asInteger++"\"^^xsd:int");
				file=File(dirName++"beatroot-vamp.n3", "w");
				file.write(string);
				file.close;
			});
		});
		cmd=("/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator -t "++dirName++"beatroot-vamp.n3 "++dirName++fileName++".aiff -w csv  --csv-force --csv-basedir "++dirName++" --force");
		cmd.unixCmd({cond.unhang}, false);
		cond.hang;
		cvsfile=dirName++fileName++"_vamp_beatroot-vamp_beatroot_beats.csv";
		if (File.exists(cvsfile), {
			bpm=(CSVFileReader.read(cvsfile, true, true).collect{|a| a[0].interpret}.differentiate.copyToEnd(1).mean.reciprocal*60);
			("rm "++dirName++fileName++".aiff").unixCmd(postOutput:false);
			("rm "++cvsfile).unixCmd(postOutput:false);
			("rm "++dirName++"beatroot-vamp.n3").unixCmd(postOutput:false);
			if (bpm>bpmLimits[0], {
				if (bpm>bpmLimits[1], {
					bpm=2.pow((log(bpm/bpmLimits[1])/2.log).ceil.neg)*bpm;
				})
			},{
				bpm=2.pow((log(bpm/bpmLimits[0])/2.log).floor.neg)*bpm;
			});
		},{
			"no cvs file....".postln;
			//bpm=120;
		});
		^bpm
	}

	tempoTracker {arg dirName=PathName("~/Desktop").fullPath++"/", saPath="/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator", bpmLimits=[60.0, 125.0], mean=false;
		var bpm, cvs, stepsize;
		var fileName="tmp", cvsfile, file, string, duration, weights;
		var cond=Condition.new;
		//this.write
		this.write(dirName++fileName++".aiff");
		this.server.sync;
		//this.write(dirName++fileName++".aiff", completionMessage: {

		//("/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator -s vamp:qm-vamp-plugins:qm-tempotracker:tempo > "++dirName++"tempo-vamp.n3").postln;

		("/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator -s vamp:qm-vamp-plugins:qm-tempotracker:tempo > "++dirName++"tempo-vamp.n3").unixCmd({
			cond.unhang
		}, false);
		cond.hang;

		if (this.sampleRate!=44100, {
			stepsize=(this.sampleRate/44100*512).asInteger;
			file=File(dirName++"tempo-vamp.n3", "r");
			string=file.readAllString;
			file.close;
			string=string.replace("vamp:step_size \"512\"^^xsd:int", "vamp:step_size \""++stepsize++"\"^^xsd:int");
			file=File(dirName++"tempo-vamp.n3", "w");
			file.write(string);
			file.close;
		});

		("/Applications/sonic-annotator-1.5-osx-amd64/sonic-annotator -t "++dirName++"tempo-vamp.n3 "++dirName++fileName++".aiff -w csv  --csv-force --csv-basedir "++dirName++" --force").unixCmd({cond.unhang}, false);
		cond.hang;
		cvsfile=dirName++fileName++"_vamp_qm-vamp-plugins_qm-tempotracker_tempo.csv";
		if (File.exists(cvsfile), {
			cvs=CSVFileReader.read(cvsfile, true, true);
			bpm=if (mean, {
				duration=this.duration;
				weights=(cvs.collect{|a| a[0].interpret}
					++duration).differentiate.copyToEnd(1);
				cvs.collect{|a| a[1].interpret}.meanWeighted(weights)
			},{
				cvs.last[1].interpret;
			});
			/*
			bpm=(CSVFileReader.read(cvsfile, true, true).collect{|a|
			a[0].interpret}.differentiate.copyToEnd(1).mean.reciprocal*60);
			*/

			("rm "++dirName++fileName++".aiff").unixCmd(postOutput:false);
			("rm "++cvsfile).unixCmd(postOutput:false);
			("rm "++dirName++"tempo-vamp.n3").unixCmd(postOutput:false);

			/*
			if (bpm>bpmLimits[0], {
			if (bpm>bpmLimits[1], {
			bpm=2.pow((log(bpm/bpmLimits[1])/2.log).ceil.neg)*bpm;
			})
			},{
			bpm=2.pow((log(bpm/bpmLimits[0])/2.log).floor.neg)*bpm;
			});
			*/
		},{
			"no cvs file....".postln;
			//bpm=120;
		});

		^bpm
	}

}