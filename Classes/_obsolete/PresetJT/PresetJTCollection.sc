PresetJTCollection : PresetJT {
	var <presetJT, <>presets;
	var <controlSpec;

	*new { arg preset, indices;
		var objects, dirname;
		if (preset.dirname!=nil, {
			dirname=preset.dirname++"Collections/";
		});
		objects=indices??{(0..preset.array.size-1)};
		^this.basicNew(objects, dirname, preset)
	}
	indices_ {arg indices;
		this.values_(indices);
	}
	prMakePresetArray {
		presets=values.deepCollect(0x7FFFFFFF, {|i| presetJT.array.clipAt(i)??{presetJT.array[0]} });
	}
	prInit {arg preset;
		if (values==nil, {values=objects});
		presetJT=preset;
		restoreAction={
			this.prMakePresetArray;
			func[\restoreAction].value;
		};
		objects=presetJT.objects;
		presetJT.func[\store]=presetJT.func[\store].addFunc({
			if (values.includes(presetJT.index), {
				this.prMakePresetArray;
			})
		});
		presetJT.func[\add]=presetJT.func[\add].addFunc({arg i;
			array=array.deepCollect(0x7FFFFFFF, {arg val; if (val>=i, {val+1},{val})   });
			this.saveAll;
			this.restore;
		});
		presetJT.func[\removeAt]=presetJT.func[\removeAt].addFunc({arg i;
			array=array.deepCollect(0x7FFFFFFF, {arg val; if (val>i, {val-1},{if (val==i, {nil},{val})   }) });
			array=array.asCompileString.replace("nil, ", "").replace(", nil", "").interpret;
			this.saveAll;
			this.restore;
		});
		presetJT.func[\name]=presetJT.func[\name].addFunc({arg i, preset, order;
			array=array.deepCollect(0x7FFFFFFF, {arg val; order[val]   });
			this.saveAll;
			this.restore;
		});
		controlSpec=ControlSpec(0,1.0).warp;
	}
	restore {arg i;
		var out;
		"restore PresetCollection".postln;
		presetJT.objects.routines.do{|r| r.stop};
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
	getValues { }
	makeGui {arg parent, bounds;
		{gui=PresetJTCollectionGUI(this, parent, bounds)}.defer
	}
}

PresetJTCollectionGUI : PresetJTGUI {
	prInit {
		compositeView=CompositeView(parent, bounds.x@(bounds.y*2)); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);
		views[\values]=TextField(compositeView, bounds).action_{|t|
			presetJT.values_(t.string.interpret);
		}.string_(presetJT.values.asCompileString).canFocus_(false).font_(font);
		views[\values].mouseDownAction={arg b;
			b.enabled_(true);
			b.canFocus_(true);
		};
		[\restore, \restoreI].do{|key|
			presetJT.func[key]=presetJT.func[key].addFunc({
				{views[\values].string_(presetJT.values.asCompileString) }.defer
			});
		}
	}
}