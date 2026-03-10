+ Buffer {

	readAndConvert {arg argpath, startFrame = 0, numFrames = -1, completionMessage, action, mono=false, serverNRT;
		var sf, folder;
		var headerFormat, sampleFormat, duration, sampleReet, numChannels, numFramez;
		var tmpPath, score, x, serverOptions, bufN, serverFlag=false;

		path = argpath;
		this.startFrame = startFrame;
		tmpPath=PathName.tmp ++ this.hash.asString;

		folder=path.dirname++"/";
		sf=SoundFile.openRead(path);
		headerFormat=sf.headerFormat;
		sampleFormat=sf.sampleFormat;
		numChannels=sf.numChannels;
		sampleReet=sf.sampleRate;
		duration=sf.duration;
		numFramez=sf.numFrames;
		sf.close;

		if ((startFrame==0) && (numFrames<=0)) {
			numFrames=numFramez;
		} {
			if (numFrames<=0) {
				numFrames=numFramez-startFrame;
				duration=numFrames/sampleReet;
			} {
				duration=(numFrames-startFrame)/sampleReet;
			}
		};
		serverOptions=server.options.deepCopy;

		serverOptions.numOutputBusChannels_(if (mono) {1} {numChannels})
		.numInputBusChannels_(2).verbosity_(-2);

		serverNRT = serverNRT??{
			serverFlag=true;
			Server(\nrt, options: serverOptions)
		};
		bufN=serverNRT.nextBufferNumber(1);

		score=Score([
			[0, ['/b_allocRead', bufN, path, startFrame, numFrames]],
			[0, ['/d_recv', SynthDef(\NRTPlayBufJT, {
				var out;
				out=PlayBuf.ar(numChannels, bufN, BufRateScale.ir(bufN), 1, 0, 0, 2);
				if (mono&&(numChannels>1)) {out=out.sum*numChannels.reciprocal};
				Out.ar(0, out)
			}).asBytes
			]],
			[0.0, (x = Synth.basicNew(\NRTPlayBufJT, serverNRT, 1000)).newMsg(args: [freq: 400])],
			[duration, x.freeMsg, [\b_free, bufN], [\c_set, 0, 0]]
		]
		);
		score.recordNRT(nil, tmpPath, nil, server.sampleRate, headerFormat, sampleFormat, serverOptions, "", duration,
			{
				numFrames=(server.sampleRate/sampleReet*numFrames).asInteger;
				{
					server.listSendMsg(this.allocReadMsg( tmpPath, 0, numFrames, completionMessage));//original allocRead
					server.sync;
					if (serverFlag) {serverNRT.remove};
					File.delete(tmpPath);
					action.value(this);
				}.fork
			}
		);
	}

	*readAndConvert {arg server, path, startFrame = 0, numFrames = -1, action, bufnum, mono=false, serverNRT;
		server = server ? Server.default;
		bufnum ?? { bufnum = server.nextBufferNumber(1) };

		^super.newCopyArgs(server, bufnum)
		//.doOnInfo_(action).cache
		.doOnInfo_(nil).cache
		.readAndConvert(path, startFrame, numFrames, {|buf| ["/b_query", buf.bufnum]}, action, mono, serverNRT )
	}

	updateInfoSync { arg action;
		this.updateInfo(action);
		if (thisProcess.mainThread.state>3, {server.sync});
	}


	// transfer a collection of numbers to a buffer through a file
	*loadCollectionMsg { arg server, collection, numChannels = 1, action;
		var data, sndfile, path, bufnum, buffer;
		server = server ? Server.default;
		bufnum ?? { bufnum = server.nextBufferNumber(1) };
		if(server.isLocal, {
			if(collection.isKindOf(RawArray).not) { collection = collection.as(FloatArray) };
			sndfile = SoundFile.new;
			sndfile.sampleRate = server.sampleRate;
			sndfile.numChannels = numChannels;
			path = PathName.tmp ++ sndfile.hash.asString;
			if(sndfile.openWrite(path),
				{
					sndfile.writeData(collection);
					sndfile.close;
					^super.newCopyArgs(server, bufnum)
					.cache.doOnInfo_({ |buf|
						if(File.delete(path), { buf.path = nil},
							{("Could not delete data file:" + path).warn;});
						action.value(buf);
					}).allocReadMsg(path, 0, -1, {|buf| ["/b_query", buf.bufnum] })

				}, { "Failed to write data".warn; ^nil }
			)
		}, { "cannot use loadCollection with a non-local Server".warn; ^nil })
	}


	/*
	loadCollectionMsg { arg collection, startFrame = 0, action;
	var data, sndfile, path;
	if(server.isLocal, {
	if(collection.isKindOf(RawArray).not,
	{data = collection.collectAs({|item| item}, FloatArray)}, {data = collection;}
	);
	if ( collection.size > ((numFrames - startFrame) * numChannels),
	{ "Collection larger than available number of Frames".warn });
	sndfile = SoundFile.new;
	sndfile.sampleRate = server.sampleRate;
	sndfile.numChannels = numChannels;
	path = PathName.tmp ++ sndfile.hash.asString;
	if(sndfile.openWrite(path),
	{
	sndfile.writeData(data);
	sndfile.close;
	this.read(path, bufStartFrame: startFrame, action: { |buf|
	if(File.delete(path), { buf.path = nil },
	{("Could not delete data file:" + path).warn });
	action.value(buf)
	})

	}, { "Failed to write data".warn });
	}, {"cannot do fromCollection with a non-local Server".warn })
	}
	*/

	writeWithFades {arg path, fadeIn=1.0, fadeOut=1.0, curveA=4, curveR= -4.0
		, headerFormat, sampleFormat, completionMessage;
		if (path==nil, {path=this.path});
		if (path==nil, {
			headerFormat=headerFormat??{"AIFF"};
			sampleFormat=sampleFormat??{"int24"};
			path=Platform.recordingsDir++"/tmp"++UniqueID.next++headerFormat.toLower;
			this.write(path,
				headerFormat, sampleFormat, -1, 0, false, {
					path.makeFades(
						fadeIn, fadeOut, curveA, curveR,
						{
							"read new file".postln;
							this.read(this.server, path, action:
								completionMessage)
						}
					)
			});
		},{
			path.makeFades(
				fadeIn, fadeOut, curveA, curveR,
				{
					this.read(path, action:
						completionMessage)
				}
			)
		});
	}

	copyDataWrap {arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1;
		var tmpstartFrame, tmpnumFrames;
		if (srcStartAt+numSamples>this.numFrames, {
			tmpstartFrame=[srcStartAt, 0];
			tmpnumFrames=[
				(this.numFrames-tmpstartFrame[0]),
				numSamples-(this.numFrames-tmpstartFrame[0])
			];
			this.copyData(buf, 0, tmpstartFrame[0], tmpnumFrames[0]);
			//this.server.syncJT;
			this.server.sync;
			this.copyData(buf, tmpnumFrames[0], tmpstartFrame[1]
				, tmpnumFrames[1]);
			//this.server.syncJT;
			this.server.sync;
		},{
			this.copyData(buf, dstStartAt, srcStartAt, numSamples);
			//this.server.syncJT;
			this.server.sync;
		});
	}
}