GaterSystemJT : JT {
	var <>gate;
	var <>functions;
	var <defaultsettings, <cmdNames, outFlag, sendReplyFlag;
	var <>thresholds, <comperators;
	var <inBusT;
	var <analyzer;
	var <descriptors, <descriptorsWithoutOnsets, <hasOnsets;

	initAll {
		this.initDescriptors;
		this.initDefaultsGate;
		this.initSettings;
		gate=0;
		controlSpecs=(
			lagU:ControlSpec(0.0, 5.0, 4.0)
			, lagD:ControlSpec(0.0, 5.0, 4.0)
			, holdTime:ControlSpec(0.0, 5.0, 4.0)
			, minTime: ControlSpec(0.0, 5.0, 4.0)
		);
		descriptorsWithoutOnsets.do{|key|
			controlSpecs[key]=analyzer.controlSpecs[key];
			controlSpecs[(\threshold_++key).asSymbol]=analyzer.controlSpecs[key];
		};
		this.initCmdNames;
		if (sendReplyFlag, {
			this.addOSCFunc;
		});
	}

	initDefaultsGate {
		defaultsettings=(holdTime:0.05, minTime:0.0, lagU:0.01, lagD:0.1
			, thresholds:()
			, lagTimesU:()
			, lagTimesD:()
			, comperators:(specflatness:"<=", fftflux:"<=", specflatnessHPZa:"<=")
		);
	}

	initDescriptors {
		hasOnsets=descriptors.includes(\onsets);
		descriptors.do{|key,i| if (analyzer.descriptors.includes(key).not
			, {descriptors.removeAt(i)})};
		descriptorsWithoutOnsets=descriptors.deepCopy;
		if (hasOnsets, {descriptorsWithoutOnsets.remove(\onsets)});
	}

	initSettings {
		defaultsettings.keysValuesDo{|key,val|
			settings[key]=settings[key]??{val};
			if (val.class==Event, {
				descriptorsWithoutOnsets.do{|desc,i|
					if ((key==\comperators) && (val[desc]==nil), {
						val[desc]=">=";
					});
					settings[key][desc]=settings[key][desc]??{val[desc]};
					if (settings[key][desc]==nil, {
						settings[key][desc]=0;
					});
				};
			});
		};
	}

	initCmdNames {
		cmdNames=();
		cmdNames[\gate]=('/gate'++id).asSymbol;
	}

	addOSCFunc {
		if (oscFunc==nil, {
			//functions=functions.addFunc({|msg| msg.postln});
			oscFunc=OSCFunc(functions, cmdNames[\gate], server.addr);
		});
	}

	removeOSCFunc {
		if (oscFunc!=nil, {
			oscFunc.free;
			oscFunc=nil;
		})
	}

	makeBusses {
		inBus=descriptorsWithoutOnsets.collect{|i| analyzer.outBusperDescriptor[i]};
		if (hasOnsets, {inBusT=analyzer.outBusT;});
		if (outFlag, {
			outBus=Bus.control(server, 2);//eventueel 3 channels [tr, gate, id]
			server.sync;});
	}

	makeSynthDef {arg synthDefName;
		var metadataSpecs=();
		synthDef=synthDefName??{\Gater};
		[\lagU, \lagD, \holdTime, \minTime].do{|key|
			metadataSpecs[key]=controlSpecs[key]
		};

		SynthDef(synthDef, {arg lagU=0.001, lagD=0.2, holdTime=0.01, minTime=0.0;
			var in;
			var threshold, gate, lagTimeU, lagTimeD, changed, time;

			gate=inBus.collect{|bus,i|
				var inp, tr, lagU, lagD, key=descriptorsWithoutOnsets[i], gateSignal;
				tr=NamedControl.kr((\threshold_++key).asSymbol
					, settings[\thresholds][key]);
				lagU=NamedControl.kr((\lagU_++key).asSymbol
					, settings[\lagTimesU][key]);
				lagD=NamedControl.kr((\lagD_++key).asSymbol
					, settings[\lagTimesD][key]);
				[\lagU, \lagD].do{|key2|
					metadataSpecs[(key2++"_"++key).asSymbol]=controlSpecs[\lagD]
				};
				metadataSpecs[(\threshold_++key).asSymbol]=
				controlSpecs[(\threshold_++key).asSymbol];
				inp=In.kr(bus.index, bus.numChannels).lag(lagU, lagD);
				gateSignal=if (tr.size==2, {
					InRange.kr(inp, tr[0], tr[1]);
				},{
					switch(settings[\comperators][key], "<=", {
						inp <= tr
					}, ">=", {
						inp >= tr
					},{inp > tr})
				});
			}.sum >= descriptorsWithoutOnsets.size;

			if (hasOnsets, {gate=In.kr(inBusT.index)+gate});
			time=Sweep.kr(Changed.kr(gate));
			gate=(time>=minTime)*gate;
			//gate=(Trig1.kr(gate-0.001, holdTime)+gate).min(1);
			gate=gate.lag(0, holdTime)>0.001;
			changed=Changed.kr(gate);

			if (outFlag, {Out.kr(outBus.index, [changed, gate]//[changed, gate, id]
			)});
			if (sendReplyFlag, {
				SendReply.kr(changed, cmdNames[\gate], gate)
			});

		}, metadata: (specs:metadataSpecs)).add;
	}
}