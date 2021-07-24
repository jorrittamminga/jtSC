+ String {
	//string is path to soundfile
	//maybe make also a +SoundFile makeFades....


	makeFades {arg fadeIn=1.0, fadeOut=1.0, curveA=4, curveR= -4.0, completionMessage, newPath;
		var path=this;
		var server,inputFile, duration, tmpPath=path++"tmp";
		inputFile = SoundFile.openRead(path);
		inputFile.close;
		duration=inputFile.duration;
		server = Server(\nrt,
			options: ServerOptions.new
			.numOutputBusChannels_(inputFile.numChannels)
			.numInputBusChannels_(inputFile.numChannels)
			.verbosity_(-1)
		);
		//if (fadeIn+fadeOut>duration, {});
		Score([
			[0.0, ['/d_recv',
				SynthDef(\fadeIn, {
					var in = SoundIn.ar((0..(inputFile.numChannels-1)));
					Out.ar(0, in
						*EnvGen.kr(Env.linen(fadeIn, inputFile.duration-fadeIn-fadeOut
							, fadeOut, 1, [curveA, 0, curveR])))
				}).asBytes
			], Synth.basicNew(\fadeIn, server).newMsg]
		]).recordNRT(nil,
			tmpPath,
			inputFile.path,
			inputFile.sampleRate,
			inputFile.headerFormat,
			inputFile.sampleFormat,
			server.options,
			"",
			inputFile.duration
			,{
				("cp "++tmpPath++" "++path).unixCmd({
					File.delete(tmpPath);
					completionMessage.value
				});
			}
		);
		server.remove;
	}

}