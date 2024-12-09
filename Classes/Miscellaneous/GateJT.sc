GateJT {

	var <>threshold, <>falseFunction, <>trueFunction, <action;
	var out=0;

	*new {arg threshold=0.5, falseFunction={0.postln}, trueFunction={1.postln};
		^super.new.init(threshold, falseFunction, trueFunction)
	}

	init {arg argthreshold, argfalseFunction, argtrueFunction;
		threshold=argthreshold;
		falseFunction=argfalseFunction;
		trueFunction=argtrueFunction;
		out=0;
		this.makeAction;
	}

	set {arg value;
		action.value(value)
	}

	value {arg value;
		action.value(value)
	}

	makeAction {
		action= {arg value;
			out = if (value>=threshold) {
				trueFunction.value;
				1.0
			} {
				falseFunction.value;
				0.0
			}
		};
	}
}

TriggerJT : GateJT {
	makeAction {
		action= {arg value;
			out = if(value < threshold) {
				if (out!=0) {
					falseFunction.value;
				};
				0.0;
			} {
				if(value >= threshold) {
					if (out!=1) {
						trueFunction.value;
					};
					1.0;
				};
			};
		}
	}
}