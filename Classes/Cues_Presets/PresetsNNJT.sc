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
	getValue {arg init=false;
		if (init, {
			value=(trainingSet:(0: {0}!(nin??{1}) ));
			input={0}!nin;
		},{
			value=this.getAction.value(this);
		})
		^value
	}
	initObject {//dit kan veeeeel netter
		var fileName, tmp;
		presetsJT=object;

		this.makeLists;//deze hier weg!
		calculate={};
		if (value==nil, {value=(trainingSet:())});
		input={0}!nin;


		nout=if (presetsJT.array==nil, {
			nin
		},{
			if (presetsJT.array.size>0, {
				tmp=presetsJT.array[0].deepCopy;
				tmp.removeAt(\deselectedKeysJT);
				tmp.values.flat.size;
			},{
				nin
			})
		});
		nhidden=nout.max(nin);
		neuralNet=NeuralNetJT(nin, nhidden, nout);

		//----------------------------------------------------------- funcs
		funcs[\restore]=funcs[\restore].addFunc{
			var tmp, presets;
			if (value!=nil, {
				if (value[\trainingSet]!=nil, {
					//this.input=value[\trainingSet][value[\trainingSet].keys.asArray.sort[0]];//???
					presets=value[\trainingSet].keys.asArray.sort;
					deselectedKeys=presets.collect{|i| presetsJT.array[i][\deselectedKeysJT]}.flat.asSet.asArray.sort;
					this.nin_(value[\trainingSet].values.collect{|i| i.size}.maxItem);
					if (mode==0, {
						input=value[\trainingSet][value[\trainingSet].keys.asArray.sort[0]];//???
					},{
						if (input.size!=nin, {input=input.lace(nin)});
					});
					nout=presets.collect{|i|
						var tmp=presetsJT.array[i].deepCopy;
						tmp.removeAt(\deselectedKeysJT);
						deselectedKeys.do{|key| tmp.removeAt(key)};
						tmp.values.flat.size
					}.maxItem;
					//nout=tmp.values.flat.size;
					nhidden=nout.max(nin);
					indicesTrainingSet=value[\trainingSet].keys.asArray.sort;
					fileName=directory.fullPath++"nns/"++basename++".scmirZ";
					if (File.exists(fileName), {
						neuralNet=NeuralNetJT(nin, nhidden, nout);
						neuralNet.load(fileName);
					});
					this.makeLists;
					this.makeCalculateFunction;
				})
			},{
				nout=if (presetsJT.array==nil, {
					nin
				},{
					if (presetsJT.array.size>0, {
						tmp=presetsJT.array[0].deepCopy;
						tmp.removeAt(\deselectedKeysJT);
						tmp.values.flat.size;
					},{
						nin
					})
				});
				nhidden=nout.max(nin);
				neuralNet=NeuralNetJT(nin, nhidden, nout);
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
	initNN {


	}
	input_ {arg in;
		input=in;
		//calculate.value(input)
	}
	nin_ {arg n;
		nin=n.asInteger;
		value[\trainingSet]=value[\trainingSet].collect{|val| val.lace(nin)};
		this.input_(input.lace(nin));
	}
	makeNormalizedTrainingSet {
		normalizedTrainingSet=[];
		value[\trainingSet].sortedKeysValuesDo{|index, input|
			var pr=presetsJT.array[index];
			var norm, cs;
			//pr.removeAt(\deselectedKeysJT);
			norm=keysList.collect{|key,i|
				var val=pr[key];
				if (val==nil, {val=presetsJT.object[key].value});
				presetsJT.controlSpecs[key].unmap(val)
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
			neuralNet=NeuralNetJT(nin, nin.max(nout), nout);
		},{

		});
		neuralNet.trainExt(normalizedTrainingSet, errorTarget, maxEpochs);
	}
	makeCalculateFunction {
		calculate=if (reshapeFlag==true, {
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
				{
					values.do{|val,i|
						viewsList[i].value_(val)
					}
				}.defer
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
		var tmp, keys;
		//=========================================================init all
		viewsList=[]; actionsList=[]; controlSpecsList=[]; keysList=[]; shape=[]; sizes=[];
		//=========================================================
		keys=presetsJT.object.keys.deepCopy.asArray.sort;
		deselectedKeys.do{|key| keys.remove(key)};
		keys.do{|key|
			var view=presetsJT.object[key];
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
		},{
			reshapeFlag=false;
		});
	}
	addToCueList {arg cueList, cueName;
		var views=(), object;
		cueName=cueName??{(directory).allFolders.last.asSymbol};
		if (gui==nil, {this.makeGui});
		views[\selectForCueList]=Button(gui.parent, gui.bounds)
		.states_([["multislider is cued"],["presetpopupmenu is cued"]]).action_{|b|
			var keys=cueJT.object.deepCopy.keys.asArray;
			keys.remove([\presetsTrainingSet, \slider][b.value]);
			keys.remove(\routinesJT);
			//keys.remove(\presets);
			cueJT.getAction={ var e=(); keys.do{|key| e[key]=cueJT.object[key].value}; e};
		};
		//[\slider, \presetsTrainingSet].do{|key| views[key]=gui.views[key]};// \presets
		[\slider, \presetsTrainingSet].do{|key| views[key]=gui.views[key]};// \presets
		views[\aaapresets]=gui.views[\presets];
		cueJT=CuesJT(views, cueName);
		cueJT.makeGui(gui.parent, gui.bounds);
		cueJT.addToCueList(cueList);
		cueJT.funcs[\store]=cueJT.funcs[\store].addFunc{arg i;
			if (cueJT.value[\method]>0, {
				cueJT.value[\extras]=cueJT.value[\extras]??{()};
				cueJT.value[\extras][\durations]=(selectForCueList:0, aaapresets:0);
				cueJT.object[\extras].string_(cueJT.value[\extras].asCompileString);
				cueJT.store(i, false)
			},{
				/*
				if (cueJT.value[\extras]!=nil, {
				(cueJT.value[\extras][\durations]==(selectForCueList:0, aaapresets:0));
				});
				cueJT.value[\extras]=nil;
				*/
			});
		};
		views[\selectForCueList].valueAction_(1);
	}
	makeGui {arg parent, bounds=350@20;
		if (gui==nil, {
			{gui=PresetsNNGUIJT(this, parent, bounds);}.defer
			//if (cueJT!=nil, {cueJT.makeGui(gui.parent)});
		});
	}
}

PresetsNNGUIJT : PresetsGUIJT {
	var cv, <index;
	updatePresets {
		views[\presetsTrainingSet].items_(
			presets.indicesTrainingSet.collect{|i| i.asString++"_"++presets.presetsJT.fileNamesWithoutNumbers[i]}
		);
		if (presets.indicesTrainingSet!=nil, {
			views[\presetsTrainingSet].value_(presets.indicesTrainingSet.indexOfEqual(presets.presetsJT.index));
		})
	}
	makeSlider {
		cv.removeAll;
		cv.decorator.reset;
		views[\slider]=EZMultiSlider(cv, cv.bounds, \in, [0.0, 1.0], {|ez|
			//presets.input_(ez.value);
			presets.calculate.value(ez.value);
		}, presets.input, false, cv.bounds.width*0.05).decimals_(8).font_(font);
		views[\slider].sliderView.indexIsHorizontal = false;
		views[\slider].sliderView.isFilled=true;
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
					//presets.input_(ez.value);
					presets.calculate.value(ez.value)
				};
			});
		}.value_(presets.mode).font_(Font(font.name, font.size*0.75));
		presets.object[\nin]=EZNumber(c, (bounds.y*2)@bounds.y, \nin, ControlSpec(1, 16, 0, 1), {|ez|
			presets.nin_(ez.value);
			//this.makeSliders;
			this.makeSlider;
		}, presets.nin, false, bounds.y).font_(Font(font.name, font.size*0.75));
		views[\removeFromTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["-"] ]).action_{
			presets.removeFromTrainingSet(presets.presetsJT.index);
			this.updatePresets;
		}.font_(font);
		views[\storeInTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["s"] ]).action_{
			var index;

			presets.storeInTrainingSet(presets.presetsJT.index, presets.input);
			this.updatePresets;
		}.font_(font);
		views[\restoreTrainingSet]=Button(c, bounds.y@bounds.y).states_([ ["r"] ]).action_{}.font_(font);
		views[\presetsTrainingSet]=PopUpMenu(c, (bounds.x-(bounds.y*12))@bounds.y).items_([]).action_{|pop|
			//views[\slider].value_(presets.trainingSet[presets.indicesTrainingSet[pop.value]])
			var i=presets.indicesTrainingSet[pop.value];
			//if (presets.mode==0, {
			views[\slider].value_(presets.value[\trainingSet][i]);
			//});

			if (presets.presetsJT.value[\method]!=nil, {
				if (presets.presetsJT.value[\method]>0, {
					//hier dan dus interpoleren, maar hoe???? maak een soort CueJT oid
					presets.presetsJT.restore(i)
				},{
					presets.presetsJT.restore(i)
				})
			},{
				presets.presetsJT.restore(i)
			});

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
		index=presets.index;
		/*
		views[\slider]=EZMultiSlider(cv, cv.bounds, \in, [0.0, 1.0], {|ez|
		//presets.input_(ez.value);
		presets.calculate.value(ez.value);
		}, presets.input, false, cv.bounds.width*0.05).decimals_(8).font_(font);
		views[\slider].sliderView.indexIsHorizontal = false;
		views[\slider].sliderView.isFilled=true;
		*/
		this.makeSlider;
		presets.funcs[\restore]=presets.funcs[\restore].addFunc{arg i;
			if (presets.index!=index, {
				this.updatePresets;
				this.makeSlider;
				index=presets.index;
				views[\slider].value_(presets.input);
				presets.object[\nin].value_(presets.nin);
			},{

			});
		};
		presets.presetsJT.funcs[\index]=presets.presetsJT.funcs[\index].addFunc{arg index;
			var i;
			if (presets.indicesTrainingSet.includes(index), {
				i=presets.indicesTrainingSet.indexOfEqual(index);
				if (presets.mode==0, {
					views[\slider].value_(presets.value[\trainingSet][index]);
				});
				if (i!=views[\presetsTrainingSet].value, {
					views[\presetsTrainingSet].value_(i)
				});
			});
		};
	}
}