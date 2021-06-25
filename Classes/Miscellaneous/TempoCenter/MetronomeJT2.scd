/*
Server.killAll
m=MetronomeJT2(nil, 350@20)
TempoClock
x={arg t_trig=1; SinOsc.ar(1000, 0,InTrig.kr(m.busTrigger).lag(0.0, 0.1))!2}.play(s, 1)
*/
+ TempoClock {
	//--------------------------------------------------------------------------------------------------- GUI
	makeGui {arg parent=nil, bounds=350@20, server, flashTime=0.06;
		var compositeview, views=(), window, bpm;
		var buttonWidth, font, fontSmall, fontBig;
		var quant=1, synth, func;
		server=server??{Server.default};
		this.stop;
		font=Font("Monoca", bounds.y*0.75);
		fontSmall=Font("Monaco", bounds.y*0.5);
		fontBig=Font("Monaco", bounds.y*1.25);
		if (parent==nil, {
			parent=Window("Metronome", Rect(400,400,bounds.x+8,(bounds.y+4*5)+12)).front;
			parent.addFlowLayout;
			parent.alwaysOnTop_(true);
			parent.onClose_{this.free};
			window=parent;
		},{
			window=parent.findWindow;
			parent.onClose=parent.onClose.addFunc({this.free});
		});
		compositeview=CompositeView(parent, bounds.x@(bounds.y+4*5+4));
		compositeview.addFlowLayout(4@4, 4@4);
		compositeview.background_(Color.grey);
		views=();
		buttonWidth=(bounds.x-8/5).floor-4;
		bounds=(bounds.x-8)@bounds.y;

		func={
			ticks=0;
			1.do{
				var timeDefer=this.seconds, beatDefer=this.beats;
				server.sendBundle(nil, [\n_set, synth.nodeID, \t_trig, 1],[\c_set, busTrigger.index, 1]);
				{
					views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
					views[\beats].string_(beatDefer);
					{compositeview.background_(Color.yellow); flashTime.wait; compositeview.background_(Color.grey)}.fork(AppClock)
				}.defer;
				(beatDur-latency).wait;
				ticks=0;
				time=time+beatDur;
				beat=beat+1;
			};
			inf.do{arg i;
				var timeDefer, beatDefer;
				timeDefer=time;
				beatDefer=beat;
				if (ticks%resolution==0, {
					server.sendBundle(latency,[\c_set, busTrigger.index, 1], [\n_set, synth.nodeID, \t_trig, 1]);
					beatFunc.value(this, beatDefer, timeDefer);
					AppClock.sched(latency, {
						compositeview.background_(Color.yellow);
						views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
						views[\beats].string_(beatDefer.asBeatsString(beats, division, subdivision, resolution));
					});
					AppClock.sched(latency+flashTime, {
						compositeview.background_(Color.grey)
					});
				},{
					AppClock.sched(latency, {
						views[\time].string_(timeDefer.asTimeString.copyToEnd(1));
						views[\beats].string_(beatDefer.asBeatsString(beats, division, subdivision, resolution));
					});
				});
				time=time+beatDurR;
				beat=beat+resolutionR;
				ticks=ticks+1;
				beatDurR.wait;
			}
		};

		views[\start]=Button(compositeview, buttonWidth@bounds.y).states_([ [\start], [\stop, Color.black,Color.green] ]).action_{|b|
			if (b.value==1, {
				this.play({arg ...msg; msg.postln}, quant);
			},{
				this.stop;
			});
		}.canFocus_(false).font_(font);
		views[\reset]=Button(compositeview, buttonWidth@bounds.y).states_([ [\reset] ]).action_{
			this.reset;
		}.font_(font);
		views[\tap]=Button(compositeview, buttonWidth@bounds.y).states_([ [\tap] ]).action_{
			this.tap;
		}.font_(font);
		views[\resync]=Button(compositeview, buttonWidth@bounds.y).states_([ [\resync] ]).action_{
			this.resync;
		}.font_(font);
		views[\bpm]=EZNumber(compositeview, bounds.x*0.35@bounds.y, \bpm, ControlSpec(40, 200, \exp), {|ez|
			this.tempo=ez.value/60.0;
			//this.tempo_(ez.value/60)
		}, this.tempo*60, false, bounds.y*2).font_(font);
		views[\beatsPerBar]=EZNumber(compositeview, bounds.x*0.2@bounds.y, \beats, ControlSpec(1, 100, 0, 1), {|ez| this.beatsPerBar_(ez.value)}, this.beatsPerBar, false
			, bounds.y*2).font_(fontSmall);
		//views[\division]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,4,8,16,32]).action_{|l| division=l.items[l.value]}.value_(2).font_(fontSmall);
		//views[\subdivision]=PopUpMenu(compositeview, bounds.x*0.15@bounds.y).items_([1,2,3,4,5,6,7]).action_{|l| subdivision=l.items[l.value]}.value_(3).font_(fontSmall);
		//views[\amp]=EZSlider(compositeview, bounds, \amp, \amp, {|ez| synth.set(\amp, ez.value)}, 0.0, false, bounds.y*2).font_(font);
		views[\time]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(
			"seconds".post; this.seconds.postln;
			this.seconds.asTimeString.copyToEnd(1)).font_(fontBig).align_(\left).stringColor_(Color.red(1.5))
		.background_(Color.black);
		views[\beats]=StaticText(compositeview, (bounds.x*0.5-4)@(bounds.y*2)).string_(
			"elapsedBeats ".post; this.elapsedBeats.postln;
			this.elapsedBeats)
		.font_(fontBig).align_(\left).stringColor_(Color.blue(1.5))
		.background_(Color.black);
	}
}