Freezer {

	var freqBus, <>index, <target, <addAction, <parent, <bounds, <bufnums, <time, <server, <ff, synthInput, <synthFreeze, <guiO, <guiO2, channels, bus, outbus, <p, <cs, <>ws, <>hop, font, <ready;
	var collapsed;

	*new {arg index, target, addAction=\addAfter, outbus=0, ws=4096, hop=0.125, channels=2, parent, bounds=300@20, args, controlSpecs, font=Font("Helvetica", 9), collapsed=false, action;
		^super.new.init(index,target, addAction, outbus, ws, hop, channels, parent, bounds, args, controlSpecs, font, collapsed, action)
	}

	init {arg argindex, argtarget, argaddAction, argoutbus, argws, arghop, argchannels, argparent, argbounds, argargs, argcontrolSpecs, argFont, argcollapsed, argaction;
		server=Server.default;
		index=argindex;
		ready=false;
		//if (index.class==Bus, {index=index.index});

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
		ws=argws;
		hop=arghop;
		channels=argchannels;
		parent=argparent;
		bounds=argbounds;
		font=argFont;
		collapsed=argcollapsed;
		font.size=bounds.y*0.75;
		freqBus={Bus.control(server,1).set(0)}!2;
		time=1;
		ff=0;
		synthFreeze=[nil,nil];

		p=(
			//pitchDev: 0.005, timeDev:0.1, rate:1.0, windowSize:1.0
			magAbove:0.0, fadeIn:1.0, fadeOut:1.0, gate:1
			, amp:1.0
			//, spread:1.0, az:0.0
			, preLevel:0.0, lpIn:19000, hpIn:200, lpOut:19000, hpOut:20, normalizer:1.0
			, shiftDepth:0.0
		);

		if (argargs!=nil, {argargs.keysValuesDo({|key,val| p[key]=val})});

		//stop die cs in de metadata van de synthdef
		cs=(
			magAbove: ControlSpec(0.1,1200,\exp)
			//, pitchDev: ControlSpec(0.0001, 1.0, \exp), timeDev: ControlSpec(0.0001, 1.0, \exp), rate: ControlSpec(0.125, 4.0, \exp), windowSize: ControlSpec(0.01,1.0,\exp)
			, amp: \amp.asSpec, fadeIn: ControlSpec(0.1, 30.0,\exp), fadeOut: ControlSpec(0.1, 30.0,\exp), shiftDepth: \unipolar.asSpec, normalizer: \amp.asSpec
			//, az: \bipolar.asSpec, spread: ControlSpec(0.1,1.0)
			, lpIn: \freq.asSpec, hpIn: \freq.asSpec, lpOut: \freq.asSpec, hpOut: \freq.asSpec
		);
		if (argcontrolSpecs!=nil, {argcontrolSpecs.keysValuesDo({|key,val| cs[key]=val})});

		//laat die aparte SoundIn weg, stop 'm in de freeze synthdef met een if statement


		if (index<server.options.numInputBusChannels, {
			bus=Bus.audio(server,1);
			synthInput=SynthDef(\SoundIn_JT, {arg inBus,outBus;
				Out.ar(outBus,SoundIn.ar(inBus))
			}).play(target, [\inBus, index, \outBus, bus.index], \addToHead);
			synthInput.register;
			index=bus.index;
			//target=synthInput
			if (target==nil, {target=synthInput});
			//addAction=\addAfter;
			if (addAction==nil, {addAction=\addAfter});
		});
		if (addAction==nil, {addAction=\addAfter});

		SynthDef(\Freezer_JT, {arg hpIn=200, lpIn=20000, hpOut=100, lpOut=20000, inBus=10, outBus=0, gate=1, fadeIn=1.0, fadeOut=1.0, ws=2048, hop=0.25,az=0.0,amp=1.0,timeDev=0.0,pitchDev=0.0,spread=1.0,normalizer=1.0, windowSize=1.0, freqBus=100, shift=0.0, magAbove=0.0, shiftDepth=1.0, rate=1.0;
			var chain,in,out,freq,ampl, hasFreq;
			//var env=EnvGen.kr(Env.new([0.0,0,1,1,0],[ws/44100,fadeIn,1.0,fadeOut], -4.0, 1),gate,doneAction:2);
			var env=EnvGen.kr(Env.dadsr(ws/SampleRate.ir,fadeIn,0,1,fadeOut,1),gate,doneAction:2);
			in=In.ar(inBus,1);
			in=HPF.ar(LPF.ar(in,lpIn),hpIn);
			in=(1.0-normalizer)*in+Normalizer.ar(in,normalizer*0.9);
			chain=FFT(LocalBuf(ws),in,hop);
			chain=PV_Freeze(chain, EnvGen.kr(Env.new([0,0,1],[ws/SampleRate.ir*2.0, 0.01],0)));
			#freq,ampl=FFTPeak.kr(chain);
			chain=PV_MagAbove(chain,magAbove.lag(1.0));
			out=IFFT(chain)*hop.sqrt*0.45;

			//			freq.poll(chain);
			Latch.kr(freq,chain);

			out=FreqShift.ar(out,((shift-freq).lag(0.1)*(1.0-env)).clip(-1000,1000)*shiftDepth);
			out=HPF.ar(LPF.ar(out,lpOut.lag(0.2)),hpOut.lag(0.2));
			//out=SplayAz.ar(channels, {PitchShift.ar(out,windowSize, rate, pitchDev, timeDev, timeDev.pow(0.1).lag(0.4))}!channels.max(2), spread, center:az.lag(1.0)) + PanAz.ar(channels, out, az.lag(1.0));
			out=PanAz.ar(channels, out, az.lag(1.0));
			Out.kr(freqBus, freq);
			Out.ar(outBus, env*out*amp.lag(0.1))
		}).store;

		{
			this.gui;
			ready=true;
			argaction.value;
		}.defer;
	}


	freeze_ {arg freeze=1;
		var prevFF=ff;
		if (synthFreeze[ff]!=nil, {if (synthFreeze[ff].isRunning, {synthFreeze[ff].set(\gate,0)})});
		ff=(ff+1)%2;

		if (freeze>0, {
			synthFreeze[ff]=Synth(\Freezer_JT, [\outBus, outbus, \freqBus, freqBus[ff], \ws,ws,\hop,hop,\inBus,index]++p.asKeyValuePairs, target, addAction).register;
			synthFreeze[ff].map(\shift, freqBus[prevFF]);
		});
	}


	set {arg ... args;
		//if (synthFreeze[ff].isRunning, {synthFreeze[ff].set(*args)});
		args.pairsDo({|key,val|
			p[key]=val;
			{guiO[key].valueAction=val}.defer
		});
	}

	close {
		[synthFreeze[0],synthFreeze[1],synthInput].do({|synth| if (synth.class==Synth, {if (synth.isPlaying, {synth.free})})});
		if (bus!=nil, {bus.free});
		//if (parent.window.isClosed.not, {parent.window.onClose_({nil}); parent.window.close});
	}

	gui {
		var button,slider,labelWidth=font.size*5.5, window, totalHeight=(bounds.y+4)*(cs.size+1);
		guiO=();guiO2=();
		window=parent;

		if (parent==nil, {
			window=Window("Freezer", Rect(0,0,bounds.x+12, totalHeight+12)).front;
			window.onClose_({this.close});
			parent=ExpandView(window, (bounds.x+8)@(totalHeight), (bounds.x+8)@((bounds.y+4)*2+4), false );
		},{
			//parent=CompositeView(parent, (bounds.x+8)@((bounds.y+4)*(cs.size+1)));
			//parent.addToOnClose({this.close});
			parent.onClose=(parent.onClose.addFunc({ this.close}));
			parent=ExpandView(parent, (bounds.x+8)@(totalHeight), (bounds.x+8)@((bounds.y+4)*2+4), false );

		});

		bounds.x=bounds.x-12;
		parent.addFlowLayout;
		//parent.background_(Color.grey);
		button=Button; slider=EZSlider;
		guiO2.freezer=button.new(parent, (bounds.x*0.5-4)@bounds.y).states_([["freeze"]]).canFocus_(false).action_({|b|
			this.freeze_(1.0)
		}).font_(font);
		guiO2.unfreezer=button.new(parent, (bounds.x*0.5-4)@bounds.y).states_([["unfreeze"]]).canFocus_(false).action_({|b| this.freeze_(0.0)}).font_(font);
		cs.keys.asArray.sort.do({|key| var val=cs[key];
			//		cs.keysValuesDo({|key,val|
			guiO[key]=slider.new(parent,bounds,key,val,{|ez| p[key]=ez.value; if (synthFreeze[ff].isPlaying, {synthFreeze[ff].set(key,ez.value)}) },p[key], false, labelWidth ).font_(font);
			guiO[key].sliderView.canFocus_(false);	guiO[key].numberView.canFocus_(false);


		});
		if (collapsed, {parent.collapse});

		//window.onClose_({this.close});

		//window.addToOnClose({this.close});

		/*
		if (parent.class==SCWindow, {
		parent.view.decorator.nextLine;
		parent.autoscaleY;
		parent.onClose_({this.close});
		},{
		parent.view.decorator.nextLine;
		parent.view.getParents.last.findWindow.addToOnClose({this.close});
		});
		*/
	}

}
