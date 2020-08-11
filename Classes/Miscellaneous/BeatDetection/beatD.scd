(

var bpm=120;
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif");
	s.sync;
	x={var out=PlayBuf.ar(1, b.bufnum, BufRateScale.ir(b.bufnum), loop:1); Out.ar(100, out); Out.ar(0, out!2)}.play;
	s.sync;
	t=TempoTrackerJT(100, x, \addAfter);
	t.makeGUI(nil, 350@40)
	}
)




(
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:0);
	s.sync;

	x={var out=PlayBuf.ar(1, b.bufnum, BufRateScale.ir(b.bufnum), loop:1); Out.ar(100, out); out*0.5!2}.play;

	s.sync;

	d=BeatDetectorJT(100, x, \addAfter, (fftSize:512
		, duration:15.0, refreshTime:3.0
		, tempoDrift:0.95, tempoLagTime:0.0, highestDivision:5));
	{
		d.makeGUI(nil, 350@80);
		/*
		o=OSCFunc({arg msg; var tempo=msg[3].postln;
		if (msg[3]>0.0, {d.synth.set(\tempo, tempo)})
		}, d.cmdNameTempo)
		*/
	}.defer
}
)






0.1/0.559
(
var bpm=120;
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:1);

	c=Buffer.alloc(s, s.sampleRate*20, bufnum:2);//write buffer
	e=Buffer.alloc(s, s.sampleRate*15, bufnum:3);//tmp buffer
	s.sync;

	x={arg t_trig, tempo=0.5, t_reset, decayTime=5.0, dur=0.0001, drift=0.95;
		var metronome, fft, onsets, trigger, resetTrigger, time;
		var in=PlayBuf.ar(1, b.bufnum, loop:1);
		var phase=Phasor.ar(1, 1, 0, BufFrames.ir(c.bufnum));
		BufWr.ar(in, c.bufnum, phase);
		SendReply.kr(t_trig, '/frame', phase);
		fft=FFT(LocalBuf(512), in);
		onsets=Onsets.kr(fft);
		resetTrigger=LocalIn.kr(1);
		metronome=TDuty.kr(tempo, t_reset+resetTrigger, 1);
		onsets=Trig1.kr(onsets, SampleDur.ir);

		trigger=metronome+Trig1.kr(onsets, tempo*drift);
		time=Timer.kr(trigger);
		LocalOut.kr( Trig1.kr( time<0.1, 0.01) //((1-drift)*tempo)
			//+ Trig1.kr( (time-tempo).abs<0.1, 0.01)
		);

		Timer.kr(Trig1.kr(metronome, tempo*0.5)).poll(Trig1.kr(metronome, tempo*0.5));

		Out.ar(0, [in, K2A.ar(metronome)])

		//Out.ar(0, SinOsc.ar([2000,3000], 0, [metronome,onsets]));
		//Out.ar(0, K2A.ar([onsets, metronome])+in);
		//Out.ar(0, in*0.5!2);
	}.play;
	o=OSCFunc({arg msg;
		{
			c.copyDataWrap(e, 0, (msg[3]-e.numFrames)%c.numFrames, e.numFrames);
			bpm=e.tempoTracker;
			x.set(\tempo, (60/bpm).postln);
			//"bpm is ".post; e.beatRoot.postln;
		}.fork
	}, '/frame');
	f={
		inf.do{
			x.set(\t_trig, 1);
			3.0.wait;
		}
	}.fork
}
)
s.recChannels_(2);
s.recSampleFormat_("int24")
x.set(\t_reset, 1);
c.plot
60/107.666
x.set(\decayTime, 15.0, \dur, 0.55)

c.getn(0, 100, {|b| b.postln})

Server.killAll


t.buffer[\writeBuffer].getn(0, 100, {|b| b.postln})
c=Buffer(s, 44100)

c.plot


