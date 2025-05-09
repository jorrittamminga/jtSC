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
	var schedulerCondition, prevTime;

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
	}

	gui {
		var w;
		w=Window("OSCPlayer", Rect(300,300,250+4+4+4,80+4+12+4)).front;
		window=w;
		w.addFlowLayout;
		w.alwaysOnTop_(true);
		w.onClose_{
			oscPlayer.stop;
			if (f.class==File) {f.close};
		};
		views=();
		views[\filename]=StaticText(w, 150@20).align_(\left).string_(path.fileNameWithoutExtension);
		views[\time]=StaticText(w, 100@20).align_(\left).stringColor_(Color.blue);
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
			views[\time].string_(0.asTimeString.copyToEnd(3));
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
			{views[\play].value_(0)}.defer;

			Dialog.openPanel({|pathname|
				path=PathName(pathname);
				oscPlayer.stop;

				f.close;
				cond.unhang;
				flag=true;
				this.play;
				{
					views[\play].value_(1);
					views[\filename].string_(path.fileNameWithoutExtension)
				}.defer;
			},{
				cond.unhang; flag=true;
				{views[\play].value_(1)}.defer;
			}, false, path.fullPath)
		};
	}
	openFile {
		var tmpFlag=flag.copy, isPlaying=views[\play].value.copy, states;
		flag=false;
		{views[\play].value_(0)}.defer;
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
			{views[\time].string_(startTimes[i].asTimeString.copyToEnd(3))}.defer;
			netaddr.sendBundle(latency, msg);
		};
		schedulerCondition.unhang;
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
				f=File(path.fullPath, "r");
				prevTime=0;
				#startTimes, deltaTimes, msgs, duration, endFlag=this.loadBlock(bufferSize);

				while {f.pos<f.length} {
					{ this.scheduler(startTimes.copy, deltaTimes.copy, msgs.copy) }.fork(SystemClock, 0, 65536);
					//now=Main.elapsedTime;
					{ #startTimes, deltaTimes, msgs, tmpDuration, endFlag=this.loadBlock(bufferSize) }.fork(SystemClock,0, 65536);
					schedulerCondition.hang;
					//delayTime=Main.elapsedTime-now;
					//(duration-delayTime).max(0.01).wait;
					//duration.wait;
					duration=tmpDuration.copy;
					if (flag.not) {cond.hang};
				};
				this.scheduler(startTimes, deltaTimes, msgs);
				duration.wait;
				if (f.class==File) {
					f.close;
				};
				playFlag=loopOSC.copy;
			};
			if (loopOSC.not) {
				{views[\play].value_(0)}.defer
			}
		}.fork(SystemClock,nil, 2.pow(24).asInteger);//30
	}
}