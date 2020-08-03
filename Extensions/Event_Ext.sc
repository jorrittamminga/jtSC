+ Event {
	makeGui {arg parent, bounds=350@20, name, controlSpecs=(), onCloseFunc, canFocus=true
		, labelWidth, gap=4@4, margin=4@4;

		^EventGUIJT(this, parent, bounds, name, controlSpecs, onCloseFunc, canFocus, labelWidth, gap, margin)
	}
}