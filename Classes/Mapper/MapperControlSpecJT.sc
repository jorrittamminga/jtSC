MapperControlSpecJT : ControlSpec {
	//var <minval, <maxval, <warp, <name, <>default, <>lag, <smoothSize;
	//var <clipLo, <clipHi;
	var <name, <lag, <smoothSize, <spec;
	var <>smoothArray, <curve;
	var <mapFunction, <warpSymbol;
	var <gui, <editor;
	var <bus, <server;

	*new { arg minval = (0.0), maxval = (1.0), warp = ('lin'), step = (0.0), default, units, grid
		, name, lag, smoothSize;
		^super.newCopyArgs(minval, maxval, warp, step, default ? minval, units ? "", grid)
		.prInit(name, lag, smoothSize)
	}
	prInit {arg argname, arglag, argsmoothSize;
		name=argname;
		lag=arglag;
		smoothSize=argsmoothSize;
		warp = warp.asWarp(this);
		curve = warp.asSpecifier;
		this.makeSmoothArray;
		this.init;
	}
	init {
		//warp = warp.asWarp(this);
		//curve = warp.asSpecifier;
		clipLo = min(minval, maxval);
		clipHi = max(minval, maxval);
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
		curve = warp.asSpecifier;
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
		^MapperJT.new(mapper, name, default, lag, smoothSize)
	}
	asSpec {^ControlSpec(minval, maxval, warp)}
	makeBus {arg server, rate=\control, free=true;
		this.bus_(Bus.alloc(rate, server, default.size.max(1)), free)
	}
	bus_ {arg b, free=true;
		if ((bus!=nil)&&free) {bus.free};
		bus=b;
		bus.set(default);
		server=bus.server;
	}
	kr {
		var value, lagger;
		value=NamedControl(this.name, this.default, \control, spec:this.asSpec);
		[\minval, \maxval].do{|key,i|
			var kee=(this.name++"_"++key).asSymbol;
			NamedControl(kee, [this.minval,this.maxval][i], \control, nil, spec:this.asSpec);
		};
		if (lag!=nil) {
			var lagName=(this.name++\_lag).asSymbol;
			lagger=NamedControl(lagName, lag, \control);
			^value.lag(*lagger);
		} {
			^value
		};
	}
	makeView {|parent, bounds, action, layout, editor=true|
		var e;
		gui=if (default.size<=1)
		{
			bounds=bounds??{350@20};
			//EZSlider(parent, bounds, name, [minval,maxval, warp].asSpec).font_(Font(Font.defaultMonoFace, bounds.size*0.5));
			e=EZSlider(parent, bounds, name, [minval,maxval, warp].asSpec).font_(Font(Font.defaultMonoFace, bounds.size*0.5))
		}
		{
			//EZMultiSlider(parent, bounds??{350@40}, name, [minval, maxval, warp].asSpec, action, default, layout:layout??{\vert});
			e=EZMultiSlider(parent, bounds??{350@40}, name, [minval, maxval, warp].asSpec, action, default, layout:layout??{\vert});
			e.sliderView.isFilled_(true);
			e
		};
		if (editor) {this.makeEditor}
	}
}