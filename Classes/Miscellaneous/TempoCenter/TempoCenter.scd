TempoClockJT {
	var <tempo, <bpm;
	var <tempoClock;
	var actionOnTheBeat;

	*new {

	}
	init {

	}

	tap { }
	tempo_ {arg t;

	}
	bpm_ {arg b;

	}

}

TempoJTGUI : GUIJT {
	*new { }
	init { }
}


{arg p;
	var deltas=[], memory=4, index=0;
	var past=Main.elapsedTime;
	var funcBeep;

	//funcBeep={p[\views][\BPM][\beep].value_(1); 0.1.wait; p[\views][\BPM][\beep].value_(0)};
	funcBeep={p[\cv][\bpm].background_(Color.yellow); 0.1.wait; p[\cv][\bpm].background_(Color.grey)};

	p[\func][\doOnTheBeat]=p[\func][\doOnTheBeat]??{ {} };

	p[\func][\BPMfork]={
		inf.do{arg beat;
			p[\func][\doOnTheBeat].value(beat);
			funcBeep.fork(AppClock);
			(60/p[\bpm]).wait;
		}
	};
	p[\views][\BPM]=();
	p[\bpm]=p[\bpm]??{88.0};

	p[\func][\sync]={
		p[\routine][\BPM].stop; p[\routine][\BPM]=p[\func][\BPMfork].fork;
		p[\linkedToSync].keysValuesDo{|key,func| func.value};
	};

	p[\func][\tapBPM]={arg tmp;
		var now=Main.elapsedTime;
		var delta=now-past;
		if ((delta<1.5) && (delta>0.333), {
			if (deltas.size==0, {deltas=delta!memory},{
				[delta, index%memory].postln;
				deltas[index%memory]=delta;
			});
			deltas.round(0.01).postln;
			p[\func][\setBPM].value(deltas.mean.reciprocal*60);
		},{
			p[\func][\sync].value;
			deltas=[];
			index=0;
		});
		index=index+1;
		past=now;
	};

	p[\func][\setBPM]={arg bpm;
		p[\bpm]=bpm;
		p[\syncedToBPM].keysValuesDo{|key, guiObject|
			{guiObject.valueAction_(bpm)}.defer
		};
		{p[\views][\BPM][\bpm].value_(p[\bpm].round(0.1))}.defer;
	};

	{
		p[\views][\BPM][\tapBPM]=Button(p[\cv][\bpm], 30@20).states_([ ["TAP"] ]).action_{
			p[\routine][\Reverser].reset;//hier meerdere reset functions!
			p[\func][\tapBPM].value;
		}.canFocus_(false);
		//p[\views][\bpm]=StaticText(p[\cv][\bpm], 80@32).string_(p[\bpm]).font_(Font("Monaco", 32)).stringColor_(Color.white);
		p[\views][\BPM][\bpm]=NumberBox(p[\cv][\bpm], 80@32).value_(p[\bpm]).font_(Font("Monaco", 32)).stringColor_(Color.white).action_{arg n;
			p[\func][\setBPM].value(n.value);
		};
		p[\views][\BPM][\beep]=Button(p[\cv][\bpm], 40@20).states_([ [""],["",Color.black,Color.yellow]]).canFocus_(false);
		p[\views][\BPM][\sync]=Button(p[\cv][\bpm], 40@20).states_([ ["SYNC"] ]).action_(p[\func][\sync]).canFocus_(false);
		p[\viewsPreset][\BPM]=();
		p[\viewsPreset][\BPM][\bpm]=p[\views][\BPM][\bpm];
	}.defer;

	p[\routine][\BPM]=p[\func][\BPMfork].fork;
}