TestJTJT {
	var <>function, value;

	*new{ arg x;
		^super.new.init(x)
	}
	init {arg x;
		value=x;
		function={arg y; value*y};
	}

	doFunction {arg y;
		^function.value(y)
	}

	mult {arg y;
		^(value*y)
	}
}