BounceLive {
	var addActionIn, targetIn, targetOut, addActionOut, inBus, outBus, buf, server, <action, maxRecTime=10, score, <latency, <latencyFrames, numChannels=2;
	var <path, <armed;
	var <synthIn, <busIn, <busOut, <synthOut;
	var <synthAna, <busAna, oscFuncOneShot, oscFunc;
	var <>argsRec, <>argsBounce, <>csRec, <>csBounce, <gui, <arrayKeys;
	var <hasOnsets, <currentFrame, <bufwrJT;

	*new {arg target=Server.default, addAction=\addToTail, inBus, targetOut=Server.default, addActionOut=\addToHead, outBus, numChannels
		, args=(rec: (maxRecTime:10), bounce:()), controlSpecs=(rec:(), bounce:());
		^super.new.init(target, addAction, inBus, targetOut, addActionOut, outBus, numChannels, args, controlSpecs)
	}
	init {arg argtargetIn, argaddActionIn, arginBus, argtargetOut, argaddActionOut, argoutBus, argnumChannels, argargs
		, argcontrolSpecs;
		targetIn=argtargetIn??{Server.default};
		addActionIn=argaddActionIn??{\addToTail};
		targetOut=argtargetOut??{Server.default};
		addActionOut=argaddActionOut??{\addToHead};

		if (targetIn.class==BufWrJT, {
			buf=targetIn.buffer;
			server=targetIn.server;
			bufwrJT=true;
		},{
			server=targetIn.asTarget.server;
		});
		this.latency_(server.sampleRate.reciprocal*512);
		//init values
		armed=false;
		csRec=(at: [0.0,1.0, 4.0], st:[0.0, 1.0, 4.0], rt:[0.0, 1.0, 4.0]);
		csBounce=(amp: \amp, rate: [0.125, 8.0, \exp], at:[0.0, 1.0, 4.0], st:[0.0, 1.0, 4.0], rt:[0.0, 16.0, 8.0]
			, az: [-16.0, 16.0], deltaTime: [0.001, 10.0, \exp]
			, bounces: [1, 128, \exp, 1], startPos: [0.0, 1.0]);
		argsRec=(index:0, inBus:0, at:0.001, st:1.0, rt:0.01);
		argsBounce=(amp:[0.01, 1.0], rate:[1.0, 1.0], at:0.0, st:0.0, rt:[1.0,1.0], az:[0.0, 2.0], deltaTime: [0.25, 0.01]
			, bounces: 16, outBus:0, startPos:[0.0, 0.0]);
		if (argargs[\rec]!=nil, {argargs[\rec].keysValuesDo{|key,val| argsRec[key]=val}});
		if (argargs[\bounce]!=nil, {argargs[\bounce].keysValuesDo{|key,val| argsBounce[key]=val}});
		inBus=arginBus; if (inBus.class==Bus, {inBus=inBus.index}); argsRec[\inBus]=inBus;
		outBus=argoutBus; if (outBus.class==Bus, {outBus=outBus.index});
		numChannels=argnumChannels;
		arrayKeys=List[];
		argsBounce.keysValuesDo{|key,val|
			if (val.size==2, {
				val.do{|val,i|
					var k=(key.asString++(i+1)).asSymbol;
					arrayKeys.add(key);
					argsBounce[k]=val.copy;
					if (csBounce[key]!=nil, {
						csBounce[k]=csBounce[key].copy;
					})
				};
				argsBounce.removeAt(key);
				csBounce.removeAt(key);
			},{
			});
		};
		if (buf==nil, {
			this.makeSynthIn;
			this.makeSynthDefRec;
		});
		this.makeSynthDefPlay;
		this.makeSynthOut;
		this.makeAction;
	}
	addOnsets {
		hasOnsets=true;
		this.makeSynthAna;
		path=('/bouncelive'++(UniqueID.next)).asSymbol;
	}
	latency_ {arg time=0.01;
		latency=time;
		latencyFrames=latency*server.sampleRate;
		if (synthIn!=nil, {
			synthIn.set(\delayTime, latency);
			latencyFrames=0.0;
		});
	}
	free {
		busAna.free;
		synthAna.free;
		oscFunc.free;
		buf.do(_.free);
		busIn.free;
		synthIn.free;
		synthOut.free;
		busOut.free;
	}
	close {this.free}
	addOSCFunc {
		armed=true;
		oscFunc=OSCFunc({arg ...msg;
			action.value(msg[3]??{0});
		}, path)
	}
	removeOSCFunc {
		armed=false;
		oscFunc.free
	}
	oneShot {
		if (armed, {
			oscFunc.free;
			if (gui!=nil, {{gui.views[\autoB].value_(0)}.defer});
		});
		armed=true;
		oscFunc=OSCFunc({arg ...msg;
			action.value(msg[3]??{0});
			armed=false;
			if (gui!=nil, {{gui.views[\oneshotB].value_(0)}.defer});
		},path).oneShot;
	}
	makeAction {
		action={arg currentFrame=0;
			var bufnum, buffer, array=(outBus1: [], outBus2: []);
			if (buf.class==Buffer, {
				buffer=buf;
			},{
				argsRec[\index]=argsRec[\index]+1%8;
				buffer=buf[argsRec[\index]];
				bufnum=buffer.bufnum;
				server.sendBundle(nil
					,[\b_zero, bufnum]
					,([\s_new, \RecBounce, -1, 3, synthIn.nodeID, \buf, bufnum]++argsRec.asKeyValuePairs)
				);
			});
			score=List[];
			argsBounce[\bounces]=argsBounce[\bounces].asInteger;
			array[\times]=Env([argsBounce[\deltaTime1], argsBounce[\deltaTime2]], [1.0], \exp)
			.discretize(argsBounce[\bounces]).as(Array).integrate;
			array[\amp]=Env([argsBounce[\amp1], argsBounce[\amp2]].max(0.001),[1.0],\exp).discretize(argsBounce[\bounces])
			.as(Array);
			array[\rate]=Env([argsBounce[\rate1], argsBounce[\rate2]],[1.0],\exp).discretize(argsBounce[\bounces]).as(Array);
			array[\az]=[argsBounce[\az1], argsBounce[\az2]].resamp1(argsBounce[\bounces]);
			array[\startPos]=[argsBounce[\startPos1], argsBounce[\startPos2]].resamp1(argsBounce[\bounces]);
			array[\rt]=Env([argsBounce[\rt1], argsBounce[\rt2]],[1.0],\exp).discretize(argsBounce[\bounces]).as(Array);
			array[\az]=array[\az].collect{|a,i| var bu1, bu2, aa; #bu1, bu2,aa=a.azToBussesAndPan2(numChannels, busOut.index);
				array[\outBus1]=array[\outBus1].add(bu1);
				array[\outBus2]=array[\outBus2].add(bu2);
				aa
			};
			[\rt].do{|key| array[key]=array[key]*array[\rate].reciprocal};
			[\at, \st].do{|key| array[key]=argsBounce[key]*array[\rate].reciprocal};
			if (argsBounce[\bounces]<2, {
				array[\times]=[array[\times][0]]
			});
			array[\times].do{|time,i|
				var para=();
				array.keysValuesDo{|key,val| para[key]=val[i]};
				para[\startPos]=(para[\startPos]+currentFrame-latencyFrames)%buffer.numFrames;
				score.add([time, [\s_new, \PlayBounce, -1, 2, synthOut.nodeID, \buf, bufnum]++para.asKeyValuePairs]);
			};
			score.do(_.postcs);
			Score.play(score, server)
		}
	}
	makeSynthIn {
		busIn=Bus.audio(server, 1);
		synthIn={arg delayTime=0.01;
			var in=In.ar(inBus);
			Out.ar(busIn.index, DelayN.ar(in, 0.2, delayTime))
		}.play(targetIn, busIn.index, 0.02, addActionIn, [\delayTime, latency]);
		latencyFrames=0;
	}
	makeSynthOut {
		busOut=Bus.audio(server, numChannels);
		synthOut={arg amp=1.0;
			var in=In.ar(busOut.index, numChannels)*amp.lag(0.1);
			Out.ar(outBus, in)
		}.play(targetOut, outBus, 0.02, addActionOut)
	}
	makeSynthDefRec {
		buf={Buffer.alloc(server, server.sampleRate*maxRecTime)}!8;
		SynthDef(\RecBounce, {arg inBus, buf, at=0.01, st=0.2, rt=0.2, ca=0, cr= -4.0;
			var env=Env.linen(at, st, rt, 1, [ca, 0, cr]).kr(2);
			var in=In.ar(inBus)*env;
			RecordBuf.ar(in, buf, 0, 1.0, 0.0, 1.0, 1.0, 1);
		}).add;
	}
	makeSynthDefPlay {
		SynthDef(\PlayBounce, {arg outBus1=0, outBus2=1, buf, at=0.001, st=0.0, rt=0.01, rate=1.0, amp=1.0, az=0.0, ca=0
			, cr= -4.0, startPos=0;
			var env=Env.linen(at, st, rt, amp, [ca,0,cr]).ar(2);
			var out;
			out=PlayBuf.ar(1, buf, rate, 1.0, startPos*SampleRate.ir, 0, 2)*env;
			out=Pan2.ar(out, az);
			Out.ar(outBus1, out[0]);
			Out.ar(outBus2, out[1]);
		}).add;
	}
	makeSynthAna {
		busAna=Bus.control(server, 1);
		synthAna={arg threshold=0.5;
			var fft, onsets;
			fft=FFT(LocalBuf(512), In.ar(inBus));
			onsets=Onsets.kr(fft, threshold);
			SendReply.kr(onsets, path);
			Out.kr(busAna.index, onsets)
		}.play(targetIn, 100, 0.02, addActionIn)
	}
	makeGui {arg parent, bounds=350@20;
		{gui=BounceLiveGUI(this, parent, bounds)}.defer
	}
}

