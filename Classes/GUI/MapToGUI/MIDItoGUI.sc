/*
TODO:
- maak het mogelijk om de naam van de outport in te geven of deze gelijk te maken aan de MIDIIn port
- er kan een MIDIOut loop ontstaan bij IAC driver
- zorg bij unmap dat de MIDIOut ook verdwijnt van deze gui
- MSB/LSB optie
*/
MIDItoGUI : MapToGUI {

	var midiThru;

	*new {arg guiObject, type, num, chan, midiInPort, argTemplate, dispatcher, controlSpec
		, mode=\continuous, glitch, timeThreshold, midiOutChan, midiOutPort
		, guiRun=true, show=false, method=\static, midiThru=true;
		^super.new.init(guiObject, type, num, chan, midiInPort, argTemplate, dispatcher
			, controlSpec, mode, glitch, timeThreshold, midiOutChan, midiOutPort
			, guiRun, show, method, midiThru)
	}

	init {arg argguiObject, argtype, argnum, argchan, argmidiInPort, argargTemplate
		, argdispatcher, argcontrolSpec, argmode=\continuous, argglitch, argtimeThreshold
		, argmidiOutChan, argmidiOutPort, argguiRun, argshow=false
		, argmethod=\static, midiThru;
		//------------------------------------- init MIDI
		maxValue=127.0;
		mul=1/maxValue;
		if (argtype==\cc, {argtype=\control});
		if (argtype==\bend, {maxValue=16383; mul=1/maxValue; });

		funcString="{arg val, number;";
		if ([\touch, \bend, \program].includes(argtype), {num=nil});
		this.prInit(argguiObject, argtype, argnum, argchan, argmidiInPort, argargTemplate
			, argdispatcher, argcontrolSpec, argmode, argglitch, argtimeThreshold
			, argmidiOutChan, argmidiOutPort, argguiRun, argshow
			, argmethod);
		this.makeFunc(string, funcString);
		//------------------------------------- MIDI OUT support
		inDevice=if (srcID!=nil, { [MIDIClient.sources[srcID].device]},{
			MIDIClient.sources.collect(_.device)
		});
		if (outPort!=nil, {
			if (outPort==\inPort, {
				if (srcID!=nil, {
					MIDIClient.destinations.do{|mc, i|
						if ( (mc.device==MIDIClient.sources[6].device) &&
							(mc.name==MIDIClient.sources[6].name), {outPort=i})};
				})
			});
			outDevice=MIDIClient.destinations[outPort].device;

			if ((outDevice=="IAC Driver")  && (inDevice.includesEqual("IAC Driver")), {
				"IAC Driver can cause feedback. Please choose another MIDI outport or MIDI inport".postln;
			},{
				if (midiThru, {hasOut=0},{hasOut=1});
				if (outChan==nil, {outChan=0});
				this.addMIDIOut
			});
		});
		//------------------------------------- MIDIFunc
		this.addResponderFunc;
	}

	makeFunc {arg initString, initFuncString;
		if (method==\static, {
			func=size.max(1).collect{|index|
				this.makeFuncStatic(string, funcString, index)};
		},{
			func=size.max(1).collect{|index| this.makeFuncDynamic(index
				, mul.asArray.wrapAt(index), add.asArray.wrapAt(index))};
		});
	}

	addResponderFunc {//arg argfunc, argnum, argchan, type, srcID, argTemplate, dispatcher;
		responderFunc=size.max(1).collect{|index|
			MIDIFunc.new(
				func[index]
				//{arg ...msg; msg.postln}
				, if (mode==\plusminus, {[num].asArray.wrapAt(index)},{
					num.asArray.wrapAt(index)})
				, chan.asArray.wrapAt(index)
				, type.asArray.wrapAt(index)
				, if (srcID.asArray.wrapAt(index)!=nil, {
					MIDIClient.sources[srcID.asArray.wrapAt(index)].uid
				},{nil})
				, argTemplate.asArray.wrapAt(index)
				, dispatcher.asArray.wrapAt(index))
		}
	}
	/*
	outFuncString {arg outPort, outChan, num;
	outFuncString=outFuncString++"var outPort";
	^switch(type, \noteOn, {
	format("MIDIOut(%).write(3, 16r90, %, %, val)", outPort, outChan.asInteger
	, num.asInteger)
	}, \noteOff, {
	format("MIDIOut(%).write(3, 16r90, %, %, val)", outPort, outChan.asInteger
	, num.asInteger)
	}, \polyTouch, {
	format("MIDIOut(%).write(3, 16rA0, %, %, val)", outPort, outChan.asInteger
	, num.asInteger)
	}, \control, {
	format("MIDIOut(%).write(3, 16rB0, %, %, val)", outPort, outChan.asInteger
	, num.asInteger)
	}, \program, {
	format("MIDIOut(%).write(2, 16rC0, %, val)", outPort, outChan.asInteger)
	}, \touch, {
	format("MIDIOut(%).write(2, 16r90, %, val)", outPort, outChan.asInteger)
	}, \bend, {
	format("MIDIOut(%).write(3, 16r90, %, val bitAnd: 127, val >> 7)"
	, outPort, outChan.asInteger)
	});
	}
	*/
	addMIDIOut {
		var val;
		/*
		funcOut={arg gui;
		val=guiObject.controlSpec.unmap(gui.value);
		val=(val-add) * mul.reciprocal
		};
		*/
		outFunc=
		//val=funcOut.value(ez.value);
		//midiOutFunc.value(val);
		switch(type, \noteOn, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(3, 16r90
				, outChan.asInteger, num.asInteger, val)}
		}, \noteOff, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(3, 16r90
				, outChan.asInteger, num.asInteger, val)}
		}, \polyTouch, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(3, 16rA0
				, outChan.asInteger, num.asInteger, val)}
		}, \control, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(3, 16rB0
				, outChan.asInteger, num.asInteger, val)}
		}, \program, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(2, 16rC0
				, outChan.asInteger, val)}
		}, \touch, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(2, 16r90
				, outChan.asInteger, val)}
		}, \bend, {{|val| val=funcOut.value(val);
			MIDIOut(outPort).write(3, 16r90
				, outChan.asInteger, val bitAnd: 127, val >> 7)}
		});
		guiObject.action=guiObject.action.addFuncFirst( outFunc );
	}
}


