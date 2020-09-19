/*
a=EventsManagerJT([ [(a:12, b:43, c:3211), (a:54, b:45)], [(a:444, b:5432), (a:524, b:4.5)]])
a=EventsManagerJT([ [(a:12, b:[43,59], c:3211), (a:54, b:[45,100])], [(a:444, b:[0,5432]), (a:524, b:[4.5,100])]])

a=EventsManagerJT([ [(a:12, b:[43,59], c:3211), (a:54, b:[45,100])], \mean ])

a.array
a.keys
a.specs
a.specsArray
a.actions
a.actionsArray
a.dictionary
a.at([0,1])
a.blendAtIndices([0,0.5])
*/
EventsManagerJT : EventManagerJT { //
	*new{arg events, specs, actions, method='clipAt';
		^super.new.init(events, specs, actions, method)
	}
	init {arg argEvents, argSpecs, argActions, argMethod;
		specs=();
		actions=();
		argMethod=method;
		this.events_(argEvents, argSpecs, argActions);
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
}