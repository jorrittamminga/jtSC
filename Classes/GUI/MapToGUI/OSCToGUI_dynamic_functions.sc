+ OSCtoGUI {

	//---------------------------------------------
	continousFunc {arg func;
		var val;
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			val=func.value(val);

			guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};

			{ guiObject.value_(val) }.defer

		}
	}

	continuousdeferFunc {arg func;
		var val;
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				guiObject.action.value(val);
				//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
				guiObject.value_(val) }.defer}
	}

	triggerFunc {arg func;
		var val;
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			val=func.value(val);
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
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				//var maxval=this.getmaxval;//hier states.size/items.size etc invullen
				val=func.value(val);
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
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				val=func.value(val);
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
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				val=func.value(val);
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
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				val=func.value(val);
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
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			{
				val=func.value(val);
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
		var val;
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			val=func.value(val);
			val=forceValue??{val};
			guiObject.action.value(val);
			//guiObject.action.array.copyToEnd(hasOut).do{|f| f.value(val)};
			{guiObject.value_(val)}.defer
		}
	}

	plusminusFunc {arg func;
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			val=func.value(val);
			/*
			if (numb==num[0], {
				{ guiObject.valueAction_(guiObject.value-step) }.defer },{
				{ guiObject.valueAction_(guiObject.value+step) }.defer });
			*/
		}
	}

	plusminusFuncListView {arg func;
		var val;
		if ((step==nil)||(step==0), {step=1;});
		^{arg msg;
			val=msg.copyToEnd(1).unbubble;
			val=func.value(val);
			/*
			if (numb==num[0], {
				{ guiObject.valueAction_( (guiObject.value-step)
					.clip(0, guiObject.items.size-1) ) }.defer },{
				{ guiObject.valueAction_( (guiObject.value+step)
					.clip(0, guiObject.items.size-1) ) }.defer });
			*/
		}
	}

}