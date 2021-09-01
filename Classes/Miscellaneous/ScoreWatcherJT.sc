ScoreWatcherJT {
	var <scoreJT, durationKey, score;

	defaultFunc {
		^{arg event, maxRoughness=0.24, maxAttempts=4;
			//rFunc={arg
			var freqs, amps, index, sortedFreqs, nearestFreq, roughness;
			var flopped, attempt=0, flag=true;
			var amp=0.0, ampPartialPow=1.0;

			flopped=score.deepCopy.collect{|i| i[1]}.flop;
			freqs=flopped[0];
			amps=flopped[1];
			sortedFreqs=freqs.copy.sort;
			sortedFreqs.removeAllSuchThat({|f| (f-event[\freq]).abs<0.01});
			nearestFreq=event[\freq].nearestInList(sortedFreqs);
			index=freqs.indexOf(nearestFreq);

			if (index!=nil, {
				while({flag&&(attempt<maxAttempts)}, {
					//----------------------------------------------------------------- CALCULATE AMP
					ampPartialPow=rrand(event.ampPartialPow[0], event.ampPartialPow[1]);
					amp=exprand(event[\amp][0], event[\amp][1])*(event[\partial].pow(ampPartialPow));
					//----------------------------------------------------------------- CHECK ROUGHNESS
					roughness=event[\freq].roughness(nearestFreq, amp, amps[index]);
					if (roughness>=maxRoughness, {
						attempt=attempt+1;
						//[\no, attempt, freq, nearestFreq, amp, amps[index], roughness, ampMin, ampMax].postln;
					},{
						flag=false;
					})
				});
				if (attempt>=maxAttempts, {
					amp=0.0;//
				});
			});
			event[\amp]=amp;
			event[\ampPartialPow]=ampPartialPow;
		}
	}

	add {|time ... msgs|
		var dur=1.0;
		msgs.do{|msg|
			dur=msg[msg.indexOfEqual(durationKey)+1]??{dur};
			score=score.add([time+dur, msg]);
		};
		score=score.sort({|a,b| b[0]>=a[0]});
	}

	at {arg time, func;


	}
	/*
	var <score, <scoreBytEndTime, <scoreAtTime;

	*new{arg score, args=();
	^super.new.init(score, args)
	}

	init {arg argscore, argargs;

	}

	at {arg time;//, score
	var index, times;
	if (score.size>0, {
	times=l.flop[0].copy;
	index=times.indexInBetween(time).ceil.asInteger;
	if (times[index]<time, {index=index+1});
	l=l.copyToEnd(index);
	});
	^scoreAtTime
	}

	*/
}