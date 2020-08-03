/*
- init NeuralNet als er een preset veranderd wordt....
- update GUI van de input als de preset (index) verandert
- parent kan ook die van de preset zijn
*/
PresetNN {

	var <controlSpecsList, <viewsList, <actionsList, <factorsList, <presetsList;
	var <out, <keys, <preset, <presetNormalized, <trainingSet, <trainingEvent;
	var <path, <neuralNet, <nin, <>gui, <>value, <out, <>input, <>mode, <id;
	var <nhidden, <nout, <sizes, <shape, <reshapeFlag;
	var <>errortarget, <>maxepochs, <>learningrate, <>initweight;
	*new {arg preset, nin, slaves=false, presets, learningrate=0.05, initweight=0.05, keys
		, path, errortarget=0.00001, maxepochs=100000;
		^super.new.init(preset, nin, slaves, presets, learningrate, initweight, keys
			, path, errortarget, maxepochs)
	}

	init {arg argpreset, argnin, argslaves, argpresets, arglearningrate, arginitweight
		, argkeys
		, argpath, argerrortarget, argmaxepochs;
		//var prevPreset, numberOfPresets;
		//------------------------------- init parameters
		controlSpecsList=[];
		sizes=[];
		shape=[]; reshapeFlag=false;
		viewsList=[];
		actionsList=[];
		keys=[];
		preset=argpreset;
		presetNormalized=[];
		path=argpath??{preset.path};
		trainingSet=[];
		trainingEvent=();
		nin=argnin??{2};
		input=Array.fill(nin, 0);
		mode=1;
		errortarget=argerrortarget??{0.00001};
		maxepochs=argmaxepochs??{1000000};
		id=UniqueID.next;
		learningrate=arglearningrate;
		initweight=arginitweight;
		//------------------------------- calculate warps
		this.getWarps;
		//------------------------------- get sizes
		this.getSizes;
		//------------------------------- init NeuralNet
		this.initNN;
		this.path_(argpath);
		this.loadNN;
		this.loadtrainingEvent;
		//------------------------------- make GUI
		//this.makeGUI;
	}

	update {

	}

	initNN {arg delete=false;
		neuralNet=NeuralNet(nin.asInteger
			, nhidden=sizes.sum.max(nin).asInteger
			, nout=sizes.sum.asInteger
			, learningrate??{0.05}, initweight??{0.05}
		);
		if (delete, {
			this.deleteNN;
		});
	}

	initTrainingEvent {
		trainingEvent=();
	}

	path_ {arg argpath;
		path=argpath ?? {var p;
			//preset.path++preset.folderName++"/neuralnet/"
			p=preset.path.deepCopy.split($/);
			p=p.copyRange(0, p.size-3).join($/)++"/neuralnets/";
			if (File.exists(p).not, {File.mkdir(p)});
			p=p++preset.folderName++"/";
			p
		};
		if (File.exists(path).not, {File.mkdir(path)});
	}

	calculate {arg arginput;
		var value, values;
		input=arginput??{input};
		out=neuralNet.calculate(input);

		if (reshapeFlag, {
			out=out.reshapeLike(shape);
		});
		values=controlSpecsList.collect{|cs,i|
			value=cs.map(out[i]);
			actionsList[i].value(value);
			/*
			{
			[i, value, actionsList[i], viewsList[i]].postln;
			viewsList[i].value_(value)
			}.defer;
			*/
			value;
		};
		{ values.do{|val,i| viewsList[i].value_(val)} }.defer
	}

	learn {arg arginput, index;
		index=index??{preset.index.deepCopy};
		arginput=arginput??{input.deepCopy};
		if (arginput.size!=nin, {
			"input is not equal to the number of inputs of the neuralnet!".postln;
		},{
			trainingEvent[index]=arginput;
		})
	}

	makeTrainigSet {
		trainingSet=[];
		"trainingEvent: ".post; trainingEvent.postln;
		trainingEvent.keysValuesDo{|index, input|
			var normalizedPreset=this.normalizePreset(index).flat;
			if ((input.size==nin) && (normalizedPreset.size==nout), {
				trainingSet=trainingSet.add([input, normalizedPreset])
			},{
				"sizes are not equal to the neuralnet!".postln;
				[nin, input.size, nout, normalizedPreset.size].postln;
				[nin.class, input.size.class, nout.class, normalizedPreset.size.class].postln;
				input.postln;
				normalizedPreset.postln;
			});
		};
		//trainingSet=trainingSet.flat;
		//^trainingSet
	}

	train {arg autoSave=true;//arg errortarget=0.001, maxepochs=100000;
		this.makeTrainigSet;
		"trainigSet: ".post; trainingSet.postcs;

		"start trainig....".postln;
		if (trainingSet.size>0, {
			neuralNet.trainExt(trainingSet, errortarget, maxepochs);
			//neuralNet.train(trainingSet, errortarget, 10);
			if (autoSave, {
				this.saveNN;
			})
		});
		"training is finished".postln;
	}

	getWarps {
		//=========================================================init all
		viewsList=[];
		actionsList=[];
		controlSpecsList=[];
		keys=[];
		shape=[];
		sizes=[];
		//=========================================================
		preset.views.sortedKeysValuesDo{|key, view|
			var cs=ControlSpec(0.0, 1.0);
			viewsList=viewsList.add(view);
			actionsList=actionsList.add(if (view.action!=nil, {
				view.action
			},{
				nil
			}));

			if (preset.controlSpecs[key]!=nil, {
				//cs=preset.controlSpecs[key]
				cs=preset.views[key].controlSpec
			},{
				if (view.class==Button, {
					cs=ControlSpec(0, view.states.size.asFloat);
				});
				if (view.class==PopUpMenu, {
					cs=ControlSpec(0, view.items.size.asFloat);
				});
				if (view.class==ListView, {
					cs=ControlSpec(0, view.items.size.asFloat);
				});
			});
			if (cs.step<0.01, {
				controlSpecsList=controlSpecsList.add(cs.warp);
			},{
				controlSpecsList=controlSpecsList.add(cs);
			});
			keys=keys.add(key);
			shape=shape.add(preset.values[key]);
			sizes=sizes.add(preset.values[key].size.max(1));
		};
		if (sizes.sum!=controlSpecsList.size, {
			reshapeFlag=true;
		});
	}

	getSizes {


	}

	normalizePreset {arg index;
		var ppreset;
		if (index==nil, {index=preset.index});
		ppreset=preset.presets.deepCopy[index];
		presetNormalized=Array.fill(keys.size, 0.5);
		keys.do{|key,k|
			var value=0.5;
			if (ppreset[key]!=nil, {
				value=ppreset[key];
			});
			presetNormalized[k]=controlSpecsList[k].unmap(value).clip(0.0, 1.0).abs;
		};
		^presetNormalized
	}

	saveSettings {}
	loadSettings {}
	savetrainingEvent {
		var file=File(path++"trainingEvent.scd", "w");
		file.write(trainingEvent.asCompileString);
		file.close;
	}
	loadtrainingEvent {
		var file;
		if (File.exists(path++"trainingEvent.scd"), {
			file=File(path++"trainingEvent.scd", "r");
			trainingEvent=file.readAllString.interpret;
			file.close;
		});
	}
	loadNN {
		if (File.exists(path++"nn.scmirZ"), {
			neuralNet.load(path++"nn.scmirZ");
		});
	}
	saveNN {
		neuralNet.save(path++"nn.scmirZ");
	}

	deleteNN {
		(path++"nn.scmirZ").postln;
		File.delete(path++"nn.scmirZ");

	}

	//rescalePresets {}

	makeGUI {arg parent, bounds=350@20;
		//{
		gui=PresetNNGUI(this, parent, bounds);
		^gui
		//}.defer
	}

}