//preset: (trainingSet: (presetNr: [input1, input2, etc], presetNr: [input1, input2, etc], etc), nin: 2, nogwat: tralala)
//maak het mogelijk om de huidige stand van de controllers blablabla
PresetsNNJT : PresetsJT {
	var <nin=2, nhidden, nout, trainingSet, normalizedTrainingSet, <input, <indicesTrainingSet;
	var <controlSpecsList, <viewsList, <actionsList, <factorsList, <presetsList, keysList, shape, sizes;
	var <reshapeFlag, <calculate, <>mode=1, out, <neuralNet;
	var <>maxEpochs=100000, <>errorTarget=0.00001;

	*new {arg presets;
		^super.basicNew(presets)
	}
	initObject {//dit kan veeeeel netter
		var fileName;
		presetsJT=object;
		this.makeLists;
		calculate={};
		if (value==nil, {value=(trainingSet:())});
		input={0}!nin;
		nout=if (presetsJT.array==nil, {
			nin
		},{
			presetsJT.array[0].values.flat.size;
		});
		nhidden=nout.max(nin);
		neuralNet=NeuralNet(nin, nhidden, nout);
		//----------------------------------------------------------- funcs
		funcs[\restore]=funcs[\restore].addFunc{
			if (value!=nil, {
				if (value[\trainingSet]!=nil, {
					//this.input=value[\trainingSet][value[\trainingSet].keys.asArray.sort[0]];//???
					input=value[\trainingSet][value[\trainingSet].keys.asArray.sort[0]];//???
					this.nin_(value[\trainingSet].values.collect{|i| i.size}.maxItem);
					nout=presetsJT.array[0].values.flat.size;
					nhidden=nout.max(nin);
					indicesTrainingSet=value[\trainingSet].keys.asArray.sort;
					fileName=directory.fullPath++"nns/"++basename++".scmirZ";
					if (File.exists(fileName), {
						neuralNet=NeuralNet(nin, nhidden, nout);
						neuralNet.load(fileName);
					});
					this.makeCalculateFunction;
				})
			});
		};
		funcs[\store]=funcs[\store].addFunc{
			var fileName;
			if (neuralNet!=nil, {
				fileName=directory.fullPath++"nns/"++basename++".scmirZ";
				neuralNet.save(fileName);
			});
		};
	}
	initGetAction {
		object=();
		getAction={ value }//kan beter toch?
	}
	initSetAction {
		action={  }//hier moet iets mee, maar wat???
	}
	initPathName {
		basename=\empty;
		pathName=(presetsJT.directory.fullPath++("NeuralNets/")).asPathName;//++numberOfFolders
		directory=pathName;
		if (File.exists(directory.fullPath).not, {
			File.mkdir(directory.fullPath);
			File.mkdir(directory.fullPath++"nns/");
		});
		this.update;
	}
	input_ {arg in;
		input=in;
		//calculate.value(input)
	}
	nin_ {arg n;
		nin=n.asInteger;
		this.input_(input.lace(nin))
	}
	makeNormalizedTrainingSet {
		normalizedTrainingSet=[];
		value[\trainingSet].sortedKeysValuesDo{|index, input|
			var pr=presetsJT.array[index];
			var norm;
			norm=keysList.collect{|key,i|
				presetsJT.controlSpecs[key].unmap(pr[key])
			}.flat;
			if (input.size!=nin, {
				input=input.lace(nin)
			});
			normalizedTrainingSet=normalizedTrainingSet.add([input, norm.flat])
		};
	}
	train {arg init=true;
		if (init, {
			this.makeCalculateFunction;
			this.makeNormalizedTrainingSet;
			nout=normalizedTrainingSet[0][1].size;
			neuralNet=NeuralNet(nin, nin.max(nout), nout);
		},{

		});
		neuralNet.trainExt(normalizedTrainingSet, errorTarget, maxEpochs);
	}
	makeCalculateFunction {
		calculate=if (reshapeFlag, {
			{arg arginput;
				var value, values;
				input=arginput??{input};
				out=neuralNet.calculate(input);
				out=out.reshapeLike(shape);
				values=controlSpecsList.collect{|cs,i|
					value=cs.map(out[i]);
					actionsList[i].value(value);
					value;
				};
				{ values.do{|val,i| viewsList[i].value_(val)} }.defer
			};
		},{
			{arg arginput;
				var value, values;
				input=arginput??{input};
				out=neuralNet.calculate(input);
				values=controlSpecsList.collect{|cs,i|
					value=cs.map(out[i]);
					actionsList[i].value(value);
					value;
				};
				{ values.do{|val,i| viewsList[i].value_(val)} }.defer
			};
		});
	}
	storeInTrainingSet {arg i, val;
		i=i??{presetsJT.index};
		value[\trainingSet][i]=val??{input};
		indicesTrainingSet=value[\trainingSet].keys.asArray.sort;
		this.store;
	}
	removeFromTrainingSet {arg i;
		i=i??{index};
		value.removeAt(i);
		indicesTrainingSet=value[\trainingSet].keys.asArray.sort;
		this.store;
	}
	makeLists {
		//=========================================================init all
		viewsList=[]; actionsList=[]; controlSpecsList=[]; keysList=[]; shape=[]; sizes=[];
		//=========================================================
		presetsJT.object.sortedKeysValuesDo{|key, view|
			viewsList=viewsList.add(view);
			actionsList=actionsList.add(if (view.action!=nil, {
				view.action
			},{
				nil
			}));
			controlSpecsList=controlSpecsList.add(presetsJT.object[key].controlSpec);
			keysList=keysList.add(key);
			shape=shape.add(presetsJT.value[key]);
			sizes=sizes.add(presetsJT.value[key].size.max(1));
		};
		if (sizes.sum!=controlSpecsList.size, {
			reshapeFlag=true;
		});
	}
	makeGui {arg parent, bounds=350@20;
		if (gui==nil, {
			{gui=PresetsNNGUIJT(this, parent, bounds);}.defer
			//if (cueJT!=nil, {cueJT.makeGui(gui.parent)});
		});
	}
}

