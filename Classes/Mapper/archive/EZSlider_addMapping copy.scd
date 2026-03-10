+ EZSlider {
	//action
	//addMapper
	addMapping {arg parent, bounds, initAction=true, decimals=4, margin, gap, stepCurve=1, width=10, colorRange=Color.green, changeControlSpec=false, smoothSize, lagAction;
		var controlSpec, mappingGUI, boundz, rangeSlider, buttons, keys=();
		var mapper;

		controlSpec=this.controlSpec;
		boundz=this.sliderView.bounds;
		boundz.width=boundz.width-width;
		this.sliderView.bounds_(boundz);
		boundz.top=boundz.top+2; boundz.height=boundz.height-4;

		margin=margin??{0@0};
		gap=gap??{1@1};

		//if (changeControlSpec) {} {
		this.sliderView.background_(Color.grey(0.5, 0));
		rangeSlider=SmoothRangeSlider(this.view, boundz).hilightColor_(colorRange).thumbSize_(0).action_{|slider|
			var val;
			//slider.value.postln;
			[\minval, \maxval].do{|key,i|
				var val, viewkey=mappingGUI[\viewsKeys][key];
				val=this.controlSpec.map(slider.value[i]);
				mappingGUI[\values][key]=val;
				{mappingGUI[\views][viewkey].value_(val)}.defer;
				val
			};
		};
		this.sliderView.mouseDownAction_{
			arg view, x, y, modifiers, buttonNumber, clickCount; if (buttonNumber==1) {rangeSlider.front};
		};
		rangeSlider.mouseDownAction_{
			arg view, x, y, modifiers, buttonNumber, clickCount; if (buttonNumber==0) {this.sliderView.front }
		};
		//};
		this.sliderView.front;

		if (this.view.onClose==nil) {
			this.view.onClose={mappingGUI.parentView.close}
		} {
			this.view.onClose=this.view.onClose.addFunc({mappingGUI.parentView.close});
		};

		boundz=this.numberView.bounds;
		boundz.left=boundz.left-width;
		this.numberView.bounds_(boundz);

		boundz.left=boundz.left+boundz.width;
		boundz.width=width;
		boundz.height=(boundz.height/2).floor.asInteger;

		buttons=2.collect{|i|
			boundz.top=i*(boundz.height);
			if (i==0) {
				Button(this.view, boundz).states_([ ["x"],["X",Color.black,Color.green] ]).action_{|b|
					mappingGUI[\viewAction]=if (b.value==1) {
						{|val| {this.value_(val)}.defer }
					} {
						nil
					}
				}.font_(Font(Font.defaultMonoFace, width));
			} {
				Button(this.view, boundz).states_([ ["e"],["E",Color.black,Color.yellow]])
				.action_{|b|
					var index=this.view.parent.children.indexOf(this.view);
					var bounds=this.view.bounds;
					this.view.parent.children.copyToEnd(index+1);
					if(b.value==1) {
						bounds.height=bounds.height+mappingGUI[\height];
						this.view.bounds_(bounds);
						//w.view
						this.view.parent.children.copyToEnd(index+1).do{|view|
							var bounds=view.bounds;
							bounds.top=bounds.top+mappingGUI[\height];
							view.bounds_(bounds);
						};
					} {
						bounds.height=bounds.height-mappingGUI[\height];
						this.view.bounds_(bounds);
						this.view.parent.children.copyToEnd(index+1).do{|view|
							var bounds=view.bounds;
							bounds.top=bounds.top-mappingGUI[\height];
							view.bounds_(bounds);
						};
					}
					/*
					if (b.value==1)
					{
					mappingGUI.parentView.visible_(true);
					mappingGUI.parentView.front;
					} {
					if (mappingGUI.parentView!=this.view) {
					mappingGUI.parentView.visible_(false)
					}
					}
					*/
				}.font_(Font(Font.defaultMonoFace, width));
			}
		};
		this.view.addFlowLayout(margin, gap);
		this.view.resize_(0);
		this.sliderView.resize_(0);
		this.numberView.resize_(0);
		this.labelView.resize_(0);
		this.view.decorator.shift(0, this.bounds.height);
		this.view.decorator.nextLine;

		mapper=MapperJT(controlSpec,smoothSize, lagAction);
		mapper.makeGui(
			parent??{this.view}
			, bounds??{this.bounds.width@(this.bounds.height*0.75).floor.asInteger}
			, this.label
			, nil
			, this.numberView.bounds.width+width
			, this.labelView.bounds.width//+width
			, margin
			, gap
			, decimals
			, stepCurve
		);
		mappingGUI=mapper.gui;
		/*
		mappingGUI=controlSpec.gui(
		parent??{this.view}
		, bounds??{this.bounds.width@(this.bounds.height*0.75).floor.asInteger}
		, this.label
		, this.numberView.bounds.width+width
		, this.labelView.bounds.width//+width
		, decimals
		, margin
		, gap
		, stepCurve
		, changeControlSpec
		, smooth
		, lagAction
		);
		*/
		[\onoff, \editor].do{|key| keys[key]=(this.label++"_"++key).asSymbol};

		mappingGUI.views[keys[\onoff]]=buttons[0];
		mappingGUI.viewsEditor[keys[\editor]]=buttons[1];
		//if (changeControlSpec.not) {
		mappingGUI.views[\rangeSlider]=rangeSlider;
		mappingGUI.action=mappingGUI.action.addFunc({
			rangeSlider.value_(this.controlSpec.unmap([mapper.controlSpec.minval,mapper.controlSpec.maxval]))
		});
		//};
		if (initAction) {
			mappingGUI.viewAction={|val| {this.value_(val)}.defer };
			{mappingGUI.views[keys[\onoff]].value_(1)}.defer;
		};
		{mappingGUI.action.value}.defer;

		^mappingGUI;
	}
}