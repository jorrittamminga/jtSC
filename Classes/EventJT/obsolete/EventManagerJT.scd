/*
events_ must be more efficient!
EventManagerJT : Collection of ArrayedCollection ????
*/
EventManagerJT { //: EventManagerJT
	var <keys, <array, <specs, <actions, <event;
	var <specsArray, <actionsArray, <events;
	var <minItem, <maxItem, <mean, <dictionary, <meanArray;//statistics
	var <method='clipAt', <gui, <>index=0, <>depth=1.0;

	method_ {arg methode='clipAt';
		method=methode;
	}
	at {arg index;
		^if (index.size==0, {this.atIndex(index)},{this.atIndices(index)});
	}
	atIndex {arg index;
		this.doMapAction(array[index]);
		^event
	}
	atIndices {arg indices;
		var out=array.deepCopy;
		indices.asArray.do{arg i; out=out[i]};
		this.doMapAction(out);
		^event
	}

	blendAt {arg index;
		^if (index.size==0, {this.blendAtIndex(index, method)},{this.blendAtIndices(index, method)});
	}
	blendAtIndex {arg index;
		var out;
		out=array.blendAt(index, method);
		this.doMapAction(out);
		^event
	}
	blendAtIndices {arg indices;
		var out;
		out=array.blendAtIndices(indices, method);
		this.doMapAction(out);
		^event
	}

	blendAtDepth {arg index, depth=1.0;
		^if (index.size==0, {this.blendAtIndexDepth(index, method, depth)},{this.blendAtIndicesDepth(index, method, depth)});
	}
	blendAtIndexDepth {arg index, depth=1.0;
		var out;
		out=array.blendAt(index, method);
		out=[meanArray, out].blendAt(depth);
		this.doMapAction(out);
		^event
	}
	blendAtIndicesDepth {arg indices, depth=1.0;
		var out;
		out=array.deepCopy.blendAtIndices(indices, method);
		out=[meanArray, out].blendAt(depth);
		this.doMapAction(out);
		^event
	}


	doMapAction {arg array;
		event=();
		array.do{|val,index|
			var key=keys[index];
			val=specsArray[index].map(val);
			actionsArray[index].value(val,key);
			event[key]=val;
		};
	}
	events_ {arg argEvents, argSpecs, argActions;
		var tmp;
		tmp=argEvents.deepCopy.flat;
		events=argEvents.deepCopy;
		array=argEvents.deepCopy;
		this.findKeys;
		this.statistics;
		this.prSpecs(argSpecs);
		this.prActions(argActions);
		meanArray=keys.collect{|key| specs[key].unmap(mean[key])};
		//tmp.indicesOfEqual(\mean).do{|i| tmp[i]=mean};
		/*
		array.deepCopy.deepCollectWithoutEvents(0x7FFFFFFF, {arg t;
			t.post; "->".post;
			if (t.class==Event, { keys.collect{|key| specs[key].unmap(t[key])  }.postln},{t.postln})});
		*/
		tmp=tmp.collect{|event| keys.collect{|key| specs[key].unmap(event[key])}};
		array=array.deepCollectKeys(0x7FFFFFFF, {|i| i}, keys: keys);
		array=array.deepCollectWithoutEvents(0x7FFFFFFF, {arg t;
			if (t.class==Event, {keys.collect{|key| t[key]}},{t})});
		array=tmp.reshapeLike(array);

		array
	}
	specs_ {arg argSpecs;
		this.events_(events, argSpecs, actions);
	}
	actions_ {arg argActions;
		this.events_(events, specs, argActions);
	}
	findKeys {
		keys=[];
		array.flat.do{arg event;
			if (event.class==Event, {
				keys=if (keys.size>0, {
					event.keys.asArray.sect(keys);
				},{
					keys=event.keys.asArray
				});
			});
		};
		keys=keys.asSet.asArray.sort;
	}
	prSpecs {arg argspecs;
		//toevoegen aan bestaande specs!
		if (argspecs!=nil, {
			argspecs.keysValuesDo{|key,spec| specs[key]=spec.asSpec}
		});
		specsArray=Array.newClear(keys.size);
		keys.do{|key, index|
			if (specs[key]==nil, {
				specsArray[index]=ControlSpec(minItem[key], maxItem[key]).warp;
				specs[key]=ControlSpec(minItem[key], maxItem[key]).warp
			},{
				specs[key]=specs[key].asSpec;
				if (specs[key].step<=0.0000001, {
					specs[key]=specs[key].warp;
					specsArray[index]=specs[key];
				},{
					specsArray[index]=specs[key];
				});
			})
		};
		^specs
	}
	prActions {arg argactions;
		//toevoegen aan bestaande actions
		if (argactions!=nil, {
			argactions.keysValuesDo{|key,func| actions[key]=func}
		});
		actionsArray=Array.newClear(keys.size);
		keys.do{|key, index|
			actionsArray[index]=actions[key];
			actions[key]=actions[key]
		};
		^actions
	}
	statistics {
		minItem=(); maxItem=(); mean=(); dictionary=();//median=();
		array.flat.do{|event|
			//event.keys.symmetricDifference(keys).do{|key| event.removeAt(key)};
			if (event.class==Event, {
				keys.do{|key|
					if (minItem[key]==nil, {
						minItem[key]=event[key].asArray.flat.minItem;
						maxItem[key]=event[key].asArray.flat.maxItem;
						dictionary[key]=[event[key]];
					},{
						if (event[key].asArray.flat.minItem<minItem[key], {minItem[key]=event[key].asArray.flat.minItem});
						if (event[key].asArray.flat.maxItem>maxItem[key], {maxItem[key]=event[key].asArray.flat.maxItem});
						dictionary[key]=dictionary[key].add(event[key]);
					})
				}
			});
		};
		keys.do{|key|
			mean[key]=dictionary[key].mean;
			//median[key]=dictionary[key].median;
		};
	}
	makeGui {arg parent, bounds, includeDepth=false;
		{gui=EventManagerJTGUI1D(this, parent, bounds, includeDepth)}.defer
	}
}

