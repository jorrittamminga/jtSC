/*
- unlearn (=remove key at trainingEvent)
*/
PresetNNGUI : GUIJT {
	var <presetNN;
	var <controlSpecs;
	var flag;

	*new {arg presetNN, parent, bounds=350@20;
		^super.new.init(presetNN, parent, bounds);
	}

	controlSpecs_ {arg argcontrolSpecs;
		argcontrolSpecs.do{|controlSpec,i|
			views[\input][i].controlSpec=controlSpec;
		};
		controlSpecs=argcontrolSpecs;
	}

	init {arg argpresetNN, argparent, argbounds;
		var widthB1, widthB2;
		flag=false;
		//font=Font(Font.defaultMonoFace, bounds.y*0.6);
		presetNN=argpresetNN;
		parent=argparent;
		bounds=argbounds;
		classJT=presetNN;
		this.initAll;
		widthB1=(bounds.x-8/4-4).floor;
		widthB2=(bounds.x-8/3-4).floor;

		views[\mode]=Button(parent, widthB1@bounds.y)
		.states_([[\learnMode,Color.white,Color.red],[\playMode,Color.white,Color.green(0.5)] ])
		.action_{|b|
			presetNN.mode=b.value;
		}.font_(font).value_(presetNN.mode);
		views[\learn]=Button(parent, widthB1@bounds.y).states_([ [\learn] ]).action_{
			presetNN.learn;
			views[\trainingEvent].items_(presetNN.trainingEvent.keys.asArray.sort);
			views[\trainingEvent].value_(
				views[\trainingEvent].items.indexOf(presetNN.preset.index)
			);
		}.font_(font);
		views[\train]=Button(parent, widthB1@bounds.y).states_([ [\train]] ).action_{|b|
			presetNN.train;
			//neuralNet.isTraining=false;
		}.font_(font);

		views[\trainingEvent]=PopUpMenu(parent, widthB1@bounds.y)
		.items_(presetNN.trainingEvent.keys.asArray.sort)
		.action_{|b|
			var value, index;
			index=b.items[b.value];
			value=presetNN.trainingEvent[index];
			flag=true;
			presetNN.preset.guis[\presetList].valueAction_(index);
			flag=false;
			value.do{|value, i|
				{views[\input][i].value_(value)}.defer;
			};
		}.font_(font);
		parent.decorator.nextLine;

		views[\save]=Button(parent, widthB1@bounds.y).states_([ ["save"] ]).action_{|b|
			presetNN.savetrainingEvent
		}.font_(font);
		views[\load]=Button(parent, widthB1@bounds.y).states_([ ["load"] ]).action_{|b|
			presetNN.loadtrainingEvent
		}.font_(font);
		views[\initTraining]=Button(parent, widthB1@bounds.y)
		.states_([ ["init training"] ]).action_{|b|
			presetNN.initTrainingEvent;
			{views[\trainingEvent].items_([])}.defer;
		}.font_(font);
		views[\initNN]=Button(parent, widthB1@bounds.y).states_([ ["init NN"] ]).action_{|b|
			presetNN.initNN(true);
		}.font_(font);

		views[\input]=presetNN.input.collect{|in,i|
			EZSlider(parent, bounds, i, ControlSpec(0.0, 1.0, \cos), {|ez|
				presetNN.input[i]=ez.value;
				//beter: addFunc / removeFunc
				if (presetNN.mode==1, {
					presetNN.calculate(presetNN.input);
				});
			}, in)
			.font_(font)
		};

		presetNN.preset.guis[\presetList].action=
		presetNN.preset.guis[\presetList].action.addFunc({|p|
			var value;
			if (views[\trainingEvent].items.includes(p.value), {
				if (flag.not, {
					views[\trainingEvent].value_(views[\trainingEvent].items.indexOf(p.value));
				});
				value=presetNN.trainingEvent[p.value];
				value.do{|value, i|
					{views[\input][i].value_(value)}.defer;
				};
			});
		});
		parent.rebounds;
	}
}