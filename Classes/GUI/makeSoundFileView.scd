SoundFileView(parent, bounds)

gui.guis[\soundView].soundfile_(soundFile);
gui.guis[\soundView].read(0, soundFile.numFrames);
guis[\soundView].timeCursorPosition_(msg[3]*player.sampleRate);
guis[\soundView].timeCursorPosition_(player.startFrame);
guis[\soundView].soundfile_(player.soundFile);
guis[\soundView].read(0, player.soundFile.numFrames);
guis[\soundView].gridOn = false;
guis[\soundView].setSelectionColor(0, Color.red);
guis[\soundView].timeCursorColor=Color.white;
guis[\soundView].timeCursorOn=true;
//guis[\soundView].action_{|ez| ez.postln};
guis[\soundView].mouseUpAction = {
	var value=
	(guis[\soundView].selections[guis[\soundView].currentSelection].integrate);
	if ((value[0]-value[1]).abs<1000, {value=[0, player.numFrames]});
	player.startFrame_(value[0]);
	player.endFrame_(value[1]);
	{
		var play=false;
		if (player.player.playNode.isPlaying, {play=true;
			player.stopPlaying;
			player.player.server.sync;
		});
		if (play, {
			player.startPlaying
		});
	}.fork
};

makeGUI

+ Synth {

makeSFView(parent, bounds=350@20, gap=4@4, margin=4@4, path)


}