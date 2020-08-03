LagHPZ {

	*kr {arg in = 0.0, lagTimeMin = 0.1, lagTimeMax = 3.0, min=20, max=20000, type=\exp, lagTime=1.0;
		var dev, curve=0.0;
		//if (in.rate!='control', {in=in.lag(0.0)});
		//in=in.lag(0.01);
		if (type.class!=Symbol, {curve=type; type=\curve});
		/*
		dev=switch (curve,
			\lin,{in.linlin(min, max, 0.0, 1.0)},
			\exp,{in.explin(min, max, 0.0, 1.0).lag(0.0)},
			\curve,{in.curvelin(min, max, 0.0, 1.0, curve)}
		);
		*/
		//.dev=in.lag(0.0).explin(min, max, 0.0, 1.0);
		dev=if (curve==\exp, {in.explin(min, max, 0.0, 1.0)},{in.linlin(min, max, 0.0, 1.0)});
		dev=HPZ1.kr(dev).abs.linlin(0.0, 0.5, lagTimeMin, lagTimeMax).lag(0.0, lagTime);
		//dev.poll(Changed.kr(in));
		^in.lag(dev)

	}
}



+ UGen {

	lagl { arg t1=0.1, t2=3.0, min=0.0, max=1.0, curve=\lin, lagTime=1.0;
		^LagHPZ.kr(this, t1, t2, min, max, curve, lagTime)
	}

	lagf { arg t1=0.1, t2=3.0, min=20.0, max=20000.0, curve=\exp, lagTime=1.0;
		^LagHPZ.kr(this, t1, t2, min, max, curve, lagTime)
	}

}