OSCJT {
	var <name, <path, <features, <rate, <srcID, <recvPort, <derivatives, <ranges, <funcs, <funcsKeys, <sanitize, <activeSensing;
	var <oscFunc, <>data, <prev, <gui, <oscPath;
	var <action, <>specs, <>warps, <isFreed;
	var <paused, <isRunning, <>activeSensingTime, activeSensingRoutine, <>activeSensingAction;

	classvar <>defaultRanges, <>defaultFuncs, <>defaultDerivatives, <>defaultFuncsKeys;


	*zigsim {arg path='/ZIGSIM/dqVa4NRrpwgIRqgL/', features=[\quaternion, \gyro, \accel], rate=30, srcID, recvPort, derivatives, ranges, funcs, funcsKeys
		, sanitize=false, activeSensing=false;
		^this.new('ZigSim', path, features, rate, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize, activeSensing)
	}
	*touchosc {arg path="", features=[\accxyz], rate=30, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize=false, activeSensing=false;
		^this.new('TouchOSC', path, features, rate, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize, activeSensing)
	}
	*gyrosc {arg path='/gyrosc/', features=[\quat, \accel, \gyro], rate=30, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize=false, activeSensing=false;
		^this.new('GyrOSC', path, features, rate, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize, activeSensing)
	}
	*new {arg name, path, features, rate=30, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize=false, activeSensing=false;
		^super.newCopyArgs(name, path, features, rate, srcID, recvPort, derivatives, ranges, funcs, funcsKeys, sanitize, activeSensing).init
	}

	init {
		specs=(); warps=();
		this.initRanges;
		this.initDerivatives;
		this.initFuncs;
		data=();
		prev=();
		oscPath=();
		oscFunc=();
		action=();
		isRunning=();
		this.makeOSCFuncs;
		isFreed=false;

		if (activeSensing!=false) {
			if (activeSensing=true) {
				activeSensingTime=0.1.max(rate.reciprocal*8);
			} {
				activeSensingTime=activeSensing;
				activeSensing=true;
			};
			this.startactiveSensing
		} {
			activeSensingRoutine={}.fork;
		}
	}
	close {

	}
	free {
		"OSCFuncs of ".post; name.post; " freed".postln;
		oscFunc.do(_.free);
		activeSensingRoutine.stop; activeSensingRoutine.free;
		isFreed=true;
		if (gui!=nil) { {gui.window.close}.defer };
	}
	pause {arg keys;
		keys=keys??{features};
		keys.asArray.do{|key| oscFunc[key].free; isRunning[key]=false;}
	}
	resume {arg keys;
		var newKeys=[];
		keys=keys??{features};
		keys.asArray.do{|key|
			if (isRunning[key]==false) {newKeys=newKeys.add(key)};
		};
		if (newKeys.size>0) {this.makeOSCFuncs(newKeys)}
	}
	initRanges {
		if (ranges!=nil) {
			if (defaultRanges[name]!=nil) {
				defaultRanges[name].keys.asArray.difference(ranges.keys.asArray).do{|key|
					ranges[key]=defaultRanges[name][key].deepCopy
				};
			}
		}{
			ranges=defaultRanges[name]??{()};
		};
		features.do{|key| if (ranges[key]==nil) {ranges[key]=[-1.0, 1.0]}};
		/*
		ranges.keysValuesDo{|key,range|
		specs[key]=ranges.asSpec;
		warps[key]=specs[key].warp;
		}
		*/
	}
	initDerivatives {
		//derivatives=derivatives??{()};
		if (derivatives!=nil) {
			if (defaultDerivatives[name]!=nil) {
				defaultDerivatives[name].keys.asArray.difference(ranges.keys.asArray).do{|key|
					ranges[key]=defaultDerivatives[name][key].deepCopy
				};
			}
		}{
			derivatives=defaultDerivatives[name]??{()};
		};
		derivatives.keysValuesDo{|a,b|
			ranges[b]= [-1, 1] * (ranges[a].maxItem - ranges[a].minItem) * rate;
		};
	}
	initFuncs {
		funcs=funcs??{()};
		funcsKeys=funcsKeys??{()};
		features.do{|key|
			if (defaultFuncs[name]!=nil) {
				defaultFuncs[name].keys.asArray.difference(funcsKeys.keys.asArray).do{|key|
					funcs[key]=defaultFuncs[name][key].deepCopy
				};
			}
		};
		features.do{|key|
			if (defaultFuncsKeys[name]!=nil) {
				defaultFuncsKeys[name].keys.asArray.difference(funcsKeys.keys.asArray).do{|key|
					funcsKeys[key]=defaultFuncsKeys[name][key].deepCopy
				};
			}
		};
	}
	makeOSCFuncs {arg keys;
		if (keys==nil) {keys=features.copy};
		keys.do{|key|
			var derivative=derivatives[key];
			var func=funcs[key], time, function;
			oscPath[key]=(path++key).asSymbol;
			function=if (derivative!=nil) {
				prev[key]=0;
				if (func==nil) {
					if (sanitize) {
						{|msg, t, addr|
							var now=Main.elapsedTime;
							var delta=if (time.isNil) { rate.reciprocal } {(now-time)};
							time=now;
							data[key]=msg.copyToEnd(1).sanitizeJT;
							func.value(data[key], data, action);
							action[key].value(data[key]);
							data[derivative]=(data[key]-prev[key])/delta;
							action[derivative].value(data[derivative]);
							prev[key]=data[key].copy;
						}
					} {
						{|msg, t, addr|
							var now=Main.elapsedTime;
							var delta=if (time.isNil) { rate.reciprocal } {(now-time)};
							time=now;
							data[key]=msg.copyToEnd(1);
							func.value(data[key], data, action);
							action[key].value(data[key]);
							data[derivative]=(data[key]-prev[key])/delta;
							action[derivative].value(data[derivative]);
							prev[key]=data[key].copy;
						}
					}
				}
			} {
				if (func==nil) {
					if (sanitize) {
						{|msg, t, addr|
							data[key]=msg.copyToEnd(1).sanitizeJT;
							action[key].value(data[key]);
						}
					} {
						{|msg, t, addr|
							data[key]=msg.copyToEnd(1);
							action[key].value(data[key]);
						}
					}
				}{
					if (sanitize) {
						{|msg, t, addr|
							data[key]=msg.copyToEnd(1).sanitizeJT;
							action[key].value(data[key]);
							func.value(data[key], data, action);
						}
					} {
						{|msg, t, addr|
							data[key]=msg.copyToEnd(1);
							action[key].value(data[key]);
							func.value(data[key], data, action);
						}
					}
				}
			};
			oscFunc[key]=OSCFunc(function, oscPath[key], srcID, recvPort);
			isRunning[key]=true;
		}
	}

	startactiveSensing {
		var time=0, delta, flag=false;
		var waitTime=rate.reciprocal;
		activeSensingAction=nil;
		/*
		{arg flag;
		if (flag) {
		"receiving data".postln;
		} {
		"stopped receiving data".postln;
		};
		};
		*/
		//deltaTime=rate.reciprocal*8;
		activeSensingRoutine={
			inf.do{
				flag=(Main.elapsedTime-time<activeSensingTime);
				activeSensingAction.value(flag);
				activeSensingTime.wait;
			}
		}.fork;
		features.do{|key|
			var opath=(path++key).asSymbol;
			oscFunc[(\activeSensing++key).asSymbol]=OSCFunc({arg msg;
				time=Main.elapsedTime;
			}, opath, srcID, recvPort)
		}
	}

	makeGui {arg bounds=350@100, margin=4@4, gap=4@4, freeOnClose=false, columns=2;
		{gui=OSCGUIJT(this, bounds, margin, gap, freeOnClose, columns)}.defer
	}
}