+ EZGui {
	mapToMIDI {arg type=\cc, num, chan, srcID, argTemplate, dispatcher, controlSpec
		, mode=\continuous, glitch, timeThreshold, midiOutChan, midiOutPort
		, guiRun=true, show=false, method=\dynamic, midiThru=false;
		var midiMap=this.midiMap, index;
		if (midiMap!=nil, {
			if (midiMap.class==MIDItoGUI, {midiMap=[midiMap]});
			midiMap.do{|mm,i|
				if ([if (type==\cc, {\control},{type}), num, chan, srcID]==[mm.type, mm.num, mm.chan, mm.srcID], {
					index=i;
				})
			};
		});
		^if (index!=nil, {
			"EZGui is already mapped to MIDI with the same settings".postln;
			midiMap[index]
		},{
			MIDItoGUI.new(this, type, num, chan, srcID, argTemplate, dispatcher, controlSpec
				, mode, glitch, timeThreshold, midiOutChan, midiOutPort, guiRun
				, show, method, midiThru)
		})
	}
	midiMap {arg id;
		var ids;
		^if (this.alwaysOnTop!=nil, {
			if (this.alwaysOnTop.class==Event, {
				ids=this.alwaysOnTop[\MIDItoGUI].keys.asArray.sort;
				id=id??{ids};
				id=id.asArray;
				id.collect{|id,i|
					if (id<1000, {id=ids[i]}); this.alwaysOnTop[\MIDItoGUI][id]}.unbubble
			},{
				nil
			})
		},{
			nil
		})
	}
	unmapToMIDI {arg id;
		if (id==nil, { this.midiMap.do(_.free); },{ this.midiMap(id).do(_.free); })
	}
	pauseMIDImap {//arg id;
		this.midiMap.pause//this.midiMap(id).do(_.pause)
	}
	resumeMIDImap {//arg id;
		this.midiMap.resume//this.midiMap(id).do(_.pause)
	}
	isMIDImapped {
		^(if (this.alwaysOnTop.class==Event, {
			this.alwaysOnTop[\MIDItoGUI].class==Event
		},{false}))
		//^(this.alwaysOnTop.class==MIDItoGUI)
	}
	learnMIDI {arg ch=true, srcID=false, post=true, action;
		^MIDItoGUIlearn(this, ch, srcID, true, post, action) }
}


+ View {
	mapToMIDI {arg type=\noteOn, num, chan, srcID, argTemplate, dispatcher, controlSpec
		, mode=\toggle, glitch, timeThreshold, midiOutChan, midiOutPort
		, guiRun=true, show=false, method=\dynamic, midiThru=false;
		var midiMap=this.midiMap, index;
		if (midiMap!=nil, {
			if (midiMap.class==MIDItoGUI, {midiMap=[midiMap]});
			midiMap.do{|mm,i|
				if ([if (type==\cc, {\control},{type}), num, chan, srcID]==[mm.type, mm.num, mm.chan, mm.srcID], {
					index=i;
				})
			};
		});
		^if (index!=nil, {
			"EZGui is already mapped to MIDI with the same settings".postln;
			midiMap[index]
		},{
			MIDItoGUI.new(this, type, num, chan, srcID, argTemplate, dispatcher, controlSpec
				, mode, glitch, timeThreshold, midiOutChan, midiOutPort, guiRun
				, show, method, midiThru)
		})
	}
	unmapToMIDI {arg id;
		if (id==nil, { this.midiMap.do(_.free); },{ this.midiMap(id).do(_.free); })
	}
	midiMap {arg id;
		var ids;
		^if (this.dragLabel!=nil, {
			if (this.dragLabel.class==Event, {
				ids=this.dragLabel[\MIDItoGUI].keys.asArray.sort;
				id=id??{ids};
				id=id.asArray;
				id.collect{|id,i|
					if (id<1000, {id=ids[i]}); this.dragLabel[\MIDItoGUI][id]}.unbubble
			},{
				nil
			})
		},{
			nil
		})
	}
	pauseMIDImap { this.midiMap.pause }
	resumeMIDImap { this.midiMap.resume }
	isMIDImapped {
		^(if (this.dragLabel.class==Event, {
			this.dragLabel[\MIDItoGUI].class==Event
		},{false}))
		//^(this.alwaysOnTop.class==MIDItoGUI)
	}
	learnMIDI {arg ch=true, srcID=false, post=true, action;
		^MIDItoGUIlearn(this, ch, srcID, true, post, action) }
}