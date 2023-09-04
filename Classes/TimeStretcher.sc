/*
af en toe nog een tik aan het begin of einde.... (testen met sinustoon)
bij rec indrukken, dan play en dan stop en dan play speelt ie niets....
zelf bij rec - play gaat het mis....
rec-stop-play gaat goed


bouw ook pre-record in (dus via delay)
bouw ook azDev in
de eerste keer rec-play gaat een beetje mis....
zorg dat bij stretch=1.0 gewoon een PlayBufCF oid wordt gebruikt
Bouw een preLevel in, voor de RecordBuf
verdeel de boel in vele functies (synthdefs etc) zodat je makkelijk meerdere versie kan maken (met overerving e.d.)
*/

TimeWarp {

	var <index, <target, <addAction, <parent, <bounds, <bufnums, <time, <maxRecTime, <server, ff, synthInput, synthRec, synthPlay, <guiO, <guiO2, channels, bus, outbus, <p, <pR, cs, csR, <>font, oscR, scrubtime, switch, cmdName;
	var collapsed, preDelay, expandview, id;

	*new {arg index=0, target=Server.default, addAction=\addAfter, outbus=0, maxRecTime=5.0, channels=2, parent, bounds=300@20, args, controlSpecs, font=Font("Helvetica", 9), collapsed=false;
		^super.new.init(index,target, addAction, outbus, maxRecTime, channels, parent, bounds, args, controlSpecs, font, collapsed)
		}

	init {arg argindex, argtarget, argaddAction, argoutbus, argmaxRecTime, argchannels, argparent, argbounds, argargs, argcontrolSpecs, argFont, argcollapsed;
		cmdName=('trig'++Date.localtime.stamp).asSymbol;
		preDelay=0.15;
		id=0;
		server=Server.default;
		index=argindex;
		target=argtarget ?? {Server.default};
		server=target;
		if (target.class!=Server, {
			if ((target.class==Group) || (target.class==Synth), {
				server=target.server;
				},{
				server=Server.default;
				})
			});
		addAction=argaddAction;
		outbus=argoutbus;
		maxRecTime=argmaxRecTime;
		scrubtime=maxRecTime;
		channels=argchannels;
		parent=argparent;
		bounds=argbounds;
		font=argFont;
		font.size=bounds.y*0.75;
		collapsed=argcollapsed;
		//font=Font(font.name, bounds.y*0.6);
		bufnums={Buffer.alloc(server,server.sampleRate*maxRecTime).bufnum}!2;
		time=1;
		ff=0;
		switch=false;
		p=(normalizer: 0.0, fadeIn:0.1, fadeOut:0.1, gate:1, start:0.0, end:1.0, timeDev:0.001, wsDev:0.01, rate:1.0, ws:0.15, overLap:4.0, amp:1.0, stretch:1.0, az:0.0, preLevel:0.0, lpIn:19000.0, hpIn:250, lpOut:19000, hpOut:20);
		if (argargs!=nil, {argargs.keysValuesDo({|key,val| p[key]=val})});

		oscR=OSCresponderNode(server.addr, cmdName, {|t,r,msg|
			if (msg[2]==id, {
				{guiO2.scrubber.value=msg[3]*(maxRecTime/scrubtime.clip(0,maxRecTime))}.defer;
				});
			}).add;

		csR=(preLevel: ControlSpec(0.0, 1.0), normalizer: ControlSpec(0,1,0,1), hpIn: \freq.asSpec, lpIn: \freq.asSpec);
		cs=(start: ControlSpec(0,1), end: ControlSpec(0,1), timeDev: ControlSpec(0.0001, 1.0, \exp), wsDev: ControlSpec(0.0001, 1.0, \exp), rate: ControlSpec(0.0625, 16.0, \exp), ws: ControlSpec(0.01, 1.0, \exp), overLap: ControlSpec(0.1, 100, \exp), stretch: ControlSpec(0.00001, 100.0, \exp), az: \bipolar.asSpec, amp: \amp.asSpec, fadeIn: ControlSpec(0.0, 30.0), fadeOut: ControlSpec(0.0, 30.0), hpOut: \freq.asSpec, lpOut: \freq.asSpec);

		if (argcontrolSpecs!=nil, {argcontrolSpecs.keysValuesDo({|key,val| cs[key]=val})});

		if (index<server.options.numInputBusChannels, {
			bus=Bus.audio(server,1);
			synthInput=SynthDef(\SoundIn_JT, {arg inBus,outBus;
				Out.ar(outBus,SoundIn.ar(inBus))
				}).play(target, [\inBus, index, \outBus, bus.index], \addToHead);
			synthInput.register;
			index=bus.index;
			});

		SynthDef(\Recorder_JT_TW, {arg bufnum=1, inBus=10, gate=1, fadeIn=0.1, fadeOut=1.0, preDelay=0.15, preLevel=0.0, normalizer=0.0, lookAheadTime=0.1, hpIn=250, lpIn=19000;
			var in=HPF.ar(LPF.ar(In.ar(inBus),lpIn),hpIn);
			var env=EnvGen.kr(Env.new([0,1,1,0],[fadeIn,preDelay,fadeOut], -4.0, 1),gate,doneAction:2);
			in=(1.0-normalizer)*in+Normalizer.ar(in,0.9*normalizer,lookAheadTime);
			in=DelayN.ar(in, preDelay, (preDelay-(normalizer*lookAheadTime)).max(0));
			RecordBuf.ar(in*env,bufnum,0,1.0,preLevel.lag(0.1),1,1);
			}).store;//add?

		SynthDef(\TimeStretch_JT, {arg fadeIn=0.1, fadeOut=0.1, gate=1, bufnum, start=0.0, end=1.0, timeDev=0.001, wsDev=0.01, rate=1.0, ws=0.15, overLap=4.0, amp=1.0, az=0.0, interp=4, outBus=0, stretch=1.0, envbufnum= -1, t_trig=1, lpOut=19000, hpOut=20, scrub=0.0, id=0, timeFraction=0.5, preDelay=0.15, zigzag=0;
			var pointer, durR=BufDur.kr(bufnum).reciprocal, output, env, ampl, isMoving, tmp, cleanPointer, clean;
			env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut),gate,doneAction:2);
			stretch=if (start<end, stretch, stretch.neg);

			tmp=start.copy;
			start=if (start>end, end, start);
			end=if (tmp>end, tmp, end);

			//pointer=Phasor.ar(t_trig,BufFrames.kr(bufnum).reciprocal*stretch, (start)*durR,(end)*durR, (start)*durR);

			pointer=Phasor.ar(0.0,BufFrames.kr(bufnum).reciprocal*stretch, (start)*durR, ((end-start).abs*zigzag+end)*durR, (start)*durR).fold2(end*durR);
			//pointer=Phasor.ar(0.0,BufFrames.kr(bufnum).reciprocal*stretch, (start)*durR, ((end-start).abs+start)*durR, (start)*durR);

			cleanPointer=pointer;
			isMoving=(stretch.abs.ceil.clip(0,1));
			//SendTrig.ar(Impulse.ar(10)*isMoving*gate, id, pointer);
			SendReply.ar(Impulse.ar(10)*isMoving*gate, cmdName, pointer, id);
			pointer=(isMoving*pointer)+((1.0-isMoving)*K2A.ar(scrub*timeFraction));
			pointer=pointer+WhiteNoise.ar(durR*timeDev);
			overLap=(stretch.abs.reciprocal*overLap).clip(overLap, 50);
			ampl=overLap.pow(-0.5).min(1.0);

			//clean=PitchShift.ar(BufRd.ar(1, bufnum, cleanPointer*BufFrames.kr(bufnum), 1), 0.1, rate, 0, rate.abs.ratiomidi.abs*0.01)*amp.lag(0.1);
			output=Warp1.ar(1, bufnum, pointer, rate.lag(0.1), ws, envbufnum, overLap.lag(0.1), wsDev, 1, amp.lag(0.1)*ampl);
			output=LPF.ar(output, rate.abs.lag(0.1).min(1)*0.49*SampleRate.ir);

			//output=XFade2.ar(clean, output, 	stretch.abs.ratiomidi.abs>1 * 2 - 1);
			output=HPF.ar(LPF.ar(output,lpOut.lag(1.0)),hpOut.lag(1.0));

			Out.ar(outBus, PanAz.ar(channels, output*env, az.lag(1.0)))
			}).store;	//add?
		{this.gui}.defer;
		}

	record_ {arg rec;
		guiO2.record.value=rec;
		if (rec==1, {
			ff=ff+1;
			time=Main.elapsedTime;
			synthRec=Synth(\Recorder_JT_TW, [\preDelay, preDelay, \bufnum, bufnums.wrapAt(ff), \inBus, index, \gate, 1, \fadeIn,0.1, \fadeOut, 0.1] ++ p.asKeyValuePairs, target, \addAfter).register;
		},{
			time=(Main.elapsedTime-time).min(maxRecTime);
			scrubtime=time;
			synthRec.set(\gate, 0);
			if (synthRec.isPlaying, {synthRec.set(\gate, 0)});
			if (synthPlay.isPlaying, {
				//better is start a new synth....
				//synthPlay.set(\bufnum, bufnums.wrapAt(ff), \start, preDelay, \end, time)
				synthPlay.set(\gate, 0.0);
				switch=true;
				this.playback_(1.0);
				});
			[\start, \end].do({|key| guiO[key].controlSpec.maxval=time });
			guiO[\start].valueAction=preDelay;
			guiO[\end].valueAction=time;
		})
	}

	playback_ {arg play;
		guiO2.playback.value=play;
		if (play==1, {
			if ( (synthPlay.isPlaying.not) || (switch), {
				synthPlay=Synth(\TimeStretch_JT, p.asKeyValuePairs++[\bufnum, bufnums.wrapAt(ff), \outBus, outbus, \timeFraction, time.clip(0,maxRecTime)/maxRecTime, \id, 10000.rand], target, \addBefore).register;
				synthPlay.set(\id, synthPlay.nodeID);
				id=synthPlay.nodeID;
				switch=false}
				/*
				hier moet je iets zetten, wat weet ik nog niet....
				,{

				}
				*/
				);
		},{
			if (synthPlay.isPlaying, {synthPlay.set(\gate,0)})
		})
	}


	set {arg ... args;
		//if (synthPlay.isRunning, {synthPlay.set(*args)});
		args.pairsDo({|key,val| guiO[key].valueAction=val});
	}

	close {
		oscR.remove;
		[synthInput, synthPlay, synthRec].do({|synth| if (synth.class==Synth, {if (synth.isPlaying, {synth.free})})});
		if (bus!=nil, {bus.free});
		bufnums.do({|buf| server.sendMsg(\b_free, buf)});
		//if (parent.window.isClosed.not, {parent.window.onClose_({nil}); parent.window.close});
		}

	gui {
		var button,slider,labelWidth=font.size*4.5, window;
		var totalHeight=(bounds.y+4)*(csR.size+cs.size+2);
		guiO=();
		guiO2=();

		if (parent==nil, {
			window=Window("TimeStretch", Rect(0,0,bounds.x+16, totalHeight+12)).front; window.asView.addFlowLayout;
			window.onClose_({this.close});
			//parent=CompositeView(window, (bounds.x+8)@((bounds.y+4)*(cs.size+1)+12));
			parent=ExpandView(window, (bounds.x+8)@(totalHeight), (bounds.x+8)@(bounds.y*4), false );
			},{
			//parent=CompositeView(parent, (bounds.x+8)@((bounds.y+4)*(csR.size+cs.size+1)+12));
			window=parent;
			parent.onClose=(parent.onClose.addFunc({ this.close}));
			parent=ExpandView(parent, (bounds.x+8)@(totalHeight), (bounds.x+8)@((bounds.y+4)*3+4), false );
			});
		bounds.x=bounds.x-12;
		parent.addFlowLayout; parent.background_(Color.grey);
		button=SmoothButton; slider=EZSlider;
		guiO2.record=button.new(parent, (bounds.x*0.25-4)@bounds.y).states_([[\record],[\record,Color.black,Color.red]]).canFocus_(false).action_({|b| this.record_(b.value)});
		guiO2.playback=button.new(parent, (bounds.x*0.25-4)@bounds.y).states_([[\play],[\play,Color.black,Color.green]]).canFocus_(false).action_({|b| this.playback_(b.value)});
		guiO2.pauser=button.new(parent, (bounds.x*0.25-4)@bounds.y).states_([[\pause],[\pause,Color.black,Color.yellow]]).canFocus_(false).action_({|b|
			if (b.value==1, {guiO2.scrubber.doAction});
			if (synthPlay.isPlaying, {
				synthPlay.set(\stretch,p[\stretch]*(1.0-b.value));
				});
			if (b.value==0, {
				if (synthPlay.isPlaying, {
					synthPlay.set(\t_trig, 1.0);
					server.sendBundle(0.1, [\n_set, synthPlay.nodeID, \start, p[\start]]);
					});
				});
			//if ((synthPlay.isPlaying) && (b.value==1), {synthPlay.set(\t_trig,1))});
			});
		guiO2.restart=button.new(parent, (bounds.x*0.25-4)@bounds.y).states_([["trig"]]).canFocus_(false).font_(font).action_({|b|
			if (synthPlay.isPlaying, {synthPlay.set(\t_trig,1)});
			});

		guiO2.scrubber=Slider.new(parent, bounds).action_({|sl|
			if (synthPlay.isPlaying, {synthPlay.set(\scrub, sl.value, \start, sl.value*scrubtime)});
			//guiO[\start].value=sl.value*time;
			if (guiO2.pauser.value==0, {guiO2.pauser.valueAction_(1.0)});

			}).canFocus_(false);

		cs.keys.asArray.sort.do({|key| var val=cs[key];
			guiO[key]=slider.new(parent,bounds,key,val,{|ez| p[key]=ez.value; if (synthPlay.isPlaying, {synthPlay.set(key,ez.value)}) },p[key],false,labelWidth).font_(font);
			guiO[key].sliderView.canFocus_(false); guiO[key].numberView.canFocus_(false);
		});
		csR.keys.asArray.sort.do({|key| var val=csR[key];
			guiO[key]=slider.new(parent,bounds,key,val,{|ez| p[key]=ez.value; if (synthRec.isPlaying, {synthRec.set(key,ez.value)}) },p[key],false,labelWidth).font_(font);
			guiO[key].sliderView.canFocus_(false); guiO[key].numberView.canFocus_(false);
			});

		if (collapsed, {parent.collapse});
		/*
		if (parent.class==SCWindow, {
			parent.view.decorator.nextLine;
			parent.autoscaleY;
			parent.onClose_({this.close});
			},{
			parent.view.decorator.nextLine;
			//parent.view.getParents.last.findWindow.addToOnClose({this.close});
			window.postln; window.class.postln;
			window.onClose_({this.close});
			});
		*/
	}

}


