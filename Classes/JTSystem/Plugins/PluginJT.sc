PluginJT : JT {
	var <bypass, <>bypassFunc, <>runFunc;

	run {arg flag=true;
		isRunning=flag;
		runFunc.value(flag);
		synth.asArray.do(_.run(flag));
	}

	bypass_ {arg flag=true;
		bypass=flag;
		bypassFunc.value(flag);
		synth.asArray.do(_.run(flag.not));
	}

}