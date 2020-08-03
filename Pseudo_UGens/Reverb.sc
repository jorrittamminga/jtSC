/*
for PartConv
(1, 0.99998 .. 0).size/44100

	
(1/(1-0.99998))/44100=1.13
(1/(1-0.99998))=1.13*44100
((1-0.99998))=1/(1.13*44100)
1-(1/(1.13*44100))=0.99998

~rvt=2.13; 1-(1/(~rvt*44100))

(1, (1-(1/(~rvt*44100))) .. 0)

(
s.waitForBoot({ {
	b = Buffer.read(s, "sounds/break");
	x={PlayBuf.ar(1,b,[1,1.25],loop:1)}.play;
	GVerbsa.new([0,1], s, bounds: 200@15);
	s.meter2;
}.fork})
)
s.queryAllNodes;
x.free;b.free;
*/

GVerbsa{
	var <server, <index, <target, <addAction, <outbus, <parent, <bounds, <args, <controlSpecs, <guiFlag, <synth, <guiO;
	
	*new {arg index=[0], target=Server.default, addAction=\addAfter, outbus, parent, bounds=300@20, args=(inputlevel: 1.0, roomsize: 150, revtime: 3.5, damping: 0.5, inputbw: 0.5, spread: 15, drylevel: 0.0, erlevel: 0.5, taillevel: 0.3, maxroomsize: 300, amp: 0.5), controlSpecs=(roomsize:ControlSpec(1,200), revtime:ControlSpec(0.1, 20, \exp), damping:\unipolar.asSpec, inputbw:\unipolar.asSpec, spread:ControlSpec(1,30), drylevel:\unipolar.asSpec, erlevel:\unipolar.asSpec, taillevel:\unipolar.asSpec, amp:\amp.asSpec, inputlevel: \unipolar.asSpec), guiFlag=true;
		^super.new.init(index, target, addAction, outbus, parent, bounds, args, controlSpecs, guiFlag)
		}	
	
	init {arg argindex, argtarget, argaddAction, argoutbus, argparent, argbounds, argargs, argcontrolSpecs, argguiFlag;
		index=argindex;
		if (index.size==0, {index=[index]});
		target=argtarget;
		addAction=argaddAction;
		outbus=argoutbus??{index.minItem};
		parent=argparent;
		bounds=argbounds;
		args=argargs;
		controlSpecs=argcontrolSpecs;
		guiFlag=argguiFlag;

		server=target;
		if (target.class!=Server, {
			if ((target.class==Group) || (target.class==Synth), {
				server=target.server;
				},{
				server=Server.default;
				})
			});
			
		synth=SynthDef(\GVerb_JT, {arg outbus, inputlevel=1.0, roomsize=50, revtime=1.2, damping=0.5, inputbw=0.5, spread=15, drylevel=0.0, erlevel=0.5, taillevel=0.3, maxroomsize=300, amp=0.5;
			Out.ar(outbus, GVerb.ar(In.ar(index)*inputlevel.lag(0.1), roomsize.lag(0.1), revtime.lag(0.1), damping.lag(0.1), inputbw.lag(0.1), spread.lag(0.1), drylevel.lag(0.1), erlevel.lag(0.1), taillevel.lag(0.1), maxroomsize, amp.lag(0.2) ))
		}).play(target, args.asKeyValuePairs, addAction);
		
		{this.gui}.defer;
	
		
	}

	free {
		synth.free;
	}


	close {
		this.free;
		if (guiFlag, {		
			if (parent.class!=SCWindow, {parent=parent.getParents.last.findWindow});
			if (parent.isClosed.not, {parent.onClose_({nil}); parent.close});
			});
		}


	gui {
		var slider;
		guiO=();
		if (parent==nil, {
			parent=Window("GVerb", Rect(0,0,bounds.x+8, (bounds.y+4)*controlSpecs.size+12)).front;
			},{
			parent=CompositeView(parent, (bounds.x+8)@((bounds.y+4)*(controlSpecs.size+1)+12));
			});
		parent.addFlowLayout; parent.background_(Color.grey);
		slider=EZSlider;
		controlSpecs.keys.asArray.sort.do({|key|
			guiO[key]=slider.new(parent, bounds, key, controlSpecs[key], {|ez| synth.set(key, ez.value); args[key]=ez.value}, args[key], false
				, bounds.y*3.2, bounds.y*2.5).font_(Font("Helvetica", bounds.y*0.75));

			});
		
		if (parent.class==Window, {
			parent.view.decorator.nextLine;
			parent.autoscaleY;
			parent.onClose_({this.close});
			},{
			parent.decorator.nextLine;
			parent.getParents.last.findWindow.addToOnClose({this.close});
			});	
	}
}

/*
s.boot;

x={Out.ar(100, SoundIn.ar([0,1]))}.play;
x={Out.ar(100, SinOsc.ar([100,230],0,0.1))}.play;
PartConvsa.new([100], args: (inputlevel: 1.0, drylevel: 0.0, amp: 0.5, revtime: 5.3));
s.meter2
*/