PVWarp {

	var <index, <target, <addAction, <parent, <bounds, <bufnums, <time, <maxRecTime, <server, ff, synthInput, synthRec, synthPlay, <guiO, channels, bus, outbus, p, cs, ws, hop, font;

	*new {arg index, target, addAction, outbus=0, maxRecTime=5, ws=2048, hop=0.25, channels=2, parent, bounds=300@20, args, controlSpecs, font=Font("Helvetica", 9);
		^super.new.init(index,target, addAction, outbus, maxRecTime, ws, hop, channels, parent, bounds, args, controlSpecs, font)
		}

	init {arg argindex, argtarget, argaddAction, argoutbus, argmaxRecTime, argws, arghop, argchannels, argparent, argbounds, argargs, argcontrolSpecs, argFont;
		server=Server.default;
		index=argindex;
		target=argtarget ?? {Server.default};
		server=target;
		if (target.class!=Server, {
			if ((target.class==Group) || (target.class==Synth), {
				server=target.server;
				},{
				server=Server.default;
				})
			});
		addAction=argaddAction;

		outbus=argoutbus;
		maxRecTime=argmaxRecTime;
		ws=argws;
		hop=arghop;
		channels=argchannels;
		parent=argparent;
		bounds=argbounds;
		font=argFont;
		bufnums={Buffer.alloc(server,maxRecTime.calcPVRecSize(ws,hop)).bufnum}!2;
		time=1;
		ff=0;

		p=(fadeIn:0.1, fadeOut:0.1, gate:1, start:0.0, end:1.0, timeDev:0.001, rate:1.0, amp:1.0, stretch:1.0, az:0.0, preLevel:0.0);

		if (argargs!=nil, {argargs.keysValuesDo({|key,val| p[key]=val})});
		cs=(start: ControlSpec(0,1), end: ControlSpec(0,1), timeDev: ControlSpec(0.0001, 1.0, \exp), rate: ControlSpec(0.0625, 16.0, \exp), stretch: ControlSpec(0.00001, 100.0, \exp), az: \bipolar.asSpec, amp: \amp.asSpec, fadeIn: ControlSpec(0.0, 30.0), fadeOut: ControlSpec(0.0, 30.0), preLevel: ControlSpec(0.0, 1.0));
		if (argcontrolSpecs!=nil, {argcontrolSpecs.keysValuesDo({|key,val| cs[key]=val})});

		if (index<server.options.numInputBusChannels, {
			bus=Bus.audio(server,1);
			synthInput=SynthDef(\SoundIn_JT, {arg inBus,outBus;
				Out.ar(outBus,SoundIn.ar(inBus))
				}).play(target, [\inBus, index, \outBus, bus.index], \addToHead);
			synthInput.register;
			index=bus.index;
			});

		SynthDef(\PVRec_JT, {arg bufnum=1, inBus=10, gate=1, fadeIn=0.1, fadeOut=1.0, ws=2048, hop=0.25, preDelay=0.1, preLevel=0.0, recTime=1.0, decayTime=0.0;
			var chain,in;
			var env=EnvGen.kr(Env.new([0,1,1,0],[fadeIn,preDelay,fadeOut], -4.0, 1),gate,doneAction:2);
			in=DelayN.ar(In.ar(inBus)*env, preDelay, preDelay);
			//in=BufCombN.ar(LocalBuf(SampleRate.ir*recTime), in, recTime, preLevel, in);
			chain=FFT(LocalBuf(ws), in, hop, 1);
			chain=PV_RecordBuf(chain, bufnum, 0, 1, 1, hop, 1);
			}).store;

		SynthDef(\PVPlay_JT, {arg fadeIn=0.1, fadeOut=0.1, gate=1, bufnum, start=0.0, end=1.0, ws=2048, hop=0.25, rate=1.0, amp=1.0, az=0.0, outBus=0, stretch=1.0, maxRecTime=5.0, timeDev=0.001;
			var pointer, durR=maxRecTime.reciprocal, output, env, ampl, chain;
			var factor=1.0.calcPVRecSize(ws,hop).reciprocal;
			env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut),gate,doneAction:2);
			stretch=if (start<end, stretch, stretch.neg);
			pointer=Phasor.ar(1
				//,SampleRate.ir.reciprocal*maxRecTime*stretch
				//, SampleRate.ir.reciprocal*stretch*hop
				, hop.reciprocal*BufFrames.ir(bufnum).reciprocal*stretch

				, start*durR,end*durR, start*durR)+WhiteNoise.ar(durR*timeDev);
			chain=PV_BufRd(LocalBuf(ws).clear,bufnum,pointer);
			chain=PV_BinShift(chain,rate,0);
			output=IFFT(chain)*amp.lag(0.1);

			Out.ar(outBus, PanAz.ar(channels, output*env, az.lag(0.1)))
			}).store;
		this.gui;
		}

	record_ {arg rec;
		var decayTime=maxRecTime/(p.preLevel.log/0.001.log);
		guiO.record.value=rec;
		if (rec==1, {
			ff=ff+1;
			time=Main.elapsedTime;
			synthRec=Synth(\PVRec_JT, [\bufnum, bufnums.wrapAt(ff), \inBus, index, \gate, 1, \fadeIn,0.1, \fadeOut, 0.1, \ws, ws, \hop, hop], target, \addAfter).register;
		},{
			time=(Main.elapsedTime-time).min(maxRecTime);
			if (synthRec.isPlaying, {synthRec.set(\gate, 0)});
			if (synthPlay.isPlaying, {synthPlay.set(\bufnum, bufnums.wrapAt(ff), \start, 0, \end, time)});//start wellicht een nieuwe synth en stop de vorige
			[\start, \end].do({|key| guiO[key].controlSpec.maxval=time });
			guiO[\start].valueAction=0;guiO[\end].valueAction=time;
		})
	}

	playback_ {arg play;
		guiO.playback.value=play;
		if (play==1, {
			if (synthPlay.isPlaying.not, {synthPlay=Synth(\PVPlay_JT, p.asKeyValuePairs++[\bufnum, bufnums.wrapAt(ff), \outBus, outbus, \ws, ws, \hop, hop], target, \addBefore).register});
		},{
			if (synthPlay.isPlaying, {synthPlay.set(\gate,0)})
		})
	}


	set {arg ... args;
		if (synthPlay.isRunning, {synthPlay.set(*args)});
		args.pairsDo({|key,val| guiO[key].value=val});
	}

	close {
		[synthInput, synthPlay, synthRec].do({|synth| if (synth.class==Synth, {if (synth.isPlaying, {synth.free})})});
		if (bus!=nil, {bus.free});
		bufnums.do({|buf| server.sendMsg(\b_free, buf)});
		}

	gui {
		var button,slider;
		guiO=();
		if (parent==nil, {
			parent=Window("TimeStretch", Rect(0,0,bounds.x+8, (bounds.y+4)*cs.size+12)).front;
			},{
			parent=CompositeView(parent, (bounds.x+8)@((bounds.y+4)*(cs.size+1)+12));
			});
		parent.addFlowLayout; parent.background_(Color.grey);
		button=SmoothButton; slider=EZSlider;
		guiO.record=button.new(parent, (bounds.x*0.5-8)@bounds.y).states_([[\record],[\record,Color.black,Color.red]]).canFocus_(false).action_({|b| this.record_(b.value)});
		guiO.playback=button.new(parent, (bounds.x*0.5-8)@bounds.y).states_([[\play],[\play,Color.black,Color.green]]).canFocus_(false).action_({|b| this.playback_(b.value)});
		cs.keys.asArray.sort.do({|key| var val=cs[key];
//		cs.keysValuesDo({|key,val|

			guiO[key]=slider.new(parent,bounds,key,val,{|ez| p[key]=ez.value; if (synthPlay.isPlaying, {synthPlay.set(key,ez.value)}) },p[key], false, 40, 40 ).font_(font)});

		if (parent.class==Window, {
			parent.view.decorator.nextLine;
			parent.autoscaleY;
//			parent.onClose_({this.close});
			},{
			parent.decorator.nextLine;
//			parent.getParents.last.findWindow.addToOnClose({this.close});
			});
	}

}

/*
b=Bus.audio(s,1);
x={Out.ar(b,SoundIn.ar(0))}.play;
//w=Window.new; w.front; w.addFlowLayout;
t=TimeWarp(0, s, maxRecTime:2.0)

PVWarp(0,s,ws:1024,hop:0.5)


*/