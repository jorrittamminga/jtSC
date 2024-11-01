SimpleMIDIFileJT : SimpleMIDIFile {
	var <timeBarEnv, <timeSignatures;
	var <bpmMap, <bpmMapFlopped;

	*read { arg pathName; ^SimpleMIDIFileJT( pathName ).read; }

	getTempo {
		this.makeTimeBarEnv;
		this.timeMode=\seconds;
		bpmMap=this.tempoMap;
		bpmMapFlopped=bpmMap.flop;
		this.timeMode=\ticks;
		^tempo =
		if( this.tempi.notNil )
		{ this.tempi[0]  ? tempo; }
		{ tempo };
	}


	makeTimeBarEnv {
		var count=0, frames=0, timeSignature=[12,8], offset=0, bpm=60, prevTicks=0, deltaTime
		, time=0, bar=0, prevTime=0, deltaTicks;
		var offsetTime=0, offsetTicks, barTicks=1440, deltaBars, tmpTicks;
		var prevTicksTimeSignature=0, addOne=1;
		var bars=[], times=[0.0];
		var funcCalculate;

		timeSignatures=[];

		funcCalculate={arg ticks, tmpBpm;
			deltaTicks=(tmpTicks-prevTicks);
			deltaBars=deltaTicks/barTicks;
			deltaTime=(( deltaTicks /480)*(60/bpm));

			time=(time+deltaTime);
			bar=bar+deltaBars;
			//[deltaBars, deltaTime].post;
			//[bar, time.asTimeString].postln;
			bars=bars.add(deltaBars);
			times=times.add(time);

			bpm=tmpBpm;//ar[3];
			count=count+1;
			prevTicks=ticks;
		};

		this.metaEvents.do{|ar,i|
			if (ar[0]==0, {
				var type=ar[2], ticks=ar[1];
				switch(type)
				{\timeSignature} {
					if (count>1, {
						//[ticks-prevTicksTimeSignature, (ticks-prevTicksTimeSignature)/barTicks+addOne].postln;
						timeSignatures=timeSignatures++(timeSignature!((ticks-prevTicksTimeSignature)/barTicks+addOne));
						tmpTicks=ticks;
						funcCalculate.value(ticks, bpm);
						addOne=0;
					});
					prevTicksTimeSignature=ticks;

					timeSignature=[ar[3][0], 2.pow(ar[3][1])];
					barTicks=(1920/(2.pow(ar[3][1])))*ar[3][0];//ticks of one bar
				}
				{\tempo} {
					if (count==1){
						tmpTicks=barTicks;
					}{
						tmpTicks=ticks;
					};
					funcCalculate.value(ticks, ar[3]);
				}
				{\endOfTrack} {
					tmpTicks=ticks;
					timeSignatures=timeSignatures++(timeSignature!((ticks-prevTicksTimeSignature)/barTicks));
					funcCalculate.value(ticks, ar[3]);
				};
			});
			timeBarEnv=Env(times, bars);
		};
	}
	barTimeSignature {arg bar;
		^timeSignatures.at(bar)
	}
	barTime {arg bar=1, numberOfBars=1;
		^[timeBarEnv.at(bar), timeBarEnv.at(bar+numberOfBars)-timeBarEnv.at(bar)]
	}
	bpmAtTime {arg time=0.0;
		var index=bpmMapFlopped[0].indexInBetween(time).floor.asInteger;
		^bpmMapFlopped[1][index]
	}
	bpmAtBar {arg bar=1.0;
		^this.bpmAtTime(this.barTime(bar,1)[0])
	}
}