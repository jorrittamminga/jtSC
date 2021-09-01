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
		//var drawName=false;
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
		if ((parent!=nil)&&(name!=nil), {
			StaticText(parent, bounds).string_(name).font_(font).stringColor_(Color.white).background_(Color.black)
		});
		event.sortedKeysValuesDo{|key,val|
			var cs, action;
			if (controlSpecs[key]!=nil, {
				if (controlSpecs[key].class==Array, {
					if ( (controlSpecs[key][0].class==String)||(controlSpecs[key][0].class==Symbol), {
						cs=controlSpecs[key]
					});
				})
			});
			cs=cs??{controlSpecs[key].asSpec??{ControlSpec(val*0.5, val*2)}};
			action=actions[key]??{{|ez|
				event[key]=ez.value}};
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