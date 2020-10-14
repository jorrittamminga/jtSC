/*
maak een argument reduce=true (is nu het geval) of juist reduce=false (en zet dan de ontbrekende keys op een soort default waarde)
a=ArrayOfEvents([ [(a:12, b:43, c:3211), (a:54, b:45)], [(a:444, b:5432), (a:524, b:4.5)]], (a: [1, 555, \exp], b: [2, 10000, \exp], c:\freq), (a: {arg val, key; [val,key].postln}, b: {arg val,key; [key,val].postln}))
a.at([0, 0])
*/
EventsManagerJT {
	var <specs, <actions, <events, event;
	var specsArray, <eventsArray, actionsArray, <array;
	var <keys;

	*new {arg eventsArray, specs, actions;
		^super.new.init(eventsArray, specs, actions)
	}
	init {arg argeventsArray, argspecs, argactions;
		specs=argspecs??{()};
		actions=argactions??{()};
		eventsArray=EventsArrayJT(argeventsArray);
		array=eventsArray.array;
		keys=eventsArray.keys;
		this.prSpecs(specs??{()});
		this.prActions(actions??{()});
	}
	mapAction {arg array;
		event=();
		array.do{|val,index|
			var key=keys[index];
			val=specsArray[index].map(val);
			actionsArray[index].value(val,key);
			event[key]=val;
		};
	}
	at {arg index;
		var array=eventsArray.deepCopy;
		index.asArray.do{arg i; array=array[i]};
		this.mapAction(array);
		^event
	}
	events_ {arg argevents;

		events=this.unmapEventsFlat(events);
		eventsArray=events.collect{|event| keys.collect{|key| event[key]}};

		events=argevents.deepCopy;

		events=events.deepCollectKeys(0x7FFFFFFF, {|i| i}, keys: keys);
		events=events.deepCollectWithoutEvents(0x7FFFFFFF, {arg t; t.postln; if (t.class==Event, {keys.collect{|key| t[key]}},{t})});//is eigenlijk .asArray achtige method

		eventsArray=eventsArray.reshapeLike(events);

		events=argevents.deepCopy
	}
	prSpecs {arg argspecs;
		specsArray=Array.newClear(keys.size);
		keys.do{|key, index|
			if (specs[key]==nil, {
				specsArray[index]=ControlSpec(min[key], max[key]).warp;
				specs[key]=ControlSpec(min[key], max[key]).warp
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
		actionsArray=Array.newClear(keys.size);
		keys.do{|key, index|
			if (argactions[key]==nil, {
				actionsArray[index]=nil;
				actions[key]=nil
			},{
				actionsArray[index]=argactions[key];
				actions[key]=argactions[key]
			})
		};
		^actions
	}
	specs_ {arg argspecs;
		if (argspecs!=nil, {
			specs=argspecs;
			this.events_(events)
		});
	}
	actions_ {arg argactions;
		if (argactions!=nil, {
			actions=argactions;
			this.events_(events)
		});
	}
	unmapEventsFlat {arg events;
		events.do{|event| event.keysValuesDo{|key,val| event[key]=specs[key].unmap(val)} };
		^events
	}
}