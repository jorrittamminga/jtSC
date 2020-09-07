InterpolationRoutineJT {
	var <fromValues, <toValues;
	var <>specs, routines, <>waitTime;
	var steps, resolution;

	*new {arg views, waitTime=0.01;

	}

	init {

	}

	makeFunctions {

	}
	makeRoutines {

	}

	interpolate {arg newValues, durations, curves, delayTimes;
		this.stop;
		this.makeFunctions;
		this.makeRoutines;
		this.start;
	}

	start {

	}

	stop {
		routines.stop;
	}


}


{
	steps.do{arg i;
		doe dit;
		doe dat;
		doe zus;
		doe zo;
		waitTime.wait;
	}
}.fork
