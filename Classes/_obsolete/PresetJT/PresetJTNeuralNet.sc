PresetJTNeuralNet : PresetJTCollection {
	classvar <numberOfInputs;
	var <nn, <trainingSet, trainingSetIndices, <nin, <path, <>learningrate=0.05, <>initweight=0.05
	, <>errortarget=0.00001, <>maxepochs=100000;
	var <nout, <nhidden, <>input;
	var <dirnameNN;
	var <>valuesIndex=0;

	*new { arg preset, nin;
		var objects, dirname;
		if (preset.dirname!=nil, {
			dirname=preset.dirname++"TrainingSets/";
		});
		//objects=indices??{(0..preset.array.size-1)};
		objects=(0..preset.array.size-1);
		numberOfInputs=nin;
		^this.basicNew(objects, dirname, preset)
		//^super.new.prInitNeuralNet(preset, nin)
	}
	prInit {arg preset;
		presetJT=preset;
		dirnameNN=PathName(dirname).moveDir(1).fullPath++"NeuralNets/";
		if (File.exists(dirnameNN).not, {File.mkdir(dirnameNN)});
		values=();
		nin=numberOfInputs;
		restoreAction={
			if (values!=nil, {
				nin=values[values.keys.asArray.sort[0]].size??{numberOfInputs};
				nout=presetJT.objects.sortedClumps.sum;
				nhidden=nin.max(nout);
				this.initNN;
				input=input??{{0}!nin};//alleen als nin is veranderd? of beter nog: update input?
			})
		};
		//--------------------------------------------------------- PresetJTNeuralNet functions
		func[\add]=func[\add].addFunc({arg index, presetJTNN;
			this.initTrainingSet;
			this.initNN(false);
		});
		func[\name]=func[\name].addFunc({arg index, this, order, prevIndex, prevName, newName;
			var pathOldNN=dirnameNN++prevName++".scmirZ";
			var pathNewNN=dirnameNN++newName++".scmirZ";
			if (File.exists(pathOldNN), {
				("mv " ++ pathOldNN ++ " " ++ pathNewNN).unixCmd;
			});
		});
		//--------------------------------------------------------- PRESETJT funcs
		presetJT.func[\store]=presetJT.func[\store].addFunc({
			if (values.includes(presetJT.index), {
				this.prMakePresetArray;
			})
		});
		presetJT.func[\add]=presetJT.func[\add].addFunc({arg i;
			//array=array.deepCollect(0x7FFFFFFF, {arg val; if (val>=i, {val+1},{val})   });
			array.do{arg values;
				var indices, index;
				if (values!=nil, {
					indices=values.keys.asArray.sort;
					index=indices.indexInBetween(i).ceil.asInteger;
					indices.copyToEnd(index).reverse.do{arg i;
						values[i+1]=values[i]; values.removeAt(i);
					}
				});
			};
			this.saveAll;
			func[\unlearn].value(i);
		});
		presetJT.func[\removeAt]=presetJT.func[\removeAt].addFunc({arg i;
			//array=array.deepCollect(0x7FFFFFFF, {arg val; if (val>i, {val-1},{if (val==i, {nil},{val})   }) });
			array.do{arg values;
				var indices, index;
				if (values!=nil, {
					indices=values.keys.asArray.sort;
					index=indices.indexInBetween(i).ceil.asInteger;
					indices.copyToEnd(index).do{arg i;
						if (index!=i, {
							values[i-1]=values[i];
						});
						values.removeAt(i);
					}
				});
			};
			//array=array.asCompileString.replace("nil, ", "").replace(", nil", "").interpret;
			this.saveAll;
			//this.restore;
			func[\unlearn].value(i);
		});
		presetJT.func[\name]=presetJT.func[\name].addFunc({arg i, preset, order;
			//array=array.deepCollect(0x7FFFFFFF, {arg val; order[val]   });
			array.do{arg values; var tmpValues=values.deepCopy;
				if (values!=nil, {
					order.do{arg newIndex, oldIndex;
						if (newIndex!=oldIndex, {
							if (values[oldIndex]!=nil, {
								values[newIndex]=tmpValues[oldIndex]
							})
						})
					};
				});
			};
			this.saveAll;
			//this.restore;
			func[\unlearn].value(i);
		});
	}
	nin_ {arg numberOfInputs;
		nin=numberOfInputs.asInteger;
		func[\nin].value(nin, this);
	}
	getValues {//dummy function
		//values;//=objects.collect{|o| o.value};
	}
	unlearn {arg index;
		var indices;
		values.removeAt(index);
		indices=values.keys.asArray.sort;
		valuesIndex=indices.clipAt(index)??{0};
		this.store;
		func[\unlearn].value(valuesIndex);
	}
	//replace {arg oldIndex, newIndex;}
	learn {arg index, inp;
		inp=inp??{input};
		input=inp;
		if (input.size!=nin, {
			"input is not equal to the number of inputs of the neuralnet!".postln;
			if (input.size<nin, {
				input=input.copyRange(0, nin-1);
			},{
				input=input++Array.fill(nin-input.size, 0);
			})
		});
		valuesIndex=index;
		values[index]=input.copy;
		presetJT.store;
		this.store;
		func[\learn].value(index);
	}
	initTrainingSet {
		values=();
	}
	train {arg autoSave=true;
		var pathNN=dirnameNN++name++".scmirZ";
		trainingSet=[];
		values.sortedKeysValuesDo{|index, input|
			trainingSet=trainingSet.add([input, presetJT.objects.unmap(presetJT.array[index])]);
		};
		"start trainig....".postln;
		if (trainingSet.size>0, {
			nn.trainExt(trainingSet, errortarget, maxepochs);
			if (autoSave, {nn.save(pathNN)})
		});
		this.calculate(input);
		"training is finished".postln;
	}
	initNN {arg autoLoad=true;
		var pathNN=dirnameNN++name++".scmirZ";
		"initNN ".post; pathNN.postln;
		nn=NeuralNetJT(nin, nhidden, nout, learningrate, initweight);
		if (autoLoad, {
			if (File.exists(pathNN), {
				"load pathNN".postln;
				nn.load(pathNN);
				this.nin_(nn.nin);
			});
		});
	}
	calculate {arg input;
		var out;
		out=nn.calculate(input);
		out=presetJT.objects.map(out);
		presetJT.values_(out);
		^out
	}
	makeGui {arg parent, bounds;
		{gui=PresetJTNeuralNetGUI(this, parent, bounds)}.defer
	}
}