PartConvsa{
//~rvt=2.13; 1-(1/(~rvt*44100))	
	
	var <inbus, <outbus, <bufnums, <synth, <isRunning, <ws, <index, <server, <args, <target, <addAction, guiFlag, <bounds, <parent, controlSpecs, <guiO;
	
	*new {arg index=[0], target=Server.default, addAction=\addAfter, outbus=0, ws=2048, irbuffer, parent, bounds=300@20, args=(inputlevel: 1.0, drylevel: 0.0, amp: 0.5, revtime: 1.3), controlSpecs=(drylevel:\unipolar.asSpec, amp:\amp.asSpec, inputlevel: \unipolar.asSpec), guiFlag=true;
		^super.new.init(index, target, addAction, outbus, ws, irbuffer, parent, bounds, args, controlSpecs, guiFlag)
		}	
	
	init {arg argindex, argtarget, argaddAction, argoutbus, argws, argirbuffer, argparent, argbounds, argargs, argcontrolSpecs, argguiFlag;
		var n;
		index=argindex ?? [0];
		if (index.size==0, {index=[index]});
		outbus=argoutbus??{index};
		if (outbus.size==0, {outbus=[outbus]});
		ws=argws??{2048};
		target=argtarget;
		addAction=argaddAction;
		args=argargs;
		server=target;
		
		parent=argparent;
		bounds=argbounds;
		controlSpecs=argcontrolSpecs;
		guiFlag=argguiFlag;		
		
		if (target.class!=Server, {
			if ((target.class==Group) || (target.class==Synth), {
				server=target.server;
				},{
				server=Server.default;
				})
			});

		{
			n=if (index.size>=outbus.size, {index.size},{outbus.size});
			bufnums=bufnums ?? {n.collect({|i| var irbuffer, bufsize, irspectrum;
				
				irbuffer=Buffer.loadCollection(server, this.ircalculate(args.revtime.postln));
				server.sync;

				bufsize= PartConv.calcBufSize(ws, irbuffer);
				irspectrum= Buffer.alloc(server, bufsize, 1);
				server.sync;

				irspectrum.preparePartConv(irbuffer, ws);
				server.sync;
				irbuffer.free;
				irspectrum.bufnum;
				})};	
			bufnums.postln;//for testing!
			index.postln;
			this.synthDef.send(server);server.sync;
			
			synth=this.synthDef.play(target, args.asKeyValuePairs, addAction);
			server.sync;
			{this.gui}.defer;
			
		}.fork;
		
		}	
	
	ircalculate{arg revtime=1.3;
		
		//~rvt=2.13; 1-(1/(~rvt*44100))
		//(1, (1-(1/(~rvt*44100))) .. 0)
		var ir= ([1] ++0.dup(100) ++ ((1, (1-(1/(revtime*server.sampleRate))) .. 0).collect{|f| 
			f = f.squared.squared; 
			f = if(f.coin){0}{f.squared}; 
			f =if(0.5.coin){0-f}{f} } * 0.1)).normalizeSum;
		^ir
		}
	
	synthDef{
		^SynthDef(\PartConv_JT, {arg amp, inputlevel=1.0;
			var in,out;
			in=In.ar(index)*inputlevel.lag(0.1);
			out=Mix.arFill(bufnums.size, {|i|
				PartConv.ar(in, ws, bufnums[i], amp.lag(0.1));
				});
			Out.ar(outbus.minItem, out)
			})
		}
		
	free {
		synth.free;
		bufnums.do({|i| server.sendMsg(\b_free, i.postln)});
		
		}

	close {
		this.free;
		if (guiFlag, {		
			if (parent.class!=SCWindow, {parent=parent.getParents.last.findWindow});
			if (parent.isClosed.not, {parent.onClose_({nil}); parent.close});
			});
		}


	gui {
		var slider;
		guiO=();
		this.class.postln;
		if (parent==nil, {
			parent=Window("PartConv", Rect(0,0,bounds.x+8, (bounds.y+4)*controlSpecs.size+12)).front;
			},{
			parent=CompositeView(parent, (bounds.x+8)@((bounds.y+4)*(controlSpecs.size+1)+12));
			});
		parent.addFlowLayout; parent.background_(Color.grey);
		slider=EZSlider;
		controlSpecs.keys.asArray.sort.do({|key|
			guiO[key]=slider.new(parent, bounds, key, controlSpecs[key], {|ez| synth.set(key, ez.value); args[key]=ez.value}, args[key], false
				, bounds.y*3.2, bounds.y*2.5).font_(Font("Helvetica", bounds.y*0.75));

			});
		
		if (parent.class==Window, {
			parent.view.decorator.nextLine;
			parent.autoscaleY;
			parent.onClose_({this.close});
			},{
			parent.decorator.nextLine;
			parent.getParents.last.findWindow.addToOnClose({this.close});
			});	
	}
	
}
