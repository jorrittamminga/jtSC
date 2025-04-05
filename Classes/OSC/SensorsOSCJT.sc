SensorsOSCJT {
	var <name, <path, <features, <rate, <srcID, <recvPort, <derivatives, <ranges, <funcs, <sanitize;
	var <oscFunc, <data, <prev, <gui, <oscPath;
	var <action, <>specs, <>warps;

	classvar <defaultRanges, <defaultFuncs, defaultDerivatives;

	*initClass {
		defaultRanges = (
			ZigSim: (
				accel: [-8, 8]//in G, gravitional force, ±8 g (gravitational force)
				, gyro: (100/9)*[pi.neg, pi]//in radians per second (angular velocity), ±2000 degrees per second (dps) = 100/9 * pi
				, quaternion: [-1.0, 1.0]//orientation, attitude
				, rotation: [-pi, pi]
				, arkitposition: [-1.0, 1.0]
				, arkitrotation: [-pi, pi]
			)
		);
		defaultFuncs = (
			ZigSim: (
				quaternion: {arg quaternionData, data=(), action=();
					var w,x,y,z, quaternion;
					#x,y,z,w=quaternionData;
					quaternion=Quaternion(w,x,y,z);
					data[\rotation]=quaternion.eulerAngles;
					action[\rotation].value(data[\rotation]);
				}
			);
		);
		defaultDerivatives = (
			ZigSim: (accel: \jerk)
		);
	}

	* new {arg name='ZigSim', path='/ZIGSIM/dqVa4NRrpwgIRqgL/', features=[\quaternion, \gyro, \accel], rate=30, srcID, recvPort, derivatives, ranges, funcs, sanitize=false;
		^super.newCopyArgs(name, path, features, rate, srcID, recvPort, derivatives, ranges, funcs, sanitize).init
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
		this.makeOSCFuncs;
	}
	close {}
	free {
		"OSCFuncs of ".post; name.post; " freed".postln;
		oscFunc.do(_.free);
	}
	initRanges {
		if (ranges!=nil) {
			if (defaultRanges[name]!=nil) {
				defaultRanges[name].keys.asArray.difference(ranges.keys.asArray).do{|key| ranges[key]=defaultRanges[name][key].deepCopy};
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
				defaultDerivatives[name].keys.asArray.difference(ranges.keys.asArray).do{|key| ranges[key]=defaultDerivatives[name][key].deepCopy};
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
		features.do{|key|
			if (defaultFuncs[name]!=nil) {
				defaultFuncs[name].keys.asArray.difference(funcs.keys.asArray).do{|key| funcs[key]=defaultFuncs[name][key].deepCopy};
			}
		};
	}
	makeOSCFuncs {
		features.do{|key|
			var sensorPath=(path++key).asSymbol;
			var derivative=derivatives[key];
			var func=funcs[key], time, function;
			oscPath[key]=sensorPath;
			function=if (derivative!=nil) {
				prev[key]=0;
				if (func==nil) {
					if (sanitize) {
						{|msg, t, addr|
							var now=Main.elapsedTime;
							var delta=if (time.isNil) { rate.reciprocal } {(now-time)};
							time=now;
							data[key]=msg.copyToEnd(1).sanitizeJT;
							func.value(data[key], data);
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
							func.value(data[key], data);
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
			oscFunc[key]=OSCFunc(function, sensorPath, srcID, recvPort);
		}
	}
	makeGui {arg bounds=350@100, margin=4@4, gap=4@4, freeOnClose=false, columns=2;
		{gui=SensorsOSCGUIJT(this, bounds, margin, gap, freeOnClose, columns)}.defer
	}
}


SensorsOSCGUIJT {
	var <sensorsOSC, <bounds, <margin, <gap, <freeOnClose, <columns;
	var <window, <views, <bounds, <oscFunc;

	*new {arg sensorsOSC, bounds=350@100, margin=4@4, gap=4@4, freeOnClose=false, columns=2;
		^super.newCopyArgs(sensorsOSC, bounds, margin, gap, freeOnClose, columns).init
	}

	init {
		var w;
		var sliderWidth, numberWidth;
		sliderWidth=((200/350)*bounds.x).floor.asInteger;
		numberWidth=bounds.x-sliderWidth;

		views=();
		oscFunc=();
		w=Window(sensorsOSC.name, Rect(0,0,columns*(bounds.x+gap.x)+(2*margin.x),500));
		w.front;
		w.addFlowLayout(margin, gap);
		w.onClose_{
			if (freeOnClose) {sensorsOSC.free};
			oscFunc.do(_.free);
		};
		w.alwaysOnTop_(true);
		window=w;
		sensorsOSC.features.do{|key|
			var derivative=sensorsOSC.derivatives[key], textkey=("text"++key).asSymbol, derivativeTextKey=("text"++derivative).asSymbol;
			var cv;
			var viewFunc;
			cv=CompositeView(w, bounds.x@(if (derivative==nil) {bounds.y}{bounds.y*2})); cv.addFlowLayout(0@0,0@0); cv.background_(Color.rand);
			[key, derivative].do{|key,i|
				var tkey;
				var size;
				if (key!=nil) {
					tkey=[textkey, derivativeTextKey][i];
					views[key]=EZMultiSlider(cv, sliderWidth@bounds.y, key, sensorsOSC.ranges[key], {|m|
						views[tkey].string_(m.value)
					}, 0, false, 100, 0);
					views[tkey]=StaticText(cv, numberWidth@bounds.y).string_("").font_(Font.defaultMonoFace(10));
				}
			};
			viewFunc=if (derivative==nil) {
				{
					{
						views[key].value_(sensorsOSC.data[key]);
						views[textkey].string_(sensorsOSC.data[key]);
					}.defer
				}
			} {
				{
					{
						views[key].value_(sensorsOSC.data[key]);
						views[textkey].string_(sensorsOSC.data[key]);
						views[derivative].valueAction_(sensorsOSC.data[derivative]);
						views[derivativeTextKey].string_(sensorsOSC.data[derivative]);
					}.defer
				}
			};
			if (key==\quaternion) {
				var cv=CompositeView(w, bounds); cv.addFlowLayout(0@0,0@0); cv.background_(Color.rand);
				[\rotation].do{|key|
					var tkey=("text"++key).asSymbol;
					views[key]=EZMultiSlider(cv, sliderWidth@bounds.y, key,  sensorsOSC.ranges[key], {|m|
						views[tkey].string_(m.value)
					}, 0, false, 100, 0);
					views[tkey]=StaticText(cv, numberWidth@bounds.y).string_("").font_(Font.defaultMonoFace(10));
					viewFunc=viewFunc.addFunc({
						{
							views[key].value_(sensorsOSC.data[key]);
							views[tkey].string_(sensorsOSC.data[key]);
						}.defer
					});
				};
			};
			oscFunc[key]=OSCFunc({|msg, t, addr|
				viewFunc.value;
			}, sensorsOSC.oscPath[key], sensorsOSC.srcID, sensorsOSC.recvPort);
		};
		w.rebounds;
	}
	close {
		window.close
	}
}