Server.killAll
(
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:0);
	s.sync;

	x={var out=PlayBuf.ar(1, b.bufnum, loop:1); Out.ar(100, out); out*0.5!2}.play;

	s.sync;

	d=BeatDetectorJT(100, x, \addAfter, (fftSize:512
		, duration:7.0, refreshTime:2
		, tempoDrift:0.95, tempoLagTime:0.0, highestDivision:7));
	{
		d.makeGUI(nil, 350@80);
		/*
		o=OSCFunc({arg msg; var tempo=msg[3].postln;
		if (msg[3]>0.0, {d.synth.set(\tempo, tempo)})
		}, d.cmdNameTempo)
		*/
	}.defer
}
)
(
o=OSCFunc({arg msg; var tempo=msg[3];
	if (msg[3]>0.0, {y.set(\tempo, tempo)})
}, d.cmdNameTempo)
)
o.free;
(
y={arg inBus=100, tempo=0.58, decayTime=5.0;
	var in=In.ar(inBus);
	var fft=FFT(LocalBuf(512), in);
	var onsets=Onsets.kr(fft);
	var out;
	decayTime=(Trig1.kr(Changed.kr(tempo), tempo).linlin(0.0, 1.0, 1.0, 0.0).lag(tempo)*decayTime);
	decayTime.poll(onsets);
	out=CombL.kr(onsets, 2.0, tempo
		, decayTime
		, 1.0, onsets*0.5);

	out=out*0.1;
	out.poll(onsets);
	WhiteNoise.ar(out)!2
}.play(x, 0, 0.02, \addAfter)
)
y.free
[].sum

d.reset;
b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/ledzeppelinbeat.aif", bufnum:0);
b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/Dream_Acoustic.aiff", bufnum:0);
b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/DeepPurple1M.aiff", bufnum:0);
b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:0);
b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/1-01 Perfect Day.aif", 0, -1, 0, bufnum:0);

d.set(\duration, 8.0, \tempoDrift, 0.95, \refreshTime, 3.0);

a=[0.125, 0.125, 0.125, 0.5];
a.tempo;
0.25.cpsmidi
o.free







o=OSCFunc({arg msg; var tempo=msg[3].postln;
	//if (msg[3]>0.0, {d.synth.set(\tempo, tempo)})
}, d.cmdNameTempo)
(
o=OSCFunc({arg msg; var ratio=msg[3], tempo=msg[4];
	var frac, error, newTempo;

	ratio.round(0.001);
	frac=ratio.asFraction(8, false);
	((frac[0]/frac[1])/ratio).round(0.001);
	error=[frac[0]/frac[1],ratio];
	error=error[0]/error[1];
	newTempo=(((frac[0]/frac[1])/ratio)*tempo);
	frac=[newTempo, tempo].sort;
	frac=frac[0]/frac[1];
	[tempo, newTempo].postln;
	if (frac>0.95, {
		newTempo.postln;
		d.synth.set(\tempo, newTempo, \tempoLagTime, 0.1)
	});
}, d.cmdNameTempo)
)

d.synth.set(\tempoLagTime, 0.1)

(

)
o.free

d.close

d.set(\tempoDrift, 0.97)
d.set(\duration, 7)

0.56723030975887*(1-0.95)

o=OSCFunc({arg msg; var tempo=msg[3].postln;
	if (msg[3]>0.0, {d.synth.set(\tempo, tempo)})
}, d.cmdNameTempo)


-> localhost
Booting server 'localhost' on address 127.0.0.1:57110.
Found 0 LADSPA plugins
Number of Devices: 2
0 : "Built-in Microph"
1 : "Built-in Output"

"Built-in Microph" Input Device
Streams: 1
0  channels 2

"Built-in Output" Output Device
Streams: 1
0  channels 2


[ 0.058049887418747, 0.3366893529892, 0.87074828147888, 0.45859411358833, 0.27863946557045, 0.41215419769287, 0.45278912782669, 0.4295691549778, 0.29024943709373, 0.29605442285538, 0.13351473212242, 0.41795918345451, 0.30185940861702, 0.7256236076355, 0.4295691549778, 0.27863946557045 ].tempo(0.5, 1.125, 2)

[ 0.15092970430851, 0.4295691549778, 0.27283447980881, 0.45278912782669, 0.69659864902496, 0.29605442285538, 0.14512471854687, 0.28444445133209, 0.12770974636078, 0.29605442285538, 0.70820862054825, 0.12190476059914, 0.30766439437866, 0.52825397253036, 0.58630383014679, 0.56888890266418, 0.27863946557045 ].tempo(0.5, 1.1, 2)

