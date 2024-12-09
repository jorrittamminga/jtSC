GateTimedJT {
	var <>threshold, <>falseFunction, <>trueFunction, <action;
	var out=0, time, deltaTime, <>dur=0.1;

	*new {arg threshold=0.5, dur=0.1, falseFunction={0.postln}, trueFunction={1.postln};
		^super.new.init(threshold, dur, falseFunction, trueFunction)
	}

	init {arg argthreshold, argdur, argfalseFunction, argtrueFunction;
		threshold=argthreshold;
		dur=argdur;
		falseFunction=argfalseFunction;
		trueFunction=argtrueFunction;
		out=0;
		time=Main.elapsedTime;
		this.makeAction;
	}

	set {arg value;
		action.value(value)
	}

	value {arg value;
		action.value(value)
	}

	makeAction {
		var tmpTime;
		action= {arg value;
			tmpTime=Main.elapsedTime;
			deltaTime=tmpTime-time;

			out = if (value>=threshold) {
				trueFunction.value;
				1.0
			} {
				if (deltaTime>dur) {
					time=tmpTime;
					falseFunction.value;
					0.0
				}
			}
		};
	}
}

TriggerTimedJT : GateTimedJT {
	makeAction {
		var tmpTime;
		action= {arg value;
			tmpTime=Main.elapsedTime;
			deltaTime=tmpTime-time;

			out = if (value>=threshold) {
				if (out!=1) {
					trueFunction.value;
				};
				1.0
			} {
				if (deltaTime>dur) {
					time=tmpTime;
					if (out!=0) {
						falseFunction.value;
					};
					0.0
				}
			}
		};
	}
}
