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
		settings[\run]=flag.not.binaryValue;
		synth.asArray.do(_.run(flag.not));
	}
	addPresets {arg path, index=0;
		var func={arg preset;
			if (preset[\run]!=nil, {
				this.bypass_(preset[\run]<1.0)
			});
		};
		this.metaAddPresets(path, index, func)
	}
	addPresetSystem {arg path, folderName="master", index=0;
		var func={arg preset;
			if (preset[\run]!=nil, {
				this.bypass_(preset[\run]<1.0)
			});
		};
		this.metaAddPresetSystem(path, folderName, index, func)
	}
}