PresetJTNeuralNetGUI : PresetJTGUI {
	var <mode=1, slidersCompositieView;

	makeFaders {arg n=3;
		var in={0}!n;
		var boundz=(bounds.x)@(bounds.y*3/n);
		slidersCompositieView.removeAll;
		slidersCompositieView.decorator.reset;
		^n.collect{|i|
			EZSlider(slidersCompositieView, boundz, i, ControlSpec(0.0, 1.0), {|ez|
				in[i]=ez.value;
				presetJT.input=in;
				if (mode==1, {
					presetJT.calculate(in);
				},{
					//presetJT.input=in;
				})
			})
		}
	}
	prInit {
		compositeView=CompositeView(parent, bounds.x@(bounds.y*5)); compositeView.addFlowLayout(0@0,0@0); compositeView.background_(Color.grey);
		views[\mode]=Button(compositeView, (bounds.y*3)@bounds.y).states_([["learn", Color.white, Color.red],["calculate", Color.black, Color.green]]).action_{arg b;
			"in views[mode] ".post; b.value.postln;
			mode=b.value;
			if (mode==1, {
				presetJT.calculate(presetJT.input)
			});
		}.value_(mode);
		views[\nin]=EZNumber(compositeView, (bounds.y*2)@bounds.y, \nin, ControlSpec(1, 16, 0, 1), {|ez|
			//presetJT.nin=ez.value;
		}, presetJT.nin, false, bounds.y);
		views[\storeNN]=Button(compositeView, (bounds.y)@bounds.y).states_([["s"]]).action_{
			presetJT.learn(
				//presetJT.valuesIndex,
				presetJT.presetJT.index,
				presetJT.input);
		};//learn is store!
		/*
		views[\replace]=Button(compositeView, (bounds.y)@bounds.y).states_([["r"]]).action_{
		//kan ook replace zijn!
		//presetJT.learn(presetJT.valuesIndex, presetJT.input);};//learn is store!
		};//restore
		views[\addNN]=Button(compositeView, (bounds.y)@bounds.y).states_([["+"]]).action_{
		presetJT.learn(presetJT.presetJT.index, presetJT.input);
		};
		*/
		views[\removeNN]=Button(compositeView, (bounds.y)@bounds.y).states_([["-"]]).action_{
			presetJT.unlearn(presetJT.valuesIndex);
		};
		views[\listNN]=PopUpMenu(compositeView, (bounds.y*5)@bounds.y).items_(
			presetJT.values.keys.asArray.sort.collect{|presetID|
				presetJT.presetJT.names[presetID]
			};
		).action_{arg pop;
			var index;
			"views[listNN] action ".post; pop.value.postln;
			index=presetJT.values.keys.asArray.sort[pop.value];
			presetJT.values[index].do{|val,i| views[\inputFaders][i].value_(val)};
			"presetJT.presetJT.restore ".post; index.postln;
			presetJT.presetJT.restore(index);
			presetJT.valuesIndex=index;
			//{views[\mode].valueAction_(0)}.defer;
		}.canFocus_(false).font_(font);

		views[\train]=Button(compositeView, (bounds.y*2)@bounds.y).states_([[\train]]).action_{
			presetJT.train;
			"views[train] action ".postln;
			{views[\mode].valueAction_(1)}.defer;
		};
		views[\initNN]=Button(compositeView, (bounds.y*2)@bounds.y).states_([["init"]]).action_{
			if (views[\nin].value!=presetJT.nin, {
				presetJT.nin_(views[\nin].value);
			});
			presetJT.initNN(false);
		};
		slidersCompositieView=CompositeView(compositeView, bounds.x@(bounds.y*3)); slidersCompositieView.addFlowLayout(0@0,0@0);
		views[\inputFaders]=this.makeFaders(presetJT.nin);
		if (views[\listNN].items.size>0, {
			views[\listNN].valueAction_(0);//
		});
		compositeView.decorator.nextLine;
		//-------------------------------------------------------------------- PRESETJT functions
		[\restore].do{|key|
			presetJT.func[key]=presetJT.func[key].addFunc({
				"presetJT.func restore".postln;
				{
					if (views[\listNN].items.size>0, {
						"presetJT.func[restore] ".postln;
						//views[\mode].valueAction_(0);
						views[\listNN].items=presetJT.values.keys.asArray.sort.collect{|presetID|
							presetJT.presetJT.names[presetID]
						};
						//views[\listNN].valueAction_( 0 )
						views[\listNN].value_( 0 )
					})
				}.defer
			});
		};
		presetJT.func[\nin]=presetJT.func[\nin].addFunc({arg n;
			{views[\nin].value_(n)}.defer;
			if (views[\inputFaders].size!=n, {
				"this.makeFaders ".post; n.postln;
				{views[\inputFaders]=this.makeFaders(n)}.defer
			});
		});
		presetJT.func[\learn]=presetJT.func[\learn].addFunc({arg index;
			//if (presetJT.values.keys.includesEqual(index).not, {
			"learn func GUI".postln;
			{
				views[\mode].valueAction_(0);
				views[\listNN].items=presetJT.values.keys.asArray.sort.collect{|presetID|
					presetJT.presetJT.names[presetID]
				};
				views[\listNN].value_( presetJT.values.keys.asArray.sort.indexOf(index))
			}.defer
			//});
		});
		presetJT.func[\unlearn]=presetJT.func[\unlearn].addFunc({arg index;
			"unlearn func GUI".postln;
			{
				views[\mode].valueAction_(0);
				views[\listNN].items=presetJT.values.keys.asArray.sort.collect{|presetID|
					presetJT.presetJT.names[presetID]
				};
				//views[\listNN].valueAction_( presetJT.values.keys.asArray.sort.indexOf(index))
			}.defer
		});
		views[\mode].valueAction_(1);
	}
}