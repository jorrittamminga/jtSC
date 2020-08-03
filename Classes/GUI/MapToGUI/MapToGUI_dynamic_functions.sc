+ MapToGUI {
	makeFuncDynamic {arg index, mul, add;
		var case, cIn=controlSpecIn.copy.asArray.wrapAt(index);
		//controlSpecOut=guiObject.controlSpec;
		case=[
			(cIn.isKindOf(Spec)||cIn.isKindOf(Warp)).binaryValue,
			(controlSpecOut.isKindOf(Spec)||controlSpecOut.isKindOf(Warp)).binaryValue,
			(mul!=nil).binaryValue,
			(roundOut>0.00001).binaryValue
		];
		case=case.convertDigits(2);
		func=switch(case, 0, {
			funcOut={arg gui;gui.value};
			{arg val, numb;
				val=val;
		}}, 1, {
			funcOut={arg gui; gui.value};
			{arg val, numb;
				val=val.round(roundOut);
		}}, 2, {
			funcOut={arg gui; (gui.value-add)*mul.reciprocal};
			{arg val, numb;
				val=val*mul+add;
		}}, 3, {
			funcOut={arg gui; (gui.value-add)*mul.reciprocal};
			{arg val, numb;
				val=(val*mul+add).round(roundOut);
		}}, 4, {
			funcOut={arg gui; guiObject.controlSpec.unmap(gui.value);};
			{arg val, numb;
				val=guiObject.controlSpec.map(val);
		}}, 5, {{"error, case = 5!".postln;
		}}, 6, {
			funcOut={arg gui;
				gui=guiObject.controlSpec.unmap(gui.value);
				(gui-add)*mul.reciprocal };
			{arg val, numb;
				val=guiObject.controlSpec.map(val*mul+add);
		}}, 7, {{"error, case = 7!".postln;
		}}, 8, {
			funcOut={arg gui; controlSpecIn.map(gui.value) };
			{arg val, numb;
				val=controlSpecIn.unmap(val);
		}}, 9, {
			funcOut={arg gui; controlSpecIn.map(gui.value) };//improve!
			{arg val, numb;
				val=controlSpecIn.unmap(val).round(roundOut);
		}}, 10, {
			funcOut={arg gui; controlSpecIn.map(gui.value) };//improve!!
			{arg val, numb;
				val=controlSpecIn.unmap(val)*mul+add;
		}}, 11, {
			funcOut={arg gui; controlSpecIn.map(gui.value) };//improve!!
			{arg val, numb;
				val=(controlSpecIn.unmap(val)*mul+add).round(roundOut);
		}}, 12, {
			funcOut={arg gui; controlSpecIn.map(gui.value) };//improve!!
			{arg val, numb;
				val=guiObject.controlSpec.map(controlSpecIn.unmap(val));
		}}, 13, {{ "error, case = 13!".postln;
		}}, 14, {
			funcOut={arg gui;
				gui=guiObject.controlSpec.unmap(gui.value);
				(gui-add)*mul.reciprocal
			};
			{arg val, numb;
				val=cIn.map(val*mul+add)
		}}, 15, {{ "error, case = 15!".postln;
		}}
		);

		//func=this.putInValue(func, index);
		func=this.funcMode(func, mode);
		/*
		if (glitch>0, {func=this.removeGlitches(func)});
		if (timeThreshold>0, {func=this.reduceSpeed(func)});
		*/
		^func
	}

	//---------------------------------------------
	funcMode {arg func, mode;
		^switch(mode
			, \continuous, {this.continousFunc(func)}
			, \continuousdefer, {this.continuousdeferFunc(func)}
			, \trigger, {this.triggerFunc(func)}
			, \toggle, {
				switch(guiObject.class
					, Button, {this.toggleFuncButton(func)}
					, PopUpMenu, {this.toggleFuncListView(func)}
					, ListView, {this.toggleFuncListView(func)},
					{this.toggleFunc(func)})
			}
			, \togglefold, {
				switch(guiObject.class
					, Button, {this.togglefoldFuncButton(func)}
					, PopUpMenu, {this.togglefoldFuncListView(func)}
					, ListView, {this.togglefoldFuncListView(func)},
					{this.togglefoldFunc(func)})
			}
			, \gate, {this.gateFunc(func)}
			, \plusminus, {
				switch(guiObject.class
					//, Button, {this.toggleFuncButton(func)}
					, PopUpMenu, {this.plusminusFuncListView(func)}
					, ListView, {this.plusminusFuncListView(func)},
					{this.plusminusFunc(func)})
			}
		)
	}

	reduceSpeed {arg func;//arg timeThreshold;
		var tmpFunc=func;
		func={arg val, numb;
			if (Main.elaspedTime-time>timeThreshold, {
				time=Main.elapsedTime;
				tmpFunc.value(val, numb);
			})
		};
		^func
	}

	removeGlitches {arg func;//arg glitch;
		var tmpFunc=func;
		func={arg val, numb;
			if ((val-previnval)<=glitch, {
				previnval=val;
				tmpFunc.value(val, numb)
			})
		};
		^func
	}
}