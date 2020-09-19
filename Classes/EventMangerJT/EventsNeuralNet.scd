EventsNeuralNet : EventManagerJT {
	var <nn, <trainingSet=[], trainingSetIndices=[], <nin, <path, <>learningrate=0.05, <>initweight=0.05, <>errortarget=0.00001, <>maxepochs=100000;
	var pathNN, pathTrainingSet, pathNNBinary, pathTrainingSetIndices;

	*new {arg events, specs, actions, nin, path;
		^super.new.init(events, specs, actions, nin, path)
	}
	init {arg argEvents, argSpecs, argActions, argNin, argPath;
		specs=();
		actions=();
		this.events_(argEvents, argSpecs, argActions)
		nin=argNin;
		nout=keys.size;
		nhidden=nin.maxItem(nout);
		path=argPath;
		this.initTrainingSet;
		this.initNN(false);
	}
	learn {arg input, index;
		if (input.size!=nin, {
			"input is not equal to the number of inputs of the neuralnet!".postln;
		},{
			if (trainingSetIndices.includesEqual([input, index]).not, {
				trainingSetIndices=trainingSetIndices.add([input, index]);
				trainingSet=trainingSet.add([input, eventsArray[index]]);
			});
		})
	}
	initTrainingSet {
		trainingSet=[]; trainingSetIndices=[];
	}
	train {arg autoSave=true;
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
		pathTrainingSet=path++"trainingSet.scmirZ";
		pathTrainingSetIndices=path++"trainingSetIndices.scmirZ";
		if (File.exists(pathTrainingSet), {trainingSet=pathTrainingSet.load});
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
		this.mapAction(out);
		^event
	}
}