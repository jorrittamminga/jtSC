IControlsView {
	var <fader, <knob, <buttonUp, <buttonDown, <bread;
	var <parentView, <window, <compositeView;

	*new {arg parent, bounds=80@60, layers=4, breads=9, labelHeight=10, gap=4@4, margin=4@4, includeTransport=false;
		^super.new.init(parent, bounds, layers, breads, labelHeight, gap, margin, includeTransport)
	}

	init {arg parent, bounds, layers, breads, labelHeight, gap, margin, includeTransport;
		var boundz=();
		if (breads.size==0, {breads=(0..breads-1);});
		if (layers.size==0, {layers=(0..layers-1);});
		if (parent==nil, {
			parent=Window("i-Controls", Rect(0,0
				,((bounds.x+gap.x)*breads.size+(2*margin.x-gap.x))+(margin.x*2)
				,(((bounds.y+gap.y)*layers.size+(2*margin.y-gap.y)))+(margin.x*2)
			)).front;
			parent.alwaysOnTop_(true);
			parent.addFlowLayout(margin, gap);
			window=parent;
		},{

		});
		parentView=parent;
		compositeView=CompositeView(parentView,
			((bounds.x+gap.x)*breads.size+(2*margin.x-gap.x))
			@
			(((bounds.y+gap.y)*layers.size+(2*margin.y-gap.y)))
		);
		compositeView.addFlowLayout(margin, gap);
		compositeView.background_(Color.grey(0.25));

		boundz[\label]=if (labelHeight==nil, {0@0}, {bounds.x@labelHeight});
		boundz[\fader]=(bounds.x*0.5)@((bounds.y-boundz[\label].y)/2);
		[\buttonUp, \buttonDown, \knob].do{|view| boundz[view]=boundz[\fader].copy};

		bread=layers.collect{|layer|
			breads.collect{|bread|
				var cv, out=(compositeView: ());
				cv=CompositeView(compositeView, bounds);
				cv.addFlowLayout(0@0,0@0);
				//cv.background_(Color.rand);
				[\label, \buttonUp, \knob, \buttonDown, \fader].do{|key|
					var c, bounds=boundz[key], view, string;
					c=CompositeView(cv, bounds);
					c.addFlowLayout(0@0,0@0);
					//c.background_(Color.rand);
					out[key]=switch (key
						, \label, {
							string=layer.asString++"_"++bread.asString;
							view=StaticText(c, bounds).string_(string)
							.background_(Color.black)
							.stringColor_(Color.white)
							.font_(Font.monospace(string.fontSize(bounds))).align_(\center);
							view
						}, \knob, {
							view=EZNumber(c, bounds, bread, [0, 1.0], {}, 0, false
								, bounds.x, bounds.x, 0, bounds.y*0.5, \line2, 0@0, 0@0);
							view.numberView.font_(Font.monospace(bounds.y*0.4)).decimals_(3);
							view.labelView.font_(Font.monospace(bounds.y*0.5)).align_(\right).stringColor_(Color.white);
							view
						}, \fader, {
							view=EZNumber(c, bounds, bread, [0, 1.0], {}, 0, false
								, bounds.x, bounds.x, 0, bounds.y*0.5, \line2, 0@0, 0@0);
							view.numberView.font_(Font.monospace(bounds.y*0.4)).decimals_(3);
							view.labelView.font_(Font.monospace(bounds.y*0.5)).align_(\right).stringColor_(Color.white);
							view
						}, {
							view=Button(c, bounds).states_([ [bread],[bread,Color.black,Color.green] ])
							.font_(Font.monospace(bounds.y));
							view
					});
					out[\compositeView][key]=c
				};
				out
			}
		};
	}
	at {arg layer=0, index=0;
		^bread[layer][index]
	}
	link {arg view, type=\fader, layer=0, bread=0, cs, action;
		var target=bread[layer][bread][type];
		view.action=view.action.addFunc({|ez|
			{target.value_(ez.value)}.defer
		})
	}
	linkFader {}
	linkKnob {}
	linkButton {}
	linkLabel {}
}