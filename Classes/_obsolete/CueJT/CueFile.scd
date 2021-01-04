/*
define the type of CueFile, to be used with CueJT
*/
CueFileJT : File {
	var func, value, path;

	makeFunc {}
	addFunc {}
	value_ {}
	save {}
	load {}
}
CueFileJTPreset : CueFileJT { }

CueFileJTPresetEvent : CueFileJTPreset {}
CueFileJTPresetArray : CueFileJTPreset {}
CueFileJTPresetString : CueFileJTPreset {}
CueFileJTPresetFunction : CueFileJTPreset {}
CueFileJTPresetSimpleNumber : CueFileJTPreset {}
