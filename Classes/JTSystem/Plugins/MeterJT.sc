/*
het lijkt alsof er soms te hoge dB waardes worden doorgegeven

*/
MeterJT : JT {
	var <peakLag;
	var <numberOfMeters, <numberOfMetersPerServer, <sumMeters;
	var <gains, <inJT;

	//	var <servers, <server, <inBus, id;
	//	var <synth, <gui, <target;

	*new {arg inBus, target, updateFreq=20, peakLag=3.0, inJT;
		^super.new.init(inBus, target, updateFreq, peakLag, inJT);
	}

	init {arg arginBus, argtarget, argupdateFreq, argpeakLag, argInJT;
		updateFreq=argupdateFreq;
		peakLag=argpeakLag;
		numberOfMeters=0;
		id=UniqueID.next;
		inJT=argInJT;

		gains=inJT.gains;

		if (this.isThreaded, {
			this.initFunc(arginBus, argtarget)
		},{ {
			this.initFunc(arginBus, argtarget)
		}.fork
		})
	}

	initFunc {arg arginBus, argtarget;
		inBus=arginBus.asArray;
		sumMeters=false;
		target=argtarget??{target=Server.default};
		if (target.asArray.size>1, {
			if (inBus.asArray.asSet.asArray.size==1, {
				sumMeters=true;
			})
		});

		this.bubbleInBusAndTarget;
		inBusFlat=inBus.deepCopy.flat;
		server=this.getServer(target);
		servers=this.getAllServers(server);
		this.makeTargetPerServer;
		busIndexPerServer=this.convertBusToBusPerServer(inBus, server, servers, false);
		numberOfMetersPerServer=servers.size.collect{ 0 };
		this.makeSynth;
		//this.convertPerFlatIndex(inBus, server);
	}

	free {
		synth.asArray.do(_.free);
		if (gui!=nil, {gui.close});
	}

	close {
		this.free;
	}

	makeSynth {
		cmdName=Array.newClear(servers.size);
		netAddr=Array.newClear(servers.size);
		synth=busIndexPerServer.collect{arg inBus, serverID;
			var synth;
			cmdName[serverID]=("/meter_"++serverID++id);
			netAddr[serverID]=servers[serverID].addr;
			inBus=inBus.asArray.collect{|b|
				if (b.class==Bus, {b},{b.asBus(\audio,server:servers[serverID])})
			};
			inBus.do{|b|
				numberOfMeters=numberOfMeters+b.numChannels;
				numberOfMetersPerServer[serverID]=
				numberOfMetersPerServer[serverID]+b.numChannels
			};
			synth=SynthDef((\Meters++id).asSymbol, {
				var in;
				in=inBus.asArray.collect{|b| In.ar(b.index, b.numChannels)};
				//Amplitude.kr(in,0.01, 1.0).ampdb.poll(1);
				SendPeakRMS.kr(in, updateFreq, peakLag, cmdName[serverID])
			}).play(targetPerServer[serverID], []
				, if (targetPerServer[serverID].class==Synth, {\addAfter},{\addToTail})
			);
			servers[serverID].sync;
			if (servers.asArray.size>1, {
				synth.run(false);
			});

			synth;
		};
		if (servers.asArray.size>1, { synth.do{|syn| syn.run(true)}; });
		synth=synth.unbubble;
		if (sumMeters, {numberOfMeters=numberOfMetersPerServer[0]});
		^synth
	}
	//, font, layout=\vert, showBus=false, orderOfMeters, gains
	makeGUI {arg parent, bounds=20@150, labels, margin=0@0, gap=0@0, font, layout=\vert, showBus=false, orderOfMeters;
		{
			gui=MeterJTGUI(this, parent, bounds, labels, margin, gap, font, layout, showBus, orderOfMeters)
		}.defer
	}
}