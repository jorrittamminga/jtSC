/*
TODO:
- maak een outFuncString/outFunc
- zorg dat deze outFunc ook verdwijnt als je de functie paused of unmapped
- maak meerdere MapToGUI's mogelijk voor één GUIobject (b.v. een MIDIToGUI én OSCToGUI)
- maak een gate method mogelijk zoals noteOn/noteOff combinatie
- ControlSpec van de input is nu strict lineair, kan nog geen andere curve nu....
- {arg ...args; } is toch niet zo ineffecient.... zeker bij lange msg is dit prima te doen. dit overrulen in de funcString
- maak eventueel een MIDItoGUIWatcher
- show is nog niet echt mooi....
*/
MapToGUI {
	var <>func, <responderFunc, <inval, <outval, <guiObject, <dispatcher, <argTemplate
	, <srcID, <recvPort;
	var <val, <previnval, <time, <glitch, <timeThreshold, <mode, <type, <chan
	, <num, <controlSpecIn, <controlSpecOut, <outChan, <ezGUI, <>value, <outPort;
	var actionNondeffered, actionDeffered, numberOfActions, <actionIndex;
	var <step, <id, <valueFunc;
	var <>maxGUIvalue=1.0, <maxValue, <outFunc, <funcOut;
	var <funcString, <string, <>initString, <>initFuncString;
	var <funcStringOut, <stringOut, <>initStringOut, <>initFuncStringOut;
	var prevval, <>maxval;
	var <>continuousFuncs, <>functionList, <>guiFunctionList, <mul, <add;
	var <roundIn, <roundOut, <triggerThreshold, <forceValue, <size;
	var <>guiVar, <guiRun, <method, <hasOut;
	var <inDevice, <outDevice;

	prInit {arg argguiObject, argtype, argnum, argchan, argsrcID, argargTemplate
		, argdispatcher, argcontrolSpecIn, argmode=\continuous, argglitch, argtimeThreshold
		, argoutChan, argoutPort, argguiRun=true, argshow=false
		, argmethod=\static;

		guiObject=argguiObject;
		type=argtype;
		num=argnum;
		chan=argchan;
		srcID=argsrcID;
		argTemplate=argargTemplate;
		dispatcher=argdispatcher;
		mode=argmode??{	\continuous };
		glitch=argglitch??{0};
		timeThreshold=argtimeThreshold??{0};
		outChan=argoutChan;
		hasOut=0;//1 = true
		id=UniqueID.next;
		maxGUIvalue=1.0;
		outPort=argoutPort;
		roundIn=0.0;
		roundOut=0.0;
		triggerThreshold=0;
		forceValue=1.0;
		guiRun=argguiRun;
		method=argmethod;

		this.initGUI;
		this.getGUIsettings;
		this.setUnmap(argcontrolSpecIn);
		this.setMap;

		if (value==nil, {value=1.0});
		if (value.size>1, {value=value.collect{|v| if (v==nil, {1},{v})}});
		if (mul!=nil, {if (add==nil, {add=0})});

		initString=string;
		initFuncString=funcString;

		if (argshow, { {this.addStaticText}.defer; });
		//this.makeFunc(string, funcString);
	}

	initGUI {
		ezGUI=guiObject.isKindOf(EZGui);
		//============================================ guiVar
		if (ezGUI, {
			controlSpecOut=guiObject.controlSpec;
			maxGUIvalue=controlSpecOut.maxval;
			guiVar=guiObject.alwaysOnTop;
		},{
			guiVar=guiObject.dragLabel;
		});

		if (guiVar.class!=Event, {
			guiVar=();
		});
		if (guiVar[this.class.asSymbol]==nil, {
			guiVar[this.class.asSymbol]=();
		});
		guiVar[this.class.asSymbol][id]=this;

		if (ezGUI, {
			guiObject.alwaysOnTop=guiVar
		},{
			guiObject.dragLabel=guiVar
		});
		//============================================ ADD TO onClose
		if (guiObject.onClose==nil, {
			guiObject.onClose_({responderFunc.asArray.do(_.free)})
		},{
			guiObject.onClose_(guiObject.onClose.addFunc({
				responderFunc.asArray.do(_.free);
			});
			)
		});
	}

	getGUIsettings {
		value=guiObject.value;
		size=value.size;
		//============================================
		if (guiObject.class==Button, {
			maxGUIvalue=(guiObject.states.size-1).max(1.0);
			roundOut=1.0;
			step=1;
		});
		if ((guiObject.class==PopUpMenu)||(guiObject.class==ListView), {
			maxGUIvalue=guiObject.items.size-1;
			roundOut=1.0;
			step=1;
		});
	}


	setUnmap {arg controlSpec;
		var min=0, max=1.0;
		var minval, maxval;
		var reverse=false, tmp;

		if (
			//(controlSpec.class==Array) ||
			(controlSpec.class==SegWarp), {
			controlSpecIn=controlSpec;
		});

		if (controlSpec!=nil, {
			switch(controlSpec.class, ControlSpec, {
				minval=controlSpec.minval;
				maxval=controlSpec.maxval;
			}, SegWarp, {
				//controlSpecIn=controlSpec;
				minval=controlSpec.env.levels.minItem;
				maxval=controlSpec.env.levels.maxItem;
			}, Array, {
				#minval, maxval=controlSpec.collect{|cs,i|
					if (cs.class==SegWarp, {
						controlSpecIn=controlSpec;
						[cs.env.levels.minItem,cs.env.levels.maxItem]
					},{
						[cs.warp.spec.minval,cs.warp.spec.maxval]
					})
				}.flop;
			},
			{
				minval=controlSpec.warp.spec.minval;
				maxval=controlSpec.warp.spec.maxval;
			});

			if (minval.asArray.sum>maxval.asArray.sum, {
				reverse=true;
				tmp=minval;
				minval=maxval;
				maxval=tmp;
			});
			if (controlSpecIn!=nil, {
				min=0.0;//improve! TODO
				max=1.0;//improve! TODO
				mul=(1/maxValue);
				add=0;
			},{
				if (controlSpecOut!=nil, {
					min=guiObject.controlSpec.unmap(minval);
					max=guiObject.controlSpec.unmap(maxval);
				},{
					min=minval*maxGUIvalue;//maxGUIvalue
					max=maxval*maxGUIvalue;//maxGUIvalue
				});
				if (reverse, {
					mul=(max-min)*(1/maxValue).neg;
					add=max;
				},{
					mul=(max-min)*(1/maxValue);
					add=min;
				});
			});
		});
	}

	setMap {arg controlSpec;
		if (guiObject.isKindOf(EZGui), {
			ezGUI=true;
			if (controlSpec!=nil, {
				if (controlSpec!=guiObject.controlSpec, {
					guiObject.controlSpec_(controlSpec)
				});
			});
			step=guiObject.controlSpec.step;
			controlSpecOut=guiObject.controlSpec;
			if (method!=\dynamic, {
				if (controlSpecOut.step>0.00001, {
					roundOut=controlSpecOut.step;
				});
				if (controlSpecOut.warp.class==LinearWarp, {
					if (mul==nil, {
						mul=controlSpecOut.maxval-controlSpecOut.minval;
						add=controlSpecOut.minval;
					},{
						mul=(controlSpecOut.maxval-controlSpecOut.minval)*mul;
						add=controlSpecOut.minval+(add??{0});//HIER GAAT HET FOUT
					});
					controlSpecOut=nil;
				});
			});
		},{
			if (maxGUIvalue.asFloat!=1.0, {
				if (mul==nil, {
					mul=maxGUIvalue;
				},{
					mul=maxGUIvalue*mul;
				});
			});
		});
		maxval=if (controlSpecOut.class==ControlSpec, {
			controlSpecOut.maxval},{maxGUIvalue});
	}

	//setControlSpec : setMap

	putInValue {arg func, index;
		if (size>1, {
			func={arg func;
				{arg val, num;
					val=func.value(val);
					value.put(index, val);
					val=value;
					val
				}
			}.value(func);
		});
		^func
	}

	//======================================================== SETTERS
	timeThreshold_ {arg val; timeThreshold=val;
		if (method==\static, { this.updateFunc(initString,initFuncString);})
	}

	glitch_ {arg val; glitch=val;
		if (method==\static, {this.updateFunc(initString,initFuncString)})
	}

	triggerThreshold_{arg val; triggerThreshold=val;
		if (method==\static, {this.updateFunc(initString,initFuncString)})
	}

	forceValue_ {arg val; forceValue=val;
		if (method==\static, {this.updateFunc(initString,initFuncString)})
	}

	step_ {arg val; step=val;
		if (method==\static, {this.updateFunc(initString,initFuncString)})
	}

	guiRun_ {arg flag; var gr=guiRun;
		guiRun=flag;
		if (method==\static, {
			if (flag!=gr, {this.update})
		})
	}

	method_ {arg m; method=m; this.update; }

	//========================================================
	update {
		responderFunc.asArray.do(_.free);
		this.getGUIsettings;
		this.setUnmap;// {arg argcontrolSpecIn, argMulIn;
		this.setMap;
		this.makeFunc(initString, initFuncString);//func=
		this.addResponderFunc;
	}

	updateFunc {arg initString, initFuncString;
		responderFunc.asArray.do(_.free);
		this.makeFunc(initString, initFuncString);//func=
		this.addResponderFunc;
	}

	addResponderFunc {}

	free {
		responderFunc.asArray.do(_.free);
		guiVar[this.class.asSymbol].removeAt(id);
	}

	pause {	responderFunc.asArray.do(_.free);}

	resume {this.addResponderFunc }

	//========================================================
	addStaticText {
		var bounds, shift=guiObject.bounds.height, width=shift.copy, view;
		var nrString="", chString="", string="";
		var guis;

		if (ezGUI, {
			if (num!=nil, {string=string++num.asCompileString});
			if (chan!=nil, {string=string++chan.asCompileString});
			guiObject.unitView.string_(string);
		},{
			/*
			guis=[guiObject];
			num.asArray.do{|nr,i| nrString=nrString++nr;
			if (i<(num.asArray.size-1), {nrString=nrString++"\n"})};
			chan.asArray.do{|nr,i| chString=chString++nr;
			if (i<(chan.asArray.size-1), {chString=chString++"\n"})};

			view=if (ezGUI, {
			guis=[guiObject.sliderView, guiObject.numberView];
			guiObject.view;
			},{
			shift=shift*2;
			guiObject.parent;
			});

			guis.do{|i, k|
			var b=i.bounds;
			if (k>0, {b.left=b.left-(shift)});
			b.width=b.width-(shift);
			bounds=b.copy;
			i.bounds_(b);
			};
			bounds=[
			Rect(bounds.left+bounds.width, bounds.top, width, bounds.height),
			Rect(bounds.left+(bounds.width+width), bounds.top, width, bounds.height);
			];

			StaticText(view, bounds[0]).string_(nrString)
			.font_(Font("Monoca", width*0.8*num.asArray.size.reciprocal))
			.background_(Color.black).stringColor_(Color.white).align_(\left);
			StaticText(view, bounds[1]).string_(chString)
			.font_(Font("Monoca", width*0.8*chan.asArray.size.reciprocal))
			.background_(Color.black).stringColor_(Color.white).align_(\right);
			*/
		})
	}
}
