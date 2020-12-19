EventPresetJTBETA {
	var <objects;
	//---------------------------------------------
	*new { arg objects;
		^super.new.init(objects)
	}
	//--------------------------------------------- INIT
	init {arg argobjects;
		objects=argobjects;
	}
	values { ^objects.collect{|o| o.value}; }
	values_ {arg event;
		event.keysValuesDo{arg key, val;
			if (objects[key]!=nil, {
				{objects[key].value_(val)}.defer;
			});
		};
	}
	valuesActions_ {arg event;
		event.keysValuesDo{arg key, val;
			if (objects[key]!=nil, {
				objects[key].action.value(val);
				{objects[key].value_(val)}.defer;
			});
		};
	}
	valuesActionsTransition_ {arg event, durations, curves, delayTimes;

	}
	//---------------------- FILESYSTEM methods

}