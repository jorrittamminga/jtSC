+ Dictionary {
	keysValues {
		^this.collect{|i,j| i.value}
	}
	changeKey {arg oldKey, newKey;
		this[newKey]=this[oldKey];
		this.removeAt(oldKey);
	}
	sortedKeys {arg sortFunc;
		var keys = this.keys(Array);
		^keys.sort(sortFunc)
	}
	sortedValues {arg sortFunc;
		^this.sortedKeys(sortFunc).collect{arg key; this[key]}
	}
	unmap {arg specs=();
		^this.sortedKeys.collect{|key|
			if (specs[key]!=nil, {
				specs[key].unmap(this[key])
			},{
				this[key]
			})
		}.flat
	}
	sortedClumps {
		^this.sortedKeys.collect{|key|
			this[key].value.size.max(1)
		}
	}
	values_ {arg event;
		event.keysValuesDo{arg key, val;
			if (this[key]!=nil, {
				if (this[key].respondsTo(\value_), {
					{this[key].value_(val)}.defer;
				},{
					this[key]=val;
				});
			});
		};
	}
	valuesActions_ {arg event;
		event.keysValuesDo{arg key, val;
			if (this[key]!=nil, {
				if (this[key].respondsTo(\action), {
					this[key].action.value(val);
					{this[key].value_(val)}.defer;
				},{
					//this[key]=val;
				})
			});
		};
	}
	doActions {arg event; this.valuesActions_(event)}
	removeAllWithoutActions {arg event;
		var out=();
		event.keysValuesDo{arg key, val;
			if (this[key]!=nil, {
				if (this[key].respondsTo(\action), {
					out[key]=val;
				},{
					//this[key]=val;
				})
			});
		};
		^out
	}
}

+ Array {
	mapAsViewEvent {arg views=();
		var specs, keys, clumps, mapMethod, event=(), array;
		keys=views.sortedKeys;
		specs=keys.collect{|key| views[key].controlSpec};
		clumps=keys.collect{|key| views[key].value.size.max(1)};
		if (clumps.sum>clumps.size, {
			mapMethod='mapClumps';
			array=this.copy.clumps(clumps).collect(_.unbubble);
			array.do{|val,i|
				val=specs[i].map(val);
				event[keys[i]]=val;
				{views[keys[i]].value_(val)}.defer;
			};
		},{
			mapMethod='mapNoClumps';
			this.do{|val,i|
				val=specs[i].map(val);
				event[keys[i]]=val;
				{views[keys[i]].value_(val)}.defer;
			};
		});
		^event
		//^this.performMsg([method, specs]);
	}
	mapAsEvent {arg method='mapNoClumps', specs=[], keys=[], clumps=[];
		^this.performMsg([method, specs, keys, clumps]);
	}
	mapNoClumps {arg specs=[], keys=[];
		var event=();
		this.do{|val,i|
			event[keys[i]]=specs[i].map(val);
		};
		^event
	}
	mapClumps {arg specs=[], keys=[], clumps=[];
		var event=(), array;
		array=this.copy.clumps(clumps).collect(_.unbubble);
		array.do{|val,i|
			event[keys[i]]=specs[i].map(val);
		};
		^event
	}
}
/*
var event=();
array=array.clumps(sortedClumps).collect(_.unbubble);
array.collect{|val,i|
event[sortedKeys[i]]=sortedSpecs[i].map(val);
};
^event
x=[0,1,2,3];
x.map
x=(a:10, b:11)
x.map
*/