/*
indexPreset: valuesInput
(0: [0.0, 0.2, 0.5], 2: [0.0, 0.0, 0.0], 5: [1.0, 1.0, 1.0]) = Preset
Button[learnmode/calculate] Button[learn] Button[unlearn] StaticText(index) Button[train] PopUpMenu(keys.asSet.sort)
learn: [index, values]
*/
PresetJTNeuralNet : PresetJT {
	var presetJT;
	var <nn, <trainingSet, trainingSetIndices=[], <nin, <path, <>learningrate=0.05, <>initweight=0.05, <>errortarget=0.00001, <>maxepochs=100000;
	var pathNN, pathTrainingSet, pathNNBinary, pathTrainingSetIndices;

	*new {arg presetJT, nin, path;
		^super.new.init(presetJT, nin, path)
	}
	init {arg argPresetJT, argNin, argPath;
		presetJT=argPresetJT;
		nin=argNin;
		nout=presetJT.objects.keys.size;
		nhidden=nin.maxItem(nout);
		path=argPath;
		this.initTrainingSet;
		this.initNN(false);
	}
	unlearn {arg index;
		trainingSetIndices.removeAt(index);
	}
	learn {arg index, input;
		if (input.size!=nin, {
			"input is not equal to the number of inputs of the neuralnet!".postln;
		},{
			trainingSetIndices[index]=input;
			/*
			if (trainingSetIndices.includesEqual([input, index]).not, {
				trainingSetIndices=trainingSetIndices.add([input, index]);
				//trainingSet=trainingSet.add([input, eventsArray[index]]);
			});
			*/
		})
	}
	initTrainingSet {
		trainingSetIndices=[];
	}
	train {arg autoSave=true;
		trainingSet=[];
		trainingSetIndices.sortedKeysValuesDo{|index, input|
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
			if (autoSave, {this.saveNN;})
		});
		"training is finished".postln;
	}
	path_ {arg p;
		path=p;
		if (File.exists(path).not, {
			File.mkdir(path);
		});
		pathNN=path++"nn.scmirZ";
		pathTrainingSetIndices=path++"trainingSetIndices.scmirZ";
		if (File.exists(pathNN), {nn.load(pathNN)});
		if (File.exists(pathTrainingSetIndices), {trainingSetIndices=pathTrainingSetIndices.load});
	}
	initNN {arg deleteNN=true;
		nn=NeuralNet(nin, nhidden, nout, learningrate, initweight);
		if (path!=nil, {
			if (deleteNN, {File.delete(pathNN)});
			this.path_(path);
		});
	}
	saveNN {
		nn.save(pathNN);
	}
	calculate {arg input;
		var out;
		out=nn.calculate(input);
		out=out.collect{|val,i| presetJT.objects.sortedSpecs[i].map(val)};
		//this.mapAction(out);
		^out
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTNeuralNetGUI(this, parent, bounds)}.defer
	}
}

PresetJTNeuralNetGUI : PresetJTGUI {
	prInit {
		compositeView=CompositeView(parent, bounds.x@(bounds.y*(this.nin+2))); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);

		presetJT.func[\restore]=presetJT.func[\restore].addFunc({
			pathNN;
			pathTrainingSetIndices;
			{views[\values].string_(presetJT.values.asCompileString) }.defer
		});
		presetJT.func[\name];
		presetJT.func[\add];
	}
}