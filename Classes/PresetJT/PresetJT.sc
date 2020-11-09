/*
PresetJT is only for writing/storing and loading/restoring presets values (mostly Events (freq:100, amp:0.1)) in a File
for morphing between presets use CueFileJT or PresetBlender or etc

array is an array with all the presets which exists in the dirname (path) => [(freq: 1000), (freq:440)]
collection is a array with indices of the array: [0,1]
*/
MetaPresetJT {
	var <>index=0;
	var <values;//<actions, <specs
	var <>extension=".scd", <>dirname, <name="0";
	var <>array, <names;
	var <>func, <>restoreAction;
	var <gui, <>objects, <objectsChanged;

	*basicNew { arg objects, dirname, extra;//objects
		^super.new.init(objects, dirname, extra)
	}
	init {arg argobjects, argdirname, extra;
		objects=argobjects;
		dirname=argdirname;
		//==============================================
		names=names??{[]};
		array=array??{[]};
		func=func??{()};
		[\restoreAction, \restore, \restoreI, \store, \save, \add, \removeAt, \name, \swap].do{|key| func[key]=func[key]};
		this.getValues;
		this.prInit(extra);
		objectsChanged=false;
		if (dirname!=nil, {
			if (File.exists(dirname).not, {
				dirname.mkdir;
				names=[name];
				array=[values];
				this.store;
				this.restore;
			},{
				this.loadAll
			});//loadAll
		});
	}
	getValues {
		values=objects.collect{|o| o.value};
	}
	values_ {arg vals;
		values=vals;
		restoreAction.value(this);
	}
	restore {arg i;
		var out;
		objects.routines.do{|r| r.stop};
		if (i!=nil, {
			if (i.class==String, {i=names.indexOf(i)??{0}});
			index=i;
			name=names[index];
		});
		values=array[index];
		out=restoreAction.value(this);
		func[\restore].value(index, this);
		^out
	}
	restoreI {arg i, durations=1.0, curves, delayTimes, update=true;
		if (i!=nil, {
			if (i.class==String, {i=names.indexOf(i)??{0}});
			index=i;
			name=names[index];
		});
		values=array[index];
		this.objects.valuesActionsTransition(values, durations, curves, delayTimes, update);
		func[\restoreI].value(index, this, durations, curves, delayTimes, update);
	}
	load {
		values=(dirname++name++extension).load;
		restoreAction.value(this);
	}
	loadAll {
		var restoreFlag=false;
		if (File.exists(dirname), {
			PathName(dirname).entries.do{|p|
				if (p.isFile, {
					names=names.add(p.fileNameWithoutExtension);
					array=array.add(p.fullPath.load);
					restoreFlag=true;
				});
			}
		});
		index=0;
		if (array.size==0, {
			this.store;
		});
		if (restoreFlag, {
			name=names[index];
			this.restore;
		});
	}
	saveAll {
		array.do{var values,i;
			if (values!=nil, {
				var file=File(dirname++names[i]++extension, "w");
				file.write(values.asCompileString);
				file.close;
			})
		}
	}
	save {
		var file=File(dirname++name++extension, "w");
		"save".postln;
		"values ".post; values.postln;
		file.write(values.asCompileString);
		file.close;
	}
	storeAll {}
	store {
		this.getValues;
		//if (array.size>index, {array[index]=values},{this.add});
		if (index>=array.size, {
			array=array.add(values);
			names=names.add(name);
			index=array.size-1;
		},{
			array[index]=values;
		});
		func[\store].value(index, this);
		this.save;
	}
	at {arg i;
		index=i.clip(0, array.size-1);
		this.restore;
	}
	add {
		"add".postln;
		index=index+1;
		"index ".post; index.postln;
		name=name++"1";
		"name ".postln; name.postln;
		array=array.insert(index, values);
		"array".post; array.postln;
		names=names.insert(index, name);
		"names ".post; names.postln;
		"this.save".postln;
		this.save;
		"func[add].value".post; [index, this].postln;
		func[\add].value(index, this);
	}
	removeAt {arg i;
		var y, z;
		i=i??{index};
		if (i<array.size, {
			File.delete(dirname++names[i]++extension);
			array.removeAt(i);
			names.removeAt(i);
			index=i.clip(0, array.size-1);
			name=names[index];
			func[\removeAt].value(i, this);//or index instead of i??
		});
	}
	put { arg i, val;
		if (i<array.size, {
			index=i;
			name=names[index];
			values=val;
			this.store;
		});
	}
	name_ {arg fileName;
		var order, prevIndex, prevName=name.copy, newName=fileName.copy;
		if (name!=fileName, {
			("mv "++(dirname++name++extension)++" " ++ (dirname++fileName++extension)).unixCmd;
			name=fileName;
			names[index]=name;
			array=names.order.collect{|i| array[i]};
			order=names.order.copy;
			names=names.sort;//ook een eventuele collections herschikken!
			prevIndex=index.copy;
			index=names.indexOf(name);
			func[\name].value(index, this, order, prevIndex, prevName, newName);
			//func[\swap].value(prevIndex, index, this);
		});
	}
	//swap {arg fromIndex, toIndex; array.swap(fromIndex, toIndex);}
	prInit {arg extra;}
}

