+ Event {
	makeGui {arg parent, bounds=350@20, name, controlSpecs=(), onCloseFunc, canFocus=true
		, labelWidth, gap=4@4, margin=4@4, actions=(), excludeKeys, scroll=false;
		^EventGUIJT(this, parent, bounds, name, controlSpecs, onCloseFunc, canFocus, labelWidth, gap, margin, actions, excludeKeys, scroll)
	}
	asViewEvent {
		var event=this.copy;
		^(objects: event, controlSpecs: event.collect(_.controlSpec), routines: (), actions: event.collect(_.action))
	}
	deepPut {arg that;
		//f={arg q, p;
		that.keysValuesDo{|key,val|
			if (val.class==Event, {
				if (this[key].class==Event, {
					this[key].deepPut(that[key])
				},{
					this[key]=val
				})
			},{
				this[key]=val
			})
		}
	}
}
/*
EventJT

clumps[key]=obj.value.size.max(1);

e[\objects]=v;
e[\controlSpecs]=e.objects.collect(_.controlSpec);
e[\actions]=e.objects.collect(_.action);
e[\valuez]=e.objects.collect(_.value);
e[\keyz]=e.objects.keys;
e[\sortedKeyz]=e.objects.sortedKeys;
e[\sortedControlSpecs]=e[\sortedKeyz].collect{|key| e[\controlSpecs][key]};
e[\sortedActions]=e[\sortedKeyz].collect{|key| e[\actions][key]};
e[\sortedValues]=e[\sortedKeyz].collect{|key| e[\valuez][key]};
e.clumps;
e.type=\Views;// \Views, \Values
e.objectsType = \Views, \Values;
*/