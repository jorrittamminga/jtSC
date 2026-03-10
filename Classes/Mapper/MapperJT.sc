MapperJT {
	classvar <>warps;

	var <name, <default, <lag, <smoothSize;//, <bus
	var <minval, <maxval, <warp;//, <warp, <name, <>default, <>lag, <smoothSize;
	var <clipLo, <clipHi, <curve;
	var <>smoothArray, <array, <arraySize, <arraySizeR, <buffer;
	var <bus, <server;
	var <mapFunction, <warpSymbol;
	var <gui, <editor;

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
	free {
		if (bus!=nil) {bus.free};
		//if (synth!=nil) {bus.free};
	}
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
	smooth {arg value, method;
		^smoothArray.smooth(value, method);
	}
	makeView {|parent, bounds, action, layout|
		var e;
		gui=switch(default.size) {1}
		{e=EZSlider(parent, bounds??{350@30}, name, [minval,maxval, warp].asSpec)}
		{
			e=EZMultiSlider(parent, bounds??{350@40}, name, [minval, maxval, warp].asSpec, action, default, layout:layout??{\vert});
			e.sliderView.isFilled_(true);
			e
		}
	}
	prValueLikelyInRange { arg value, lo, hi;
		^(value >= lo) && (value <= hi)
	}
}
MapperEnvJT : MapperJT {
	var <env;
	var <indicesMin, <indicesMax;
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
	minval_ {|v|
		var tmpLevel=env.levels.deepCopy;
		indicesMin.do{|i| tmpLevel[i]=v};
		env.levels_(tmpLevel);
		minval=v;
		this.initArray;
	}
	maxval_ {|v|
		var tmpLevel=env.levels.deepCopy;
		indicesMax.do{|i| tmpLevel[i]=v};
		env.levels_(tmpLevel);
		maxval=v;
		this.initArray;
	}
	curve_ {|c|
		env.curves_(c);
		this.initArray;
	}
	initEnvelope {
		this.normalizeEnvTimes;
		minval=env.levels.minItem;
		maxval=env.levels.maxItem;
		curve=env.curves;
		indicesMin=env.levels.indicesOfEqual(minval);
		indicesMax=env.levels.indicesOfEqual(maxval);
		this.initArray;
	}
	initArray {
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
}

MapperArrayJT : MapperJT {
	*new { arg array, name, default, lag, smoothSize;
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
