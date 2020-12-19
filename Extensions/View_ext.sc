+ View {
	controlSpec { ^ControlSpec(0.0, 1.0, 0, 0) }
}
//+ AbstractStepValue {}
+ ItemViewBase {
	controlSpec { ^ControlSpec(0, this.items.size-1, 0, 1) }
}

+ Button {
	controlSpec { ^ControlSpec(0, this.states.size-1, 0, 1) }
}

+ RoundButton {
	controlSpec { ^ControlSpec(0, this.states.size-1, 0, 1) }
}

+ CheckBox{
	controlSpec { ^ControlSpec(0, 1, 0, 1) }
}

+ EZRanger {
	*new { arg parent, bounds, label, controlSpec, action, initVal,
			initAction=false, labelWidth=60, numberWidth=45,
			unitWidth=0, labelHeight=20,  layout=\horz, gap,margin;
		var cs=controlSpec??{[0.0, 1.0]};
		var tmpVal=[cs.asSpec.minval, cs.asSpec.maxval];
		^super.new.init(parent, bounds, label, controlSpec, action,
			initVal??{tmpVal}, initAction, labelWidth, numberWidth,
				unitWidth, labelHeight, layout, gap, margin)
	}
}
/*
+ Array {
	asSpec { var cs=ControlSpec( *this ); if (cs.step<0.00001, {cs=cs.warp}); ^cs }
}

+ ControlSpec {
	mapNoRound { arg value;
		// maps a value from [0..1] to spec range
		^warp.map(value.clip(0.0, 1.0));
	}
	unmapNoRound { arg value;
		// maps a value from spec range to [0..1]
		^warp.unmap(value.round(step).clip(clipLo, clipHi));
	}
}
*/