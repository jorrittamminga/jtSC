+ AnalyzerJTGUI {

	addOSCFuncGUI {arg key, func, cmdName, server, type;
		if (oscGUI[key]==nil, {
			func=func??{switch (type, 1, {
				{arg msg;
					{views[key].value_(msg[3])}.defer;
			}}, 2, {
				{arg msg; msg=msg.copyToEnd(3);
					msg.do{|msg, i|
						{views[key][i].value_(msg)}.defer;
					};
			}},{
				{arg msg; msg=msg.copyToEnd(3);
					{views[key].value_(msg)}.defer;
			}})};
			oscGUI[key]=OSCFunc(func, cmdName, server.addr);
		})
	}
	removeOSCFuncs {
		oscGUI.keysValuesDo{|key,osc| osc.free; oscGUI[key]=nil;}
	}
	addOSCFuncs {arg holdTimeOnsets=0.1;
		if (oscGUI.size==0, {
			descriptorsWithoutOnsets.do{|key|
				this.addOSCFuncGUI(key, nil, analyzer.cmdNames[key].asString
					, analyzer.server, analyzer.numberOfOutputs[key]);
			};
			if (hasOnsets, {
				this.addOSCFuncGUI(\onsets, {|msg|
					{
						{views[\onsets].value_(1)}.defer;
						holdTimeOnsets.wait;
						{views[\onsets].value_(0)}.defer;
					}.fork;
				}, analyzer.cmdNames[\onsets].asSymbol, analyzer.server);
			});
		})
	}
	front {
		this.addOSCFuncs;
	}
	close {
		this.removeOSCFuncs
	}
	pause {
		this.close;
	}
	resume {
		this.front;
	}
}