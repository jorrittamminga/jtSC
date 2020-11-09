TestClassJT {
	var <value;
	*new {arg value;
		^super.new.init(value)
	}

	init {arg argvalue;
		value=argvalue;

	}

	mulTwo {
		^(value*2)
	}
	mulThree {
		^(value*3)
	}
}