EventGUIJT : GUIJT {
	var <controlSpecs, <event, <actions;
	var onClose, canFocus;

	*new {arg event, parent, bounds=350@20, name, controlSpecs, onClose=true, canFocus=true
		, labelWidth
		, gap=4@4, margin=4@4, actions;
		^super.new.init(event, parent, bounds, name, controlSpecs, onClose, canFocus
			, labelWidth
			, gap, margin, actions);
	}

	init {arg argevent, argparent, argbounds, argname, argcontrolSpecs, argonClose
		, argcanFocus, arglabelWidth, arggap, argmargin, argactions;

		event=argevent;
		parent=argparent;
		bounds=argbounds;
		name=argname;
		controlSpecs=argcontrolSpecs??{()};
		actions=argactions??{()};
		onClose=argonClose;
		canFocus=argcanFocus;
		labelWidth=arglabelWidth;
		gap=arggap;
		margin=argmargin;
		this.initVars;
		this.initGUI;

		labelWidth=event.keys.asArray.collect{|key| key.asString.size}.maxItem*font.size*0.6;

		event.sortedKeysValuesDo{|key,val|
			var cs, action;
			cs=controlSpecs[key].asSpec??{ControlSpec(val*0.5, val*2)};
			action=actions[key]??{{|ez| event[key]=ez.value}};
			views[key]=this.makeEZGUI(bounds, key
				, cs
				, action, val, false, labelWidth, equalLength:false)
		};
		parent.rebounds;
		if (hasWindow, {window.rebounds});

		if (onClose!=nil, {
			parent.onClose=parent.onClose.addFunc(onClose);
		});
	}
}