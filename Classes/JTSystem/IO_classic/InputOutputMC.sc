/*
IOjtMC.soundin([0,1],nil,[Server.default, t])
zorg dat je ook bepaalde bussen/index aan een specifieke server kunt toewijzen, b.v.:
IOjtMC.soundin([[0,1],[2,4,5]], nil, [s,t])
- zorg ook dat het mogelijk is om automatisch servers aan te maken (hoe weet ik nog niet...)
- geef de synthdefs een unieke naam, door b.v. datum en tijd erin te zetten oid
- zorg dat die masterfader er eens fatsoenlijk uitziet man!
*/
IOjtMC {
	var <index, <target, <addAction, <type, <group, <bus, <busArray, <indexReRoute;
	var <soundfilepath, <server, <window, <window2, <path, <namesAndBusses, <inputsAndBusses;
	var <synthIn, <synthInput, <synthReRoute, <busInput, <groupSoundIn, <groupInput, <busses, <name, <meters, <recorder, <player;
	var heightFader, widthFader, widthMeter, heightBox, heightName, masterFader;
	var <outputBus;
	var <synthMix, <synthOut, <groupOutput, <inputBusses, mixChannels, mixBus, mixIndex;
	var <bounds, <faderBounds, <boxBounds;
	var showMasterFader, busReRoute;
	var showPlayer, showRecorder, guiFlag, ioFlag;
	var <guiO;

	*new {arg index, name, target=Server.default, addAction=\addBefore, mixIndex, window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=true, showMasterFader=false, type=\soundin, indexReRoute;
		//type: \in, \out, \bus, \soundin, \audioin, \ambi2, \ambi3
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type, indexReRoute);
	}

	init {arg argindex, argname, argtarget, argaddAction, argmixIndex, argwindow, argbounds, argfaderBounds, argboxBounds, argpath, showMeter, showFader, showPresets, argshowRecorder, argshowPlayer, argshowMasterFader, argtype, argindexReRoute;

		var audioin=0, soundin=0, audioout=0, tmpBus, tmp;

		guiFlag=true;
		ioFlag=true;
		guiO=();

		showPlayer=argshowPlayer;
		showRecorder=argshowRecorder;

		target=argtarget.asArray ?? {[Server.default]};
		server=target;

		//		if (server.serverRunning, {

		index=argindex ?? {[0,1]};
		if (index.size==0, {index=[index]});
		mixIndex=argmixIndex;//if nil then no mix output
		if (index.class==Bus, {index={|i| i+index.index }!index.numChannels});//convert Bus to array of bus indices
		indexReRoute=argindexReRoute;//if nil then no rerouting
		type=argtype.asSymbol??{\soundin};//type of IO
		if ([\in, \out, \audioout, \bus, \soundin, \audioin, \ambi2, \ambi3].includes(type).not, {type=\soundin});
		if ((type==\audioin), {audioin=1; soundin=1;});
		if ((type==\soundin) && (soundin==0), {soundin=1});//why check soundin==0????
		name=argname ?? {tmp=index; if (indexReRoute!=nil, {tmp=indexReRoute});
			tmp.collect({|i| (type.asSymbol++(i)).asSymbol })
		};
		if (name.class==String, {name=[name]});
		if (name.class==Symbol, {name=[name]});
		path=argpath??{
			if (thisProcess.nowExecutingPath!=nil,{
				thisProcess.nowExecutingPath.dirname ++ "/"
			},{
				"./"
			})
		};

		addAction=argaddAction ?? {(in: \addBefore, out: \addAfter, audioin: \addBefore, soundin: \addBefore)[type]};
		if (addAction==nil, {addAction=\addBefore});
		if (type==\audioout, {
			if (indexReRoute!=nil, {
				indexReRoute=indexReRoute-1
			});
			index=index-1;
			if (mixIndex!=nil, {mixIndex=mixIndex-1})
		});


		group=0!server.size;
		bus=0!server.size; busArray=0!server.size; namesAndBusses=0!server.size;
		inputsAndBusses=0!server.size; synthIn=0!server.size; busReRoute=0!server.size;
		synthReRoute=0!server.size;synthOut=0!server.size;

		server.do{|server, nr|
			group[nr]=Group(server, addAction).register;//allocate a Group for all the IO synths

			if ([\in, \audioin, \soundin].includes(type), {
				bus[nr]=Bus.audio(server, index.size);
				busArray[nr]={|i| i+bus[nr].index }!bus[nr].numChannels;//convert bus to an array
				synthIn[nr]=SynthDef(\Input_JT, {//arg index=#[0,1];
					Out.ar(busArray[nr].minItem, In.ar(NumOutputBuses.ir*soundin + index - audioin))
				}).play(group[nr], nil, \addToHead);// ++ args (index:index)
			},{
				busArray[nr]=index;
				bus[nr]=busArray[nr].minItem.asBus(\audio, busArray[nr].size, server);
			});

			namesAndBusses[nr]=();//dict with names and corresponding new bus-index
			inputsAndBusses[nr]=();//dict with inputbus and corresponding new bus-index
			index.do({|b,i|
				namesAndBusses[nr][name[i].asSymbol]=busArray[nr][i];
				inputsAndBusses[nr][b]=busArray[nr][i];
			});

			if (indexReRoute!=nil, {
				busReRoute[nr]=Bus.audio(server, index.size);
				synthReRoute[nr]=SynthDef(\ReRoute_JT, {
					Out.ar(busReRoute[nr], In.ar(index));
				}).play(group,nil,\addToTail);
				synthOut[nr]=SynthDef(\OutputR_JT, {
					var output, amp;
					output=In.ar(busReRoute[nr].index, busReRoute[nr].numChannels);
					amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));
					{|i| ReplaceOut.ar(indexReRoute[nr][i], output[i]*amp[i].lag(1.0))}!index.size;
				}).play(group[nr], nil, \addToTail).register;
				busArray[nr]=indexReRoute.copy;
			},{
				synthOut[nr]=SynthDef(\Output_JT, {
					var output, amp;
					output=In.ar(busArray[nr]);
					amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));
					ReplaceOut.ar(busArray[nr].minItem, output*amp.lag(1.0));
				}).play(group[nr], nil, \addToTail).register;
			});
		};


		{
			bounds=argbounds;
			faderBounds=argfaderBounds;
			boxBounds=argboxBounds;
			showMasterFader=argshowMasterFader;
			window=argwindow;

			if ([showMeter, showRecorder, showPlayer].collect({|i| i.binaryValue}).sum>0, {
				this.gui;
			},{guiFlag=false});

			if (showMeter, {
				if ([\in, \audioin, \soundin].includes(type), {
					meters=Meter_JT(server[0], window, busArray[0], busArray[0], name
						//, synthOut[0]
						, synthOut
						, showMasterFader, "/In", -70, 0, true, true, faderBounds: faderBounds, boxBounds: boxBounds, realInputBus: indexReRoute??{index})
				},{
					meters=Meter_JTMC(server, window, busArray, busArray, name, synthOut, showMasterFader, "/In", -70, 0, true, true, faderBounds: faderBounds, boxBounds: boxBounds, realInputBus: indexReRoute??{index})

				})

			});

			window2=Window(type, Rect(0,0, 259, 132)).front;
			window2.addFlowLayout;
			window2.visible_(false);
			window2.userCanClose_(false);
			window2.alwaysOnTop_(true);
			//});

			if (showRecorder, {
				recorder=Recorder_JT(server[0], window2, busArray[0],
					if ([\in, \audioin, \soundin].includes(type), {synthIn[0]},{group[0]})
					, path)});
			if (showPlayer, {
				player=Player_JTMC(server, window2, bus, group, path, mixIndex?0)

			});

		}.defer;

		//		}, {"please boot the server first".postln})


	}


	mix_ {|mix=true|
		if (mixIndex!=nil, {
			if (mix, {
				{guiO.mixButton.value_(1)}.defer;

				synthMix=server.collect{|server, nr|
					SynthDef(\OutputMix_JT, {|amp=1.0|
						Out.ar(mixIndex.minItem, SplayAz.ar(mixIndex.size, In.ar(busArray[nr])*amp.lag(1.0)))
					}).play(synthOut[nr], nil, \addAfter).register;
				}

			},{
				{guiO.mixButton.value_(0)}.defer;
				synthMix.do{|syn|
					if (syn.isPlaying, {syn.free})
				}
			})
		})
	}

	free {
		this.close
	}

	close {
		if (ioFlag, {
			server.do{|server, nr|
				group[nr].free;//also frees all including synths
				bus[nr].free;
				if (busReRoute[nr].class==Bus, {busReRoute[nr].free});
			};

			if (guiFlag, {

				if (window.isClosed.not, {window.onClose_({nil}); window.close});
				if (showPlayer, {player.close});
				window2.close;
			});

		});
		ioFlag=false;

		//ServerTree.remove(this.init);//werkt niet.... moet iets van een functie zijn oid
	}

	*in{arg index, name, target=Server.default, addAction=\addBefore, mixIndex, window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=true, showMasterFader=false, type=\in;
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type);
	}

	*out{arg index, name, target=Server.default, addAction=\addAfter, mixIndex=[0,1], window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=false, showMasterFader=true, type=\out,indexReRoute;
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type,indexReRoute);
	}

	*audioout{arg index, name, target=Server.default, addAction=\addAfter, mixIndex=[0,1], window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=false, showMasterFader=true, type=\audioout,indexReRoute;
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type,indexReRoute);
	}

	*soundin{arg index, name, target=Server.default, addAction=\addBefore, mixIndex, window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=true, showMasterFader=false, type=\soundin;
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type);
	}

	*audioin{arg index, name, target=Server.default, addAction=\addBefore, mixIndex, window, bounds=0@1000, faderBounds=20@120, boxBounds=40@10, path, showMeter=true, showFader=true, showPresets=false, showRecorder=true, showPlayer=true, showMasterFader=false, type=\audioin;
		^super.new.init(index, name, target, addAction, mixIndex, window, bounds, faderBounds, boxBounds, path, showMeter, showFader, showPresets, showRecorder, showPlayer, showMasterFader, type);
	}


	gui {
		var extra= ( ((showRecorder.binaryValue+showPlayer.binaryValue)>0) || (mixIndex!=nil) ).binaryValue;
		var width=boxBounds.x, height=boxBounds.y*5+faderBounds.y, masterFaderWidth=boxBounds.x*1.0, c, isWindow=false;
		var buttonWidth=9;
		//		var containerWidth=index.size*(width+4)+4+(showMasterFader.binaryValue*(masterFaderWidth+4))+(extra*buttonWidth)+12, containerHeight=(height+4)+4+8;
		var containerWidth=index.size*(width+4)+4+(showMasterFader.binaryValue*(masterFaderWidth+4))
		//+12
		, containerHeight=(height+4)+4+8;
		var height2=containerHeight+(showPlayer.binaryValue*132*0), width2=(showPlayer.binaryValue*259*0).max(containerWidth);
		guiFlag=true;


		if (window==nil, {
			isWindow=true;
			window=Window(type, Rect(bounds.x,bounds.y,containerWidth,containerHeight),false); window.view.decorator=FlowLayout(window.view.bounds, 0@0, 0@0);
			window.alwaysOnTop_(true);
			window.front;
		},{
			//window.alwaysOnTop=true;
			window=CompositeView(window, width2@height2); window.decorator=FlowLayout(window.bounds, 0@0, 0@0);
		});

		//		if ( (extra==1) || (mixIndex!=nil), {
		//c=CompositeView(window, buttonWidth@(buttonWidth*4)); c.addFlowLayout(0@0, 0@0);
		c=CompositeView(window, width2@(buttonWidth)); c.addFlowLayout(0@0, 0@0);

		//			});

		//c.getParents.last.findWindow.addToOnClose({this.close});

		window.onClose_({this.close});

		/*
		if (window.class==Window, {
		window.onClose_({this.close});
		},{
		//window.asView.getParents.last.findWindow.addToOnClose({this.close});
		});
		*/
		//		if (showMeter, {
		Button(c, buttonWidth@(buttonWidth*1.5)).states_([ [\p], [\p,Color.black, Color.blue]]).action_({|b| meters.synth.set(\peakHold, b.value) }).canFocus_(false).font_(Font("Helvetica", 9));
		Button(c, buttonWidth@(buttonWidth*1.5)).states_([ [\r]]).action_({|b| meters.synth.set(\t_resetPeak, 1) }).canFocus_(false).font_(Font("Helvetica", 9));
		//			});



		if ((showRecorder.binaryValue+showPlayer.binaryValue)>0, {

			guiO.playrecButton=Button(c, buttonWidth@(buttonWidth*1.5)).states_([ [\s],[\h] ]).action_({|but|
				//if (isWindow, {
				if (but.value==0, {
					//window.bounds_(Rect(window.bounds.left,window.bounds.top+(height2-containerHeight),containerWidth,containerHeight));
					window2.visible_(false)
				},{
					//window.bounds_(Rect(window.bounds.left,window.bounds.top-(height2-containerHeight),width2,height2));
					window2.visible_(true)
				})
				//})
			}).canFocus_(false).font_(Font("Helvetica", 9));
		});


		if (mixIndex!=nil, {
			guiO.mixButton=Button(c, buttonWidth@(buttonWidth*1.5)).states_([ ["+"],["+", Color.black, Color.red] ]).action_({|but|
				this.mix_(but.value.booleanValue)
			}).canFocus_(false).font_(Font("Helvetica", 9));
		});
	}
}