/*
- maak TouchOSC shortcuts (en eventueel Lemur) of een method mapToTouchOSC
- maak de lengte van de osc msg een variabele, laat het ergens vanaf hangen
- maak net als bij MIDItoGUI ook een OSCOut functie
- zorg bij unmap dat de OSCOut ook verdwijnt
*/
//arg guiObject, type, num, chan, midiInPort, argTemplate, dispatcher, controlSpec
//		, mode=\continuous, glitch, timeThreshold, midiOutChan, midiOutPort
//		, guiRun=true, show=false, method=\static, midiThru=true;

OSCtoGUI : MapToGUI {
	var <netAddr;

	*new {arg guiObject, type, num, chan, srcID, argTemplate, dispatcher, controlSpec
		, mode=\continuous, glitch, timeThreshold, netAddr, guiRun=true, show=false
		, method=\dynamic;

		^super.new.init(guiObject, type, num, chan, srcID, argTemplate, dispatcher, controlSpec
			, mode, glitch, timeThreshold, netAddr, guiRun=true, show, method)
	}

	init {arg argguiObject, argtype, argnum, argchan, argsrcID, argargTemplate, argdispatcher
		, argcontrolSpec, argmode=\continuous, argglitch, argtimeThreshold, argnetAddr
		, argguiRun=true, argshow=false, argmethod;

		//maxValue=127.0;
		//mul=1.0;

		this.prInit(argguiObject, argtype, argnum, argchan
			, argsrcID//argmidiInPort
			, argargTemplate
			, argdispatcher, argcontrolSpec, argmode, argglitch, argtimeThreshold
			//, argmidiOutChan, argmidiOutPort
			, nil, nil
			, argguiRun, argshow, argmethod);
		funcString="{arg msg; var val=msg[1]; ";
		this.makeFunc(string, funcString);
		this.addResponderFunc;

		if (argnetAddr.class==NetAddr, {
			hasOut=1;
			netAddr=argnetAddr;
			this.addOSCOut
		});

	}

	addResponderFunc {
		var ttype;
		ttype=if (type.class==Array, {type},{[type]});
		responderFunc=size.max(1).collect{|index|
			OSCFunc(
				func[index]
				//{arg msg; msg.postln}
				, ttype.wrapAt(index)
				, srcID.asArray.wrapAt(index)
				, chan.asArray.wrapAt(index)
				, argTemplate.asArray.wrapAt(index)
				, dispatcher.asArray.wrapAt(index)
			)
		}

	}

	addOSCOut {
		//guiObject.action=guiObject.action.addFuncFirst( outFunc );
		outFunc=if (controlSpecOut!=nil, {
			{|ez| netAddr.sendMsg(type, controlSpecOut.unmap(ez.value))};
		},{
			{|ez| netAddr.sendMsg(type, ez.value)};
		});
		guiObject.action=guiObject.action.addFuncFirst(outFunc);
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

}


+ EZGui {
	mapToOSC {arg type='/1/fader1', srcID, recvPort, argTemplate, dispatcher, controlSpec
		, mode=\continuous, glitch, timeThreshold, netAddr, guiRun=true, show=false
		, method=\dynamic;
		^OSCtoGUI.new(this, type, nil, recvPort, srcID, argTemplate, dispatcher, controlSpec
			, mode, glitch, timeThreshold, netAddr, guiRun, show, method)
	}

	unmapToOSC {
		//	this.alwaysOnTop.responderFunc.free;
		//	this.alwaysOnTop=false;
	}
}

+ View {
	mapToOSC {arg type='/1/fader1', srcID, recvPort, argTemplate, dispatcher, controlSpec
		, mode=\continuous, glitch, timeThreshold, netAddr, guiRun=true, show=false
		, method=\dynamic;
		^OSCtoGUI.new(this, type, nil, recvPort, srcID, argTemplate, dispatcher, controlSpec
			, mode, glitch, timeThreshold, netAddr, guiRun, show, method)
	}

	unmapToOSC {
		//	this.alwaysOnTop.responderFunc.free;
		//	this.alwaysOnTop=false;
	}
}