EventManagerJTGUI1D {
	var <eventsManagerJT, parent, bounds;
	var <views, <compositeView, <font;
	var <method;

	*new {arg eventsManagerJT, parent, bounds=350@20, includeDepth;
		^super.new.init(eventsManagerJT, parent, bounds, includeDepth)
	}
	init {arg argeventsManagerJT, argparent, argbounds, includeDepth;
		var blenderFunc;
		eventsManagerJT=argeventsManagerJT;
		parent=argparent;
		bounds=argbounds;
		compositeView=CompositeView(parent, bounds.x@(bounds.y*(1+includeDepth.binaryValue))); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);
		views=();
		font=Font("Monaco", bounds.y*0.75);

		blenderFunc=if (includeDepth, {
			{|ez|
				eventsManagerJT.index=ez.value;
				eventsManagerJT.blendAtIndexDepth(ez.value, eventsManagerJT.depth)
			}
		},{
			{|ez|
				eventsManagerJT.index=ez.value;
				eventsManagerJT.blendAtIndex(ez.value)
			}
		});

		views[\blender]=EZSlider(compositeView, bounds, \blend
			, ControlSpec(0.0, eventsManagerJT.array.size-('clipAt': 1, 'wrapAt':0)[eventsManagerJT.method])
			, blenderFunc, eventsManagerJT.index);
		if (includeDepth, {
			views[\depth]=EZSlider(compositeView, bounds, \depth, ControlSpec(0.0, 1.0)
				, {|ez|
					eventsManagerJT.depth=ez.value;
					eventsManagerJT.blendAtIndexDepth(eventsManagerJT.index, eventsManagerJT.depth)
			}, eventsManagerJT.depth)
		});
	}
	makeBlenderGUI {}
}

//+ Array { asEventsArray { ^EventsArray(this) } }

+ Object {
	deepCollectKeys { arg depth, function, index = 0, rank = 0, keys; ^function.value(this, index, rank) }
}
