/*
EventJT = Event waarvan de content een object (b.v. View) is waarbij er een value, object, action is per key
//kijk ook of het gewoon met een Event kan, b.v.
(values: (freq:110, amp:0.1), objects: (freq: EZSlider, amp: EZSlider), specs: (), actions: ()
, specsArray, actionsArray, objectsArray, valuesArray
, clumps, clumpsArray, valuesActionsFuncs, sectKeys, sortedKeys, sortedSpecs, sortedClumps, mapMethod, arrayUnmappedSorted
, hasObjects, hasActions, hasSpecs, presetJT
, transitionFlag, noTransitionKeys, routines, routinesArray, resolution, waitTime, curves, steps, stepSizes, durations, delayTimes)
o=(freq: EZSlider, amp: EZSlider)//de views
d=(
specs: (freq: \freq.asSpec, amp: \amp.asSpec),
actions: (amp: Function, freq: Function),
values: (freq: 1000, amp:1.0),
routines: (freq: Routine, amp: Routine),
sectKeys: [], sortedKeys: []
specsArray: [], actionsArray: [], objectsArray: [], valuesArray: [], routinesArray: [],
arrayUnmappedSorted: [],
)
o.parent(d)

e.specs

//let op! een event kan objecten bevaten (functions, views, etc) of values (0, [0, 1.0], compileString)
Event = (a: object, b: object)
Event = (a: 123, b: 452)
Event.parentEvents.keys
Event.partialEvents.keys
EventsArrayJT = [(a:123, b:432), (a:43, b:543)];
ReducedEventsArrayJT = [[1.0,0.0], [0.0, 1.0]];//reduced, unmapped, unkeyed/sorted by key, sectKeys only
.copy

[1,2,3].value(1)
.valueAction(key, value)
.value(key, value)
.addAll
.putAll
.keysValuesDo
.sectKeysDo
.sectKeysValuesDo
.sectKeysValuesActionsDo
.keys
//EventWithObjectsJT : EventJT
//EventWithActionsJT : EventJT
//EventWithObjectsAndActionsJT :  EventJT
putAll
*/
EventJT : Event {
	var <specs, <actions, <objects, <values;
	var specsArray, actionsArray, objectsArray, valuesArray, <clumps, clumpsArray;
	var <valuesActionsFunc;
	var <sectKeys, <sortedKeys, <sortedSpecs, <sortedClumps, mapMethod;
	var <arrayUnmappedSorted;
	var <hasObjects, <hasActions, <hasSpecs;
	var <presetJT;
	//-------------------------------------- transtion keys
	var <transitionFlag=true, <>noTransitionKeys;
	var <>routines, routinesArray, <resolution=10, <waitTime, <curves, <steps, <stepSizes, <durations, <delayTimes
	//, <transitionDurationKey=\eventJTtransitionDuration
	//, <transitionCurveKey=\eventJTtransitionCurve
	//, <transitiondelayTimeKey=\eventJTtransitionDelayTime
	;

	//-------------------------------------- METHODS
	initEventJT {arg argspecs, argactions;
		sortedKeys=this.keys.asArray.sort;
		specs=argspecs??{()};
		actions=argactions??{()};
		objects=();
		values=();
		routines=();
		curves=();
		steps=();
		stepSizes=();
		durations=();
		delayTimes=();
		clumps=();
		mapMethod='mapNoClumps';
		this.resolution_(10);
		hasObjects=false;
		hasActions=false;
		hasSpecs=false;
		this.getObjects;
		this.sortAll;
		this.unmapValues;
	}
	update {
		this.getObjects;
		this.unmapValues;
	}
	resolution_ {arg r;
		resolution=r??{resolution};
		waitTime=resolution.reciprocal;
	}
	//override
	sortAll {
		sortedSpecs=sortedKeys.collect{|key| specs[key]};
		sortedClumps=sortedKeys.collect{|key| clumps[key]};
		if (sortedClumps.sum>sortedClumps.size, {
			mapMethod='mapClumps'
		},{
			mapMethod='mapNoClumps'
		});
	}
	put {arg key, obj;//value = object
		_IdentDict_Put
		obj ?? { this.removeAt(key); ^this };
		sortedKeys=sortedKeys.add(key).asSet.asArray.sort;//check of er een nieuwe key is toegevoegd
		this.getObject(key, obj);
		this.unmapValues;//brute force, can be more efficient by inserting new value to the list....
		this.sortAll;
		^this.primitiveFailed
	}
	//putAll {arg event; this.valuesActions(event);}
	keyValueAction {arg key, value;
		values[key]=value;
		actions[key].value(value, key);
	}
	values_ {arg event;
		var sectKeys=sortedKeys.sect(event.keys.asArray);
		sectKeys.do{|key|
			var val=event[key];
			values[key]=val;
		};
		//objects.keysValuesDo{|key,obj| {obj.value(values[key])}.defer};
		this.unmapValues
	}
	valuesActions {arg event;
		var sectKeys=sortedKeys.sect(event.keys.asArray);
		sectKeys.do{|key|
			var val=event[key];
			values[key]=val;
			actions[key].value(val);
		};
		//objects.keysValuesDo{|key,obj| {obj.value(values[key])}.defer};
		this.unmapValues
	}
	blend_ {arg event, blend=0.5;
		^this.blend(event, blend, true, specs)
	}
	getObject {arg key, obj;
		switch (obj.class.topclass, QObject, {
			objects[key]=obj;
			//actions[key]=obj.action;
			actions[key]={arg val; obj.action.value(val); {obj.value_(val)}.defer};
			values[key]=obj.value;
			specs[key]=ControlSpec(0.0, 1.0).warp;
			clumps[key]=obj.value.size.max(1);
			if (obj.class.superclass==ItemViewBase, {specs[key]=ControlSpec(0, obj.items.size-1, 0, 1)});
			if ((obj.class==Button) || (obj.class==RoundButton), {specs[key]=ControlSpec(0, obj.states.size-1, 0, 1)});
		}, EZGui, {
			objects[key]=obj;
			//actions[key]=obj.action;
			actions[key]={arg val; obj.action.value(val); {obj.value_(val)}.defer};
			values[key]=obj.value;
			specs[key]=this.getSpec(obj);
			clumps[key]=obj.value.size.max(1);
		}, Function, {
			actions[key]=obj;
			clumps[key]=1;
		}, FunctionList, {
			actions[key]=obj;
			clumps[key]=1;
		}, {
			"no idea what to do with ".post; key.post; " ".post; obj.post; " ".post;
			[obj.class, obj.class.topclass].postln;
		})
	}
	getObjects {
		this.keysValuesDo{|key,obj|
			objects[key]=obj;
			if (obj.value!=obj, {
				this.getObject(key, obj);
			},{
				values[key]=obj
			})
		}
	}
	getValues {
		this.keysValuesDo{|key,val|
			values[key]=val.value;
		}
	}
	getSpec {arg obj;
		var spec;
		^if (obj.class.methods.collect{|method| (method.name==\controlSpec).binaryValue}.sum>0, {
			spec=obj.controlSpec.asSpec;
			if (spec.step<0.00001, {spec=spec.warp},{spec});
		},{
			spec
		})
	}
	getAction {arg obj, key;
		var flag=false, action;
		[\action].do{|k| flag=obj.methods.collect{|method|
			action=obj.action
			(method.name==k).binaryValue
		}.sum+flag};
		if (obj.class==String, {
			if (obj.interpret.class==Function, {
				action=obj.interpret;
				flag=flag+1

			})
		});
		if (flag>0, {
			hasActions=true;
			actions[key]=action
		})
	}
	getSpecs {
		objects.keysValuesDo{|key,obj|
			specs[key]=this.getSpec(obj)
		}
	}
	getActions {
		var flag=0, action;
		objects.keysValuesDo{|key,obj|
			this.getAction(obj, key)
		}
	}
	unmap {arg event;
		^sortedKeys.collect{|key|
			if (specs[key]!=nil, {
				specs[key].unmap(event[key])
			},{
				event[key]
			})
		}.flat
	}
	map {arg array;
		^this.performMsg([mapMethod, array]);
	}
	mapNoClumps {arg array;
		var event=();
		array.collect{|val,i|
			event[sortedKeys[i]]=sortedSpecs[i].map(val);
		};
		^event
	}
	mapClumps {arg array;
		var event=();
		array=array.clumps(sortedClumps).collect(_.unbubble);
		array.collect{|val,i|
			event[sortedKeys[i]]=sortedSpecs[i].map(val);
		};
		^event
	}
	unmapValues {
		arrayUnmappedSorted=sortedKeys.collect{|key|
			if (values[key]!=nil, {
				if (specs[key]!=nil, {
					specs[key].unmap(values[key])
				},{
					values[key]
				})
			},{
				values[key]
			})
		};
	}
	transitionTo {arg event, durations, curves, delayTimes, specs, actions;

	}
	asEventJT {^this}
	//makeGui { }
	addPresetJT { arg dirname;
		presetJT=PresetJT.basicNew(this, dirname);
		presetJT.restoreAction={arg presetJT;
			presetJT.values.keysValuesDo{|key,value|
				actions[key].value(value);
				{objects[key].value=value}.defer;
			};
			values
		};
	}
}

+ Event {
	//transitionTo {arg event, durations, curves, delayTimes, specs, actions;}
	asEventJT { arg specs, actions;
		var e;
		e=EventJT.new(this.size).putAll(this);
		e.initEventJT(specs, actions);
		^e
		//^EventJT.newFromEvent(this, specs, actions)
	}
	//makeGui { }
}
/*
EventsArrayJT : Array { //of EventJT? => is nu voornamelijk EventManager
var <>specs, <>actions, <>objects, <>values, <>routines;
var <reducedEventsArray, <statistics;
//check of het Events zijn of EventsJT
//automatisch omzetten naar ReducedEventsArray, hoeft ws geen aparte class te worden hoor
blendAt {}

}

//ReducedEventsArrayJT : Array {//of Array}

+ Array {
asEventsArrayJT {
if (er zijn Events aan boord, {
^EventsArrayJT.newFromArray(this)
},{
^this
})
}
}
*/