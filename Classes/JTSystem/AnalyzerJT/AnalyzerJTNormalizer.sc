/*
- kijk of IEnvGen efficienter is dan explin e.d.
- meer van dit soort functies, zoals addGUIS oid
*/
+AnalyzerSystemJT {

	addNormalizedOSCFuncs {arg addFunc;//={arg key, value, time; };
		descriptorsWithoutOnsets.do{|key|
			var func={arg msg, time; msg=msg.copyToEnd(3).
				msg=msg.collect{|value| normalizers[key].value(value)};
				if (addFunc!=nil, {addFunc.value(key, msg, time)});
			};
			osc[key]=OSCFunc(func, cmdNames[key], server.addr);
		}
	}

	makeNormalizers {
		normalizers=();
		descriptorsWithoutOnsets.do{|key|
			var cs=controlSpecs[key];
			var normalizer, min=0.0, max=1.0, warp, curve;
			if (cs!=nil,{
				min=cs.minval;
				max=cs.minval;
				warp=cs.warp;
				if (warp.class==CurveWarp, {curve=warp.curve});
				normalizers[key]=switch (warp.class, CurveWarp, {
					{|value| value.curvelin(min, max, 0.0, 1.0, curve)}
					//{|value| IEnvGen.kr(Env.new([0.0, 1.0],[
				},ExponentialWarp, {
					{|value| value.explin(min, max, 0.0, 1.0)}
				}, FaderWarp, {
					{|value| value.sqrt}
				}, DbFaderWarp, {
					{|value| value.dbamp.sqrt}
				}, LinearWarp, {
					{|value| value.linlin(min, max, 0.0, 1.0)}
				}, SineWarp, {
					{|value| asin(value.linlin(min, max, 0.0, 1.0)) / 0.5pi}
				}, CosineWarp, {
					{|value| acos(1.0 - (value.linlin(min,max,0.0, 1.0) * 2.0)) / pi}
				});
			},{
				normalizers[key]={arg value; value};
			});
		}
	}

}