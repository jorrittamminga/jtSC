ViewsPresetJT {
	var <views, <actions, <specs, <values;
	var defaultValues;
	var <>index;
	var <>extension=".scd", <>dirname, <name;
	var <>presetCollection, <names;

	*new { arg views, dirname;
		^super.new.init(views, dirname)
	}
	init {arg argviews, argdirname;
		views=argsviews;
		dirname=argdirname;
		this.getValues;
		this.getActions;
		this.getSpecs;
		if (dirname!=nil, {
			this.readAllPresets;
		});
		//presetCollection=iets??{[]};
		//index=iets??{0};
	}
	//============================================== VIEWS HANDELING
	getValues {
		values=views.collect{|view| view.value};
		^values
	}
	getActions {
		actions=views.collect{|view| view.action};
		^actions
	}
	getSpecs {
		specs=specs??{()};
		defaultValues=defaultValues??{()};
		views.keysValuesDo{|key, gui|
			if (gui.isKindOf(EZGui), {
				specs[key]=gui.controlSpec;
				defaultValues[key]=gui.controlSpec.default;
			},{
				defaultValues[key]=0.5;
			})
		}
		^specs
	}
	//==============================================
	loadAllPresets {
		names=names??{[]};
		presetCollection=presetCollection??{[]};
		if (File.exists(dirname), {
			PathName(dirname).entries.do{|p|
				names=names.add(p.fileNameWithoutExtension);
				presetCollection=presetCollection.add(p.load)
			};

		},{

		});
	}
	//============================================== FILE HANDELING
	sort {
		presetCollection=names.order.collect{|i| presetCollection[i]};
		names=names.sort;
	}
	save {
		var file=File(dirname++name++extension, "w");
		this.getValues;
		file.write(values.asCompileString);
		file.close;
	}
	store {
		this.save;
		presetCollection[index]=values;
	}
	at {arg i;
		index=i.clip(0, presetCollection.size-1);
		this.restore;
	}
	add {
		index=presetCollection.size;
		name=index;
		this.save;
		presetCollection=presetCollection.add(values);
	}
	removeAt {arg i;
		i=i??{index};
		if (i<presetCollection.size, {
			File.delete(dirname++names[i]++extension);
			presetCollection.removeAt(i);
		})
	}
	put { arg i;
		if (i<presetCollection.size, {
			index=i;
			name=names[index];
			this.store;
		});
	}
	restore {
		values=presetCollection[index];
		values.keysValuesDo{|key,value|
			actions[key].value(value)
			{views[key].value=value}.defer;
		};
		^values
	}
	load {
		values=(dirname++name++extension).load;
		values.keysValuesDo{|key,value|
			actions[key].value(value)
			{views[key].value=value}.defer;
		}
	}
	name_ {arg fileName;
		if (name!=fileName, {
			("cp "++(dirname++name++extension)++" " ++ (dirname++fileName++extension)).unixCmd;
			name=fileName;
			names[index]=name;
		});
	}
}