/*
e=EZMultiSlider.new(nil, 300@300, "freq", \freq.asSpec, {|ez|
ez.sliderView.index.postln;
}, {exprand(100,10000)}!5, layout:\vert)
layout
*/
EZMultiSlider : EZGui {

	var <sliderView, <numberView, <unitView, <>controlSpec,
	popUp=false, numSize,numberWidth,unitWidth, gap;
	var <>round = 0.001, <round2 = 0.001;
	var <>sliderViewActionFunc;

	*new { arg parent, bounds, label, controlSpec, action, initVal,
		initAction=false, labelWidth=60, numberWidth=45,
		unitWidth=0, labelHeight=20,  layout=\horz, gap, margin;

		^super.new.init(parent, bounds, label, controlSpec, action,
			initVal, initAction, labelWidth, numberWidth,
			unitWidth, labelHeight, layout, gap, margin)
	}

	init { arg parentView, bounds, label, argControlSpec, argAction, initVal,
		initAction, labelWidth, argNumberWidth,argUnitWidth,
		labelHeight, argLayout, argGap, argMargin;

		var labelBounds, numBounds, unitBounds,sliderBounds;
		var numberStep;

		// Set Margin and Gap
		this.prMakeMarginGap(parentView, argMargin, argGap);

		unitWidth = argUnitWidth;
		numberWidth = argNumberWidth;
		layout=argLayout;
		bounds.isNil.if{bounds = 350@350};


		// if no parent, then pop up window
		# view,bounds = this.prMakeView( parentView,bounds);

		labelSize=labelWidth@labelHeight;
		numSize = numberWidth@labelHeight;

		// calculate bounds of all subviews
		# labelBounds,numBounds,sliderBounds, unitBounds
		= this.prSubViewBounds(innerBounds, label.notNil, unitWidth>0);

		// instert the views
		label.notNil.if{ //only add a label if desired
			labelView = GUI.staticText.new(view, labelBounds);
			labelView.string = label;
		};

		(unitWidth>0).if{ //only add a unitLabel if desired
			unitView = GUI.staticText.new(view, unitBounds);
		};

		sliderView = GUI.multiSliderView.new(view, sliderBounds);
		numberView = NumberBox(view,numBounds);//
		//numberView = GUI.numberBox.new(view, numBounds);

		// set view parameters and actions

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);
		this.onClose = { controlSpec.removeDependant(this) };
		(unitWidth>0).if{unitView.string = " "++controlSpec.units.asString};
		initVal = initVal ? ({controlSpec.default}!5);
		action = argAction;

		sliderView.elasticMode=1;
		sliderView.indexThumbSize=sliderBounds.width/initVal.asArray.size;


		sliderViewActionFunc={arg sl;
			{labelView.string_(sl.index)}.defer;
		};

		sliderView.action = {|sl|
			//sl.currentvalue.postln;
			//sl.index.postln;
			//{labelView.string_(sl.index)}.defer;
			sliderViewActionFunc.value(sl);
			this.valueAction_(controlSpec.map(sliderView.value));
		};

		sliderView.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		sliderView.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};
		/*
		sliderView.mouseDownAction={arg slider;
		[slider, slider.index, slider.value].postln;
		labelView.string_(slider.index)
		};
		*/
		sliderView.mouseUpAction={arg slider;
			labelView.string_(label)
		};

		numberView.action = { this.valueAction_(numberView.value) };

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			// controlSpec wants a step, so zooming in with alt is disabled.
			numberView.alt_scale = 1.0;
			//sliderView.alt_scale = 1.0;
		};

		numberView.step = numberStep;
		numberView.scroll_step = numberStep;
		//numberView.scroll=true;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
			//sliderView.value_([0,0,0,0,0]);
		};

		if (labelView.notNil) {
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};

		this.prSetViewParams;
	}

	value_ { arg val;
		value = controlSpec.constrain(val);
		numberView.value = value.asArray.clipAt(sliderView.index??{0}).round(round);
		sliderView.value = controlSpec.unmap(value);
	}

	valueAction_ { arg val;
		this.value_(val);
		this.doAction;
	}

	index {^sliderView.index}
	index_ {arg index; sliderView.index_(index)}
	currentvalue {^controlSpec.map(sliderView.currentvalue)}
	currentvalue_ {arg val;
		value[sliderView.index]=val;
		numberView.value = val.round(round);
		sliderView.currentvalue_(controlSpec.unmap(val));
	}

	doAction { action.value(this) }

	set { arg label, spec, argAction, initVal, initAction = false;
		labelView.notNil.if { labelView.string = label.asString };
		spec.notNil.if { controlSpec = spec.asSpec };
		argAction.notNil.if { action = argAction };

		initVal = initVal ? value ? controlSpec.default;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
	}


	setColors{arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background;

		stringBackground.notNil.if{
			labelView.notNil.if{labelView.background_(stringBackground)};
			unitView.notNil.if{unitView.background_(stringBackground)};};
		stringColor.notNil.if{
			labelView.notNil.if{labelView.stringColor_(stringColor)};
			unitView.notNil.if{unitView.stringColor_(stringColor)};};
		numBackground.notNil.if{
			numberView.background_(numBackground);};
		numNormalColor.notNil.if{
			numberView.normalColor_(numNormalColor);};
		numTypingColor.notNil.if{
			numberView.typingColor_(numTypingColor);};
		numStringColor.notNil.if{
			numberView.stringColor_(numStringColor);};
		sliderBackground.notNil.if{
			sliderView.fillColor_(sliderBackground);};
		knobColor.notNil.if{
			sliderView.strokeColor_(knobColor);};
		background.notNil.if{
			view.background=background;};
		numberView.refresh;
		sliderView.refresh;
	}

	font_{ arg font;

		labelView.notNil.if{labelView.font=font};
		unitView.notNil.if{unitView.font=font};
		numberView.font=font;
	}

	///////Private methods ///////

	prSetViewParams{ // sets resize and alignment for different layouts

		switch (layout,
			\line2, {
				labelView.notNil.if{
					labelView.resize_(2);
					unitView.notNil.if{unitView.resize_(3)};
					numberView.resize_(3);
				}{
					unitView.notNil.if{
						unitView.resize_(2);
						numberView.resize_(1);
					}{
						numberView.resize_(2);
					};
				};
				//			sliderView.resize_(5);
				popUp.if{view.resize_(2)};
			},
			\vert, {
				labelView.notNil.if{labelView.resize_(2)};
				unitView.notNil.if{unitView.resize_(8)};
				numberView.resize_(8);
				sliderView.indexIsHorizontal = false;
				//			sliderView.resize_(5);
				popUp.if{view.resize_(4)};
			},
			\horz, {
				labelView.notNil.if{labelView.resize_(4).align_(\right)};
				unitView.notNil.if{unitView.resize_(6)};
				numberView.resize_(6);
				//			sliderView.resize_(5);
				popUp.if{view.resize_(2)};
		});

	}

	prSubViewBounds{arg rect, hasLabel, hasUnit;  // calculate subview bounds
		var numBounds,labelBounds,sliderBounds, unitBounds;
		var gap1, gap2, gap3, tmp, labelH, unitH;
		gap1 = gap.copy;
		gap2 = gap.copy;
		gap3 = gap.copy;
		labelH=labelSize.y;//  needed for \vert
		unitH=labelSize.y; //  needed for \vert
		hasUnit.not.if{ gap3 = 0@0; unitWidth = 0};

		switch (layout,
			\line2, {

				hasLabel.if{ // with label
					unitBounds = (unitWidth@labelSize.y)
					.asRect.left_(rect.width-unitWidth);// view to right
					numBounds = (numSize.x@labelSize.y)
					.asRect.left_(rect.width-unitBounds.width-numberWidth-gap3.x); // view to right
					labelBounds = (labelSize.x@labelSize.y)
					.asRect.width_(numBounds.left-gap2.x); //adjust width
				}{ // no label
					labelBounds = (0@labelSize.y).asRect; //just a dummy
					numBounds = (numberWidth@labelSize.y).asRect; //view to left
					(unitWidth>0).if{
						unitBounds = Rect (numBounds.width+gap3.x, 0,
							rect.width-numBounds.width-gap3.x,labelSize.y); //adjust to fit
					}{
						unitBounds = Rect (0, 0,0,0); //no unitView
						numBounds = (rect.width@labelSize.y).asRect; //view to left
					};

				};
				sliderBounds = Rect( //adjust to fit
					0,
					labelSize.y+gap1.y,
					rect.width,
					rect.height-numSize.y-gap1.y;
				);
			},

			\vert, {
				hasLabel.not.if{ gap1 = 0@0; labelSize.x = 0 ;};
				hasLabel.not.if{labelH=0};
				labelBounds = (rect.width@labelH).asRect; // to top
				hasUnit.not.if{unitH=0};
				unitBounds = (rect.width@unitH)
				.asRect.top_(rect.height-labelSize.y); // to bottom
				numBounds = (rect.width@labelSize.y)
				.asRect.top_(rect.height-unitBounds.height-numSize.y-gap3.y); // to bottom

				sliderBounds = Rect( //adjust to fit
					0,
					labelBounds.height+gap1.y,
					rect.width,
					rect.height - labelBounds.height - unitBounds.height
					- numBounds.height - gap1.y - gap2.y - gap3.y
				);
			},

			\horz, {
				hasLabel.not.if{ gap1 = 0@0; labelSize.x = 0 ;};
				labelSize.y = rect.height;
				labelBounds = (labelSize.x@labelSize.y).asRect; //to left
				unitBounds = (unitWidth@labelSize.y).asRect.left_(rect.width-unitWidth); // to right
				numBounds = (numSize.x@labelSize.y).asRect
				.left_(rect.width-unitBounds.width-numSize.x-gap3.x);// to right
				sliderBounds  =  Rect( // adjust to fit
					labelBounds.width+gap1.x,
					0,
					rect.width - labelBounds.width - unitBounds.width
					- numBounds.width - gap1.x - gap2.x - gap3.x,
					labelBounds.height
				);
		});


		^[labelBounds, numBounds, sliderBounds, unitBounds].collect{arg v; v.moveBy(margin.x,margin.y)}
	}

	canFocus_ {
		nil
	}

	update {arg changer, what ...moreArgs;
		var oldValue;
		if(changer === controlSpec, {
			oldValue = this.value;
			this.value = oldValue;
			if(this.value != oldValue, { this.doAction });
		});
	}

	editSpec {
		var ezspec;
		[labelView, sliderView, numberView, unitView].do({|view|
			view.notNil.if({ view.enabled_(false).visible_(false)});
		});
		ezspec = EZControlSpecEditor(view, view.bounds.moveTo(0,0), controlSpec: controlSpec, layout: layout);
		ezspec.labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
			if(clickCount == 2, {
				ezspec.remove;
				[labelView, sliderView, numberView, unitView].do({|view|
					view.notNil.if({ view.enabled_(true).visible_(true)});
				});
			});
		};
	}

}
