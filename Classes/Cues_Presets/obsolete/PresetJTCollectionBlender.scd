PresetJTCollectionBlender : PresetJTCollection {
	classvar <methodClassVar, <blendTypeClassVar;
	var <blendFunc, <method, <blendType;
	var <>blendIndex=0, <>blendDepth=1.0;

	*new { arg preset, indices, methode='clipAt', blender='normal';//'normal', 'depth'
		var objects, dirname;
		methodClassVar=methode;
		blendTypeClassVar=blender;
		if (preset.dirname!=nil, {
			dirname=preset.dirname++"Collections/";
			objects=indices??{(0..preset.array.size-1)};
		},{
			objects=indices??{(0..preset.array.size-1)};
		});
		^this.basicNew(objects, dirname, preset)
	}
	method_ {arg methode;
		methodClassVar=methode;
	}
	prMakePresetArray {
		var preset;
		method=methodClassVar;
		blendType=blendTypeClassVar;
		presets=values.deepCollect(0x7FFFFFFF, {|i| presetJT.array.clipAt(i)??{presetJT.array[0]} });
		presets=EventsArrayJT.fill(presets, presetJT.objects.asEventJT, method);
		blendIndex=controlSpec.unmap(blendIndex);
		controlSpec=ControlSpec(0, presets.array.size-('clipAt': 1, 'wrapAt':0)[method]).warp;
		blendIndex=controlSpec.map(blendIndex);
		blendFunc=if (presets.array.rank<3, {
			if (blendType=='depth', {
				preset=presets.blendAtIndexDepth(blendIndex, blendDepth, \doMapValue);
				{arg index, depth; presets.blendAtIndexDepth(index, depth) }
			},{
				preset=presets.blendAtIndex(blendIndex, \doMapValue);
				{arg index; presets.blendAtIndex(index)}
			})
		},{
			if (blendType=='depth', {
				preset=presets.blendAtIndicesDepth(blendIndex, blendDepth, \doMapValue);
				{arg index, depth; presets.blendAtIndicesDepth(index, depth)}
			},{
				preset=presets.blendAtIndices(blendIndex, blendDepth, \doMapValue);
				{arg index; presets.blendAtIndices(index)}
			})
		});
		^preset
	}
	restore {arg i, blendInit, depthInit;
		var out;
		presetJT.objects.routines.do{|r| r.stop};
		if (i!=nil, {
			if (i.class==String, {i=names.indexOf(i)??{0}});
			index=i;
			name=names[index];
		});
		values=array[index];
		blendIndex=blendInit??{blendIndex};
		blendDepth=depthInit??{blendDepth};
		out=restoreAction.value(this);
		func[\restore].value(index, this);
		blendFunc.value(blendIndex, blendDepth);
		^out
	}
	restoreI {arg i, durations=1.0, curves, delayTimes, blendInit, depthInit;
		var preset;
		if (i!=nil, {
			if (i.class==String, {i=names.indexOf(i)??{0}});
			index=i;
			name=names[index];
		});
		values=array[index];
		blendIndex=blendInit??{blendIndex};
		blendDepth=depthInit??{blendDepth};
		preset=this.prMakePresetArray;
		presetJT.objects.valuesActionsTransition(preset, durations, curves, delayTimes, false);
		func[\restoreI].value(index, this, durations, curves, delayTimes);
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTCollectionBlenderGUI(this, parent, bounds)}.defer
	}
}

PresetJTCollectionBlenderGUI : PresetJTGUI {
	var blenderCompositeView, <index=0.0, <depth=1.0;
	//updateControlSpec { ^ControlSpec(0, presetJT.presets.array.size-('clipAt': 1, 'wrapAt':0)[presetJT.method]).warp}
	makeBlendFader {arg rank=2;
		var faders, boundz=bounds.deepCopy;
		if (rank<3, {
			if (presetJT.blendType=='depth', {
				views[\depth]=EZSlider(blenderCompositeView, bounds, \depth, ControlSpec(0, 1.0), {|ez|
					presetJT.blendDepth=ez.value;
					depth=ez.value;
					presetJT.blendFunc.value(index, ez.value)
				}, 1.0)
			}).font_(font);
			views[\blender]=EZSlider(blenderCompositeView, bounds, \blend
				, presetJT.controlSpec
				, {|ez|
					index=ez.value;
					presetJT.blendIndex=ez.value;
					presetJT.blendFunc.value(ez.value, depth)
			}, presetJT.blendIndex).font_(font);
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
		[\restore, \restoreI].do{|key|
			presetJT.func[key]=presetJT.func[key].addFunc({
				{
					views[\values].string_(presetJT.values.asCompileString);
					rank=presetJT.presets.array.rank;
					if (prevrank!=rank, {
						blenderCompositeView.removeAll;
						blenderCompositeView.decorator.reset;
						this.makeBlendFader;
						prevrank=rank
					},{
						views[\blender].controlSpec_(presetJT.controlSpec).value_(presetJT.blendIndex);
					});
				}.defer
			});
		}
	}
}