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
o.parent_(d)

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

EventJT : Event {
	//keys, values;
	var <>specs, <>actions, <objects, <>values, <>routines;
	var specsArray, actionsArray, objectsArray, valuesArray, routinesArray;
	var <valuesActionsFunc;
	var <sectKeys, <keys, <sortedKeys;
	var <arrayUnmappedSorted;
	var <hasObjects, <hasActions; // value of object != object
	var <presetJT;

	*newFromEvent {arg event, specs, actions;
		this=event;
		^super.new.initEventJT(specs, actions)
	}
	initEventJT {arg argspecs, argactions;
		keys=this.keys.asArray.sort;
		specs=argspecs??{()};
		actions=argactions??{()};
		objects=();
		hasObjects=false;
		hasActions=false;
		this.getObjects_;
		this.unmapValues_;
	}
	put_ {arg key, value;
		_IdentDict_Put
		value ?? { this.removeAt(key); ^this };
		keys=keys.add(key).sort;//check of er een nieuwe key is toegevoegd
		this.getObject_(key, value);
		this.unmapValues_;//brute force, can be more efficient by inserting new value to the list....
		^this.primitiveFailed
	}
	merge_ {arg event;
		event.keysValuesDo{|key,val|
			values[key]=val;
			if (keys.includes(key).not, {
				values[key]=val;
				if (value.value!=value, {
					this.getObject_(key, value);
				});
			});
			actions[key].value(val)
		};
		objects.keysValuesDo{|key,obj|
			obj.value_(values[key])
		};
		keys=values.keys.asArray.sort;
		this.unmapValues_
	}
	blend_ {arg event, blend=0.5;
		^this.blend(event, blend, true, specs)
	}
	getObject_ {arg key, obj;
		switch (obj.class.topclass, QObject, {
			objects[key]=obj;
			actions[key]=obj.action;
			values[key]=obj.value;
		}, EZGui, {
			objects[key]=obj;
			actions[key]=obj.action;
			values[key]=obj.value;
			specs[key]=this.getSpec(obj);
		}, Function, {
			actions[key]=obj;
		}, FunctionList, {
			actions[key]=obj;
		}, {
			"no idea what to do with ".post; key.post; " ".post; obj.post; " ".post; [obj.class, obj.class.topclass].postln;
		})
	}
	getObjects_ {
		this.keysValuesDo{|key,obj|
			objects[key]=obj;
			if (obj.value!=obj, {
				this.getObject_(key, obj);
			},{
				values[key]=obj
			})
		}
	}
	getValues_ {
		this.keysValuesDo{|key,val|
			values[key]=val.value;
		}
	}
	getSpec_ {arg obj;
		var spec;
		^if (obj.methods.collect{|method| (method.name==\controlSpec).binaryValue}.sum>0, {
			spec=obj.controlSpec.asSpec;
			if (spec.step<0.00001, {spec=spec.warp});
		},{
			spec
		})
	}
	getAction_ {arg obj;
		var flag=false;
		[\action].do{|k| flag=obj.methods.collect{|method| (method.name==k).binaryValue
			action=obj.action
		}.sum+flag};
		if (obj.class==String, {if (obj.interpret.class==Function, {
			action=obj.interpret;
			flag=flag+1})});
		if (flag>0, {
			hasActions=true;
			actions[key]=action
		})
	}
	getSpecs_ {
		objects.keysValuesDo{|key,obj|
			specs[key]=this.getSpec_(obj)
		}
	}
	getActions_ {
		var flag=0, action;
		objects.keysValuesDo{|key,obj|
			this.getAction_(obj)
		}
	}
	unmapValues_ {
		arrayUnmappedSorted=keys.collect{|key|
			if (specs[key]!=nil, {
				specs[key].unmap(values[key])
			},{
				values[key]
			})
		}
	}
	transitionTo_ {arg event, durations, curves, delayTimes, specs, actions;

	}
	asEventJT_ {^this}
	makeGui_ { }
	addPresetJT_ { arg dirname;
		presetJT=PresetJT.basicNew(this, dirname);
	}
}

+ Event {
	transitionTo_ {arg event, durations, curves, delayTimes, specs, actions;

	}
	asEventJT { arg specs, actions;
		Event.newFromEvent(this, specs, actions)
	}
	makeGui { }
}

EventsArrayJT : Array { //of EventJT? => is nu voornamelijk EventManager
	var <>specs, <>actions, <>objects, <>values, <>routines;
	var <reducedEventsArray, <statistics;
	//check of het Events zijn of EventsJT
	//automatisch omzetten naar ReducedEventsArray
	blendAt {}

}

ReducedEventsArrayJT : Array {//of Array

}

+ Array {
	asEventsArrayJT {
		if (er zijn Events aan boord, {
			^EventsArrayJT.newFromArray(this)
		},{
			^this
		})
	}
}