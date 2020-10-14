PresetJTCollectionBlender : PresetJTCollection {
	classvar <methodClassVar, <blendTypeClassVar;
	var <blendFunc, <method, <blendType;

	*new { arg preset, indices, methode='clipAt', blender='normal';//'normal', 'depth'
		var objects, dirname;
		methodClassVar=methode;
		blendTypeClassVar=blender;
		if (preset.dirname!=nil, {
			dirname=preset.dirname++"Collections/";
		});
		objects=indices??{(0..preset.array.size-1)};
		^this.basicNew(objects, dirname, preset)
	}
	method_ {arg methode;
		methodClassVar=methode;
	}
	prMakePresetArray {
		method=methodClassVar;
		blendType=blendTypeClassVar;
		presets=values.deepCollect(0x7FFFFFFF, {|i| presetJT.array.clipAt(i)??{presetJT.array[0]} });
		presets=EventsArrayJT.fill(presets, presetJT.objects.asEventJT, method);
		blendFunc=if (presets.array.rank<3, {
			if (blendType=='depth', {
				{arg index, depth; presets.blendAtIndexDepth(index, depth)}
			},{
				{arg index; presets.blendAtIndex(index)}
			})
		},{
			if (blendType=='depth', {
				{arg index, depth; presets.blendAtIndicesDepth(index, depth)}
			},{
				{arg index; presets.blendAtIndices(index)}
			})
		});
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTCollectionBlenderGUI(this, parent, bounds)}.defer
	}
}

PresetJTCollectionBlenderGUI : PresetJTGUI {
	var blenderCompositeView, <index=0.0, <depth=1.0;
	updateControlSpec {
		^ControlSpec(0, presetJT.presets.array.size-('clipAt': 1, 'wrapAt':0)[presetJT.method]).warp
	}
	makeBlendFader {arg rank=2;
		var faders, boundz=bounds.deepCopy;
		if (rank<3, {
			if (presetJT.blendType=='depth', {
				views[\depth]=EZSlider(blenderCompositeView, bounds, \depth, ControlSpec(0, 1.0), {|ez|
					depth=ez.value;
					presetJT.blendFunc.value(index, ez.value)
				}, 1.0)
			});
			views[\blender]=EZSlider(blenderCompositeView, bounds, \blend
				, this.updateControlSpec
				, {|ez|
					index=ez.value;
					presetJT.blendFunc.value(ez.value, depth)
			}, 0);
		},{

		})
	}
	prInit {
		var prevrank, rank;
		compositeView=CompositeView(parent, bounds.x@(bounds.y*4)); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);
		views[\values]=TextField(compositeView, bounds).action_{|t|
			presetJT.values_(t.string.interpret);
		}.string_(presetJT.values.asCompileString).canFocus_(false).font_(font);
		blenderCompositeView=CompositeView(compositeView, bounds.x@(bounds.y*2)); blenderCompositeView.addFlowLayout(0@0, 0@0); blenderCompositeView.background_(Color.yellow);
		this.makeBlendFader;
		views[\values].mouseDownAction={arg b;
			b.enabled_(true);
			b.canFocus_(true);
		};
		rank=presetJT.presets.array.rank;
		prevrank=rank;
		presetJT.func[\restore]=presetJT.func[\restore].addFunc({
			{
				views[\values].string_(presetJT.values.asCompileString);
				rank=presetJT.presets.array.rank;
				if (prevrank!=rank, {
					blenderCompositeView.removeAll;
					blenderCompositeView.decorator.reset;
					this.makeBlendFader;
					prevrank=rank
				},{
					views[\blender].controlSpec_(this.updateControlSpec)
				});
			}.defer
		});
	}
}