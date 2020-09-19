/*
PresetRootFolderJT (/folders/folders/) PresetDirJT (o.a. master) PresetFolderJT/PresetFileJT
'cueList' of 'array' of 'presetFileJTCollection' b.v. [PresetFileJT, nil, nil, nil, PresetFileJT, PresetFileJT, nil ]

maak ook een lijst van alle presets (popupmenu) die je ook kunt oproepen of zelfs 'forceren' (zodat je altijd één bepaalde preset kunt terugroepen)

PresetCollection, een folder met presets
*/
PresetRootFolderJT : RootFolderJT {

}

PresetDirJT : FolderJT {

}

PresetFolderJT : FolderJT {

}

PresetFileJT : FileJT {

}

PresetMasterFolderJT : PresetFolderJT { }
PresetMasterFileJT : PresetFileJT {}
PresetSlaveFolderJT : PresetFolderJT { }
PresetSlaveFileJT : PresetFileJT {}

