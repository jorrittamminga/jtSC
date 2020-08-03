/*
- granularity meten (maar hoe?), de tikkerigheid van het signaal
- preset inbouwen. thisProcess.nowExecutingPath
- e=EZAnalyser; e.oscFunc.add{|msg| msg.postln};
- bouw gatedescriptors in als argument
- bouw beattrack3 in
- bouw ritmische/timing parameters in als density, legato/staccato, regular/irregular, tempo
* deltatime events regularity (0..1) [irregular..regular]
* legato events (0..1) [staccato..legato]
- change in events detectie (dat is een combinatie van onsets en hpz
- smooth or abrupt changes (is een meta parameter?)

Maak ook een analyzer toolbox (met metadingen, KDtree, NearestN, HPZ1, learn functie). also think of behaviour of descriptors (developement, modulation, devation), bouw dit in in een analyser toolbox
one 'meta' parameter which describes elements like tempo, timing, grilligheid, etc
gesture recognition
Server.killAll
s.waitForBoot{EZAnalyser(inbus: 8)}
*/


EZAnalyser {
	var isClosed, bounds;
	var <bus, <oscFunc;
	var <parent, <descriptors, <gateDescriptors, <server, <oscCmd, <synth, <size, <window, <index, <busIndex;
	var <guis, <controlSpecs, <busses;
	var <guish, <settings, <latency;
	var <rect;

	*new { arg parent, bounds=400@20, inbus=0, target, addAction=\addAfter, descriptors=[\amplitude, \tartini, \specFlatness, \specPcile, \specCentroid, \fftFlux, \fftCrest, \mfcc, \onsets, \gater]
		//, gateDescriptors=[\amplitude, \specFlatness, \fftFlux, \fftFluxHPZ1]
		, settings, buttonSize=10;
		^super.new.init(parent, bounds, inbus, target, addAction, descriptors
			//,gateDescriptors
			, settings, buttonSize)
	}

	init { arg argparent, argbounds, inbus, target, addAction, argdescriptors
		//, arggateDescriptors
		, settingz, buttonSize;
		var tmp, fftIndex, fftSizes=[512,1024,2048,4096,8192], argsettings, mfccSettings;
		oscCmd=('/analyze'++bus++Date.localtime.stamp).asSymbol;
		oscFunc=();
		descriptors=argdescriptors;
		parent=argparent;
		//if (target==nil, {target=Server.default});
		target=target??Server.default;
		server=if (target.class==Server, {server=target}, {target.server});
		if (inbus.class==Bus, {inbus=inbus.index});
		settings=settingz?();
		bounds=argbounds;

		gateDescriptors=[\amplitude, \specFlatness, \fftFlux, \fftFluxHPZ1];

		if (descriptors.includes(\gater), {
			descriptors=descriptors.union([\amplitude, \specFlatness, \fftFlux, \onsets, \phraseLength])
		});
		controlSpecs=(tartini: ControlSpec(0.01, 22050.0, \exp).asSpec, fftCrest: ControlSpec(0.0, 1000.0), fftFlux: ControlSpec(0.0, 10, 10.0), amplitude: ControlSpec(0.0, 1.0, 8.0), specFlatness: ControlSpec(0.0, 1.0, 5.0), onsets: ControlSpec(0.0, 1.0), specCentroid: \freq.asSpec, specPcile: \freq.asSpec, gater: ControlSpec(0,1), mfccd: ControlSpec(0.0, 4.0), lagTime: ControlSpec(0.0, 5.0, 6.0), phraseLength: ControlSpec(0.01, 20, 4.0)
			, fftFluxHPZ1: ControlSpec(0.0, 1.0, 12.0)//moet eigenlijk bipolair
		);

		argsettings=(tartini:[0.93, 1024, 0, 512, 0.5], amplitude:[0.05, 0.2], specFlatness:2048, onsets: [512, 0.5, \rcomplex, 1, 0.1, 10, 11, 1, 0], specCentroid: [2048], specPcile: [2048, 0.9, 0], gater: [0.001, 0.5, 1.0, 0.05, 0.0, 0.9], fftCrest: [2048, 20, 20000], fftFlux: 2048, mfcc: [1024, 13]);
		descriptors.do{|d|
			if (settings[d]==nil, {
				settings[d]=argsettings[d]
				},{
					if (settings[d].asArray.size<argsettings[d].size,{
						settings[d]=settings[d].asArray++argsettings[d].copyToEnd(settings[d].size)
					})
			})
		};
		mfccSettings=if (descriptors.includes(\mfcc), {settings[\mfcc][1]}, {argsettings[\mfcc][1]});

		if (descriptors.includes(\mfcc), {
			settings[\mfcc][0]=if (server.sampleRate>50000, {2048},{1024});
			descriptors=descriptors.add(\mfccd);
		});
		if (descriptors.includes(\fftFlux), {
			descriptors=descriptors.add(\fftFluxHPZ1);
		});
		size=descriptors.collect{|d| var i; i=(tartini:2, mfcc:mfccSettings)[d]; if (i==nil, {i=1}); i};
		fftIndex=();
		fftSizes=[\onsets, \specFlatness, \specCentroid, \specPcile, \fftFlux, \mfcc, \fftCrest].sect(descriptors).collect{|d| settings[d].asArray[0]}.asSet.asArray.sort;
		[\onsets, \specFlatness, \specCentroid, \specPcile, \fftFlux, \mfcc, \fftCrest].sect(descriptors).do{|d|
			fftIndex[d]=fftSizes.indexOf(settings[d].asArray[0])
		};
		fftSizes=fftIndex.values.asSet.asArray.sort.collect{|i| fftSizes[i]};
		isClosed=true;
		bus=Bus.control(server, size.flatten.sum);
		busses=();
		tmp=(bus.index..(bus.index+bus.numChannels-1)).clumps(size);
		descriptors.do{|d,i| busses[d]=tmp[i].unbubble};

		this.makeOSCFunc;

		latency=if (fftSizes.size>0, {fftSizes.maxItem/server.sampleRate}, {0});

		index=(); descriptors.do{|d,i| index[d]=i};

		synth=SynthDef(oscCmd, {arg
			//inBus,
			outBus, updateFreq=10, latency=0.1, gate=1, bypass=0;
			var in, out, p=(), tmp, fft=(), onset;
			var freq, hasFreq;
			var gateG;
			var tartini, onsets, amplitude, gater, specFlatness, specPcile, specCentroid, flux, mfcc, fftCrest;
			var interval;
			var descriptorsTMP=descriptors.deepCopy;
			var trigger, timer;

			if (inbus<server.options.numInputBusChannels, {in=SoundIn.ar(inbus)}, {in=In.ar(inbus)});

			in=in+WhiteNoise.ar(-80.0.dbamp);

			p[\phraseLength]=LocalIn.kr(1);

			descriptorsTMP.remove(\gater);

			gater=NamedControl.kr(\gater, [0.0008, 0.5, 0.4, 0.01, 0.0, 0.9]);

			if (fftSizes.size>0, {
			fft=fftSizes.collect{|size| FFT(LocalBuf(size).clear, in)};
			});
			if (descriptors.includes(\specFlatness),{
				specFlatness=NamedControl.kr(\specFlatness, [1024]);
				p[\specFlatness]=SpecFlatness.kr(fft[fftIndex[\specFlatness]]);
			});
			if (descriptors.includes(\tartini), {
				tartini=NamedControl.kr(\tartini, [0.93, 1024, 0, 512, 0.5]);
				#freq, hasFreq=Tartini.kr(in, *tartini);
				if (descriptors.includes(\specFlatness), {
					freq=Gate.kr(
						DelayN.kr(freq, 0.1, ((settings[\specFlatness]-settings[\tartini][1]).max(0)/SampleRate.ir))
						,p[\specFlatness]<0.2
					);
				});
				p[\tartini]=[hasFreq,freq];
				//				interval=(HPZ1.kr(freq)+freq/freq).ratiomidi;
				//				interval.poll(interval.abs>0.4);
				};
			);
			if (descriptors.includes(\fftCrest),{
				fftCrest=NamedControl.kr(\fftCrest, [2048]);
				p[\fftCrest]=FFTCrest.kr(fft[fftIndex[\fftCrest]])
			});
			if (descriptors.includes(\fftFlux),{
				flux=NamedControl.kr(\fftFlux, [1024]);
				p[\fftFlux]=FFTFlux.kr(fft[fftIndex[\fftFlux]])
			});
			if (descriptors.includes(\specPcile), {
				specPcile=NamedControl.kr(\specPcile, [2048, 0.9, 0]);
				p[\specPcile]=SpecPcile.kr(fft[fftIndex[\specPcile]], *specPcile.copyToEnd(1));
			});
			if (descriptors.includes(\mfcc), {
				mfcc=NamedControl.kr(\mfcc, [1024, 4]);
				p[\mfcc]=MFCC.kr(fft[1], settings[\mfcc][1] );
				p[\mfccd]=p[\mfcc].differentiate.abs.copyToEnd(1).sum;
			});
			if (descriptors.includes(\specCentroid),{
				specCentroid=NamedControl.kr(\specCentroid, [2048]);
				p[\specCentroid]=SpecCentroid.kr(fft[fftIndex[\specCentroid]])
			});
			if (descriptors.includes(\amplitude),{
				amplitude=NamedControl.kr(\amplitude, [0.01,0.01]);
				p[\amplitude]=Amplitude.kr(in, *amplitude).lag(*amplitude)
			});
			if (descriptors.includes(\onsets),{
				onsets=NamedControl.kr(\onsets, [512, 0.5, \rcomplex, 1, 0.1, 10, 11, 1, 0]);
				p[\onsets]=Onsets.kr(fft[0], *onsets.copyToEnd(1));
				SendReply.kr(p[\onsets], oscCmd++'t');
				//p[\Onsets]=Timer.kr(onset);
			});

			if ( (descriptors.includes(\fftFlux)) && (descriptors.includes(\specFlatness)), {
			p[\fftFluxHPZ1]=Gate.kr(HPZ1.kr(Gate.kr(p[\fftFlux]+p[\specFlatness], fft[fftIndex[\fftFlux]])), fft[fftIndex[\fftFlux]]);
			//+ HPZ1.kr(p[\amplitude]).neg

				p[\fftFluxHPZ1]=p[\fftFluxHPZ1].abs.lag(0.0, 2048/SampleRate.ir*2);//hoe bepaal ik de lagtimes????
			})
			;

			if (descriptors.includes(\gater),{
			 	p[\gater]=(
			 		Schmidt.kr(p[\amplitude], gater[0]*0.99, gater[0]*1.01)
			 		*(1-Schmidt.kr(p[\specFlatness], gater[1]*0.99, gater[1]*1.01))
					*(1-Schmidt.kr(p[\fftFlux].lag(0.0, gater[4]), gater[2]*0.99, gater[2]*1.01))
			 		*(p[\fftFluxHPZ1]<gater[3])
					+(Trig1.kr(p[\onsets], 512/SampleRate.ir)*Schmidt.kr(p[\amplitude], gater[0]*0.99, gater[0]*1.01))//gater[3]
			 	).clip(0,1);
			 });

			out=descriptors.collect{|i| p[i] }.flat;
			if (descriptors.includes(\gater),{
				p[\gater]=p[\gater]*gate;
				p[\phraseLength]=Timer.kr(p[\gater]);//.lag3(p[\phraseLength].clip(0.01, 10));
				LocalOut.kr(p[\phraseLength]);
				//LocalOut.kr(p[\gater]);
				p[\phraseLength]=p[\phraseLength].lag3(p[\phraseLength].clip(0.01, 10));
				out=Gate.kr(out, p[\gater]+bypass);
				p[\gater]=p[\gater].lag(gater[4],gater[5])>0.1;
				trigger=Changed.kr(p[\gater]);
				timer=Timer.kr(trigger);
				p[\pauseTime]=Gate.kr(timer, p[\gater]);
				p[\phaseTime]=Gate.kr(timer, 1-p[\gater]);
				SendReply.kr(Changed.kr(p[\gater]), oscCmd++'g', [p[\gater], p[\phaseTime], p[\pauseTime]]);
			});
			Out.kr(outBus, out);
			SendReply.kr(Impulse.kr(updateFreq), oscCmd, out);
		}).add.play(target, [
			//\inBus, inbus,
			\outBus, bus, \updateFreq, 20]++settings.asKeyValuePairs, addAction).register;

		{
			guis=();
			if (parent==nil, {
				parent=Window("analyse",Rect(0,1000,68,28), scroll:true).front; parent.addFlowLayout;
				parent.onClose_{this.free};
				},{
					parent.onClose=(parent.onClose.addFunc({ this.free}));
			});
			window=Window.new; window.close;
			guis[\buttonOnOff]=Button(parent, buttonSize@buttonSize).canFocus_(false).font_(Font("Helvetica", buttonSize)).states_([ ["a"],["a", Color.black, Color.green]]).action_{|b|
				if (b.value==1, {isClosed=false; this.gui;
				},{this.close})
			};
		}.defer;
		//		}.fork
	}


	makeOSCFunc {
		oscFunc[\main]=OSCFunc({|msg|
			if (isClosed.not, {
				{
					try {
						msg=msg.copyToEnd(3).clumps(size);
						descriptors.do{|d,i|
							guis[d].value=msg[i].unbubble;
						};
					} { |error|
						if(error.isKindOf(PrimitiveFailedError).not) { error.throw }
					};
				}.defer;
			})
		}, oscCmd.asSymbol, server.addr);

		oscFunc[\onsets]=OSCFunc({|msg|
			if (isClosed.not, {
				{
					{guis[\onsetsT].value_(1)}.defer;
					0.1.wait;
					{guis[\onsetsT].value_(0)}.defer;
				}.fork;
			})
		}, (oscCmd++'t').asSymbol, server.addr);

		oscFunc[\gate]=OSCFunc({|msg|
			if (isClosed.not, {
				{
					guis[\gateT].value_(msg[3]);
					guis[\gateTime].string_(msg[4].round(0.001).asString);
					guis[\gatePause].string_(msg[5].round(0.001).asString);
				}.defer;
			})
		}, (oscCmd++'g').asSymbol, server.addr);

		oscFunc[\unstableness]=OSCFunc({|msg|
			if (isClosed.not, {
				{guis[\unstablenessT].value_(msg[3])}.defer;
			})
		}, (oscCmd++'u').asSymbol, server.addr);
	}


	free {
		synth.free;
		bus.free;
		this.close;
		oscFunc.do{|os| os.free};//of moet het remove zijn?
	}


	close {
		//oscFunc.do{|os| os.free};//of moet het remove zijn?
		//window.bounds;
		if (window.isClosed.not, {window.close});
		guis[\buttonOnOff].value_(0);
		isClosed=true;
	}


	gui {
		var font=Font("Helvetica", bounds.y*0.6), descriptorsGUI=descriptors.deepCopy;
		window=Window("Analyzer", Rect(0,0,bounds.x+8,400), scroll:true).front;
		window.addFlowLayout;
		window.onClose_{this.close};
		window.alwaysOnTop_(true);
		isClosed=false;
		guish=();
		guis[\gate]=Button(window, (bounds.x*0.5-4)@bounds.y).font_(font).states_([ ["gate"], ["gate", Color.black, Color.green]]).action_{|b| synth.set(\gate, b.value)}.value_(1);

		guis[\bypass]=Button(window, (bounds.x*0.5-4)@bounds.y).font_(font).states_([ ["bypass"], ["bypass", Color.black, Color.yellow]]).action_{|b| synth.set(\bypass, b.value)}.value_(0);

		descriptors.postln;

		descriptors.do{|d,i|
			if (descriptors.includes(\gater), {
				if (gateDescriptors.includes(d), {
					var index=gateDescriptors.indexOf(d), sl;
					sl=EZSlider(window, bounds, d++"G", controlSpecs[d], {|ez|
						settings[\gater][index]=ez.value;
						synth.set(\gater, settings[\gater])
					}
						, settings[\gater][index]
						, labelWidth: 5*bounds.y, numberWidth: 3*bounds.y).round2_(0.0001).font_(font).setColors(Color.white, Color.black, Color.grey, Color.white, Color.black, Color.grey, Color.red, Color.blue, Color.black);
					sl.value_(settings[\gater][index])
				})
			});

			[d,i,size[i]].postln;

			guis[d]=case {size[i]==1} {
				EZSlider(window, bounds, d, controlSpecs[d], labelWidth: 5*bounds.y, numberWidth: 3*bounds.y).round2_(0.0001).font_(font);
			} {size[i]==2} {
				EZRanger(window, bounds, d, controlSpecs[d], labelWidth: 5*bounds.y, numberWidth: 3*bounds.y).round2_(0.0001).font_(font);
			} {size[i]>2} {
				MultiSliderView(window, bounds.x@(bounds.y*4)).indexThumbSize_(bounds.x/size[i]).gap_(0).value_(0!size[i]).elasticMode_(1);
			};
		};

		if (descriptors.includes(\onsets),{
			guis[\onsetsT]=Button(window, (bounds.x/6-4)@bounds.y).font_(font).states_([ ["onset"], ["onset", Color.black, Color.blue]]);
		});

		if (descriptors.includes(\gater),{
			guis[\gateT]=Button(window, (bounds.x/6-4)@bounds.y).font_(font).states_([ ["gate"], ["gate", Color.black, Color.red]]);
			StaticText(window, (bounds.x/6-4)@bounds.y).font_(font).string_("time:").align_(\right);
			guis[\gateTime]=StaticText(window, (bounds.x/6-4)@bounds.y).font_(font);
			StaticText(window, (bounds.x/6-4)@bounds.y).font_(font).string_("pause:").align_(\right);
			guis[\gatePause]=StaticText(window, (bounds.x/6-4)@bounds.y).font_(font);

			[\lagTime].do{|d,i|
				2.do{|j|
					guis[(\gater++d++j).asSymbol]=EZSlider(window, bounds, if (j==0, {d},{""}), controlSpecs[d], {|ez|
						settings[\gater][i*2+j+4]=ez.value;
						synth.set(\gater, settings[\gater])
						}
						, settings[\gater][i*2+j+4]
						, labelWidth: 5*bounds.y, numberWidth: 3*bounds.y).round2_(0.000001).font_(font).value_(settings[\gater][i*2+j+4]);
				};
			};
			/*
			guis[\gatePrint]=Button(window, bounds).states_([ ["noiseprint"] ]).font_(font).action_{|b|
				var values=();
				{
					1.0.wait;
					20.do{|k|
						[\amplitude, \specFlatness, \mfccd, \fftFlux].do{|d,i|
							if (k==0, {values[d]=List[]});
							values[d].add(guis[d].value);
							if (k==19, {
								[values[d].minItem, values[d].maxItem].do{|val,j|
									guis[(\gater++d++j).asSymbol].valueAction_(val*1.5)
								}
							});
						};
						0.1.wait;
					};
				}.fork(AppClock);
			}
			*/
		});
		//window.view.rescale;
	}
}