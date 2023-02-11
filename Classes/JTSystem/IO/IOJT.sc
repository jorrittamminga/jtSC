/*
let op: this.unbubbleAll is uit de initFunc!
- maak een 'pair' optie
inBus=    [ [0,1,2,3], [4,5,6],7  ,8,   9 ];
server=   [  s,        [s, t], u, [s,t],s ];
label=    [ \0, \1.....\9 ]
servers=  [ s,t,u ];
bus=      [ Bus.audio(s,9), Bus.audio(t,4), Bus.audio(u,1) ];
synth=    [ Synth, Synth, Synth ];
singles=  ( 0: (synth: , bus:, plugins: ()), 1: (), 2: ()..... )
*/
JTmulti : JT {
	var <singles, <labels;
	var <busses, <synths;

	makeJTsingles {
		singles=();
		label.asArray.do{|label,i|
			singles[label]=JTSingle(busPerFlatIndex[i], synthPerFlatIndex[i], label, i);
		}

	}

	at {arg label;
		//if (singles[label]==nil, {});
		^singles[label]
	}

	free {
		plugins.do{arg plugin;
			plugin.asArray.do{|plug|
				plug.free}
		};
		singles.do{|single| single.plugins.do{|plugin| plugin.asArray.do(_.free)}};
		synth.asArray.do(_.free);
		group.asArray.do(_.free);
		bus.asArray.do(_.free);
		if (gui!=nil, {gui.close});
		//plugins.keysValuesDo{|key,val| val.asArray.flat.do{|class| class.free}};
	}

}

IOJT : JTmulti {

	var <gains, hasInputGains;

	initFunc {arg arginBus, argtarget, arglabel, argaddAction, makeNewBus=true;
		inBus=arginBus;//.asArray
		target=argtarget??{target=Server.default};
		this.bubbleInBusAndTarget;
		//inBusFlat=inBus.deepCopy.flat;
		inBusFlat=inBus.asArray.deepCopy.flat;
		label=arglabel??{inBusFlat};
		if (label.class!=Array, {
			label=[label];
		});
		labels=label;
		server=this.getServer(target);
		servers=this.getAllServers(server);
		addAction=argaddAction??{\addToHead};
		group=this.makeGroup(servers, addAction);
		busIndexPerServer=this.convertBusToBusPerServer(inBus, server, servers
			, makeNewBus);
		if (makeNewBus||(bus==nil), {bus=busPerServer});
		//if (makeNewBus, {this.makeBusPerServer(busIndexPerServer, servers)});
		synth=this.makeSynth;
		this.convertPerFlatIndex(inBus, server);
		//this.unbubbleAll;
		this.makeJTsingles;//is dit ook voor OutJT het geval???
		//this.addPlugin(\Meter);
	}


	unbubbleAll {
		busPerFlatIndex=this.deepUnBubble(busPerFlatIndex, 1);
		synthPerFlatIndex=this.deepUnBubble(synthPerFlatIndex, 1);
		server=server.unbubble;
		group=group.unbubble;
		synth=synth.unbubble;
		bus=bus.unbubble;
		inBus=inBus.unbubble;
		label=label.unbubble;
	}
	/*
	makeBusses {
	var labelList=List[];
	var inBus={[]}!servers.size;
	labelsPerServerID.do{arg labels, serverID;
	labels.do{|label|
	if (labelList.includes(label).not, {
	inBus[serverID]=inBus[serverID].add(bus[label].asArray[0].index);
	labelList.add(label)
	})
	}
	};
	if (servers.size==1, {inBus=inBus.unbubble});
	^inBus
	}
	*/
}