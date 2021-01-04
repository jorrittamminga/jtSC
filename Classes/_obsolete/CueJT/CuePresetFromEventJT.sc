CuePresetFromEventJT : CuePresetJT {
	makeGui {arg parent, bounds=350@20;
		{gui=CuePresetJTGUI(this, parent, bounds)}.defer
	}

	add {
		object.add;
		object.name_( PathName(paths[cueMaster.cueID]).split($/).last );
		this.prAdd;
	}

	delete {
		object.removeAt;
		this.prDelete;
	}
}