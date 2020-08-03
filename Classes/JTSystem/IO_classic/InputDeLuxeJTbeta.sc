/*
id inbouwen of juist alleen te gebruiken als multi input???
*/

InputDeLuxeJTbeta {
	var <id, flaggy;
	var <synth, <bus, <input, <group;
	var <settings, <guis;
	var width, height, numberHeight, totalWidth, parent, path, inputChannel, <server;
	var <playrecWindow, player, recorder, updateFreq, settingsFolder, soundfilesFolder;
	var <>meters, <dBLow, name;
	var <bypassCompressor, <bypassEQ;

	*new {arg inputChannel=1, server=Server.default, parent, bounds=20@200, name, sync=false, settingsFolder, soundfilesFolder, updateFreq=20, dBLow=80.neg;
		^super.new.init(inputChannel, server, parent, bounds, name, sync, settingsFolder, soundfilesFolder, updateFreq, dBLow);
	}

	init {arg arginputChannel, argserver, argparent, bounds, argname, sync, argsettingsFolder, argsoundfilesFolder, argupdateFreq=15, argdBLow=80.neg;
		var routine;
		var inputDef, soundfilesFolderExists=false;

		flaggy=true;
		inputChannel=arginputChannel;
		updateFreq=argupdateFreq;
		dBLow=argdBLow;
		/*
		soundfilesFolder = if (argsoundfilesFolder.class==String, {
			if (File.exists(argsoundfilesFolder), {
				argsoundfilesFolder
			},{
				PathName("~/Desktop/").fullPath
			})
		},{
			PathName("~/Desktop/").fullPath
		});
		*/
		width=bounds.x;//dit kan in gui
		height=bounds.y;
		numberHeight=bounds.x*2/3;
		totalWidth=(10+width+15);
		parent=argparent;
		name=argname??{inputChannel.asArray};

		if ((name.class==String) || (name.class==Symbol), {name=[name]});
		bypassCompressor={false}!inputChannel.asArray.size;
		settings=();
		guis={()}!inputChannel.asArray.size;
		server=argserver;
		//-------------------------------------------------------------  SETTINGS FILES
		path=thisProcess.nowExecutingPath;
		settingsFolder=argsettingsFolder;

		if (path!=nil, {

			soundfilesFolder=argsoundfilesFolder??{thisProcess.nowExecutingPath.dirname++"/recordings/"};
			if (File.exists(soundfilesFolder).not, {
				("mkdir "++soundfilesFolder).unixCmd;
				0.1.wait;//wacht zolang de folder nog in de maak is....
			});

			settingsFolder=argsettingsFolder??{thisProcess.nowExecutingPath.dirname++"/settings/"};
			if (File.exists(settingsFolder).not, {
				("mkdir "++settingsFolder).unixCmd;
				0.1.wait;//wacht zolang de folder nog in de maak is....
			});
			inputChannel.asArray.do{|ch,i|
				["compressor"].do{|fx|
					var file, settingspath;
					settingspath=settingsFolder++fx++i++".scd";
					settings[(fx++i).asSymbol]=();
					if (File.exists(settingspath).not, {
						file=File(settingspath, "w");
						file.write( ().asCompileString );
						file.close;
					},{
						file=File(settingspath, "r");
						settings[(fx++i).asSymbol]=file.readAllString.interpret;
						file.close;
					});
				};
			};
		},{
			inputChannel.asArray.do{|ch,i|
				["compressor"].do{|fx|
					settings[(fx++i).asSymbol]=();
			}}

		});
		group={0}!server.asArray.size;
		synth={ {0}!inputChannel.asArray.size }!server.asArray.size;
		bus={ 0 }!server.asArray.size;
		input={ 0 }!server.asArray.size;
		//-------------------------------------------------------------  SYNTHDEFS INIT
		routine={
			[1,2].do{|i|
				SynthDef((\Compressor_JT++i).asSymbol, {arg inBus, at=0.01, rt=0.1, thresh=0.1, slopeAbove=1.0, amp=1.0, knee=6, makeUp=0.0, mute=1;
					var in=In.ar(inBus, i);
					makeUp=((thresh.ampdb.neg * ( 1 - slopeAbove )) * makeUp ).dbamp;
					ReplaceOut.ar(inBus,
						Compander.ar(in, DelayN.ar(in, at), thresh, 1.0, slopeAbove, at, rt
							, amp*mute*makeUp)
						//SoftKneeCompressor.ar(in, DelayN.ar(in, at), thresh.ampdb, slopeAbove, knee, at, rt, makeUp, 0)*amp
					)
				}).add;
			};
			inputDef=(\Input_JT++(inputChannel.asArray.flat.size)).asSymbol;


			server.asArray.do{|s, i|
				//-------------------------------------------------------------  SYNTHS INIT
				group[i]=Group(s, \addBefore).register;//allocate a Group for all the IO synths
				bus[i]=Bus.audio(s, inputChannel.asArray.flat.size);
				if (sync, {s.sync});

				input[i]=SynthDef(inputDef, {arg outBus;
					Out.ar(outBus, SoundIn.ar(inputChannel.asArray.flat))//hier staan -1 !
				}).play(group[i], [\outBus, bus[i]], \addToHead).register;
				if (sync, {s.sync});
				inputChannel.asArray.flat.size.do{|j|
					synth[i][j]=Synth.after(input[i], \Compressor_JT1
						, [\inBus, bus[i].index+j]
					).register;
					if (sync, {s.sync});
				};
			};

		};
		if (sync, {
			routine.value;
			{this.gui}.defer;
			while( {flaggy}, {0.001.wait});
		}, {
			sync=true;
			{
				routine.value;
				{this.gui}.defer;
				while({flaggy}, {0.001.wait});
			}.fork
		});
	}

	bypass {arg voice=0, bypass=false;
		var compressor=("compressor"++voice).asSymbol;
		server.asArray.do{|s, i|
			synth[i][voice].run(bypass.not);
		};
		if (meters[voice][0].class==EZLevelIndicator, {
			{
				meters[voice][0].synth.server.sync;
				meters[voice][0].synth.run(bypass.not);
				if (bypass, {
					{meters[0][0].levelIndicator[0].value=0}.defer;
				})
			}.fork
		});
		bypassCompressor[voice]=bypass;
		settings[compressor][\bypass]=bypass.binaryValue;
		{guis[voice][\bypass].value_(bypass.not.binaryValue)}.defer;
	}

	update {
		/*
		if (server.asArray.size==1, {
		input=input.flat[0];
		synth=synth.flat;
		bus=bus.flat[0];
		if (inputChannel.asArray.size==1, {
		synth=synth[0];
		});
		},{
		if (inputChannel.asArray.size==1, {
		synth=synth.collect{|syn| syn.unbubble};
		});
		});
		*/
		flaggy=false;
	}

	//-------------------------------------------------------------  GUI
	gui {
		var v, c, i, j;

		if (parent==nil, {
			parent=Window("SoundIn", Rect(0,0,totalWidth*inputChannel.asArray.size+16,(height+40))).front;
			parent.addFlowLayout(4@4, 0@0);
			parent.alwaysOnTop_(true);
		});

		parent.onClose_(parent.onClose.addFunc({
			//input.do{|in| in.do(_.free)};
			//synth.do{|syn| syn.do(_.free)};
			bus.do{|b| b.do(_.free)};
			v.do(_.close);
			group.do(_.free);
			playrecWindow.close;
			inputChannel.asArray.do{|ch,i|
				["compressor"].do{|fx|
					var settingspath=settingsFolder++fx++i++".scd", file;
					file=File(settingspath, "w");
					file.write( settings[(fx++i).asSymbol].asCompileString );
					file.close;
				};
			};
		}));
		parent=CompositeView(parent, (totalWidth*inputChannel.asArray.size+16)@(height+40));
		parent.addFlowLayout(0@0, 0@0);

		v={|i| Window("compressor "++i, Rect(0,0,400,120))}!inputChannel.asArray.size;
		v.do{|v|
			v.addFlowLayout;
			v.alwaysOnTop_(true);
			v.visible_(false);
			v.userCanClose_(false);
		};
		playrecWindow=Window("playrec", Rect(0,1000,262,134)).front; playrecWindow.alwaysOnTop_(true);
		playrecWindow.addFlowLayout;
		recorder=Recorder_JT(
			server.asArray[0], playrecWindow, bus[0].indices, input[0], soundfilesFolder);
		player=Player_JTMC(server, playrecWindow, bus
			, synth.collect({|syn| syn[0].group}), soundfilesFolder, 0);

		playrecWindow.visible_(false);
		playrecWindow.userCanClose_(false);

		meters={[0,0]}!inputChannel.asArray.size;

		inputChannel.asArray.flat.size.do{|i|
			var compressor=("compressor"++i).asSymbol, c;
			c=CompositeView(parent, (totalWidth+2)@(height+20));
			c.addFlowLayout(0@0, 0@0); c.background_(Color.white);

			meters[i][0]=EZLevelIndicator(c, 5@height
				, bus[0].index+i
				, input[0]
				, updateFreq:updateFreq, dBLow:dBLow
				, showValue:false);
			/*
			meters[i][0].synth;
			input[0].server.sync;
			if (bypassCompressor[i].not, {
			meters[i][0].synth.run(bypassCompressor[i].not)
			});
			*/
			guis[i][\threshSlider]=EZSlider(c, 5@height, nil, ControlSpec(dBLow, 0, 0, 1), {|ez|
				guis[i][\threshKnob].value_(ez.value);
				synth.do{|syn| syn[i].set(\thresh, ez.value.dbamp)};
				settings[compressor][\thresh]=ez.value;
			}, 0, false, 0, numberHeight*0, 0, numberHeight*0
			, \vert, 0@0, 0@0).font_(Font("Helvetica",15/2));
			guis[i][\threshSlider].sliderView.thumbSize_(0);
			guis[i][\threshSlider].sliderView.canFocus_(false);
			guis[i][\threshSlider].numberView.canFocus_(false);

			meters[i][1]=EZLevelIndicator(c, width@height
				, bus[0].index+i, synth[0][i], updateFreq:updateFreq, dBLow:dBLow
				, showValue:true);

			guis[i][\amp]=EZSlider(c, 15@height, nil, ControlSpec(dBLow, 20, 0, 1), {|ez|
				synth.do{|syn| syn[i].set(\amp, ez.value.dbamp)};
				settings[compressor][\amp]=ez.value;
			}, 0, false, 0, numberHeight, 0, numberHeight, \vert, 0@0, 0@0).font_(Font("Helvetica",15/2));
			guis[i][\amp].sliderView.canFocus_(false);
			guis[i][\amp].numberView.canFocus_(false);

			StaticText(c, totalWidth@10).string_(name[i]).font_(Font("Helvetica", 10)).align_(\center).background_(Color.white);

			Button(c, totalWidth/2@10).states_([ [\show,Color.black,Color.white],[\hide,Color.black,Color.white] ]).font_(Font("Helvetica", 15/2)).canFocus_(false).action_{|b|
				if (b.value==1, {
					v[i].visible_(true);
				},{
					v[i].visible_(false);
				})
			};
			Button(c, totalWidth/2@10).states_([ [i.asString++"M",Color.grey,Color.white],[i,Color.black,Color.white] ]).font_(Font("Helvetica", 15/2)).canFocus_(false).action_{|b|
				synth.do{|synth| synth[i].set(\mute, b.value)}
			}.value_(1);

			guis[i][\bypass]=Button(v[i], 50@50).states_([ [\on], [\on, Color.black, Color.green] ]).action_{|b|
				this.bypass(i, b.value<1);
				settings[compressor][\bypass]=1-b.value;
			}.canFocus_(false);
			guis[i][\threshKnob]=EZKnob(v[i], 50@100, \thresh, ControlSpec(dBLow, 0, 0, 0.1), {|ez|
				guis[i][\threshSlider].valueAction_(ez.value)
			}, settings[compressor][\thresh]??{-20}, false);
			guis[i][\threshKnob].doAction;
			EZKnob(v[i], 50@100, \ratio, ControlSpec(1.0, 30.0, \exp, 0.1), {|ez|
				synth.do{|synth| synth[i].set(\slopeAbove, 1/ez.value)};
				settings[compressor][\ratio]=ez.value;
			}, settings[compressor][\ratio]??{5.0}, true);
			EZKnob(v[i], 50@100, \at, ControlSpec(0.0, 200.0, 2.0, 0.5), {|ez|
				settings[compressor][\at]=ez.value;
				synth.do{|synth| synth[i].set(\at, ez.value/1000)}}, settings[compressor][\at]??{10}, true);
			EZKnob(v[i], 50@100, \rt, ControlSpec(1, 5000.0, 2.0, 0.5), {|ez|
				settings[compressor][\rt]=ez.value;
				synth.do{|synth| synth[i].set(\rt, ez.value/1000)}}, settings[compressor][\rt]??{50}, true);
			EZKnob(v[i], 50@100, \makeup, ControlSpec(dBLow, 0.0, -3.5, 1), {|ez|
				settings[compressor][\makeUp]=ez.value;
				synth.do{|synth|synth[i].set(\makeUp, ez.value.dbamp)}}
			, settings[compressor][\makeUp]??{-4.0}, true);
			EZKnob(v[i], 50@100, \knee, ControlSpec(0, 20.0, 0.0), {|ez|
				settings[compressor][\knee]=ez.value;
				synth.do{|synth|synth[i].set(\knee, ez.value)}}, settings[compressor][\knee]??{0.0}, true);

		};
		Button(parent, totalWidth@10).states_([ ["show recplay"],["hide recplay"] ]).action_{|b|
			playrecWindow.visible_(b.value.binaryValue)
		}.canFocus_(false).font_(Font("Helvetica", 8));

		this.update;
		/**/
		//		v.minimize;
	}

}
