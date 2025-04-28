/*

o=OSCPlayerJT.new("/Users/jorrit/Desktop/epiloog_241203_152036_0.txt", bufferSize: 20).gui

o=OSCPlayerJT.new("/Users/jorrit/Desktop/_250404_092707_0.txt", bufferSize: 20).gui
o.pause
o

*/
OSCPlayerJT {
	var <path, <>netaddr, <>bufferSize, <>latency;
	var <fileName;
	var w, <views, files, entries;
	var time, delta, msg, pos, min, max, loadFile, cond, flag;
	var f, oscPlayer, <>loopOSC, forwind, getLines;
	var dirname, oscPaths, oscFileNames, index, originalStates, x;
	var <>clock;

	*new {arg path, netaddr, bufferSize=20, latency=0.2;
		^super.newCopyArgs(path, netaddr, bufferSize, latency).init
	}

	init {
		netaddr=netaddr??{NetAddr("localhost", 57120)};
		cond=Condition.new;
		flag=true;
		loopOSC=true;
		forwind=false;
		getLines=1;
		dirname=path.dirname;
		entries=PathName(dirname).entries;
		index=entries.indexOfEqual(path)??{0};
		getLines=20;
		path=PathName(path);
		"path is ".post; path.postln;
	}

	gui {
		w=Window("OSCPlayer", Rect(300,300,250+4+4+4,80+4+12+4)).front;
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
	//views[\dropdown].mouseUpAction_{"test".postln};
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
		/*
		} {
		cond.unhang;
		{
		views[\filename].string_(PathName(path).fileNameWithoutExtension)
		}.defer;
		}
		*/
	}
	pause {flag=false;}
	continue { cond.unhang; flag=true}
	readLines {

	}
	schedule {}

	play {
		var playFlag=true;
		var array,i=0;
		var realTime=Main.elapsedTime, realDelta, prevTime;
		oscPlayer={
			//var flag=true;
			var offsetTime, restDelta=0, delayTime;
			var startTimes, msgs;
			var prev, now, deltaTime;
			while{playFlag} {
				f=File(path.fullPath, "r");
				//1.do({
				offsetTime=nil;//the first starttime of the osc-file
				prevTime=nil;

				while {f.pos<f.length} {
					array=[]; i=0;//array contains the buffered lines of the osc-file
					startTimes=[];
					msgs=[];
					realTime=Main.elapsedTime;//the realtime at the start of the playback
					while{ (i<bufferSize) && (f.pos<f.length)} {
						x=f.getLine(65536);
						x=x.replace(" ", "");
						x=x.replace("nan", "0.0");
						x=x.split($,).collect{|i|
							i.interpretSafeJT
						};
						//[f.pos,x[0]].post; " ".post;
						//array=array.add(x);
						startTimes=startTimes++x[0];
						msgs=msgs.add(x[1..]);
						i=i+1;
					};
					//offsetTime=offsetTime??{array.first[0]};//the starttime of the first line of the osc-file
					offsetTime=offsetTime??{startTimes[0]};//the starttime of the first line of the osc-file

					delayTime=(Main.elapsedTime-realTime);
					//"\nconverting took ".post; delayTime.postln;
					//array.first[0].post; " and ".post; array.last[0].postln;
					//realTime=Main.elapsedTime;
					prevTime=prevTime??{startTimes[0]};

					delta=(startTimes[0]-prevTime-delayTime);
					//"delta ".post; delta.postln;
					delta=delta.max(0);
					delta.wait;

					netaddr.sendBundle(latency-delayTime, msgs[0]);
					prevTime=startTimes[0];
					if (flag.not) {cond.hang};

					now=Main.elapsedTime;

					startTimes.copyToEnd(1).do{|startTime,i|
						delta=startTime-prevTime;
						msg=msgs[i+1];
						{views[\time].string_(startTime.asTimeString.copyToEnd(3))}.defer;
						prev=now;
						//"delta ".post; delta.postln;
						delta.max(0).wait;
						now=Main.elapsedTime;
						deltaTime=now-prev;
						//[delta, deltaTime, delta-deltaTime].postln;
						netaddr.sendBundle(latency, msg);
						prevTime=startTime;//now
					}
				};
				if (f.class==File) {
					f.close;
				};
				playFlag=loopOSC.copy;
			};
			if (loopOSC.not) {
				{views[\play].value_(0)}.defer
			}
		}.fork(AppClock,nil, 2.pow(24).asInteger);//30
	}

	/*
	s.waitForBoot{
	~port=~port??{9006};
	netaddr=netaddr??{NetAddr("localhost", ~port??{9006})};//fake network, for faking the live OSC input from Xsens

	netaddr.addr.asIPString;
	//path="/Users/jorrit/Dropbox/1_PROJECTS/ICK_AI_Toolbox/SC/OSC_recordings/Chapters_data_total/alphabet_total.txt";
	//path="/Users/jorrit/Desktop/test.txt";

	}

	*/
}
