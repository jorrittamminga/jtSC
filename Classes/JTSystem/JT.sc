/*
- busIndexPerServer is niet altijd een getal maar kan blijkbaar ook een Bus zijn
*/
JT {
	var <>inBus, <outBus, <>target, <>addAction, <id, <>synth, <>synthDef, <server, <group;
	var <inBusFlat, <args;
	var <bus, <hasGUI=true;
	var <servers;
	var <busPerServer, <busIndexPerServer, <busPerFlatIndex;
	var <targetPerServer;
	var <synthPerFlatIndex;//<serverPerIndex
	var <label;
	var <>path, <>folderName, <>fileName;
	var <presetSystem, <presetPath, <presetFolder, <hasPresetSystem, <preset, <presetType;
	var <presetIndex;
	var <>settings;
	var <threaded=false, <>gui, <>windowBounds;
	var <>controlSpecs, <plugins, <numChannels;
	var <cmdName, <netAddr, <updateFreq, <oscFunc;
	var <mute;
	var <isRunning;
	var <>name;//could be used for a Window name, after makeGUI

	isThreaded {
		threaded=(thisProcess.mainThread.state>3);
		^threaded
	}

	metaAddPresetSystem {arg path, folderName="master", index=0, func;//setFlag=true
		var tmpPath;
		hasPresetSystem=true;
		if (path.class==PresetSystem, {
			presetSystem=path;
			path=path.path;
			presetType=\slave;
		},{
			presetType=\master;
		});
		presetPath=path??{thisProcess.nowExecutingPath.dirname++"/"};
		presetFolder=folderName??{presetType.asString};
		presetIndex=index;
		if (File.exists(presetPath++presetFolder), {
			if (PathName(presetPath++presetFolder).entries.size>0, {
				tmpPath=PathName(presetPath++presetFolder).entries.clipAt(index).fullPath;
				preset=tmpPath.load;
				if (settings==nil, {settings=()});
				if (preset.class==Event, {
					preset.keysValuesDo{|key,val|
						if (settings[key]!=nil, {
							settings[key]=val;
							synth.asArray.do{|syn| syn.set(key,val)};
						})
					};
					func.value(preset.deepCopy);
				});
			})
		});
	}

	addPresetSystem {arg path, folderName="master", index=0;
		this.metaAddPresetSystem(path, folderName, index)
	}

	addPlugins {arg type, plugin, replaceSynth=false;
		if (plugins==nil, {plugins=()});
		if (plugins[type]==nil, {
			plugins[type]=plugin
		},{
			plugins[type]=[plugins[type]];
			plugins[type]=plugins[type].add(plugin);
		});
		if (replaceSynth, {synth=plugin.synth});
		^plugins;
	}

	free {
		plugins.do{arg plugin;
			plugin.asArray.do{|plug| plug.free}
		};
		synth.asArray.do(_.free);
		group.asArray.do(_.free);
		bus.asArray.do(_.free);
		if (gui!=nil, {
			if (gui.hasWindow, {
				windowBounds=gui.window.bounds;
			});
			gui.close;
		});
		//ook eventuele specifieke synthdefs removen
		//plugins.keysValuesDo{|key,val| val.asArray.flat.do{|class| class.free}};
	}

	getServer {arg target;
		var rank;//deepCollect
		^target.asArray.deepCollect(target.asArray.maxRank, {|target|
			switch(target.class, Synth, {
				target.server
			}, Group, {
				target.server
			}, Server, {
				target
			})
		})//.unbubble
	}

	getAllServers {arg server;
		var servers=[];
		server.asArray.flat.do{|server|
			if (servers.includesEqual(server).not, {servers=servers.add(server)})
		};
		^servers
	}

	makeTargetPerServer {
		targetPerServer={0}!servers.size;
		target.asArray.do{|target|
			var server=if (target.class==Server, {target},{target.server});
			var serverIndex=servers.indexOfEqual(server);
			if (targetPerServer[serverIndex]==0, {
				targetPerServer[serverIndex]=target
			},{
				if (targetPerServer[serverIndex]!=nil, {
					if (targetPerServer[serverIndex].asArray.includesEqual(target).not
						, {
							if (targetPerServer[serverIndex].size==0, {
								targetPerServer[serverIndex]=
								[targetPerServer[serverIndex]];
							});
							targetPerServer[serverIndex]=
							targetPerServer[serverIndex].add(target);
					})
				})
			})
		};
	}

	//hierbinnen ook meteen dat hele gedoe met die bussen doen
	convertBusToBusPerServer {arg bus, server, servers, makeNewBus=false;
		var busIndexPerServer={[]}!servers.size;
		var serverIndex, changed=false;
		server.asArray.do{|server,i|
			serverIndex=server.asArray.collect{|server| servers.indexOf(server)};
			serverIndex.asArray.do{|serverIndex|
				busIndexPerServer[serverIndex]=busIndexPerServer[serverIndex]
				++bus.asArray[i];
			};
		};
		//busIndexPerServer=busPerServer.collect;
		if (makeNewBus, {
			busPerServer=busIndexPerServer.collect{arg bus, i;
				var b;
				b=Bus.audio(servers[i], bus.asArray.size);
				servers[i].sync;
				b
			};
			bus=busPerServer;
		}
		,{
			busPerServer=busIndexPerServer.collect{arg bus, i;
				var b=bus.deepCopy;
				if (bus.class==Array, {
					if (bus[0].class!=Bus, {
						changed=true;
						b=bus.asBus('audio', bus.size, servers[i]);
						servers[i].sync;
						b
					})
				});
				b
				//b=Bus.audio(servers[i], bus.asArray.size);
				//servers[i].sync;
				//b
			};
			if (changed, {bus=busPerServer});
		}
		);

		^busIndexPerServer
	}

	makeBusPerServer {arg busIndexPerServer, servers;
		^busIndexPerServer.collect{arg bus, i;
			var b=Bus.audio(servers[i], bus.asArray.size);
			servers[i].sync;
			b
		}
	}

	convertPerFlatIndex {arg inBus, server;
		var indexPerServer={-1}!servers.size;
		var indexFlat=0;

		busPerFlatIndex={-1}!inBusFlat.size;
		synthPerFlatIndex={-1}!inBusFlat.size;

		inBus.asArray.do{|b,i|
			var serverIndex=server[i].asArray.collect{|server|
				servers.indexOfEqual(server)
			};
			b.asArray.do{|b,j|
				var nr;
				synthPerFlatIndex[indexFlat]=serverIndex.collect{|index| synth[index]};
				busPerFlatIndex[indexFlat]=serverIndex.collect{|serverIndex|
					nr=busIndexPerServer[serverIndex].indexOf(b);
					if (busPerServer==nil, {
						b.asBus('audio', 1, servers[serverIndex])
					},{

						(busPerServer[serverIndex].index+nr)
						.asBus('audio', 1, servers[serverIndex])
					})
				};
				indexFlat=indexFlat+1;
			};
		};
	}

	makeGroup {arg targets, addActions;
		^targets.collect{|target,i|
			var server=if (target.class==Server, {target}, {target.server});
			var g=Group(target, addActions.asArray.wrapAt(i));
			server.sync;
			g
		}
	}

	deepUnBubble {arg array, rank=1;
		^if (array.asArray.size>1, {
			array.asArray.collect{|array|
				array.unbubble
			}
		},{
			array.unbubble
		}).unbubble
	}

	bubbleInBusAndTarget {
		if (inBus.size>target.size, {
			inBus=[inBus];
			if (target.size==0, {target=[target]});
		},{
			if (inBus.size<target.size, {
				target=[target];
				if (inBus.size==0, {inBus=[inBus]});
			})
		});
	}
	/*
	inBus=[[0,1,2,3],[4,5,6],7,8,9];
	target=[s, [s, t], u,[s,t],s];
	label=[\0, \1.....\9]
	s: Bus.audio(s, 9) (namelijk [0,1,2,3]++[4,5,6]++8++9)
	t: Bus.audio(t, 4) (namelijk [4,5,6]++8)
	u: Bus.audio(u, 1) (namelijk 7)
	bus=[Bus.audio(s,9), Bus.audio(t,4), Bus.audio(u,1)];
	*/
}