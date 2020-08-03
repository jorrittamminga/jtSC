/*
Spectral Delay

is guiO and guiO2 handy?
maak iets handigs voor het interpoleren tussen twee arrays (dts oid)

s.boot;
b=Buffer.read(s,Platform.resourceDir +/+ "sounds/a11wlk01-44_1.aiff");a=Bus.audio(s,1); x={Out.ar(a, PlayBuf.ar(1, b, 1, Impulse.kr((BufDur.ir(b)+2).reciprocal), loop:0))}.play;

q=SpectralDelaySimple(a.index, x, ws:2048, hop:0.25, bounds: 350@20, guiFlag: true);

q.set(\dts, {exprand(0.1, q.maxdelayTime)}!(q.ws/2));
q.set(\fbs, {0.5.rand}!(q.ws/2));

a={5.0.rand}!(q.ws/2);b={5.0.rand}!(q.ws/2);{1000.do({|i| q.set(\dts, a.blend(b,i+1/1000)); 0.01.wait; })}.fork



q.close;x.free;a.free;b.free;
//q.free;

*/

SpectralDelaySimple {
	var <server, <index, <target, <addAction, <outbus, <parent, <bounds, <p, <cs, <synthInput, <synthSpectralDelay, <ws, <hop, <bus, <dtBuf, <fbBuf, <guiO, <guiO2, <maxdelayTime, guiFlag, <buf, <array, <font;
	var isPlaying,isOn;
	
	*new {arg index, target, addAction=\addAfter, outbus=[0,1], ws=1024, hop=0.25, maxdelayTime, parent, bounds=300@20, args, controlSpecs, guiFlag=true, font=Font("Helvetica", 9), isOn=true;
		^super.new.init(index, target, addAction, outbus, ws, hop, maxdelayTime, parent, bounds, args, controlSpecs, guiFlag, font, isOn)
		}
	
	init {arg argindex, argtarget, argaddAction, argoutbus, argws, arghop, argmaxdelayTime, argparent, argbounds, argargs, argcontrolSpecs, argguiFlag, argfont, argisOn;
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
		parent=argparent;
		bounds=argbounds;
		ws=argws;
		hop=arghop;
		font=argfont;
		font.size=bounds.y*0.75;
		maxdelayTime=argmaxdelayTime??{ws*(512-12)/server.sampleRate*hop};
		guiFlag=argguiFlag;
		isPlaying=false;
		isOn=argisOn;

		p=(fadeIn:0.1, fadeOut:0.1, gate:1, amp:1.0, inputLevel:1.0);
		if (argargs!=nil, {argargs.keysValuesDo({|key,val| p[key]=val})});
		cs=(inputLevel: \unipolar.asSpec, amp: \amp.asSpec);
		if (argcontrolSpecs!=nil, {argcontrolSpecs.keysValuesDo({|key,val| cs[key]=val})});
		{
		if (index<server.options.numInputBusChannels, {
			bus=Bus.audio(server,1);
			synthInput=SynthDef(\SoundIn_JT, {arg inBus,outBus;
				Out.ar(outBus,SoundIn.ar(inBus))
				}).store; server.sync;
			synthInput=Synth(\SoundIn_JT, [\inBus, index, \outBus, bus.index], 1, \addToHead).register;
			index=bus.index;
			});
		buf=(); array=();
		buf.dts=Buffer.alloc(server, ws);
		buf.fbs=Buffer.alloc(server, ws); server.sync;
		array.dts=0.dup(ws/2); buf.dts.setn(0, array.dts);
		array.fbs=0.dup(ws/2); buf.fbs.setn(0, array.fbs); server.sync;

		synthSpectralDelay=SynthDef(\SpectralDelaySimple_JT, {arg inBus,outBus,amp=1.0, maxdelayTime=2.5, az=0.0, dtBuf, fbBuf, fadeIn=0.1, fadeOut=1.0, gate=1.0, smooth=0.0, shift=50.0, fb=0.0, inputLevel=1.0, wet=0.0, room=0.5, damp=0.3, doneAction=2;
			var env=EnvGen.kr(Env.asr(fadeIn,1,fadeOut), gate, doneAction:doneAction);
			var safetyEnv=EnvGen.kr(Env.new([0.0,0.0,1.0],[1.0,1.0]));
			var in,out,chain;
			in=In.ar(inBus)*inputLevel.lag(0.1);
			in=in*env;
			chain=FFT(LocalBuf(ws).clear, in, hop);
			chain=PV_BinDelay(chain, maxdelayTime, dtBuf, fbBuf, hop);
			out=IFFT(chain)*hop.sqrt;
			out=out*safetyEnv;
			Out.ar(outBus, out*env*amp.lag(0.1));
			}).store;
		server.sync;
		if (isOn, {this.start});
		//synthSpectralDelay.register;
		}.fork;
		if (guiFlag, {this.gui});
		}

	start {
		isPlaying=true;
		if (synthSpectralDelay.isPlaying.not, {
			synthSpectralDelay=Synth(\SpectralDelaySimple_JT, [\inBus, index, \outBus, outbus.minItem, \dtBuf, buf.dts, \fbBuf, buf.fbs, \maxdelayTime, maxdelayTime, \fadeOut, this.maxdelayTime]++p.asKeyValuePairs, target, addAction).register;
			});
		}
	
	pause {
		nil	
	}
	
	stop {
		isPlaying=false;		
		if (synthSpectralDelay.isPlaying, {
			synthSpectralDelay.set(\gate, 0);
			})
		}


	set {arg ... args;
		var bufs=List[];
		if (guiFlag, {args.pairsDo({|key,val| 
			if (guiO[key]!=nil, { {guiO[key].value=val}.defer });
			if (guiO2[key]!=nil, { {guiO2[key].value=val}.defer })
			})});
		[\dts, \fbs].do({|i| var index=args.indexOf(i);
			if (index!=nil, {
			bufs.add(args[index]); bufs.add(args[index+1]); 
			args.removeAt(index); args.removeAt(index);
			});
			});
		if (synthSpectralDelay.isRunning, {synthSpectralDelay.set(*args)});
		bufs.pairsDo({|key,val| 
			if (key==\mags, {
				buf[key].setn(0, val.dup(2).flop.flatten);
				},{
				buf[key].setn(0, val) 
				})
			});
	}

	free {
		{
		[synthInput, synthSpectralDelay].do({|synth| if (synth.class==Synth, {if (synth.isPlaying, {synth.free})})});
		if (bus!=nil, {bus.free});
		[dtBuf, fbBuf].do({|buf| buf.free});
		}.fork;	
	}

	close {
		this.free;
		if (guiFlag, {		
			//if (parent.view.class!=SCWindow, {parent=parent.view.getParents.last.findWindow});
			if (parent.window.isClosed.not, {parent.window.onClose_({nil}); parent.window.close});
			
			});
		}
	
	gui {
		var button,slider, c, labelWidth=font.size*5, e2, e2s, totalHeight=(bounds.y+4)*(cs.size+7)+12+bounds.x,window;
		guiO=();
		guiO2=();		
		if (parent==nil, {
			window=Window("Spectral Delay", Rect(0,0,bounds.x+12, totalHeight+12)).front;
			parent=ExpandView(window, (bounds.x+8)@(totalHeight), (bounds.x+8)@((bounds.y+4)*2+8), false );
			},{
			parent=ExpandView(parent, (bounds.x+8)@(totalHeight), (bounds.x+8)@((bounds.y+4)*2+8), false );
			});
		bounds.x=bounds.x-12;	
		parent.addFlowLayout; parent.background_(Color.grey);
		button=RoundButton; slider=EZSmoothSlider;
		//c=CompositeView(parent, bounds.x@totalHeight);c.addFlowLayout;
		SCStaticText(parent, (bounds.x-12-bounds.y)@bounds.y).string_("Spectral Delay").font_(font).background_(Color.blue(0.25)).stringColor_(Color.white);
		guiO[\isRunning]=button.new(parent, (bounds.y)@(bounds.y)).states_([[\power],[\power, Color.black, Color.green]]).action_({|b|
			if (b.value==1, {this.start},{this.stop});
			}).value_(isOn.binaryValue);

		cs.keys.asArray.sort.do({|key| 
			var val=cs[key];
			guiO[key]=slider.new(parent,(bounds.x-4)@bounds.y,key,val,{|ez| p[key]=ez.value; if (synthSpectralDelay.isPlaying, {synthSpectralDelay.set(key,ez.value)}) },p[key], false, labelWidth).font_(font)});
		
		SCStaticText(parent, (bounds.x-4)@bounds.y).string_("ws: " ++ this.ws ++ ".  max delaytime: " ++ this.maxdelayTime.round(0.001)).font_(font).background_(Color.white.alpha_(0.25)).stringColor_(Color.white);
		[\dts, \fbs].do({|key|
			StaticText(parent, (font.size*2)@bounds.y).string_(key).font_(font);
			guiO[(key++\Text).asSymbol]=TextField(parent, (bounds.x-12-(2*font.size))@bounds.y).action_({|t| 
				var array=t.string.interpret.asArray;
				this.set(key, array.min(this.maxdelayTime));
				});
			});
		
		
		e2s={var e=ExpandView(parent, bounds.x@bounds.x, bounds.x@(bounds.y+8), false); e.addFlowLayout; e}!2;
		e2s.do( _.expandAction = { |vw| e2s.do({ |vwx| if( vwx != vw ) { vwx.collapse } }) } );
		bounds.x=bounds.x-14;
		StaticText(e2s[0], (bounds.x-8)@bounds.y).string_("delaytimes").font_(font);
		guiO2.dts=SCMultiSliderView(e2s[0], (bounds.x-bounds.y-4)@(bounds.x-bounds.y-4)).value_(0.0.dup(ws/2)).thumbSize_(1.0).elasticMode_(1).valueThumbSize_(1).indexThumbSize_(1).action_({|m|
			buf.dts.set(m.index, m.currentvalue*maxdelayTime);
			array.dts[m.index]=m.currentvalue;
			//buf.dts.setn(0, m.value*maxdelayTime);
			});
		StaticText(e2s[1], (bounds.x-8)@bounds.y).string_("feedbacks").font_(font);
		guiO2.fbs=SCMultiSliderView(e2s[1], (bounds.x-bounds.y-4)@(bounds.x-bounds.y-4)).value_(0.0.dup(ws/2)).thumbSize_(1.0).elasticMode_(1).valueThumbSize_(1).indexThumbSize_(1).action_({|m|
			buf.fbs.set(m.index, m.value);
			array.fbs[m.index]=m.currentvalue;
			//buf.fbs.setn(0, m.value);
			});
		e2s.do( _.collapse );
		
		if (parent.class==SCWindow, {
			parent.view.decorator.nextLine;
			parent.autoscaleY;
			parent.onClose_({this.close});
			},{
			parent.view.decorator.nextLine;
			parent.view.getParents.last.findWindow.addToOnClose({this.close});
			});
	}	
}