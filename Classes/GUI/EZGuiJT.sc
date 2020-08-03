EZGuiJT {
	var bounds, type, labelWidth, numberWidth, font;

	*new { arg parent, bounds, label, controlSpec, action, initVal,
		initAction=false, labelWidth=60, numberWidth=45,
		unitWidth=0, labelHeight=20,  layout=\horz, gap, margin, fontName, equalLength=false
		, defaultRound=0.0001;

		^super.new.init(parent, bounds, label, controlSpec, action,
			initVal, initAction, labelWidth, numberWidth,
			unitWidth, labelHeight, layout, gap, margin, fontName, equalLength
			, defaultRound)
	}

	init { arg parentView, argbounds, label, argControlSpec, argAction, initVal,
		initAction, argLabelWidth, argNumberWidth, argUnitWidth,
		labelHeight, argLayout, argGap, argMargin, fontName, equalLength, defaultRound;

		var bounds=argbounds.copy, type, fontSize=bounds.y*0.6, round;
		labelWidth=argLabelWidth??{bounds.y*0.6 * 8;};
		numberWidth=argNumberWidth??{bounds.x*0.15};
		type=if (initVal!=nil, {
			switch(initVal.asArray.size, 1, {EZSlider}, 2, {
				EZRanger}, {
				bounds.y=bounds.x*0.25;
				EZMultiSlider
			})
		},{EZSlider});

		if ((type==EZRanger) && (equalLength), {
			labelWidth=labelWidth-numberWidth-4;
		});
		if ((type==EZMultiSlider) && ((bounds.x/bounds.y)>8)
			, {bounds.y=bounds.x*0.125});
		font=Font(fontName??{Font.defaultMonoFace}, fontSize);//Font.defaultMonoFace
		round=argControlSpec.step;
		if (round<0.00000001, {round=defaultRound.copy});
		^type.new(parentView, bounds, label, argControlSpec, argAction
			, initVal, initAction, labelWidth, numberWidth
			, layout: argLayout
			, margin: argMargin, gap: argGap).font_(font).round2_(round);

	}
}

/*
(
var w=Window.new.front; w.alwaysOnTop_(true); w.addFlowLayout;
EZGuiJT(w, 350@20, "test", ControlSpec(10, 1000, \exp), {|ez| ez.value.postln}, [40,500]);
EZGuiJT(w, 350@20, "test", ControlSpec(10, 1000, \exp), {|ez| ez.value.postln}, 500);
EZGuiJT(w, 350@20, "test", ControlSpec(10, 1000, \exp), {|ez| ez.value.postln}, [40,500], equalLength:true);
EZGuiJT(w, 350@20, "test", ControlSpec(10, 1000, \exp), {|ez| ez.value.postln}, [40,500,60], equalLength:true);
)
*/