[ 0.29605442285538, 0.55147391557693, 0.56308388710022, 0.5572789311409, 1.1261677742004, 0.47600907087326, 0.10448979586363, 0.19736962020397, 0.08707483112812, 0.13931973278522, 0.27863946557045, 0.14512471854687, 0.27863946557045, 0.42376416921616, 0.26702946424484, 0.15092970430851, 0.27283447980881, 0.29605442285538 ].tempo(0.5, 1.1, 2)

a=[ 0.12770974636078, 0.27863946557045, 0.14512471854687, 0.27283447980881, 0.4295691549778, 0.26702946424484, 0.14512471854687, 0.28444445133209, 0.45859411358833, 0.12770974636078, 0.28444445133209, 0.30766439437866, 0.42376416921616, 0.30185940861702, 0.13931973278522, 0.30185940861702, 0.40634921193123, 0.32507935166359, 0.13931973278522, 0.29024943709373, 0.43537414073944, 0.43537414073944 ].tempo(0.5, 1.1, 2)
//a=a.clumpsClusters
a=a.cpsmidi;
a=a.clumpsClustersDif(3)

a=a.collect{|a| a.midicps}




[ [ 0.12770974636078, 0.12770974636078, 0.13931973278522, 0.13931973278522, 0.14512471854687, 0.14512471854687 ], [ 0.26702946424484, 0.27283447980881, 0.27863946557045, 0.28444445133209, 0.28444445133209, 0.29024943709373, 0.30185940861702, 0.30185940861702, 0.30766439437866, 0.32507935166359 ], [ 0.40634921193123, 0.42376416921616, 0.4295691549778, 0.43537414073944, 0.43537414073944, 0.45859411358833 ] ]

a=[ [ 0.12770974636078, 0.12770974636078, 0.13931973278522, 0.13931973278522, 0.14512471854687, 0.14512471854687 ], [ 0.26702946424484, 0.27283447980881, 0.27863946557045, 0.28444445133209, 0.28444445133209, 0.29024943709373, 0.30185940861702, 0.30185940861702, 0.30766439437866, 0.32507935166359 ], [ 0.40634921193123, 0.42376416921616, 0.4295691549778, 0.43537414073944, 0.43537414073944, 0.45859411358833 ] ]


30/a[2].mean
30/a[2].cpsmidi.mean.midicps




a=[ 6, 4, 3, 2, 2 ];
a=(a.size-1).collect{|i| a[0]/a[i+1]}.indexOfGreaterThan(4);//


(
s.recChannels_(3);
s.recSampleFormat_("int24");
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif");
	s.sync;

	x={var in=PlayBuf.ar(1, b.bufnum, loop:1);
		var fft512, onsets512;
		var fft1024, onsets1024;
		fft512=FFT(LocalBuf(512), in, 0.5, 0);
		onsets512=Onsets.kr(fft512);
		fft1024=FFT(LocalBuf(1024), in, 0.5, 0);
		onsets1024=Onsets.kr(fft1024);
		[in, K2A.ar(onsets512), K2A.ar(onsets1024)]
	}.play;

}
)
Server.killAll
2.35442/4
(
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/SC_four_seasons_residentieorkest/files/soundfiles/Arezoo-Rezvani-Farid_M2.aif", bufnum:0);
	s.sync;

	x={arg tr=0.0, tempo=0.58;
		var out=PlayBuf.ar(1, b.bufnum, loop:1);
		var beat,fft=FFT(LocalBuf(1024), out), change;
		beat=BeatTrack.kr(fft)[3]*0.25;
		beat=Latch.kr(beat, fft);
		tempo=LocalIn.kr(1, tempo);
		tempo=Latch.kr(beat, (beat-tempo).abs<tr);
		LocalOut.kr(tempo);
		tempo.poll(Changed.kr(tempo));
	}.play;
}
)
x.set(\tr, 1000000)
x.set(\tr, 0.05)





