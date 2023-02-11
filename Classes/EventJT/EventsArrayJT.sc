EventsArrayJT {
	classvar <>event;//an EventJT

	var <keys, <array, <specs, <actions;
	var <keysJT;
	var <specsArray, <actionsArray, <events;
	var <minItem, <maxItem, <mean, <dictionary, <meanArray;//statistics
	var <method='clipAt', <gui, <>index=0, <>depth=1.0;

	*fill {arg arrayOfEvents, eventJT, method, specs, actions, removeKeysJT=true;
		event=eventJT.asEventJT(specs, actions);
		^super.new.fill(arrayOfEvents, method, true, removeKeysJT);
	}
	fill {arg arrayOfEvents, method, update=true, removeKeysJT;
		var tmp;
		this.method_(method);//.events_(arrayOfEvents, update)
		if (update, {
			event.update;
			specs=event.specs;
			actions=event.actions;
		});
		tmp=arrayOfEvents.deepCopy.flat;
		events=arrayOfEvents.deepCopy;
		array=arrayOfEvents.deepCopy;
		this.findKeys;
		//keys [ deselectedKeysJT, durations_CuesJT, method_CuesJT, phase ]
		if (removeKeysJT, {
			keysJT=[];
			keys.do{|key| if (key.asString.contains("JT"), {keysJT=keysJT.add(key.asSymbol)})};
			keysJT.do{|k| keys.remove(k)};
		});
		this.statistics;
		this.prSpecs;
		this.prActions;
		meanArray=keys.collect{|key|
			var out;
			out=specs[key].unmap(mean[key]);
			out
		};
		tmp=tmp.collect{|event| keys.collect{|key| specs[key].unmap(event[key])}};
		array=array.deepCollectKeys(0x7FFFFFFF, {|i| i}, keys: keys);
		array=array.deepCollectWithoutEvents(0x7FFFFFFF, {arg t;
			if (t.class==Event, {keys.collect{|key| t[key]}},{t})});
		array=tmp.reshapeLike(array);
		array
	}
	//update {}
	doMapAction {arg array;
		array.do{|val,index|
			var key=keys[index];
			val=specsArray[index].map(val);
			actionsArray[index].value(val,key);
			//event[key]=val;//of
			event.values[key]=val
		};
	}
	doMapValue {arg array;
		array.do{|val,index|
			var key=keys[index];
			val=specsArray[index].map(val);
			event.values[key]=val
		};
	}
	method_ {arg methode;
		method=methode??{method};
	}
	//with Action
	at {arg index, methode=\doMapAction;
		^if (index.size==0, {this.atIndex(index, methode)},{this.atIndices(index, methode)});
	}
	atIndex {arg index, methode=\doMapAction;
		this.performMsg([methode, array[index]]);
		^event
	}
	atIndices {arg indices, methode=\doMapAction;
		var out=array.deepCopy;
		indices.asArray.do{arg i; out=out[i]};
		this.performMsg([methode,out]);
		^event
	}
	blendAt {arg index, methode=\doMapAction;
		^if (index.size==0, {this.blendAtIndex(index, method, methode)},{this.blendAtIndices(index, method, methode)});
	}
	blendAtIndex {arg index, methode=\doMapAction;
		var out;
		out=array.blendAt(index, method);
		this.performMsg([methode,out]);
		^event.values
	}
	blendAtIndices {arg indices, methode=\doMapAction;
		var out;
		out=array.blendAtIndices(indices, method);
		this.performMsg([methode,out]);
		^event.values
	}
	blendAtDepth {arg index, depth=1.0, methode=\doMapAction;
		^if (index.size==0, {
			this.blendAtIndexDepth(index, depth, methode)
		},{
			this.blendAtIndicesDepth(index, depth, methode)
		});
	}
	blendAtIndexDepth {arg index, depth=1.0, methode=\doMapAction;
		var out;
		out=array.blendAt(index, method);
		out=[meanArray, out].blendAt(depth);
		this.performMsg([methode,out]);
		^event.values
	}
	blendAtIndicesDepth {arg indices, depth=1.0, methode=\doMapAction;
		var out;
		out=array.deepCopy.blendAtIndices(indices, method);
		out=[meanArray, out].blendAt(depth);
		this.performMsg([methode,out]);
		^event.values
	}
	//without Action
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
	prSpecs {
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
	prActions {
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
						if (event[key].asArray.flat.minItem<minItem[key], {
							minItem[key]=event[key].asArray.flat.minItem});
						if (event[key].asArray.flat.maxItem>maxItem[key], {
							maxItem[key]=event[key].asArray.flat.maxItem});
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
		//{gui=EventManagerJTGUI1D(this, parent, bounds, includeDepth)}.defer
	}
}
/*
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
*/