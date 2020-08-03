Server.killAll

(
s.waitForBoot{
b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/malespeaker.aiff", bufnum:1);
//b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/lacrimaeneuten.aiff");
s.sync;
c=ClusterJT(\render, [\buffer, b]);
}
)

c.play(true, [\voices, 124, \sync, 0.0, \loop, 0, \delayTime, [0.0, 0.05], \rate, [0.25, 8.0], \attackTime, 0.0, \releaseTime, 1.0])

c.free

(
var path=thisProcess.nowExecutingPath.dirname++"/";
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/malespeaker.aiff");
	//b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/lacrimaeneuten.aiff");
	s.sync;
	c=ClusterJT(\render, [\buffer, b]);
	{
		var gui;
		gui=c.makeGUI;
		p=gui.addPresetSystem(gui.viewsPreset, path, "cluster",parentPS: gui.window
			, preLoad:false);
		p.restoreActionType_(1);
	}.defer;
}
)
b
c.argsEvent[\buffer]

//------------------------------------------------- LIVE CLUSTERING
(
var path=thisProcess.nowExecutingPath.dirname++"/";
s.waitForBoot{
	i=InJT([0], [s], [\mic]);
	i.addPlugin(\Meter);
	i.makeGUI;

	b=BufWrJT(i[\mic].bus, i[\mic].synth, 5, hasOverdub:false);
	b.startRecording;
	b.makeGUI;

	z=Buffer.alloc(s, 1024); s.sync;
	z=b.writeBefore(z, s.sampleRate*2, completionMessage: {"init buffer".postln});
	c=ClusterJT(\render, [\buffer, z]);
	{
		var gui;
		gui=c.makeGUI;
		p=gui.addPresetSystem(gui.viewsPreset, path, "cluster",parentPS: gui.window
			, preLoad:false);
		p.restoreActionType_(1);
		Button(b.gui.window, 100@40).states_([ ["Grap&Cluster"] ]).action_{
			{z=b.writeBefore(z, s.sampleRate*2, completionMessage:{c.play })}.fork

		};
		b.gui.window.rebounds;
	}.defer;
}
)

z.path
{z=b.writeBefore(z, s.sampleRate*2, completionMessage:{c.play })}.fork



//--------------------------------------------------------------------------------
(10-100).wrap(0,1000)

b.play
BufWrJT
CompressorJT
i[\mic].plugins[\BufWr].buffer.plot

c.argsEvent[\dur]
c.funcsEvent[\dur]

c.read("/Users/jorrittamminga/Music/Samples/1laag_flag_1CPY.aiff")
c.read("/Users/jorrittamminga/Music/Samples/amenbreak.wav")

PresetSystem

c.play(true)
c.controlSpecs
c.type=\render;
c.type_(\render).play(true, [\sync, 0.5, \curveAttack, 4, \attackTime, 0, \releaseTime, 0.5, \voices, 512, \rate, {exprand(1, 24.0)}], [\rate, -1], normalize:true)
c.paths
c.play(true, [\attackTime, 0.0, \releaseTime, 1.0, \voices, 100, \delayTime, [0.0, 1.0], \sync, 0.5, \rate, [0.5, 8.0]], [\rate, {|p| exprand(p[0], p[1])}, \delayTime, {|p| rrand(p[0], p[1])}], normalize:true)

c.play(true)
Env.linen(1, 0, 1, 1, [4, 0, -4]).plot
c.type=\realtime;
c.play(true, [\rate, [0.125, 4.0]], normalize:true)
c.play(true, [\rate, [0.5, 2.0]], normalize:false)
c.play(false, [\rate, [0.5, 2.0]], normalize:false)
c.play
c.type_(\render)
c.type_(\realtime).play

c.play(true, [\rate, {0.5+0.25.rand2}], normalize:true)
c.playRealTime

c.render([\rate, {2.0+0.5.rand2}], normalizeFile:true)
c.playDiskIn
c.playPlayBuf
c.play(false)
c.path

c.score.postcs;""
c.path


x=(poep:1234, kak:432, _drol: 5432)
x.sortedKeysValuesDo{|key,val| key.postln}



~score=[[0.1, [\s_new, \Sine, -1, 1, 1, \freq, 1000, \amp, 0.1]], [1.0, [\c_set, 0,0]]];
~score=c.score;
Score.render(~score)

c.function.value
Server.killAll

c.close



ClusterJT(\type, [\buffer, b, \outBus, 0],
	Synth(
		(
			s.waitForBoot{
				b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/malespeaker.aiff");
				s.sync;
				c=ClusterJT.new(b, type:\nothing)
			}
		)

		b=Buffer.read(s, "/Users/jorrittamminga/Dropbox/Current/Bosgraaf_Tamminga/Live_Electronics v3/files/recordings/LachrimaeFragment.aif", bufnum:b.bufnum);

		(
			c.values[\sync]=0.382;
			c.values[\attackTime]=0.5;
			c.values[\releaseTime]=0.5;
			c.values[\transpose]=1/16;
			c.ratios={exprand(1.0, 127)}!128;
			c.values[\cAttack]=0;
			c.values[\cRelease]= -4.0;
			c.values[\startTimeDev]=0.025;
			c.values[\dur]=10.0;
			c.values[\startPos]=[0.0, 1.0];
			c.values[\durPow]= -0.25;
			c.buffer_(b)
		)

		c.renderPlay
		c.playSF
		c.duration
		b.duration*4

		+ Buffer {

			*overDub {arg server, path, numFrames = 44100, action, bufnum;
				var score=List[], buf, numFrames, overDubs, duration, ampCor, soundFile;
				server=server??{Server.default};

				buf=soundFile.openRead(path);
				score.add([0.0, [\b_allocRead, 1, path]]);
				overDubs=(buf.numFrames/numFrames).ceil;
				duration=numFrames/server.sampleRate;
				ampCor=overDubs.reciprocal.sqrt;
				overDubs.do{|i|
					score.add([0.0, [\s_new, \PlayBuf, -1, 1, 1, \startPos, numFrames*i
						, \duration, duration, \amp, ampCor]]);
				};
				score.add([duration, [\c_set, 0, 0]]);
				Score.render(score, newPath, numberOfChannels, sampleRate, headerFormat, sampleFormat
					, action, false, false);
				Buffer.read(server, newPath, 0, -1)
			}

			overDub {arg argpath, fileStartFrame = 0, numFrames = -1, bufStartFrame = 0, leaveOpen = false, action


			}
		}

		Buffer.overDub

		makeEZGui


		