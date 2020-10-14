ViewsPresetJT : PresetJT {
	var defaultValues;
	*new {arg views, dirname;
		^this.basicNew(views, dirname)
	}
	prInit {
		restoreAction={
			values.keysValuesDo{|key,value|
				actions[key].value(value);
				{objects[key].value=value}.defer;
			};
			values
		};
		this.getActions;
		this.getSpecs;
	}
	//============================================== VIEWS HANDELING
	getActions {
		actions=objects.collect{|view| view.action};
		^actions
	}
	getSpecs {
		specs=specs??{()};
		defaultValues=defaultValues??{()};
		objects.keysValuesDo{|key, gui|
			if (gui.isKindOf(EZGui), {
				specs[key]=gui.controlSpec;
				defaultValues[key]=gui.controlSpec.default;
			},{
				defaultValues[key]=0.5;
			})
		}
		^specs
	}
}

ViewsPresetJTCollection : PresetJTCollection {
	var defaultValues;
}