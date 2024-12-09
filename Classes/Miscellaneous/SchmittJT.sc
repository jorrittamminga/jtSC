SchmittJT {

	var <>minValue, <>maxValue, <>falseFunction, <>trueFunction, <action;
	var out=0;

	*new {arg minValue=0.0, maxValue=1.0, falseFunction={0.postln}, trueFunction={1.postln};
		^super.new.init(minValue, maxValue, falseFunction, trueFunction)
	}

	init {arg argminValue, argmaxValue, argfalseFunction, argtrueFunction;
		minValue=argminValue;
		maxValue=argmaxValue;
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
			out = if(value < minValue) {
				falseFunction.value;
				0.0
			} {
				if(value >= maxValue) {
					trueFunction.value;
					1.0
				} {
					out
				}
			};
		};
	}
}

SchmittTriggerJT : SchmittJT {
	makeAction {
		action= {arg value;
			out = if(value < minValue) {
				if (out!=0) {
					falseFunction.value;
				};
				0.0;
			} {
				if(value >= maxValue) {
					if (out!=1) {
						trueFunction.value;
					};
					1.0;
				} {
					out
				}
			};
		};
	}

}