PresetsNNGUIJT : PresetsGUIJT {
	var cv;
	makeSliders {
		cv.removeAll;
		cv.decorator.reset;
		//cv.bounds;
		presets.object[\nin].value_(presets.input.size);
		//presets.object[\nin];

		views[\slider]=EZMultiSlider(cv, cv.bounds, \in, [0.0, 1.0], {|ez|
			presets.input_(ez.value);
			presets.calculate.value(ez.value);
		}, presets.input, true, cv.bounds.width*0.05).decimals_(8).font_(font);
		views[\slider].sliderView.indexIsHorizontal = false;
		views[\slider].sliderView.isFilled=true;
	}
	updatePresets {
		views[\presetsTrainingSet].items_(
			presets.indicesTrainingSet.collect{|i| i.asString++"_"++presets.presetsJT.fileNamesWithoutNumbers[i]}
		);
		views[\presetsTrainingSet].value_(presets.indicesTrainingSet.indexOfEqual(presets.presetsJT.index));
	}
	preInit {
		var c=CompositeView(parent, bounds);
		c.addFlowLayout(0@0, 0@0);
		views[\mode]=Button(c, (bounds.y*3)@bounds.y)
		.states_([["learn", Color.white, Color.red],["calculate", Color.black, Color.green]]).action_{arg b;
			presets.mode=b.value;
			if (b.value==0, {
				views[\slider].action={|ez|
					presets.input_(ez.value)
				};
			},{
				views[\slider].action={|ez|
					presets.input_(ez.value);
					presets.calculate.value(ez.value)
				};
			});
		}.value_(presets.mode).font_(Font(font.name, font.size*0.75));
		presets.object[\nin]=EZNumber(c, (bounds.y*2)@bounds.y, \nin, ControlSpec(1, 16, 0, 1), {|ez|
			presets.nin_(ez.value);
			this.makeSliders;
		}, presets.nin, false, bounds.y).font_(Font(font.name, font.size*0.75));
		views[\removeFromTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["-"] ]).action_{
			presets.removeFromTrainingSet(presets.presetsJT.index);
			this.updatePresets;
		}.font_(font);
		views[\storeInTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["s"] ]).action_{
			presets.storeInTrainingSet(presets.presetsJT.index, presets.input);
			this.updatePresets;
		}.font_(font);
		views[\restoreTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["r"] ]).action_{}.font_(font);
		views[\presetsTrainingSet]=PopUpMenu(c, (bounds.x-(bounds.y*12))@bounds.y).items_([]).action_{|pop|
			//views[\slider].value_(presets.trainingSet[presets.indicesTrainingSet[pop.value]])
			var i=presets.indicesTrainingSet[pop.value];
			views[\slider].value_(presets.value[\trainingSet][i]);
			presets.presetsJT.restore(i)
		}.font_(font);
		this.updatePresets;
		views[\train]=Button(c, bounds.y*2@bounds.y).states_([ ["train"] ]).action_{
			presets.train(views[\init].value==0);
			presets.mode=1;
			{views[\mode].valueAction_(1)}.defer;
		}.font_(Font(font.name, font.size*0.75));
		views[\init]=Button(c, bounds.y*2@bounds.y)
		.states_([ ["init",Color.black, Color.red],["no init"] ]).action_{

		}.font_(Font(font.name, font.size*0.75));
		cv=CompositeView(parent, bounds.x@(bounds.y*4));
		cv.addFlowLayout(0@0,0@0);
		this.makeSliders;

		presets.funcs[\restore]=presets.funcs[\restore].addFunc{arg index;
			this.updatePresets;
			this.makeSliders;
		};
		presets.presetsJT.funcs[\index]=presets.presetsJT.funcs[\index].addFunc{arg index;
			var i;
			if (presets.indicesTrainingSet.includes(index), {
				i=presets.indicesTrainingSet.indexOfEqual(index);
				views[\slider].value_(presets.value[\trainingSet][index]);
				if (i!=views[\presetsTrainingSet].value, {
					views[\presetsTrainingSet].value_(i)
				});
			});
		};
	}
}