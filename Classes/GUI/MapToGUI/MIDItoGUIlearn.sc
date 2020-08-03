/*
let op! er bestaat al een MIDIFunc.learn!!!!
maar deze is beter! :-)
*/
MIDItoGUIlearn {
	var midifuncs, <midiMapping;

	*new {arg guiObject, ch=false, srcID=false, freeAfterLearn=true, post=true, action;
		^super.new.init(guiObject, ch, srcID, freeAfterLearn, post, action)
	}

	init {arg guiObject, ch, srcID, freeAfterLearn, post, action;
		if (post, {"\nlearning midi....".postln});
		midifuncs=[\noteOn, \noteOff, \control, \polytouch, \touch, \program, \bend
			, \sysex].collect{|type|
			var type2=type.copy;
			if (type2==\control, {type2=\cc});
			MIDIFunc({arg ...msg;
				if (post, {([type]++msg).postln});
				{
					if (guiObject!=nil, {
						if (guiObject.isMIDImapped.not, {
							guiObject.mapToMIDI(
								type2, msg[1]
								, if (ch, {msg[2]},{nil}), if (srcID, {msg[3]},{nil})
							);
						});
					});
					midiMapping=[type]++msg.copyToEnd(1);
					if (post, {"\nmidi learned: ".post; midiMapping.postln});
					if (action!=nil, {action.value(midiMapping)});
					if (freeAfterLearn, {
						this.free;
					});
				}.defer
			}, msgType:type)
		}
	}

	free {midifuncs.do{|m| m.free}}
}