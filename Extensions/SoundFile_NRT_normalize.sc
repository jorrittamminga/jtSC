+ SoundFile {
	*normalize2 { |path, outPath, newHeaderFormat, newSampleFormat,
		startFrame = 0, numFrames, maxAmp = 1.0, linkChannels = true, chunkSize = 4194304,
		threaded = false, deleteOriginal = true, openNewFile = false, action|

		var	file, outFile,
		action2 = {
			protect {
				outFile = file.normalize2(outPath, newHeaderFormat, newSampleFormat,
					startFrame, numFrames, maxAmp, linkChannels, chunkSize, threaded, action);
			} { file.close };
			file.close;
			if (deleteOriginal, {("rm " ++ (path.asUnixPath)).unixCmd});
			if (openNewFile, { ("open " ++ (outPath.asUnixPath)).unixCmd });
		};

		(file = SoundFile.openRead(path.standardizePath)).notNil.if({
			// need to clean up in case of error
			if(threaded, {
				Routine(action2).play(AppClock)
			}, action2);


			^outFile
		}, {
			MethodError("Unable to read soundfile at: " ++ path, this).throw;
		});
	}

	normalize2 { |outPath, newHeaderFormat, newSampleFormat,
		startFrame = 0, numFrames, maxAmp = 1.0, linkChannels = true, chunkSize = 4194304,
		threaded = false, action |

		var	peak, outFile;

		outFile = SoundFile.new.headerFormat_(newHeaderFormat ?? { this.headerFormat })
		.sampleFormat_(newSampleFormat ?? { this.sampleFormat })
		.numChannels_(this.numChannels)
		.sampleRate_(this.sampleRate);

		// can we open soundfile for writing?
		outFile.openWrite(outPath.standardizePath).if({
			protect {
				"Calculating maximum levels...".postln;
				peak = this.channelPeaks(startFrame, numFrames, chunkSize, threaded);
				Post << "Peak values per channel are: " << peak << "\n";
				peak.includes(0.0).if({
					MethodError("At least one of the soundfile channels is zero. Aborting.",
						this).throw;
				});
				// if all channels should be scaled by the same amount,
				// choose the highest peak among all channels
				// otherwise, retain the array of peaks
				linkChannels.if({ peak = peak.maxItem });
				"Writing normalized file...".postln;
				this.scaleAndWrite(outFile, maxAmp / peak, startFrame, numFrames, chunkSize,
					threaded);
				"Done.".postln;
				action.value
			} { outFile.close };
			outFile.close;
			^outFile
		}, {
			MethodError("Unable to write soundfile at: " ++ outPath, this).throw;
		});
	}


	channelPeaksNRT { |startFrame = 0, numFrames, chunkSize = 1048576, threaded = false|
		var score, buf, cmd, options, oscpath, file, nrtOutPath, result, resultPath, cond
		, ch;
		var i;

		if(thisProcess.platform.name == \windows) {
			nrtOutPath = "NUL"
		} {
			nrtOutPath = "/dev/null";
		};
		score = Score([
			[0, [\d_recv, SynthDef(\nrtcp, {
				var sig = SoundIn.ar((0 .. this.numChannels - 1)),
				peak = Peak.ar(sig),
				timer = Sweep.ar(1),
				done = timer >= this.duration;
				Poll.ar(HPZ1.ar(timer % 30) < 0, timer, "Seconds processed: ");
				Poll.ar(done, peak);
			}).asBytes]],
			[0, Synth.basicNew(\nrtcp, Server.default, 1000).newMsg],
			[this.duration, [\c_set, 0, 0]]
		]);
		options = ServerOptions.new.numOutputBusChannels_(this.numChannels)
		.sampleRate_(this.sampleRate).verbosity_(-1)
		.memSize_(2**18)
		.maxSynthDefs_(2**18)
		;
		oscpath = (PathName.tmp +/+ "osc" ++ this.hash);
		score.write(oscpath, (tempo: 1));  // (tempo: 1) mimics a clock
		cmd = Server.program + " -N" + oscpath.quote
		+ this.path.unixPath + "'" ++ nrtOutPath ++ "'"
		+ this.sampleRate + "AIFF int16"
		+ options.asOptionsString;
		protect {
			if(threaded) {
				resultPath = PathName.tmp +/+ "result%.txt".format(this.hash);
				cond = Condition.new;
				(cmd + ">" ++ resultPath).unixCmd({ cond.unhang });
				cond.hang;
				file = File(resultPath, "r");
				result = file.readAllString;
			} {
				file = Pipe(cmd, "r");
				result = CollStream.new;
				while { (ch = file.getChar).notNil } { result << ch };
				result = result.collection;
			}
		} { file.close };
		i = result.findRegexp("UGen\\(Peak\\)");
		^i.collect { |pair| result[pair[0] + 12 ..].asFloat }
	}

	scaleAndWriteNRT { |outPath, scale, startFrame, numFrames, chunkSize, threaded = false, newHeaderFormat(this.headerFormat), newSampleFormat(this.sampleFormat)|
		var score, outbuf, options, cond, oscpath, inbuf;
		scale = scale.asArray.keep(this.numChannels);

		if(numFrames.isNil or: { numFrames < 0 }) {
			numFrames = this.numFrames - startFrame;
		};

		score = Score([
			[0, [\d_recv, SynthDef(\scaler, {
				Out.ar(0, SoundIn.ar((0 .. this.numChannels - 1)) * scale)
			}).asBytes]],
			[0, Synth.basicNew(\scaler, Server.default, 1000).newMsg]
		]);

		options = ServerOptions.new
		.verbosity_(-1)
		.numInputBusChannels_(this.numChannels)
		.numOutputBusChannels_(this.numChannels)
		.sampleRate_(this.sampleRate);

		oscpath = (PathName.tmp +/+ "osc" ++ this.hash);

		forkIfNeeded {
			cond = Condition.new;
			score.recordNRT(oscpath, outPath, this.path, this.sampleRate, newHeaderFormat, newSampleFormat, duration: numFrames / this.sampleRate, options: options, action: {
				cond.unhang });
			cond.hang;
			File.delete(oscpath);
		};
	}

	normalizeNRT { |outPath, newHeaderFormat, newSampleFormat, startFrame = 0, numFrames, maxAmp = 1, linkChannels = true, chunkSize = 4194304, threaded = false, action|
		var peaks,
		doIt = {
			peaks = this.channelPeaksNRT(startFrame, numFrames, chunkSize, threaded);
			if(linkChannels) { peaks = peaks.maxItem };
			if (peaks==nil, {peaks=1.0});
			this.scaleAndWriteNRT(outPath, (maxAmp / peaks), startFrame, numFrames, chunkSize, threaded, newHeaderFormat, newSampleFormat);
			action.value;
		};
		if(threaded) {
			fork(doIt);
		} {
			doIt.value
		};
	}

	*normalizeNRT { |path, outPath, newHeaderFormat, newSampleFormat, startFrame = 0, numFrames, maxAmp = 1, linkChannels = true, chunkSize = 4194304, threaded = false, action|
		var file = this.openRead(path);

		//("open " ++ (path.unixPath)).unixCmd;

		if(file.notNil) {
			protect {
				file.normalizeNRT(outPath, newHeaderFormat, newSampleFormat, startFrame, numFrames, maxAmp, linkChannels, chunkSize, threaded, action);
			} { file.close };
		} { Error("Couldn't open % for reading.".format(path)).throw };
	}

	*normalizeJT { |path, outPath, newHeaderFormat, newSampleFormat,
		startFrame = 0, numFrames, maxAmp = 1.0, linkChannels = true, chunkSize = 4194304,
		threaded = false, actionWhenReady|
		var cond=Condition.new;
		var	file, outFile,
		action = {
			protect {
				outFile = file.normalizeJT(outPath, newHeaderFormat, newSampleFormat,
					startFrame, numFrames, maxAmp, linkChannels, chunkSize, threaded
					, actionWhenReady);
			} { file.close };
			file.close;
		};

		(file = this.openRead(path.standardizePath)).notNil.if({
			if(threaded, {
				Routine(
					action

				).play(AppClock)
			}, action);
			^outFile
		}, {
			MethodError("Unable to read soundfile at: " ++ path, this).throw;
		});
	}

	normalizeJT { |outPath, newHeaderFormat, newSampleFormat,
		startFrame = 0, numFrames, maxAmp = 1.0, linkChannels = true, chunkSize = 4194304,
		threaded = false, actionWhenReady|

		var	peak, outFile;

		outFile = this.class.new.headerFormat_(newHeaderFormat ?? { this.headerFormat })
		.sampleFormat_(newSampleFormat ?? { this.sampleFormat })
		.numChannels_(this.numChannels)
		.sampleRate_(this.sampleRate);

		// can we open soundfile for writing?
		outFile.openWrite(outPath.standardizePath).if({
			protect {
				peak = this.channelPeaks(startFrame, numFrames, chunkSize, threaded);
				//Post << "Peak values per channel are: " << peak << "\n";
				peak.includes(0.0).if({
					MethodError("At least one of the soundfile channels is zero. Aborting.",
						this).throw;
				});
				// if all channels should be scaled by the same amount,
				// choose the highest peak among all channels
				// otherwise, retain the array of peaks
				linkChannels.if({ peak = peak.maxItem });
				this.scaleAndWrite(outFile, maxAmp / peak, startFrame, numFrames, chunkSize,
					threaded);
			} { outFile.close };
			outFile.close;
			actionWhenReady.value;
			^outFile
		}, {
			MethodError("Unable to write soundfile at: " ++ outPath, this).throw;
		});
	}




}
