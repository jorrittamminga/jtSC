//----------------------------------------
var w, guis=(), synth=();
var server, serverIndex, nn=0;
var cs=(nn: ControlSpec(48, 98), nnInterval: ControlSpec(0, 12, 6.0), stableGate:ControlSpec(0.0, 1.0), ampl:\amp.asSpec, specflatness:ControlSpec(0.0, 1.0, 4.0), key: ControlSpec(0.0, 24.0), key2: ControlSpec(0.0, 24.0));
var nns={0}!12;
~histo={0}!24;

//~histo.plot
//~histo.order.reverse
//----------------------------------------
server=t;
serverIndex=1;

//----------------------------------------
SynthDef(\SimpleBell, {arg freq=440, amp=0.1, az=0.0, dur=1.0, outBus=0, at=0.2;
	var env=EnvGen.kr(Env.perc(at*dur, dur, amp), doneAction:2);
	var out;
	out=LFTri.ar(freq, 0, env);
	Out.ar(outBus, PanAz.ar(~noc, out, az))
}).add;


SynthDef(\Analysis, {arg inBus, outBus=0, lagU=0.3, lagD=0.3, durThreshold=1.0, intervalThreshold=0.1, lagSafe=0.2, dur=0.01, changeThreshold=1.0, changeDur=0.05, freqLow=600, freqHigh=2500;
	var in=In.ar(inBus), fft512, fft1024, fft2048, fft4096;
	var freq,hasFreq, nn, nnInterval, trig, length, stableGate, stableGateLagged;
	var ampl, specflatness;
	var stableOut, key, onsets, change, inFilt;
	var env=EnvGen.kr(Env.dadsr(4.0, 1.0, 1.0, 1.0, 1.0));
	var trackb,trackh,trackq,tempo;
	in=in+WhiteNoise.ar(0.00000001);//0.00000001
	inFilt=BLowPass4.ar(BHiPass4.ar(in, freqLow), freqHigh);
	#freq,hasFreq=Tartini.kr(inFilt, 0.93, 1024, 0, 512);
	freq=Gate.kr(freq, (freq>freqLow));
	//#freq,hasFreq=Pitch.kr(in, 440, 300, 2000);
	fft512=FFT(LocalBuf(512), in);
	fft1024=FFT(LocalBuf(1024), in);
	fft2048=FFT(LocalBuf(2048), in);
	fft4096=FFT(LocalBuf(4096), in);
	key=KeyTrack.kr(fft4096);
	ampl=Amplitude.kr(in, 0.0, 0.1);
	specflatness=SpecFlatness.kr(fft2048);
	onsets=Onsets.kr(fft512);
	nn=freq.cpsmidi;
	nnInterval=HPZ1.kr(nn.lag(0.0001));
	nnInterval=Latch.kr(nnInterval, Changed.kr(nn));
	nnInterval=nnInterval.abs.lag(0, 0.01);
	length=Sweep.kr(Changed.kr(nnInterval>intervalThreshold));
	lagU=lagU.min(durThreshold*0.5);
	#trackb,trackh,trackq,tempo=BeatTrack.kr(fft1024);

	stableGate=SetResetFF.kr(length>durThreshold
		, TDelay.kr(Changed.kr(nnInterval>intervalThreshold), durThreshold-lagU-lagSafe));

	stableGateLagged=EnvGen.kr(Env.dadsr(lagSafe, lagU, 0,1,lagD, 1, 0), stableGate);

	stableOut=DelayN.ar(
		in//Normalizer.ar(in, 1.0, dur)
		, 4.0
		, durThreshold//-dur
	)*stableGateLagged;
	trig=Impulse.kr(SampleRate.ir/1024);

	change=Trig1.kr(
		Changed.kr(DelayN.kr(nn, 1.0, changeDur).round(changeThreshold)%12)
		//+DelayN.kr(onsets, 1.0, changeDur)//onsets
		, changeDur);

	//change=onsets;

	SendReply.kr(trig, '/analysis', [nn, nnInterval, stableGate, ampl, specflatness]);
	SendReply.kr(stableGate, '/stable', [nn, durThreshold]);
	SendReply.kr(1-stableGate, '/unstable', [durThreshold-lagSafe]);
	SendReply.kr(fft4096*(specflatness<0.2), '/key', [key]);
	SendReply.kr(fft4096*(specflatness<0.2), '/notenumber', [nn]);
	SendReply.kr(change*(specflatness<0.2), '/change', [nn]);

	SendReply.kr(trackb, '/beat', [tempo]);

	Out.ar(outBus, stableOut);
	//Out.ar(0, stableOut!2)
	//Out.kr(outKBus, [nn, nnInterval, stableGate, ampl]);
}, metadata: (specs:(
	lagU: ControlSpec(0.001, 1.0, \exp), lagSafe: ControlSpec(0.001, 0.2, \exp)
	, durThreshold: ControlSpec(0.3, 2.0, \exp)
	, intervalThreshold: ControlSpec(0.0, 1.0, 1.0)
	, changeThreshold: ControlSpec(0.0, 1.0), changeDur: ControlSpec(0.0001, 1.0, \exp)
	, freqLow: \freq.asSpec, freqHigh: \freq.asSpec
))).add;
server.sync;

//----------------------------------------
~bus[\Stable]=Bus.audio(server, 1);
server.sync;
~synth[\Analysis]=Synth.after(~synth[\in][serverIndex], \Analysis, [\inBus, ~bus[\in][serverIndex], \outBus, ~bus[\Stable]]);
server.sync;

//---------------------------------------- GUI
{
	w=Window("Analysis", Rect(0,1000,400,400)).front;
	w.addFlowLayout; w.alwaysOnTop_(true);
	~win[\Analysis]=w;
	cs.sortedKeysValuesDo{|key,val|
		guis[key]=EZSlider(w, 350@20, key, val, labelWidth: 100);
		guis[key].round2_(0.00001);
	};
	guis[\nns]=MultiSliderView(w, 350@100).value_(nns);
	~osc[\analysis]=OSCFunc({arg msg;
		var parameters=[\nn, \nnInterval, \stableGate, \ampl, \specflatness], p=();
		parameters.do{|par, i|
			var val=msg[3+i];
			p[par]=val;
			{guis[par].value_(val)}.defer
		};
	},'/analysis');
	~osc[\pitch]=OSCFunc({arg msg, time;
		var val=msg[3].round(1.0).asInteger;
		var nn=val.round(1.0)%12;
		/*
		nns[nn.asInteger]=nns[nn.asInteger]+1;
		{guis[\nns].value_(nns.floor.normalize)}.defer;
		nns=(nns*0.99);
		~weightNN=nns;
		*/
	}, '/notenumber');
	~osc[\change]=OSCFunc({arg msg, time;
		var val=msg[3];
		//dit moet worden ge-uncomment!
		/*
		~midifile.add([val, time]);
		if (~midifile.size>1, {
			~midifile[~midifile.size-2][1]=time-~midifile[~midifile.size-2][1];
		});

		Synth(\SimpleBell, [\freq, val.round(1.0).midicps, \amp, 0.1, \az, 0.0.rand2, \dur, 0.1, \outBus, 0, \at, 0.0]);
		*/
	}, '/change');
	~synth[\Analysis].makeGUI(w, onClose:true);
	w.view.decorator.nextLine;
	~func[\showhide].value(\Analysis);
}.defer;