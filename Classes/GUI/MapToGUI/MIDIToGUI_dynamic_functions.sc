+ MIDItoGUI {
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

		func=this.putInValue(func, index);
		func=this.funcMode(func, mode);
		if (glitch>0, {func=this.removeGlitches(func)});
		if (timeThreshold>0, {func=this.reduceSpeed(func)});
		^func
	}

	//---------------------------------------------
	continousFunc {arg func;
		^{arg val,numb;
			val=func.value(val,numb);
			//guiObject.action.value(val);
			guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
			{ guiObject.value_(val) }.defer
		}
	}

	continuousdeferFunc {arg func;
		^{arg val,numb;
			val=func.value(val,numb);
			{
				guiObject.action.value(val);
				//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
				guiObject.value_(val) }.defer}
	}

	triggerFunc {arg func;
		^{arg val,numb;
			val=func.value(val,numb);
			if (val>triggerThreshold, {
				val=1.0;
				guiObject.action.value(val);
				//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
				{guiObject.value_(val);}.defer
			})
		}
	}

	toggleFunc {arg func;
		var prevval=guiObject.value;
		var modulo=2;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			{
				//var maxval=this.getmaxval;//hier states.size/items.size etc invullen
				val=func.value(val,numb);
				if (val>triggerThreshold, {
					val=step;
					prevval=guiObject.value;
					val=(prevval+val)%(modulo);
					//guiObject.action.value(val);
					guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
					guiObject.value_(val)
				})
			}.defer
		}
	}

	toggleFuncButton {arg func;
		var prevval;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			{
				val=func.value(val,numb);
				if (val>triggerThreshold, {
					val=step;
					prevval=guiObject.value;
					val=(prevval+val)%(guiObject.states.size);
					//guiObject.action.value(val);
					guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
					guiObject.value_(val)
				})
			}.defer
		}
	}

	toggleFuncListView {arg func;
		var prevval;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			{
				val=func.value(val,numb);
				if (val>triggerThreshold, {
					val=step;
					prevval=guiObject.value;
					val=(prevval+val)%(guiObject.items.size);
					//guiObject.action.value(val);
					guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
					guiObject.value_(val)
				})
			}.defer
		}
	}

	togglefoldFuncButton {arg func;
		var prevval=guiObject.value;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			{
				val=func.value(val,numb);
				if (val>triggerThreshold, {
					val=step;
					prevval=guiObject.value;
					val=(prevval+val);
					if (val<0, {
						step=step.neg;
						val=val.neg;
					});
					if (val>=guiObject.states.size, {
						val=val-(2*step);
						step=step.neg;
					});
					guiObject.action.value(val);
					//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
					guiObject.value_(val)
				})
			}.defer
		}
	}

	togglefoldFuncListView {arg func;
		var prevval=guiObject.value;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			{
				val=func.value(val,numb);
				if (val>triggerThreshold, {
					val=step;
					prevval=guiObject.value;
					val=(prevval+val);
					if (val<0, {
						step=step.neg;
						val=val.neg;
					});
					if (val>=guiObject.items.size, {
						val=val-(2*step);
						step=step.neg;
					});
					guiObject.action.value(val);
					//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
					guiObject.value_(val)
				})
			}.defer
		}
	}

	gateFunc {arg func;
		^{arg val,numb;
			val=func.value(val,numb);
			val=forceValue??{val};
			guiObject.action.value(val);
			//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
			{guiObject.value_(val)}.defer
		}
	}

	plusminusFunc {arg func;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			val=func.value(val,numb);
			if (numb==num[0], {
				{ guiObject.valueAction_(guiObject.value-step) }.defer },{
				{ guiObject.valueAction_(guiObject.value+step) }.defer });
		}
	}

	plusminusFuncListView {arg func;
		if ((step==nil)||(step==0), {step=1;});
		^{arg val,numb;
			val=func.value(val,numb);
			if (numb==num[0], {
				{ guiObject.valueAction_( (guiObject.value-step)
					.clip(0, guiObject.items.size-1) ) }.defer },{
				{ guiObject.valueAction_( (guiObject.value+step)
					.clip(0, guiObject.items.size-1) ) }.defer });
		}
	}

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