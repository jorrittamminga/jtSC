+ Buffer {

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