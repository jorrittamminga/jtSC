/*
maak controlSpec ook een onderdeel van de args .
maak een faderreset (dat ie op 0dB staat)
maak een mute all
*/
Meter_JT {

	var <soundfilepath, <server, <window, <path, <target, <group;
	var <synth, <inputBus, <names, offset;
	var <meterGUI, <guis, <>dBLow, <oscr, masterFader, inputbusses, cmdName, inputSynth, ampFader;
	var numRMSSamps, numRMSSampsRecip, <updateFreq=20, showFaders;
	var <bounds, <faderBounds, <boxBounds, realInputBus;
	var <amps;
	var heightFader, widthFader, widthMeter, heightBox, heightName;
	var newWindow;

	classvar formats;
	*new {arg server, window, inputBus, inputbusses, names, inputSynth, masterFader=false, cmdName="/Levels", dBLow= -60, offset=0, showMeters=true, showFaders=true
		//, heightFader=150, widthFader=10, widthMeter=15, heightBox=10, heightName=15;
		, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, realInputBus;

		^super.new.init(server, window, inputBus, inputbusses, names, inputSynth, masterFader, cmdName, dBLow, offset, showMeters, showFaders, bounds, faderBounds, boxBounds, realInputBus);
	}

	init {arg argserver, argwindow, arginputBus, arginputbusses, argnames, arginputSynth, argmasterFader, argcmdName, argdBLow, argoffset, argshowMeters, argshowFaders, argbounds, argfaderBounds, argboxBounds, argrealInputBus;

		server=argserver ?? {Server.default};
		if (server.serverRunning, {
			newWindow=false;
			window=argwindow ?? {newWindow=true; Window("Meter", Rect(0,1000,275,120), false); window.alwaysOnTop_(true) };

			inputBus=arginputBus ?? {Bus.audio(server,2)};

			if (inputBus.class==Bus, {inputBus={|i| i+inputBus.index }!inputBus.numChannels});
			realInputBus=argrealInputBus??{inputBus};
			inputbusses=arginputbusses ?? {inputBus};
			//names=argnames ?? {inputBus.numChannels.collect({|i| ("in"++(inputBus.index+i)).asString})};
			//names=inputBus.numChannels.collect({|i| var name=names[i]; if (name!=nil, {names[i]},{"in"++inputbusses[i]}) });

			names=argnames ?? {inputBus.collect({|i| ("in"++(i)).asString})};
			//names=inputBus.numChannels.collect({|i| var name=names[i]; if (name!=nil, {names[i]},{"in"++inputbusses[i]}) });

			target=arginputSynth.asArray[0];
			inputSynth=arginputSynth.asArray;
			masterFader=argmasterFader;
			cmdName=argcmdName ++ Process.elapsedTime.asString;
			dBLow=argdBLow;
			//		window=argwindow;
			offset=argoffset;

			showFaders=argshowFaders;
			{
				faderBounds=argfaderBounds;
				boxBounds=argboxBounds;
				this.gui;
				this.oscresponder;
			}.defer;

			//		this.updateFreq(updateFreq);
			numRMSSamps=server.sampleRate/updateFreq;
			numRMSSampsRecip=numRMSSamps.reciprocal;
			//		group=Group(target, \addAfter);

			group=target.group;
			group.register;//dit is wellicht al gebeurd!

			synth=SynthDef(\Meter_JT, {|updateFreq=20, id=0, decayTime=1.0, decayTimeAmp=0.6, decayTimePeak=0.02, numRMSSamps, peakHold=0, t_resetPeak|
				//var input=In.ar(inputBus.index, inputBus.numChannels), peak;
				var input=In.ar(inputBus), peak;
				var imp=Impulse.ar(updateFreq);
				SendReply.ar(imp, cmdName, [RunningSum.ar(input.squared, numRMSSamps).lag(0,0.25), Peak.ar(input
					, Delay1.ar(imp)*(1-peakHold)+t_resetPeak
				).lag(0, 3)].flop.flat, id);
			}).play(target, [\id, inputBus[0], \updateFreq, updateFreq, \numRMSSamps, numRMSSamps], \addAfter);
			synth.register;

			amps=Array.fill(names.size, {1});

		}, {"please boot the server first".postln})
	}

	oscresponder {
		/*
		oscr=OSCresponderNode(server.addr, cmdName.asSymbol, {|t,r,msg|
		{try{
		msg.copyToEnd(3).pairsDo({|val,peak,i|
		var meter, valmeter;
		i = i * 0.5;
		meter = meterGUI[i.asInteger].meter;
		meter.value = (val.max(0.0) * numRMSSampsRecip).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
		meterGUI[i.asInteger][\valMeter].string=peak.ampdb.round(0.1).max(dBLow);//is dit de boosdoener voor alle traagheid?
		meter.peakLevel = peak.ampdb.linlin(dBLow, 0, 0, 1);
		});}}.defer;
		}).add;
		*/
		oscr=OSCFunc({|msg, t, r|
			{try{
				msg.copyToEnd(3).pairsDo({|val,peak,i|
					var meter, valmeter;
					i = i * 0.5;
					meter = meterGUI[i.asInteger].meter;
					meter.value = (val.max(0.0) * numRMSSampsRecip).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
					meterGUI[i.asInteger][\valMeter].string=peak.ampdb.round(0.1).max(dBLow);//is dit de boosdoener voor alle traagheid?
					meter.peakLevel = peak.ampdb.linlin(dBLow, 0, 0, 1);
		});}}.defer;},cmdName.asSymbol,server.addr)


	}

	updateFreq_ {arg freq;
		updateFreq=freq;
		numRMSSamps=server.sampleRate/updateFreq;
		numRMSSampsRecip=numRMSSamps.reciprocal;
		synth.set(\updateFreq, updateFreq, \numRMSSamps, numRMSSamps)
	}

	close {
		//oscr.remove;
		oscr.free;
		if (group.isRunning, { synth.free});
		//if (window.isClosed.not, {window.onClose_({nil}); window.close});
	}

	gui {
		var width=boxBounds.x, height=boxBounds.y*5+faderBounds.y, masterFaderWidth=boxBounds.x*1.0;
		var containerWidth=inputBus.size*(width+4)+4+(masterFader.binaryValue*(masterFaderWidth+4)), containerHeight=(height+4)+4;
		var font=Font("Helvetica", 9), c, d, cs, masterMove=false;
		var g,h;

		ampFader=(); meterGUI=(); guis=();
		if (newWindow, {window.addFlowLayout; window.front; window.bounds_(Rect(0, 1000, containerWidth+8, containerHeight+8)) });
		c=CompositeView(window, containerWidth@containerHeight); c.decorator=d=FlowLayout(c.bounds);
		//c.background_(Color.grey);//remove???
		cs=(fader:ControlSpec(0.0001.ampdb, 10.0, -1.5, 1.0));
		if (faderBounds.x<=0, {showFaders=false});
		if (showFaders.not, {widthMeter=width});
		inputBus.do({|ch,i| var g,h,name;
			//if (ch.class==Bus, {ch=ch.index});
			name=names[i];
			guis[name]=();
			meterGUI[i]=();

			g=CompositeView(c, width@height); g.decorator=h=FlowLayout(g.bounds, Point(0,0), Point(0,0));

			//g.background_(Color.grey(0.7));//remove?

			StaticText(g, boxBounds).string_(inputbusses[i]+offset).align_(\center).font_(font);
			meterGUI[i][\valMeter]=StaticText(g, boxBounds).string_(0).canFocus_(false).background_(Color.grey(0.95)).font_(font);
			meterGUI[i][\meter]=LevelIndicator(g, (boxBounds.x-faderBounds.x)@faderBounds.y).warning_(0.9).critical_(1.0).drawsPeak_(true);
			if(showFaders, {
				guis[name][\fader]=Slider(g, faderBounds.x@faderBounds.y).canFocus_(false).background_(Color.grey(0.5)).action_({|sl|
					var val;
					val=cs[\fader].map(sl.value);
					guis[name][\valFader].string_(val);
					amps[i]=val.dbamp;
					/*
					amps=names.collect({|name|
					cs[\fader].map(guis[name][\fader].value).dbamp * guis[name][\mute].value
					});
					*/
					inputSynth.do{|syn|
						syn.set(\amp, names.collect({|name|
							//cs[\fader].map(guis[name][\fader].value).dbamp
							amps
							* guis[name][\mute].value }) );
					};

					if (masterFader, {
						if (masterMove.not
							, {
								ampFader[name]=cs.fader.unmap(guis.masterFader.value) - sl.value});});//dit mag mooier

				}).value_(cs[\fader].unmap(0.0));
				ampFader[name]=0.0;
				guis[name][\valFader]=StaticText(g, boxBounds).string_(0).canFocus_(false).font_(font).background_(Color.grey(0.95));
			});
			StaticText(g, boxBounds).string_(name).align_(\center).font_(font);
			//StaticText(g, boxBounds).string_(realInputBus[i]).align_(\center).font_(font);

			guis[name][\mute]=Button(g, (boxBounds.x*0.5)@(boxBounds.y*1.4)).states_([ [realInputBus[i].asString++"m",Color.grey,Color.grey(1.15)],[realInputBus[i].asString] ]).font_(font).canFocus_(false).value_(1).action_({|b|
				guis[name][\fader].doAction
			});

			guis[name][\master]=Button(g, (boxBounds.x*0.5)@(boxBounds.y*1.4)).states_([[],[]]).font_(font).canFocus_(false);
			//MasterIO.new(argbus, argtarget, argparent, argname, argsettings)
		});

		if (masterFader, {
			c.decorator.shift(0, boxBounds.y);
			g=CompositeView(c, (masterFaderWidth)@(height));
			g.decorator=h=FlowLayout(g.bounds, Point(0,0), Point(0,0));

			//g.background_(Color.grey(0.7));//remove???

			guis.masterFader=EZSlider(g
				, (masterFaderWidth)@(faderBounds.y+boxBounds.y+boxBounds.y)
				, \master, cs.fader, {|ez|

					names.do({|name|
						{
							masterMove=true;
							guis[name][\fader].valueAction_(
								cs.fader.unmap(ez.value)
								-ampFader[name]
							);
							masterMove=false;
						}.defer
					});

				}, 0, false
				, (boxBounds.y*0.5)//labelWidth
				, (boxBounds.y*0.5)//numberWidth
				, 0//unitwidth
				, (boxBounds.y)//labelheight, boxBounds.y
				//, gap: Point(0,0)
				//, layout: \vert
				,  \vert, Point(0,0), Point(0,0)
			).font_(font);
			guis.masterFader.sliderView.canFocus_(false); guis.masterFader.numberView.canFocus_(false);
			guis.masterMute=Button(g, masterFaderWidth@boxBounds.y).states_([["unmute",Color.grey, Color.white],["mute"]]).font_(font).canFocus_(false).action_{|b|
				inputBus.do({|ch,i| var g,h,name;
					//if (ch.class==Bus, {ch=ch.index});
					name=names[i];
					guis[name][\mute].valueAction_(b.value)

				})
			}.value_(1);
		});
		/*
		if (window.class==SCWindow, {
		c.parent.findWindow.addToOnClose({ this.close });
		});
		if (window.class==SCCompositeView, {
		window.parent.findWindow.addToOnClose({ this.close });
		});
		*/
	}

}
