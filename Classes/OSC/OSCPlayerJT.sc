/*
o=OSCPlayerJT.new("/Users/jorrit/Desktop/epiloog_241203_152036_0.txt", bufferSize: 16).gui
*/
OSCPlayerJT {
	var <path, <>netaddr, <>bufferSize, <>latency;
	var <fileName;
	var <window, <views, files, entries;
	var time, delta, msg, pos, min, max, loadFile, cond, flag;
	var f, oscPlayer, <>loopOSC, forwind, getLines;
	var dirname, oscPaths, oscFileNames, index, originalStates, x;
	var schedulerCondition, prevTime, <duration, <lineLength, <endPos, startPos, startTime, endTime;
	var scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs;

	*new {arg path, netaddr, bufferSize=20, latency=0.2;
		^super.newCopyArgs(path, netaddr, bufferSize, latency).init
	}

	init {
		netaddr=netaddr??{NetAddr("localhost", 57120)};
		cond=Condition.new;
		schedulerCondition=Condition.new;
		flag=true;
		loopOSC=true;
		forwind=false;
		getLines=1;
		dirname=path.dirname;
		entries=PathName(dirname).entries;
		index=entries.indexOfEqual(path)??{0};
		getLines=20;
		path=PathName(path);
		prevTime=0;
		duration=0;
		startPos=0;
		endPos=0;
		startTime=0;
		endTime=0;
		this.initFile;
	}

	initFile {
		var x;
		if (File.exists(path.fullPath)) {
			f=File(path.fullPath, "r");
			x=f.getLine(65536);
			lineLength=f.pos;
			startPos=0;
			endPos=f.length;
			f.pos_( (f.length-(4*lineLength)).max(0) );
			while {f.pos<f.length} {
				x=f.getLine(65536);
				x=x.replace(" ", "").replace("nan", "0.0");
				x=x.split($,).collect{|i| i.interpretSafeJT};
				duration=x[0];
			};
			startTime=0;
			endTime=duration;
			if (views!=nil) {if (views[\duration]!=nil) {views[\duration].string_(duration.asTimeString)}.defer};
			f.close;
		}
	}

	gui {
		{
			var w;
			w=Window("OSCPlayer", Rect(300,300,250+4+4+4,180+4+12+4)).front;
			window=w;
			w.addFlowLayout;
			w.alwaysOnTop_(true);
			w.onClose_{
				oscPlayer.stop;
				if (f.class==File) {f.close};
			};
			views=();
			views[\filename]=StaticText(w, (w.view.bounds.width-8)@20).align_(\left).string_(path.fileNameWithoutExtension);
			views[\duration]=StaticText(w, 150@20).string_(duration.asTimeString);
			views[\time]=StaticText(w, 100@20).align_(\left).string_(0.asTimeString).stringColor_(Color.blue);
			views[\play]=Button(w, 60@20).states_([ [\paused, Color.black, Color.red(1.0, 0.5)],[\PLAY,Color.black,Color.green]]).action_{|b|
				var states;
				if (b.value==1) {
					cond.unhang;
					flag=true;
					if (oscPlayer.isPlaying.not) {
						this.play
					};
					states=b.states;
					states[0]=[\paused, Color.black, Color.red(1.0, 0.5)];
					views[\play].states_(states).value_(1);
				} {
					flag=false;
				}
			};//.value_(1);
			//originalStates=views[\play].states.deepCopy;

			views[\reset]=Button(w, 60@20).states_([ ["reset"] ]).action_{
				var states;
				oscPlayer.stop;
				if (f.class==File) {f.close};
				flag=false;
				views[\play].value_(0);
				views[\time].string_(startTime.asTimeString);
				states=views[\play].states;
				states[0]=["play"];
				views[\play].states_(states);
			};
			//views[\forwind]=Button(w, 40@20).states_([ [">>"] ]).action_{|b| getLines=getLines;};
			views[\loop]=Button(w, 60@20).states_([ [\loop],[\LOOP, Color.black,Color.yellow] ]).action_{|b| loopOSC=(b.value>0)}.value_(loopOSC.binaryValue);

			Button(w, 60@20).states_([ [\load] ]).action_{
				//File.openDialog(
				//FileDialog
				flag=false;
				{
					views[\play].value_(0)
				}.defer;

				Dialog.openPanel({|pathname|
					path=PathName(pathname);
					oscPlayer.stop;
					f.close;
					cond.unhang;
					this.initFile;
					flag=true;
					this.play;
					{
						views[\play].value_(1);
						views[\filename].string_(path.fileNameWithoutExtension)
					}.defer;
				},{
					cond.unhang; flag=true;
					{
						views[\play].value_(1);
					}.defer;
				}, false, path.fullPath)
			};
			views[\progressSlider]=Slider(w, (w.view.bounds.width-8)@20).thumbSize_(1).canFocus_(false).background_(Color.white).knobColor_(Color.blue);
			views[\rangeLoop]=RangeSlider(w, (w.view.bounds.width-8)@20).knobColor_(Color.blue).action_{|sl|
				views[\startTime].string_((sl.lo*duration).asTimeString);
				views[\startTimeSecs].value_((sl.lo*duration));
				views[\endTime].string_((sl.hi*duration).asTimeString);
				views[\endTimeSecs].value_((sl.hi*duration));
			}.mouseUpAction_{|sl|
				//[sl.lo, sl.hi].postln;
				this.seek(sl.lo*duration, sl.hi*duration)
			};
			views[\startTimeSecs]=EZNumber(w, 120@20, \startTime, [0, duration], {|ez|
				views[\rangeLoop].lo_(ez.value/duration);
				this.seek(ez.value)
			});
			views[\startTime]=StaticText(w, 100@20).string_(0.asTimeString);
			views[\endTimeSecs]=EZNumber(w, 120@20, \endTime, [0, duration], {|ez|
				views[\rangeLoop].hi_(ez.value/duration);
				this.seek(ez.value)}, duration);
			views[\endTime]=StaticText(w, 100@20).string_(duration.asTimeString);
			views[\backward]=Button(w, 100@20).states_([ [\backward] ]).action_{this.backward};
			views[\forward]=Button(w, 100@20).states_([ [\forward] ]).action_{this.forward};

			//views[\endTime]=EZNumber(w, 100@20, \endTime, [0, 1000], {|ez| this.seek(ez.value)});
		}.defer
	}

	openFile {
		var tmpFlag=flag.copy, isPlaying=views[\play].value.copy, states;
		flag=false;
		{
			views[\play].value_(0)
		}.defer;
		oscPlayer.stop;
		if (f.class==File) {
			f.close;
		};
		//if (tmpFlag) {
		if (isPlaying==1) {
			flag=true;
			this.play;
			{
				views[\play].value_(1);
				views[\filename].string_(path.fileNameWithoutExtension)
			}.defer;
		} {

		}
	}

	pause {flag=false;}

	continue { cond.unhang; flag=true}

	loadBlock {arg bufferSize=16;
		var i=0, x;
		var startTimes=[], msgs=[], deltaTimes, totalDuration;
		while{ (i<bufferSize) && (f.pos<f.length)} {
			x=f.getLine(65536);
			x=x.replace(" ", "").replace("nan", "0.0");
			x=x.split($,).collect{|i| i.interpretSafeJT};
			//array=array.add(x);
			startTimes=startTimes++x[0];
			msgs=msgs.add(x[1..]);
			//0.0001.yield;
			i=i+1;
		};
		deltaTimes=startTimes.differentiate;
		deltaTimes[0]=startTimes[0]-prevTime;
		totalDuration=startTimes.last-startTimes.first;
		prevTime=startTimes.last;
		^[startTimes, deltaTimes, msgs, totalDuration, i==bufferSize]
	}

	scheduler {arg startTimes, deltaTimes, msgs;
		var msg;
		deltaTimes.do{|delta,i|
			delta.wait;
			msg=msgs[i];
			{
				views[\time].string_(startTimes[i].asTimeString);
				views[\progressSlider].value_(startTimes[i]/duration);
			}.defer;
			netaddr.sendBundle(latency, msg);
		};
		schedulerCondition.unhang;
	}

	seek {arg startT=0, endT;
		var nowTime=0, string;
		var wasPlaying=flag.copy;
		var tmpDuration, endFlag;
		var posEstimation;

		this.pause;
		{
			views[\play].value_(0);
		}.defer;

		{
			if (f.isOpen.not) {f=File(path.fullPath, "r");};
			if (endT!=nil) {
				posEstimation=(((endT/duration)*f.length).floor.asInteger-(3*lineLength)).clip(0, f.length-lineLength).asInteger;
				//"posEstimation ".post; [posEstimation, f.length].postln;
				f.pos_(posEstimation);
				f.getLine(65536);
				endPos=f.pos.copy;
				endTime=duration.copy;
				while { (nowTime<endT) && (f.pos<f.length)} {
					string=f.getLine(65536);
					nowTime=string.split($,)[0].interpret;
					//"endTime ".post; nowTime.postln;
				};
				endTime=nowTime;
				endPos=f.pos.copy.min(f.length);
			} {
				endTime=duration;
				endPos=f.length;
			};

			if (startT==0.0) {
				f.pos_(0);
				nowTime=0;
				startTime=0;
				startPos=0;
			} {
				nowTime=0;
				posEstimation=(((startT/duration)*f.length).floor.asInteger-(2*lineLength)).clip(0, f.length-lineLength).asInteger;
				f.pos_(posEstimation);
				f.getLine(65536);
				startPos=f.pos.copy;
				startTime=startT.copy;
				while {nowTime<startT} {
					string=f.getLine(65536);
					nowTime=string.split($,)[0].interpret;
					//"startTime ".post; nowTime.postln;
				};
			};
			prevTime=nowTime.copy;
			startTime=nowTime.copy;
			startPos=f.pos.copy;
			{
				views[\time].string_(nowTime.asTimeString);
				views[\progressSlider].value_(nowTime/duration);
				views[\rangeLoop].lo_(startPos/f.length).hi_(endPos/f.length).doAction;
			}.defer;
			//scheduler_msgs[0].postcs;
			#scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag=this.loadBlock(bufferSize).copy;
			scheduler_msgs.do{|msg| netaddr.sendBundle(latency, msg);};

		}.fork(SystemClock,0, 65536)
	}

	play {
		var playFlag=true;
		var array,i=0;
		var realTime=Main.elapsedTime, realDelta;
		oscPlayer={
			//var flag=true;
			var offsetTime, restDelta=0, delayTime;
			var startTimes, msgs, deltaTimes, duration, tmpDuration;
			var prev, now, deltaTime;
			var endFlag;

			while{playFlag} {
				if (f.isOpen.not) {
					f=File(path.fullPath, "r");
				};
				f.pos_(startPos);
				prevTime=startTime.copy;//

				#scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, duration, endFlag=this.loadBlock(bufferSize).copy;
				while {f.pos<endPos} //f.length
				{
					{
						this.scheduler(scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs)
					}.fork(SystemClock, 0, 65536);
					{
						#scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag=this.loadBlock(bufferSize).copy
					}.fork(SystemClock,0, 65536);
					schedulerCondition.hang;
					duration=tmpDuration.copy;
					if (flag.not) {cond.hang};
				};
				this.scheduler(scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs);
				duration.wait;
				if (f.class==File) {
					f.close;
				};
				playFlag=loopOSC.copy;
				1.0.wait;//safety loop wait
			};
			if (loopOSC.not) {
				{
					views[\play].value_(0);
				}.defer
			}
		}.fork(SystemClock,nil, 2.pow(24).asInteger);//30
	}

	forward {arg size;
		var scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag, nowTime;
		#scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag=this.loadBlock(size??{bufferSize}).copy;
		nowTime=scheduler_startTimes.last.copy;
		prevTime=nowTime.copy;
		startTime=nowTime.copy;
		startPos=f.pos.copy;
		{
			views[\time].string_(nowTime.asTimeString);
			views[\progressSlider].value_(nowTime/duration);
			views[\rangeLoop].lo_(startPos/f.length).hi_(endPos/f.length).doAction;
		}.defer;
		scheduler_msgs.do{|msg| netaddr.sendBundle(latency, msg);};
	}

	backward {arg size;
		var scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag, nowTime;
		var n=size??{bufferSize};
		f.pos_( (f.pos - (n*3*lineLength)).max(0) );
		f.getLine(65536);
		#scheduler_startTimes, scheduler_deltaTimes, scheduler_msgs, tmpDuration, endFlag=this.loadBlock(size??{bufferSize}).copy;
		scheduler_startTimes.postln;
		nowTime=scheduler_startTimes.last.copy;
		prevTime=nowTime.copy;
		startTime=nowTime.copy;
		startPos=f.pos.copy;
		{
			views[\time].string_(nowTime.asTimeString);
			views[\progressSlider].value_(nowTime/duration);
			views[\rangeLoop].lo_(startPos/f.length).hi_(endPos/f.length).doAction;
		}.defer;
		scheduler_msgs.do{|msg| netaddr.sendBundle(latency, msg);};

	}
}