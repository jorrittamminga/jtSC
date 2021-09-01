+ EZMultiSlider {
	decimals_ {arg decimals;
		this.numberView.decimals_(decimals);
	//	this.numberView.value_(value);
	}
	round2_ {arg r;
		this.round=r;
		if (this.numberView.class==NumberBox, {//QNumberBox
			this.numberView.decimals_(r.decimals);//SCNumberBox
			//this.numberView.value_(value);
		});
	}

}

+ EZSlider {
	decimals_ {arg decimals;
		this.numberView.decimals_(decimals);
		this.numberView.value_(value);
	}
	canFocus_ { arg val;
		this.sliderView.canFocus_(val);
		this.numberView.canFocus_(val);
	}

	mouseUpAction_ {arg f;
		this.sliderView.mouseUpAction_(f);
		this.numberView.mouseUpAction_(f);
	}

	mouseUpAction {arg f;
		^this.sliderView.mouseUpAction;
	}

	mouseDownAction_ {arg f;
		this.sliderView.mouseDownAction_(f);
		this.numberView.mouseDownAction_(f);
	}

	keyDownAction_ {arg f;
		this.sliderView.keyDownAction_(f);
		this.numberView.keyDownAction_(f);
	}

	round2_ {arg r;
		this.round=r;
		if (this.numberView.class==NumberBox, {//QNumberBox
			this.numberView.decimals_(r.decimals);//SCNumberBox
			this.numberView.value_(value);
		});
	}

}

+ EZPopUpMenu {

	action {^globalAction}

	round2_ {arg r;
		//dummy
	}
	controlSpec {
		^[0,items.size-1, 0, 1].asSpec
	}
	controlSpec_ {arg items;
		this.items_(items)
	}
}

+ EZKnob {
	decimals_ {arg decimals;
		this.numberView.decimals_(decimals);
		this.numberView.value_(value);
	}
	round2_ {arg r;
		this.round=r;
		if (this.numberView.class==NumberBox, {//QNumberBox
			this.numberView.decimals_(r.decimals);//SCNumberBox
			this.numberView.value_(value);
		});
	}

}

+ EZRanger {

	canFocus_ { arg val;
		this.rangeSlider.canFocus_(val);
		this.hiBox.canFocus_(val);
		this.loBox.canFocus_(val);
	}

	mouseUpAction_ {arg f;
		this.rangeSlider.mouseUpAction_(f);
		this.hiBox.mouseUpAction_(f);
		this.loBox.mouseUpAction_(f);
	}

	mouseUpAction {arg f;
		^this.rangeSlider.mouseUpAction;
	}

	mouseDownAction_ {arg f;
		this.rangeSlider.mouseDownAction_(f);
		this.hiBox.mouseDownAction_(f);
		this.loBox.mouseDownAction_(f);
	}

	keyDownAction_ {arg f;
		this.rangeSlider.keyDownAction_(f);
		this.hiBox.keyDownAction_(f);
		this.loBox.keyDownAction_(f);
	}
	decimals_ {arg decimals;
		this.loBox.decimals_(decimals);//SCNumberBox
		this.hiBox.decimals_(decimals);//SCNumberBox
		value=this.value;
		this.value_(value);
	}
	round2_ {arg r;
		this.round=r;
		if (this.loBox.class==NumberBox, {
			this.loBox.decimals_(r.decimals);//SCNumberBox
			this.hiBox.decimals_(r.decimals);//SCNumberBox
			value=this.value;
			this.value_(value);
		});
	}
}

+ EZNumber {
	decimals_ {arg decimals;
		this.numberView.decimals_(decimals);
		this.numberView.value_(value);
	}
	round2_ {arg r;
		this.round=r;
		if (this.numberView.class==NumberBox, {
			this.numberView.decimals_(r.decimals);//SCNumberBox
			this.numberView.value_(value);
		});
	}

	canFocus_ { arg val;
		this.numberView.canFocus_(val);
	}

	mouseUpAction_ {arg f;
		this.numberView.mouseUpAction_(f);
	}

	mouseUpAction {arg f;
		^this.sliderView.mouseUpAction;
	}

	mouseDownAction_ {arg f;
		this.numberView.mouseDownAction_(f);
	}

	keyDownAction_ {arg f;
		this.numberView.keyDownAction_(f);
	}

}