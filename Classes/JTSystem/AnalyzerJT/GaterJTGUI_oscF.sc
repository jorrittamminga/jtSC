+ GaterJTGUI {

	addOSCFuncGUI {arg key, cmdName, server, type;
		var extraFunc=funcs[key];
		var func={arg msg;
			msg=msg[3];
			gater.gate=msg;
			extraFunc.value(msg);
			{views[key].value_(msg)}.defer;
		};
		//or: if (extraFunc!=nil, {func=func.addFunc(extraFunc)});
		oscGUI[key]=OSCFunc(func, cmdName, server.addr);
	}

	removeOSCFuncs {
		oscGUI.keysValuesDo{|key,osc| osc.free; oscGUI[key]=nil;}
	}
	addOSCFuncs {
		[\gate].do{|key|
			this.addOSCFuncGUI(key, gater.cmdNames[key], gater.server);
		};
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