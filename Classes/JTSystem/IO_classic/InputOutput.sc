/*
- zorg dat je makkelijk de inputbussen (of outputbussen) kunnen veranderen met b.v. .set of synthIn.set en synthOut.set of .index (dus de soundin of audioin krijgt een bus argument bij de synth)
- maak preset systeem, met een settings file oid
- bouw master in per input kanaal (eq + compressor, minstens)
- bouw master in voor de totale output (eigenlijk alleen een compressor/limiter! MasterEQ kan de rest doen)

- Zorg dat CmdPeriod dit ding niet killt. evenals een s.killAll (oid).
	zie masterEQ, CmdPeriod.add, ServerTree, s.meter
- bouw ook gap en margin in!
- maak ook een variabele 'sync',

ideetje: (hoeft niet hoor, gebruiker kan dit ook met InOut zelf veranderen)
zorg ook dat dit kan:
[0,[1,2],3,4]
[1,2] krijgen dezelfde bus toegewezen
SynthDef(Out.ar(bus, In.ar(in)))

IOjt.soundin([0,1])
*/
IOjt {
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

		target=argtarget ?? {Server.default};
		server=target;
		if (target.class!=Server, {
			if ((target.class==Group) || (target.class==Synth), {
				server=target.server;
				},{
				server=Server.default;
				})
			});
		if (server.size>0, {"multicore!".postln});//this needs to be implemented!

		if (server.serverRunning, {
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

			group=Group(server, addAction).register;//allocate a Group for all the IO synths

			if (type==\audioout, {
				if (indexReRoute!=nil, {
					indexReRoute=indexReRoute-1
				});
				index=index-1;
				if (mixIndex!=nil, {mixIndex=mixIndex-1})
			});
			if ([\in, \audioin, \soundin].includes(type), {
				//bus=argbus??{Bus.audio(server, index.size)};//geef evt bus al mee als argument (voor b.v. multicore)
				bus=Bus.audio(server, index.size);//allocate a bus for all the inputs (why also for type==\in????)
				busArray={|i| i+bus.index }!bus.numChannels;//convert bus to an array
				synthIn=SynthDef(\Input_JT, {//arg index=#[0,1];
					Out.ar(busArray.minItem, In.ar(NumOutputBuses.ir*soundin + index - audioin))
					}).play(group, nil, \addToHead);// ++ args (index:index)
				},{
				busArray=index;
				bus=busArray.minItem.asBus(\audio, busArray.size);//or an array of single channel busses
			});

			namesAndBusses=();//dict with names and corresponding new bus-index
			inputsAndBusses=();//dict with inputbus and corresponding new bus-index
			index.do({|b,i|
				namesAndBusses[name[i].asSymbol]=busArray[i];
				inputsAndBusses[b]=busArray[i];
			});

			if (indexReRoute!=nil, {
				busReRoute=Bus.audio(server, index.size);
				synthReRoute=SynthDef(\ReRoute_JT, {
					Out.ar(busReRoute, In.ar(index));
					}).play(group,nil,\addToTail);
				synthOut=SynthDef(\OutputR_JT, {
					var output=In.ar(busReRoute.index, busReRoute.numChannels), amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));//waarom werkt kr niet????
					{|i| ReplaceOut.ar(indexReRoute[i], output[i]*amp[i].lag(1.0))}!index.size;//is this the most efficient to do this?????
					}).play(group, nil, \addToTail).register;
				busArray=indexReRoute.copy;
				},{
				synthOut=SynthDef(\Output_JT, {
					//haal NamedControl eruit!
					var output=In.ar(busArray), amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));//waarom werkt kr niet????
					ReplaceOut.ar(busArray.minItem, output*amp.lag(1.0));
					//var good=BinaryOpUGen('==', CheckBadValues.ar(output), 0);
					//ReplaceOut.ar(busArray.minItem, output*amp.lag(1.0)*good);
					}).play(group, nil, \addToTail).register;
				});

			{
				bounds=argbounds;
				faderBounds=argfaderBounds;
				boxBounds=argboxBounds;
				showMasterFader=argshowMasterFader;
				window=argwindow;

				if ([showMeter, showRecorder, showPlayer].collect({|i| i.binaryValue}).sum>0, {
					this.gui;
					},{guiFlag=false});

				if (showMeter, {meters=Meter_JT(server, window, busArray, busArray, name, synthOut, showMasterFader, "/In", -70, 0, true, true, faderBounds: faderBounds, boxBounds: boxBounds, realInputBus: indexReRoute??{index})});

				//if (showRecorder.binaryValue+showPlayer.binaryValue>0, {
					//height2=containerHeight+(showPlayer.binaryValue*132), width2=(showPlayer.binaryValue*259).max(containerWidth);

				window2=Window(type, Rect(0,0, 259, 132)).front; window2.addFlowLayout; window2.visible_(false); window2.userCanClose_(false); window2.alwaysOnTop_(true);
				//});

				if (showRecorder, {recorder=Recorder_JT(server, window2, busArray,
						if ([\in, \audioin, \soundin].includes(type), {synthIn},{group})
						, path)});
				if (showPlayer, {player=Player_JT(server, window2, bus, group, path, mixIndex?0)});
				}.defer;
			//ServerTree.add(this.init);//werkt niet.... moet iets van een functie zijn oid
			}, {"please boot the server first".postln})
		}

	/*
	allocateBus{arg argindex, argindexReRoute;

		argindexReRoute=indexReRoute??{argindexReRoute};
		argindex=index??{argindex};

		if (type==\audioout, {
			if (indexReRoute!=nil, {
				indexReRoute=indexReRoute-1
			});
			index=argindex-1;
			if (mixIndex!=nil, {mixIndex=mixIndex-1})
		},{
			index=argindex
		});

		if ([\in, \audioin, \soundin].includes(type), {
			if (bus.class==nil, {
				bus=Bus.audio(server, index.size);
				busArray={|i| i+bus.index }!bus.numChannels;//convert bus to an array
			});

			if (synthIn.isPlaying, {
				synthIn.set(\index, index);
				},{
				synthIn=SynthDef(\Input_JT, {//arg index=#[0,1];
					Out.ar(busArray.minItem, In.ar(NumOutputBuses.ir*soundin + index - audioin))
					}).play(group, nil, \addToHead);// ++ args (index:index)
				});
			},{
			busArray=index;
			bus=busArray.minItem.asBus(\audio, busArray.size);//or an array of single channel busses
		});

 		//namesAndBusses=();//dict with names and corresponding new bus-index
		//inputsAndBusses=();//dict with inputbus and corresponding new bus-index

		index.do({|b,i|
			namesAndBusses[name[i].asSymbol]=busArray[i];
			inputsAndBusses[b]=busArray[i];
		});

		if (indexReRoute!=nil, {
			if (busReRoute.class==nil, {busReRoute=Bus.audio(server, index.size)});
			if (synthOut.isPlaying, {
				synthReRoute.set(\busReRoute, busReRoute, \index, index);
				synthOut.set(\busReRoute, busReRoute, \indexReRoute, indexReRoute, \index, index);
			},{
				synthReRoute=SynthDef(\ReRoute_JT, {
					Out.ar(busReRoute, In.ar(index));
				}).play(group,nil,\addToTail);

				synthOut=SynthDef(\OutputR_JT, {
					var output=In.ar(busReRoute.index, busReRoute.numChannels), amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));
					{|i| ReplaceOut.ar(indexReRoute[i], output[i]*amp[i].lag(1.0))}!index.size;//is this the most efficient to do this?????
				}).play(group, nil, \addToTail);
			});
			busArray=indexReRoute.copy;

			},{
			if (synthOut.isPlaying, {
				synthOut.set(\busArray, busArray);
			},{
				synthOut=SynthDef(\Output_JT, {
					var output=In.ar(busArray), amp=NamedControl(\amp, 1.0.dup(index.size), 0.25.dup(index.size));
					ReplaceOut.ar(busArray.minItem, output*amp.lag(1.0));
				}).play(group, nil, \addToTail);
			})
		});

	}


	index_{|newIndex|
		this.allocateBus(newIndex);

		if (showMeter, {meters});
		if (showRecorder, {recorder});
		if (showPlayer, {player});

	}
	*/

	mix_ {|mix=true|
		if (mixIndex!=nil, {
			if (mix, {
				{guiO.mixButton.value_(1)}.defer;
				synthMix=	SynthDef(\OutputMix_JT, {|amp=1.0|
					Out.ar(mixIndex.minItem, SplayAz.ar(mixIndex.size, In.ar(busArray)*amp.lag(1.0)))
					}).play(synthOut, nil, \addAfter).register;
				},{
				{guiO.mixButton.value_(0)}.defer;
				if (synthMix.isPlaying, {synthMix.free})
				})
			})
		}

	free {
		this.close
		}

	close {
		if (ioFlag, {
			group.free;//also frees all including synths
			bus.free;
			if (busReRoute.class==Bus, {busReRoute.free});
			if (guiFlag, {
				//if (window.class!=Window, {window=window.getParents.last.findWindow});
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