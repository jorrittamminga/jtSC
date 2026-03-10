+ MapperControlSpecJT {
	makeEditor {arg parent, bounds, initAction=true, decimals=4, margin, gap, stepCurve=1, width=10, colorRange=Color.green, thumbSize=4;
		var controlSpec, boundz, rangeSlider, buttons, keys=();
		var sliderBounds;

		if (gui==nil) {this.makeView(parent, bounds)};

		controlSpec=gui.controlSpec;
		"before ".post;gui.sliderView.bounds.postln;
		boundz=gui.sliderView.bounds;
		boundz.width=boundz.width-width;
		sliderBounds=boundz.deepCopy;
		sliderBounds.height=(sliderBounds.height*0.75).ceil.asInteger;
		gui.sliderView.bounds_(sliderBounds);
		if (gui.class==EZSlider) {gui.sliderView.thumbSize_(thumbSize);};
		margin=margin??{0@0};
		gap=gap??{1@1};
		boundz.top=boundz.top+2;
		boundz.height=boundz.height-4;
		//boundz.top=boundz.top;
		//boundz.height=boundz.height;

		gui.sliderView.background_(Color.grey(0.5, 0));
		rangeSlider=SmoothRangeSlider(gui.view, boundz).hilightColor_(colorRange).thumbSize_(0).action_{|slider|
			var val;
			[\minval, \maxval].collect{|key,i|
				var val, viewkey=editor.viewsKeys[key];
				val=gui.controlSpec.map(slider.value[i]);
				editor.values[key]=val;
				{editor.views[viewkey].value_(val)}.defer;
				if (i==0) {
					this.minval_(val);
				}{
					this.maxval_(val);
				};
				val
			};
		}.value_([0,1]);
		gui.sliderView.front;
		if (gui.view.onClose==nil) {
			gui.view.onClose={editor.parent.close}
		} {
			gui.view.onClose=gui.view.onClose.addFunc({editor.parent.close});
		};

		boundz=gui.numberView.bounds;
		boundz.left=boundz.left-width;
		gui.numberView.bounds_(boundz);

		boundz.left=boundz.left+boundz.width;
		boundz.width=width;
		boundz.height=(boundz.height/2).floor.asInteger;

		buttons=2.collect{|i|
			boundz.top=i*(boundz.height);
			if (i==0) {
				Button(gui.view, boundz).states_([ ["x"],["X",Color.black,Color.green] ]).action_{|b|
					editor.viewAction=if (b.value==1) {
						{|val| {gui.value_(val)}.defer }
					} {
						nil
					}
				}.font_(Font(Font.defaultMonoFace, width));
			} {
				Button(gui.view, boundz).states_([ ["e"],["E",Color.black,Color.yellow]])
				.action_{|b|
					var index=gui.view.parent.children.indexOf(gui.view);
					var bounds=gui.view.bounds;
					gui.view.parent.children.copyToEnd(index+1);
					if(b.value==1) {
						bounds.height=bounds.height+editor.height;
						gui.view.bounds_(bounds);
						//w.view
						gui.view.parent.children.copyToEnd(index+1).do{|view|
							var bounds=view.bounds;
							bounds.top=bounds.top+editor.height;
							view.bounds_(bounds);
						};
					} {
						bounds.height=bounds.height-editor.height;
						gui.view.bounds_(bounds);
						gui.view.parent.children.copyToEnd(index+1).do{|view|
							var bounds=view.bounds;
							bounds.top=bounds.top-editor.height;
							view.bounds_(bounds);
						};
					}
				}.font_(Font(Font.defaultMonoFace, width));
			}
		};
		gui.view.addFlowLayout(margin, gap);
		gui.view.resize_(0);
		gui.sliderView.resize_(0);
		gui.numberView.resize_(0);
		gui.labelView.resize_(0);
		gui.view.decorator.shift(0, gui.bounds.height);
		gui.view.decorator.nextLine;

		"after ".post;gui.sliderView.bounds.postln;

		editor=MapperControlSpecJTGUI(this, gui.view, bounds, nil, nil
			, gui.numberView.bounds.width+width
			, gui.labelView.bounds.width//+width
			//, margin
			//, gap
		);
		[\onoff, \editor].do{|key| keys[key]=(gui.label++"_"++key).asSymbol};

		editor.views[keys[\onoff]]=buttons[0];
		editor.views[keys[\editor]]=buttons[1];
		editor.views[\rangeSlider]=rangeSlider;
		editor.action=editor.action.addFunc({
			rangeSlider.value_(gui.controlSpec.unmap([minval,maxval]))
		});
		//};
		if (initAction) {
			editor.viewAction={|val| {gui.value_(val)}.defer };
			{editor.views[keys[\onoff]].value_(1)}.defer;
		}{
			editor.action.value
		}.defer;
	}
}