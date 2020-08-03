+ EZGui {

	midi {
		^this.midiMap
	}

	addAction { arg func, selector=\action;
		this.perform(selector.asSetter, this.perform(selector).addFunc(func));
	}

	removeAction { arg func, selector=\action;
		this.perform(selector.asSetter, this.perform(selector).removeFunc(func));
	}

	findWindow {
		var parent=this.view.getParents.last;
		^if ((parent.class==TopView)||(parent.class==ScrollTopView), {
			parent.findWindow
		},{
			parent.canvas.win
		})
	}
}


+ View {

	midi { ^this.midiMap }
}
/*
+ EZRanger {

addAction { arg func, selector=\action;
var rangeSliderFunc=func;
this.hiBox.perform(selector.asSetter, this.hiBox.perform(selector).addFunc(func));
this.loBox.perform(selector.asSetter, this.loBox.perform(selector).addFunc(func));
this.rangeSlider.perform(selector.asSetter, this.rangeSlider.perform(selector).addFunc(func));
}

removeAction { arg func, selector=\action;
var rangeSliderFunc=func;
this.hiBox.perform(selector.asSetter, this.hiBox.perform(selector).removeFunc(func));
this.loBox.perform(selector.asSetter, this.loBox.perform(selector).removeFunc(func));
this.rangeSlider.perform(selector.asSetter, this.rangeSlider.perform(selector).removeFunc(func));
}

}
*/