BounceLiveGUI {
	var bounceLive, <parent, <bounds, <cv, <views;

	*new {arg bounceLive, parent, bounds;
		^super.new.init(bounceLive, parent, bounds)
	}
	init {arg argbounceLive, argparent, argbounds;
		var rows, font, labelWidth, numberWidth;
		bounceLive=argbounceLive;
		bounds=argbounds;
		views=();
		font=Font("Monaco", bounds.y*0.8);
		labelWidth=bounds*0.2;
		numberWidth=bounds*0.2;
		rows=bounceLive.csBounce.keys.size+bounceLive.csRec.keys.size+4;
		parent=argparent??{var w=Window.new("BounceLive", Rect(400,400,(bounds.x+8),bounds.y*(rows+1))).front;
			w.alwaysOnTop_(true); w.addFlowLayout; w};
		cv=CompositeView(parent, bounds.x@(bounds.y*rows));
		cv.addFlowLayout(0@0,0@0);
		cv.background_(Color.grey);
		views[\amp]=EZSlider(cv, bounds, \amp, \amp.asSpec, {|ez|
			bounceLive.synthOut.set(\amp, ez.value)}, 1.0, false, labelWidth, numberWidth).font_(font);
		StaticText(cv, bounds).string_("REC").font_(font);
		bounceLive.csRec.sortedKeysValuesDo{|key,cs|
			views[(\rec++key).asSymbol]=EZSlider(cv, bounds, key, cs.asSpec, {|ez| bounceLive.argsRec[key]=ez.value}
				, bounceLive.argsRec[key], false, labelWidth, numberWidth)
			.round2_(0.0001).font_(font);
		};
		StaticText(cv, bounds).string_("BOUNCE").font_(font);
		bounceLive.csBounce.sortedKeysValuesDo{|key,cs|
			views[(\bounce++key).asSymbol]=EZSlider(cv, bounds, key, cs.asSpec, {|ez| bounceLive.argsBounce[key]=ez.value}
				, bounceLive.argsBounce[key], false, labelWidth, numberWidth)
			.round2_(0.0001).font_(font);
		};
		//[\at, \st, \rt1, \rt2]
		views[\autoB]=Button(cv, ((bounds.x/3).asInteger)@bounds.y)
		.states_([ [\auto], [\auto, Color.black, Color.green] ]).action_{|b|
			if (b.value==1, {
				bounceLive.addOSCFunc
			},{
				bounceLive.removeOSCFunc
			})
		};
		views[\oneshotB]=Button(cv, ((bounds.x/3).asInteger)@bounds.y)
		.states_([ [\oneshot], [\oneshot, Color.black, Color.green] ]).action_{|b|
			if (b.value==1, {
				bounceLive.oneShot
			},{
				bounceLive.removeOSCFunc
			})
		};
		views[\actionB]=Button(cv, ((bounds.x/3).asInteger)@bounds.y).states_([ [\action] ]).action_{bounceLive.action.value};
		parent.onClose=parent.onClose.addFunc({bounceLive.close});
	}
}