+ SimpleMIDIFile {
	barToTicks {arg bar, offset= 1;
		var env;
		env=this.barToTicksEnv;
		^env.at(bar-offset).round(1.0).asInteger
	}
	barToTime {arg bar, offset=1; ^this.barToTicks(bar, offset)}
	timeSignatureAtTicks {arg ticks;
		var timeSignatures=this.timeSignatures;
		var tick, ts;
		#tick, ts = timeSignatures.flop;
		^ts[tick.indexInBetween(ticks).floor.asInteger]
	}
	timeSignatureAtTime {arg time;
		^this.timeSignatureAtTicks(time)
	}
	timeSignatureAtBar {arg bar, offset=1;
		var ticks;
		ticks=this.barToTicks(bar, offset);
		^this.timeSignatureAtTicks(ticks)
	}
	barToTicksEnv {
		var env, timeSignatures, flop, ticks, ts, lastTicks, bars;
		var length=this.length;
		timeSignatures=this.timeSignatures;
		#ticks, timeSignatures=timeSignatures.flop;
		timeSignatures=timeSignatures.collect{|t| t.interpret*4};
		lastTicks=((length-ticks.last)/(timeSignatures.last*division)).ceil;
		lastTicks=lastTicks*(timeSignatures.last*division)+ticks.last;
		ticks=ticks.add(lastTicks);
		bars=ticks.differentiate.copyToEnd(1);
		bars=bars/division/timeSignatures;
		^Env(ticks, bars)
	}
	//TODO: add a note off message if there is no, and add 'sustained' note(s)
	//[ 5, 156480, noteOn, 4, 40, 80 ]
	copyRange {arg startTicks, endTicks;
		var events, allPreviousEvents, usedTracks=[], usedChannels=[], allTracks, unusedTracks;
		var noteOns={{[]}!17}!16, maxTicks, prevNotes=();
		events=this.midiEvents.select({arg event; (event[1]>=startTicks) && (event[1]<endTicks)}).deepCopy;
		usedTracks=events.flop[0].asSet.asArray.sort;
		//-------------------------------------------------------------------------------- check for tied notes, if there are no notes...
		allPreviousEvents=this.midiEvents.select({arg event; (event[1]>=0) && (event[1]<startTicks)}).deepCopy;
		allTracks=allPreviousEvents.flop[0].asSet.asArray.sort;
		unusedTracks=allTracks.difference(usedTracks);
		allPreviousEvents.do{|event|
			if (unusedTracks.includes(event[0])) {
				if (event[2]==\noteOn) {
					if (event[5]>0) {
						if (prevNotes[event[0]]==nil) {prevNotes[event[0]]=()};
						//[\noteOn, event[4], event[0], event[3]].postln;
						if (prevNotes[event[0]][event[3]]==nil) {prevNotes[event[0]][event[3]]=[]};
						prevNotes[event[0]][event[3]]=prevNotes[event[0]][event[3]].add(event[4])
					} {
						//[\noteOff, event[4], event[0], event[3]].postln;
						prevNotes[event[0]][event[3]].remove(event[4])
					}
				};
				if (event[2]==\noteOff) {
					prevNotes[event[0]][event[3]].remove(event[4])
				}
			}
		};
		prevNotes.sortedKeysValuesDo{|track, notes|
			notes.sortedKeysValuesDo{|ch, notes|
				notes.do{|note, i|
					events=events.addFirst([track, startTicks, \noteOn, ch, note, 80]);//postln
					//events=events.add([track, endTicks, \noteOn, ch, note, 0]);
				}
			}
		};
		//-------------------------------------------------------------------------------- check for tied notes
		if (events.size>0) {
			//-------------------------------------------------------------------------------- give long notes a note off
			maxTicks=events.flop[1].maxItem;
			events.do{|event|
				if (event[2]==\noteOn) {
					if (event[5]>0) {
						noteOns[event[0]][event[3]]=noteOns[event[0]][event[3]].add(event[4])
					} {
						noteOns[event[0]][event[3]].removeEvery(event[4].asArray)
					}
				}
			};
			noteOns.do{|noteOns,track|
				noteOns.do{|noteOns, ch|
					noteOns.do{|noteOn|
						events=events.add([ track, endTicks, \noteOn, ch, noteOn, 0 ]);
					}
				}
			};
			noteOns=nil;
		};
		^events
	}
	//TODO: add a note off message if there is no, and add 'sustained' note(s)
	copyBars {arg startBar, endBar;
		var startTicks, endTicks;
		var env;
		env=this.barToTicksEnv;
		startTicks=env.at(startBar);
		endTicks=env.at(endBar);
		^this.midiEvents.select({arg event; (event[1]>=startTicks) && (event[1]<endTicks)}).deepCopy
	}
}