OSCGUIJT {
	var <oscJT, <bounds, <margin, <gap, <freeOnClose, <columns;
	var <window, <views, <bounds, <oscFunc;

	*new {arg oscJT, bounds=350@100, margin=4@4, gap=4@4, freeOnClose=false, columns=2;
		^super.newCopyArgs(oscJT, bounds, margin, gap, freeOnClose, columns).init
	}

	init {
		var w;
		var sliderWidth, numberWidth;
		sliderWidth=((200/350)*bounds.x).floor.asInteger;
		numberWidth=bounds.x-sliderWidth;
		views=();
		oscFunc=();
		w=Window(oscJT.name, Rect(0,0,columns*(bounds.x+gap.x)+(2*margin.x),500));
		w.front;
		w.addFlowLayout(margin, gap);
		w.onClose_{
			if (freeOnClose) {
				if (oscJT.isFreed.not) {
					oscJT.free
				}
			};
			oscFunc.do(_.free);
		};
		w.alwaysOnTop_(true);
		window=w;
		oscJT.features.do{|key|
			var derivative=oscJT.derivatives[key], textkey=("text"++key).asSymbol, derivativeTextKey=("text"++derivative).asSymbol;
			var cv;
			var viewFunc;
			cv=CompositeView(w, bounds.x@(if (derivative==nil) {bounds.y}{bounds.y*2})); cv.addFlowLayout(0@0,0@0); cv.background_(Color.rand);
			[key, derivative].do{|key,i|
				var tkey;
				var size;
				if (key!=nil) {
					tkey=[textkey, derivativeTextKey][i];
					views[key]=EZMultiSlider(cv, sliderWidth@bounds.y, key, oscJT.ranges[key], {|m|
						views[tkey].string_(m.value)
					}, 0, false, 100, 0);
					views[tkey]=StaticText(cv, numberWidth@bounds.y).string_("").font_(Font.defaultMonoFace(10));
				}
			};
			viewFunc=if (derivative==nil) {
				{
					{
						views[key].value_(oscJT.data[key]);
						views[textkey].string_(oscJT.data[key]);
					}.defer
				}
			} {
				{
					{
						views[key].value_(oscJT.data[key]);
						views[textkey].string_(oscJT.data[key]);
						views[derivative].valueAction_(oscJT.data[derivative]);
						views[derivativeTextKey].string_(oscJT.data[derivative]);
					}.defer
				}
			};
			if (oscJT.funcsKeys[key]!=nil) {
				//if (key==\quaternion) {
				var funcKey=oscJT.funcsKeys[key];
				var cv=CompositeView(w, bounds); cv.addFlowLayout(0@0,0@0); cv.background_(Color.rand);
				[funcKey].do{|key|
					var tkey=("text"++key).asSymbol;
					views[key]=EZMultiSlider(cv, sliderWidth@bounds.y, key, oscJT.ranges[key], {|m|
						views[tkey].string_(m.value)
					}, 0, false, 100, 0);
					views[tkey]=StaticText(cv, numberWidth@bounds.y).string_("").font_(Font.defaultMonoFace(10));
					viewFunc=viewFunc.addFunc({
						{
							views[key].value_(oscJT.data[key]);
							views[tkey].string_(oscJT.data[key]);
						}.defer
					});
				};
			};
			oscFunc[key]=OSCFunc({|msg, t, addr|
				viewFunc.value;
			}, oscJT.oscPath[key], oscJT.srcID, oscJT.recvPort);
		};
		w.rebounds;
	}
	close {
		window.close
	}
}