+ Server {
	/*
	0                1  2  3   4    5    6    7      8  9     10    11   12   13    14   15   16   17   18
	[ /g_queryTree.reply, 0, 0, 7, 1005, -1, Sine, 1004, -1, Sine, 1003, -1, Sine, 1002, -1, Sine, 1001, -1, Sine,

	19    20  21    22 23
	1000, -1, Sine, 1, 9, 1014, -1, Sine, 1013, -1, Sine, 1012, -1, Sine, 1011, -1, Sine, 1010, -1, Sine, 1009, -1, Sine, 1008, -1, Sine, 1007, -1, Sine, 1006, -1, Sine ]


	queryAllNodesAsEvent {
	OSCFunc({ arg msg;
	var size=msg.size, flag=true;
	var offset=3, event=();
	while({flag}, {
	msg[offset].do{|i|
	event(msg[3*i+offset+1])=msg[3*i+offset+3];
	};
	if (size<(), {
	offset=size*3+offset;
	},{
	flag=false;
	})
	});

	//msg[3].do{|i| this.sendMsg(\n_set, msg[3*i+4], \gate, 0)};

	}, '/g_queryTree.reply', addr).oneShot;
	this.sendMsg("/g_queryTree", 0);
	}
	*/

	releaseAll {arg group=0;
		OSCFunc({ arg msg;
			msg[3].do{|i| this.sendMsg(\n_set, msg[3*i+4], \gate, 0)};
		}, '/g_queryTree.reply', addr).oneShot;
		this.sendMsg("/g_queryTree", group);
	}

/*
	*quitAll { |watchShutDown = true|
		all.do { |server|
			if(server.sendQuit === true) {
				server.quit(watchShutDown: watchShutDown)
			};
			if (server.window!=nil, {{server.window.close}.defer})
		};
	}
*/

	*internalNew { arg argName, argOptions, argClientID=0, makeWindow=true;
		var server, ports;
		argName=argName??{Date.localtime.stamp;};
		if (argOptions==nil, {argOptions=Server.default.options});

		if (Server.all.collect{|i| i.name.asString}.includes(argName.asString), {
			server=Server.named[argName.asSymbol];
			if (argOptions!=nil, {server.options=argOptions});
			//server.options.memSize=2**18;
			if (makeWindow, {
				{
					if (server.window==nil, {
						{server.makeWindow}.defer
					})
				}.defer
			})
		},{
			//o=ServerOptions.new;
			//o.numOutputBusChannels=out.maxItem+1;
			//o.memSize = 2**18;
			ports=Server.all.asArray.collect{|i| i.addr.port};
			ports.removeAllSuchThat{|i| i==nil};
			server=Server.new(argName
				, NetAddr("127.0.0.1", ports.maxItem+1)
				, argOptions);
			if (makeWindow, {
				{
					if (server.window==nil, {
						{server.makeWindow}.defer
					})
				}.defer
			})
		});

		^server
	}
}