/*
value cue = [values, method, extra], waarbij values en extra een Event, Array of SimpleNumber kan zijn
of alleen de GUI anders bij CuesJT tov PresetsJT?
*/
CuesJT : PresetsJT {
	/*
	*new {arg object, name=\cue;
		^super.new.init(object, name)
	}
	init {arg argobject, argname;
		basename=argname;
	}
	index_ {arg id;

	}
	restore {}
	store {}
	add {}
	delete {}
	makeGui {arg parent, bounds=350@20;
		{gui=CuesJTGUI(this,parent,bounds)}.defer
	}
	*/
}

CuesJTGUI {
	var <cues;
	var <views, <parent, <bounds;

	*new {arg cues, parent, bounds;
		^super.newCopyArgs(cues).init(parent, bounds)
	}
	init {arg argparent, argbounds;
		var boundsName=(argbounds.x/3).floor@argbounds.y;
		var boundsButton=(boundsName.x/7).floor@argbounds.y;
		var c;
		views=();
		c=CompositeView(argparent, argbounds.x@(argbounds.y*2));
		c.addFlowLayout(0@0, 0@0);
		views[\add]=Button(c, boundsButton).states_([ ["+"] ]).action_{ cues.add };
		views[\delete]=Button(c, boundsButton).states_([ ["-"] ]).action_{ cues.delete };
		views[\store]=Button(c, boundsButton).states_([ ["s"] ]).action_{ cues.store };
		views[\restore]=Button(c, boundsButton).states_([ ["r"] ]).action_{ cues.restore };
		views[\cuename]=StaticText(c, boundsName).string_("");
		views[\prev]=Button(c, boundsButton).states_([ ["<"] ]).action_{ cues.prev };
		views[\cues]=PopUpMenu(c, boundsName).items_().action_{ };
		views[\next]=Button(c, boundsButton).states_([ [">"] ]).action_{ cues.next };
		//------------------------------------------------------------------------------- OBJECT SPECIFIC
		this.addView;
		//------------------------------------------------------------------------------- FUNCTIONS
		//combi update en index is dubbelopperdepop!
		[\update, \basename, \index].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					views[\basename].string_( presets.fileNamesWithoutNumbers[presets.index] );
					views[\presets].items_( presets.fileNamesWithoutNumbers ).value_(presets.index);
				}.defer
			});
		};
	}
	addView {

	}
}

CuesEventJTGUI : CuesJTGUI {
	addView {}
}
CuesPresetsJTGUI : CuesJTGUI {
	addView {}
}
CuesFunctionJTGUI : CuesJTGUI {
	addView {}
}