PresetJT : MetaPresetJT {
	*new { arg objects, dirname, extra;//objects
		^this.basicNew(objects, dirname, extra)
	}
	prInit {arg extra;
		switch(objects.class, EventJT, {
			restoreAction={
				values.keysValuesDo{|key,value|
					objects.actions[key].value(value);
					{objects.objects[key].value=value}.defer;
				};
				values
			};
		}, Event, {
			objects=objects.asEventJT;
			restoreAction=if (objects.objects.size>0, {
				if (objects.actions.size>0, {
					{
						values.keysValuesDo{|key,value|
							objects.actions[key].value(value);
							{objects.objects[key].value=value}.defer;
						};
						values
					}
				},{
					{
						values.keysValuesDo{|key,value|
							//actions[key].value(value);
							{objects.objects[key].value=value}.defer;
						};
						values
					}
				})
			},{
				if (objects.actions.size>0, {
					{
						values.keysValuesDo{|key,value|
							objects.actions[key].value(value);
							//{objects[key].value=value}.defer;
						};
						values
					}
				},{
					{
						//values.keysValuesDo{|key,value|
						//actions[key].value(value);
						//{objects[key].value=value}.defer;
						//};
						values
					}
				})
			})
		}, {

		})
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTGUI(this, parent, bounds)}.defer
	}
}

PresetJTGUI {
	var <presetJT, parent, bounds;
	var <views, <compositeView, <font;

	*new {arg presetJT, parent, bounds=350@20;
		^super.new.init(presetJT, parent, bounds)
	}
	prInit {
		compositeView=CompositeView(parent, bounds); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);
	}
	init {arg argpresetJT, argparent, argbounds;
		var width;
		presetJT=argpresetJT;
		parent=argparent;
		bounds=argbounds;
		views=();
		font=Font("Monaco", bounds.y*0.6);
		this.prInit;
		this.makeFileGUI;
	}
	makeFileGUI {
		var width;
		width=(bounds.x-(7*bounds.y)*0.5);
		views[\store]=Button(compositeView, bounds.y@bounds.y).states_([[\s]]).action_{presetJT.store}.canFocus_(false).font_(font);
		views[\restore]=Button(compositeView, bounds.y@bounds.y).states_([[\r]]).action_{presetJT.restore}.canFocus_(false).font_(font);
		views[\add]=Button(compositeView, bounds.y@bounds.y).states_([["+"]]).action_{
			"presetJT.add".postln;
			presetJT.add;
			"this.update".postln;
			this.update;
		}.canFocus_(false).font_(font);
		views[\remove]=Button(compositeView, bounds.y@bounds.y).states_([["-"]]).action_{
			presetJT.removeAt;
			this.update;
		}.canFocus_(false).font_(font);
		views[\name]=TextField(compositeView, width@bounds.y).action_{|t|
			presetJT.name_(t.string);
			this.update;
		}.string_(presetJT.name).canFocus_(false).font_(font);
		views[\name].mouseDownAction={arg b;
			b.enabled_(true);
			b.canFocus_(true);
		};
		views[\list]=PopUpMenu(compositeView, width@bounds.y).items_(
			if (presetJT.names.size==0, {["0"]},{
				presetJT.names
			})
		).action_{arg i;
			{
				views[\name].string_(presetJT.names[i.value]);
				views[\index].string_(i.value);
			}.defer;
			presetJT.restore(i.value)
		}.canFocus_(false).font_(font);
		views[\index]=StaticText(compositeView, bounds.y@bounds.y).string_("0").align_(\right).font_(font)
		.stringColor_(Color.white).background_(Color.black);
		views[\prev]=Button(compositeView, bounds.y@bounds.y).states_([["<"]]).canFocus_(false).action_{
			var i=presetJT.index-1;
			if (i>=0, {
				presetJT.restore(i);
				{
					views[\index].string_(i);
					views[\list].value_(presetJT.index);
					views[\name].string_(presetJT.name);
				}.defer
			})
		};
		views[\next]=Button(compositeView, bounds.y@bounds.y).states_([[">"]]).canFocus_(false).action_{
			var i;
			i=presetJT.index+1;
			if (i<presetJT.array.size, {
				presetJT.restore(i);
				{
					views[\index].string_(i);
					views[\list].value_(presetJT.index);
					views[\name].string_(presetJT.name);
				}.defer
			});
		};

		[\restore, \restoreI].do{|key|
			presetJT.func[key]=presetJT.func[key].addFunc({arg index, preset;
				{
					views[\index].string_(index);
					views[\list].value_(index);
					views[\name].string_(presetJT.names[index])
				}.defer
			})
		};
	}
	update {
		{
			views[\list].items_(presetJT.names);
			views[\list].value_(presetJT.index);
			views[\name].string_(presetJT.name);
			views[\index].string_(presetJT.index);
		}.defer
	}
}