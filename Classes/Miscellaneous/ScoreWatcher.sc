ScoreWatcher {
	var <score, <args, <freqs, <freqsIDs, <turnOffs, <lastTime;
	*new{arg score, args=();
		^super.new.init(score, args)
	}
	init {arg argscore, argargs;
		args=(freq: \freq, amp: \amp, gate: \gate, fadeOut: \fadeOut, dur: \dur);
		score=argscore;
		argargs.keysValuesDo{|key,val| args[key]=val};
		lastTime= -1.0;
		turnOffs=();
		freqsIDs=();
		freqs=List[];
	}
	register {arg time, msg, autoUnregister=true;
		var dur, freq, event;
		switch(msg[0], \s_new, {
			event=msg.copyToEnd(5).asEvent;
			freq=event[args[\freq]];
			freqs.add(freq);
			freqsIDs[msg[1]]=freq;
			dur=event[args[\dur]];
			if (dur!=nil, {
				if (turnOffs[time+dur]==nil, {
					turnOffs[time+dur]=List[freq]
				},{
					turnOffs[time+dur].add(freq);
				});
			});
		},\n_set, {
			if (msg.asEvent[args[\gate]]==0, {
				//freqs.
			})
		});
		if (autoUnregister, {
			if (turnOffs.size>0, {
				this.unregister(time)
			})
		});
	}
	unregister {arg time;
		var times=turnOffs.keys.asArray.sort;
		while({times[0]<time},{
			turnOffs[times[0]].do{|freq| freqs.remove(freq)};
			turnOffs.removeAt(times[0]);
			times.removeAt(0);
		});
		^freqs
	}
	calculateFreqs {arg time;
		var tmpScore=score.deepCopy, t= -1.0, i=0, scoreTime, msg, turnOff=[];
		tmpScore=tmpScore.sort({|a,b| a[0]<b[0]});
		while({(tmpScore[i][0]<time)||(tmpScore[i]==nil)},{
			i=i+1;
		});
	}
	checkFreq {arg time, freq, calculateFreqsFlag=false;
		var tmpFreqs=freqs.deepCopy, tmpTurnOffs=turnOffs.deepCopy, nearest, distance, min, max, times=turnOffs.keys.asArray.sort;
		^if (tmpFreqs.size>0, {
			if (calculateFreqsFlag==true, {this.calculateFreqs(time)});
			while({times[0]<time},{
				tmpTurnOffs[times[0]].do{|freq| tmpFreqs.remove(freq)};
				tmpTurnOffs.removeAt(times[0]);
				times.removeAt(0);
			});
			tmpFreqs=tmpFreqs.sort;
			nearest=freq.nearestInList(tmpFreqs);
			distance=(freq-nearest).abs;
			min=freq.explin(160.0, 320, 0, 16.0);
			max=freq.explin(320.0, 640, 40, 15.0);
			((distance<min) || (distance>max))
		},{
			true
		})

	}
}