Mapper2JT {
	classvar <>warps;

	var <name, <default, <lag, <smoothSize;//, <bus
	var <minval, <maxval, <warp;//, <warp, <name, <>default, <>lag, <smoothSize;
	var <clipLo, <clipHi;
	var <>smoothArray, <array, <arraySize, <arraySizeR, <buffer;
	var <bus, <server;
	var <mapFunction, <warpSymbol;
	var <gui;

	*initClass {
		// support Symbol-asWarp
		warps = IdentityDictionary[
			\env -> MapperEnvJT,
			\array -> MapperArrayJT
		];
	}

	*new {arg mapper, name, default, lag, smoothSize;
		switch (mapper.class
			, ControlSpec, {^MapperControlSpecJT.new(mapper.minval, mapper.maxval, mapper.warp
				, 0, default, nil, nil, name, lag, smoothSize)}
			, Env, {^MapperEnvJT(mapper, name, default, lag, smoothSize)}
			, Array, {^MapperArrayJT(mapper, name, default, lag, smoothSize)}
		)
	}
	init {}
	asSpecifier {
		^warps.findKeyForValue(this.class)
	}
	name_ {arg n;
		name=n;
		//this.changed(\name);
	}
	lag_ {arg lagTimes;
		lag=lagTimes;
		//this.changed(\lag);
	}
	smoothSize_ {arg size;
		smoothSize=size;
		smoothArray.size_(smoothSize??{2});
		//this.changed(\smoothSize);
	}
	makeBus {arg server, rate=\control, free=true;
		this.bus_(Bus.alloc(rate, server, default.size.max(1)), free)
	}
	bus_ {arg b, free=true;
		if ((bus!=nil)&&free) {bus.free};
		bus=b;
		bus.set(default);
		server=bus.server;
	}
	makeSmoothArray {
		smoothArray=SmoothArrayJT(smoothSize??{2});
	}
	changeMapper {arg mapper;
		^this.new(mapper, name, default, lag, smoothSize)
	}
}

MapperControlSpecJT : ControlSpec {
	//var <minval, <maxval, <warp, <name, <>default, <>lag, <smoothSize;
	//var <clipLo, <clipHi;
	var <name, <lag, <smoothSize, <spec;
	var <>smoothArray;
	var <mapFunction, <warpSymbol;
	var <gui;

	*new { arg minval = (0.0), maxval = (1.0), warp = ('lin'), step = (0.0), default, units, grid
		, name, lag, smoothSize;
		^super.newCopyArgs(minval, maxval, warp, step, default ? minval, units ? "", grid)
		.init(name, lag, smoothSize)
	}
	init {arg argname, arglag, argsmoothSize;
		name=argname;
		lag=arglag;
		argsmoothSize=smoothSize;

		warp = warp.asWarp(this);
		clipLo = min(minval, maxval);
		clipHi = max(minval, maxval);
		this.makeSmoothArray
	}
	map {arg value;
		^warp.map(value.clip(0.0, 1.0));
	}
	unmap {arg value;
		^warp.unmap(value.clip(clipLo, clipHi));
	}
	mapNoClip {arg value;
		^warp.map(value);
	}
	unmapNoClip {arg value;
		^warp.unmap(value);
	}
	name_ {arg n;
		name=n;
		//this.changed(\name);
	}
	lag_ {arg lagTimes;
		lag=lagTimes;
		//this.changed(\lag);
	}
	warp_ { arg w;
		warp = w.asWarp(this);
		this.changed(\warp);
	}
	smoothSize_ {arg size;
		smoothSize=size;
		smoothArray.size_(smoothSize??{2});
		//this.changed(\smoothSize);
	}
	makeSmoothArray {
		smoothArray=SmoothArrayJT(smoothSize??{2});
	}
	changeMapper {arg mapper;
		^Mapper2JT.new(mapper, name, default, lag, smoothSize)
	}
	asSpec {^ControlSpec(minval, maxval, warp)}
	namedControls {arg rate=\control;
		var out=();
		if (lag!=nil) {
			var lagName=(name++\Lag).asSymbol;
			out[lagName]=NamedControl(lagName, lag, rate);
			out[name]=NamedControl(name, default, rate, nil, spec:this.asSpec).lag(*out[lagName]);
		} {
			out[name]=NamedControl(name, default, rate, spec:this.asSpec);
		};
		^out
	}
	makeGui {}
}

MapperEnvJT : Mapper2JT {
	var <env;
	*new { arg env, name, default, lag, smoothSize;//mapper, name, default, lag, smoothSize
		^super.newCopyArgs(name, default?env.levels[0], lag, smoothSize).init(env);
	}
	init {arg argenv;
		env=argenv;
		arraySize=1024; arraySizeR=arraySize.reciprocal;
		this.initEnvelope;
		this.makeSmoothArray;
		warp=this;
	}
	env_ {arg e;
		env=e;
		//this.init;
		this.initEnvelope;
		this.changed(\env);
	}
	initEnvelope {
		this.normalizeEnvTimes;
		minval=env.levels.minItem;
		maxval=env.levels.maxItem;
		clipLo = min(minval, maxval);
		clipHi = max(minval, maxval);
		this.makeArray;
	}
	normalizeEnvTimes {
		env.times_(env.times.normalizeSum)
	}
	makeArray {
		array=env.discretize(arraySize).as(Array);
	}
	arraySize_ {arg size;
		arraySize=size;
		arraySizeR=arraySize.reciprocal;
		this.makeArray;
	}
	map {arg value;
		^env.at(value)
	}
	unmap {arg value;
		^(array.indexInBetween(value)*arraySizeR)
	}
	namedControls {}
}

MapperArrayJT : Mapper2JT {
	*new { arg array, name, default, lag, smoothSize;//mapper, name, default, lag, smoothSize
		^super.newCopyArgs(name, default?array[0], lag, smoothSize).init(array);
	}
	init {arg argarray;
		array=argarray;
		this.initArray;
		this.makeSmoothArray;
		warp=this;
	}
	array_ {arg a;
		array=a;
		this.initArray;
		this.changed(\array);
	}
	initArray {
		minval=array.levels.minItem;
		maxval=array.levels.maxItem;
		clipLo = min(minval, maxval);
		clipHi = max(minval, maxval);
		arraySize=array.size;
		arraySizeR=arraySize.reciprocal;
	}
	map {arg value;
		^array.blendAt(value*arraySize)
	}
	unmap {arg value;
		^(array.indexInBetween(value)*arraySizeR)
	}
	namedControls {}
}