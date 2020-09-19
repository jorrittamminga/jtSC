/*
events is an Array of events [(),()] or [[(),()],[(),()]] or etc
*/
ViewsPresetJTBlender : EventManagerJT {
	var <viewsPresetJT, <objects;
	var <>updateFunc;

	*new { arg viewsPresetJT, method='clipAt', events;
		^super.new.init(viewsPresetJT, method, events)
	}
	init {arg argviewsPresetJT, argMethod, argEvents;
		specs=();
		actions=();
		viewsPresetJT=argviewsPresetJT;
		objects=viewsPresetJT.objects;
		method=argMethod;

		updateFunc={
			if (this.gui!=nil, {
				index=this.gui.views[\blender].controlSpec.warp.unmap(index);
				this.gui.views[\blender].controlSpec_(
					ControlSpec(0.0, this.array.size-('clipAt': 1, 'wrapAt':0)[this.method])
				);
				index=this.gui.views[\blender].controlSpec.warp.map(index);
			});
			this.blendAtIndex(index, method);
		};

		switch(viewsPresetJT.class, ViewsPresetJT, {
			argEvents=argEvents??{viewsPresetJT.array};
			[\store, \removeAt, \add, \name].do{|key|
				viewsPresetJT.func[key]=viewsPresetJT.func[key].addFunc({
					this.events_(viewsPresetJT.array);
					//onderstaande hoeft alleen bij \removeAt en \add....
					updateFunc.value;
				})
			};
		}, ViewsPresetJTCollection, {
			viewsPresetJT.presetJT.func[\restore]=viewsPresetJT.presetJT.func[\restore].addFunc({arg i;
				if (viewsPresetJT.values.includes(i), {
					index=(viewsPresetJT.values.indexOf(i));
					if (gui!=nil, {
						{gui.views[\blender].value_(index)}.defer
					});
				})
			});
			argEvents=viewsPresetJT.presets;
			[\restoreAction, \restore, \store].do{|key|
				viewsPresetJT.func[key]=viewsPresetJT.func[key].addFunc({
					this.events_(viewsPresetJT.presets);
					updateFunc.value;
				})
			};
		});

		this.events_(argEvents, viewsPresetJT.specs, viewsPresetJT.actions);
	}
	doMapAction {arg array;
		event=();
		array.do{|val,index|
			var key=keys[index];
			val=specsArray[index].map(val);
			actionsArray[index].value(val,key);
			event[key]=val;
			{objects[key].value=val}.defer;
		};
	}
}