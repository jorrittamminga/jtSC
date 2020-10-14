PresetJTNeuralNet : PresetJTCollection {
	var <nn, <trainingSet, trainingSetIndices=[], <nin, <path, <>learningrate=0.05, <>initweight=0.05, <>errortarget=0.00001, <>maxepochs=100000;
	var <dirnameNN;

	*new { arg preset, nin;
		^super.new.prInitNeuralNet(preset, nin)
	}
	prInitNeuralNet {arg preset, argNin;
		presetJT=preset;
		objects=presetJT.objects;
		values=();
		restoreAction={
			//this.prMakePresetArray;
			func[\restoreAction].value;
		};
		//------------------------------------------------------------- NEURAL NET SPECIFIC
		if (presetJT.dirname!=nil, {
			dirname=preset.dirname++"NeuralNets/TrainingSets/";
			dirnameNN=preset.dirname++"NeuralNets/NN/";
		});
		nin=argNin;
		nout=presetJT.objects.keys.size;
		nhidden=nin.maxItem(nout);

		//------------------------------------------------------------- PRESET UPDATE FUNCS
		presetJT.func[\add]=presetJT.func[\add].addFunc({arg i;
			array=array.collect{|values|
				var indices=values.keys.asArray.sort;
				indices=indices.copyToEnd(indices.indexInBetween(i).ceil.asInteger).reverse.do{arg index;
					values[index+1]=values[index]; values.removeAt(index)
				};
			};
			this.saveAll;
		});
		presetJT.func[\removeAt]=presetJT.func[\removeAt].addFunc({arg i;
			array=array.collect{|values|
				var indices;
				values.removeAt(i);
				indices=values.keys.asArray.sort;
				indices=indices.copyToEnd(indices.indexInBetween(i).ceil.asInteger).do{arg index;
					values[index-1]=values[index]; values.removeAt(index)
				};
			};
			this.saveAll;
		});
		//------------------------------------------------------------- NEURAL NET SPECIFIC
		this.initTrainingSet;
		this.initNN(false);
	}
	getValues {//dummy function
		values;//=objects.collect{|o| o.value};
	}
	unlearn {arg index;
		values.removeAt(index);
	}
	learn {arg index, input;
		if (input.size!=nin, {
			"input is not equal to the number of inputs of the neuralnet!".postln;
		},{
			values[index]=input;
			this.store;
		});
	}
	initTrainingSet {
		values=();
	}
	train {arg autoSave=true;
		var pathNN=dirnameNN++name++".scmirZ";
		trainingSet=[];
		values.sortedKeysValuesDo{|index, input|
			trainingSet=trainingSet.add([input,
				presetJT.sortedKeys.collect{|key|
					presetJT.specs[key].unmap(presetJT.array[index]??{presetJT.specs[key].default})
				}
			])
		};
		"start trainig....".postln;
		if (trainingSet.size>0, {
			neuralNet.trainExt(trainingSet, errortarget, maxepochs);
			//neuralNet.train(trainingSet, errortarget, 10);
			if (autoSave, {neuralNet.save(pathNN)})
		});
		"training is finished".postln;
	}
	initNN {//arg deleteNN=true;
		var pathNN=dirnameNN++name++".scmirZ";
		nn=NeuralNet(nin, nhidden, nout, learningrate, initweight);
		if (File.exists(pathNN), {nn.load(pathNN);});
	}
	calculate {arg input;
		var out;
		out=nn.calculate(input);
		out=out.collect{|val,i|
			var value=presetJT.objects.sortedSpecs[i].map(val);
			objects.actions[key].value(value);
			{objects.objects[key].value=value}.defer;
			value
		};
		^out
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTNeuralNetGUI(this, parent, bounds)}.defer
	}
}

PresetJTNeuralNetGUI : PresetJTGUI {
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