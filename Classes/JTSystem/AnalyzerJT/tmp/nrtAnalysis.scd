AnalyzerJT_NRT {

	var <soundFile, <dur, <numChannels;

	* new {arg inpath, outpath, descriptors=[\onsets, \loudness]
		, settings=(), metadataSpecs=()
		, outFlag=true, sendreplyFlag=true
		, outFFTFlag=true
		, fftsizes=(), hopsizes=(), updateFreq, normalized=false;


	}

	init {arg argpath, argoutpath, argdescriptors;
		//======================================= init stuff
		id=UniqueID.next;
		this.initAll;
		if (normalized, {this.makeNormalizers});
		//======================================= soundfile info
		soundFile=SoundFile.openRead(argpath);
		dur=soundFile.duration;
		numChannels=soundFile.numChannels;
		sampleRate=soundFile.sampleRate;
		headerFormat=soundFile.headerFormat;
		sampleFormat=soundFile.sampleFormat;
		soundFile.close;

		//======================================= init some more


		this.makeSynthDef((\AnalyzerNRT++id++numChannels).asSymbol);
		this.makeScore;

	}

	makeSynthDef {arg synthDefName;
		synthDef=synthDefName??{\Analyzer};
		SynthDef(synthDef, {
			var in=SoundIn.ar(0);//SoundIn.ar([0,1]).sum;

		})

	}
}



(
fork {
var server = Server(\nrt,
options: ServerOptions.new
.numOutputBusChannels_(2)
.numInputBusChannels_(2)
);
var resultbuf, resultpath, oscpath, score, dur, sf, cond, size, data;

// get duration
sf = SoundFile.openRead(Platform.resourceDir +/+ "sounds/a11wlk01.wav");
dur = sf.duration;
sf.close;

resultpath = PathName.tmp +/+ UniqueID.next ++ ".aiff";
oscpath = PathName.tmp +/+ UniqueID.next ++ ".osc";

score = Score([
[0, (resultbuf = Buffer.new(server, 1000, 1, 0)).allocMsg],
[0, [\d_recv, SynthDef(\onsets, {
var sig = SoundIn.ar(0), // will come from NRT input file
fft = FFT(LocalBuf(512, 1), sig),
trig = Onsets.kr(fft),
// count the triggers: this is the index to save the data into resultbuf
i = PulseCount.kr(trig),
// count time in seconds
timer = Sweep.ar(1);
// 'i' must be audio-rate for BufWr.ar
BufWr.ar(timer, resultbuf, K2A.ar(i), loop: 0);
BufWr.kr(i, resultbuf, DC.kr(0), 0);  // # of points in index 0
}).asBytes]],
[0, Synth.basicNew(\onsets, server, 1000).newMsg],
[dur, resultbuf.writeMsg(resultpath, headerFormat: "AIFF", sampleFormat: "float")]
]);

cond = Condition.new;

// osc file path, output path, input path - input is soundfile to analyze
score.recordNRT(oscpath, "/dev/null", sf.path, sampleRate: sf.sampleRate,
options: ServerOptions.new
.verbosity_(-1)
.numInputBusChannels_(sf.numChannels)
.numOutputBusChannels_(sf.numChannels)
.sampleRate_(sf.sampleRate),
action: { cond.unhang }  // this re-awakens the process after NRT is finished
);
cond.hang;  // wait for completion

sf = SoundFile.openRead(resultpath);
// get the size: one frame at the start
sf.readData(size = FloatArray.newClear(1));
size = size[0];
// now the rest of the data
sf.readData(data = FloatArray.newClear(size));
sf.close;

File.delete(oscpath);
File.delete(resultpath);
server.remove;

data.postln;  // these are your onsets!
};
)
