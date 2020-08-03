//noem busses iets anders, b.v. index ofzo
Recorder_JT {

	var <>fileName, <>path, buf, synth, busses, server, isRecording, <>window, <>guis, <>autoname;
	var <>headerFormat, <>sampleFormat, target;

	classvar headerFormats, sampleFormats;

//arg w, path, busses=[0,1], target, fileName="[automatic]";

	*new {arg server, window, busses=[0,1], target, path, headerFormat="aiff", sampleFormat="int16";
		^super.new.init(server, window, busses, target, path, headerFormat, sampleFormat);
		}

	init {arg argserver, argwindow, argbusses, argtarget, argpath, argheaderFormat, argsampleFormat;
		var newWindow=false;

		headerFormats=["aiff", "wav"];
		sampleFormats=["int16","int24","int32","float"];

		server=argserver ?? {Server.default};
		window=argwindow ?? {newWindow=true; Window("Recorder", Rect(0,1000,260,65))};
		if (newWindow, {window.addFlowLayout; window.front;});
		busses=argbusses ?? {[0,1]};//SoundIn
		target=argtarget ?? {1};
		path=argpath ?? {"./"};
		isRecording=false;
		autoname=1;
		headerFormat=argheaderFormat;//["aiff", "wav"]
		sampleFormat=argsampleFormat;//["int16","int24","int32","float"]

		if (headerFormats.indexOfEqual(headerFormat) == nil, {"invalid header format".postln; headerFormat="aiff"});
		if (sampleFormats.indexOfEqual(sampleFormat) == nil, {"invalid sample format".postln; sampleFormat="int16"});
		{this.gui;}.defer;
		}

	startRecording_ {arg argserver=server, argbusses=busses, argtarget=target, argpath=path, argheaderFormat=headerFormat, argsampleFormat=sampleFormat;
		{


			if (PathName(path).isFolder, {
				this.generateFileName
			},{
				fileName=PathName(path).fileName;
				path=PathName(path).pathOnly;
				});
			if (autoname==1, {this.generateFileName});


			buf=Buffer.alloc(server, 65536, busses.size); server.sync;

			buf.write( (path++fileName++"."++headerFormat).deunixPath, headerFormat, sampleFormat, 0, 0, true); server.sync;

			synth=SynthDef(\StandAloneRecorder_JT, {|bufnum|
				DiskOut.ar(bufnum, In.ar(busses))
				}).play(target,[\bufnum,buf.bufnum],\addAfter);
			server.sync;
			isRecording=true;
			}.fork;
		}

	stopRecording {
		synth.free;
		buf.close;
		buf.free;
		isRecording=false;
		}

	generateFileName {
		fileName=Date.localtime.stamp;
		}

	close {
		if (isRecording, {this.stopRecording});
		}

/*
	synthDefs {
		SynthDef(\Recorder_JT, {|bufnum|
			DiskOut.ar(bufnum, In.ar(busses))
			}).send(server);
		}
*/

	gui {
		var c, d, func=(), ontrafelen, ontrafelen2, tmp, buf, synth;
		var buttonWidth=40, buttonHeight=20, textWidth=100, font=Font(\Helvetica, 10);
		var containerWidth=(buttonWidth+4)*3+textWidth+4+4, containerHeight=buttonHeight*2+10;
		var noc=busses.size, synthName=(\Recorder_JT++noc).asSymbol, parent;

		guis=();

		c=CompositeView(window, containerWidth@containerHeight); c.decorator=d=FlowLayout(c.bounds);
		//c.background_(Color.red(1.9));
		guis[\rec]=Button(c, buttonWidth@buttonHeight).states_([ [\rec],[\rec, Color.black, Color.red]]).font_(font).canFocus_(false).action_({|but|
		if (but.value==1, {
			this.startRecording_(server, busses, target);
			},{
			this.stopRecording;
			})
		});
		guis[\path]=Button(c, buttonWidth@buttonHeight).states_([ [\path]]).canFocus_(false).font_(font).action_({
			guis[\auto].value=0;
			Dialog.savePanel({|pathy|
				path=PathName(pathy).pathOnly;
				if (guis[\auto].value==0, {
					guis[\pathName].string=PathName(pathy).fileName;
					fileName=PathName(pathy).fileName;
					});
			})});
		guis[\auto]=Button(c, buttonWidth@buttonHeight).states_([ [\auto], [\auto, Color.black, Color.blue(1.5)]]).font_(font).canFocus_(false).action_({|but|
			autoname=but.value;
			if (but.value==1, {guis[\pathName].string="[automatic]"});
			}).value_(1);
		guis[\pathName]=StaticText(c, textWidth@buttonHeight).string_(fileName).font_(font);

		StaticText(c, 40@buttonHeight).string_("header:").font_(font).align_(\right).canFocus_(false);
		PopUpMenu(c, 60@buttonHeight).items_(headerFormats)
			//.background_(Color.grey(1.5))
			.action_({|p| p.items[p.value]}).font_(font).canFocus_(false).value_(headerFormats.indexOfEqual(headerFormat));
		StaticText(c, 40@buttonHeight).string_("sample:").font_(font).align_(\right).canFocus_(false);
		PopUpMenu(c, 60@buttonHeight).items_(sampleFormats)
			//.background_(Color.grey(1.5))
			.action_({|p| p.items[p.value]}).font_(font).canFocus_(false).value_(sampleFormats.indexOfEqual(sampleFormat));


		c.getParents.last.findWindow.addToOnClose({this.close});
		}

}
