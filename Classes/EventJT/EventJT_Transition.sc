/*
zou dit ook met een normale Event kunnen?
for a specific parameter it can be durations[\test]=0, delayTimes[\test]=10;
*/
+ EventJT {
	free {
		this.stopTransition
	}
	close {
		this.free
	}
	stopTransition {
		routines.do{|r| r.stop}
	}
	prInitTransition {arg newEvent, argdurations, argcurves, argdelayTimes;
		var newKeys=newEvent.keys.asArray;
		//[transitionDurationKey, transitionCurveKey, transitiondelayTimeKey].do{|key|
		//newKeys.remove(key)
		//};
		newKeys.do{|key| routines[key].stop};
		#argdurations, argcurves, argdelayTimes=[argdurations, argcurves, argdelayTimes].collect{|key|
			if (key.class.superclass==SimpleNumber, {key.asFloat},{key});
		};
		switch(argcurves.class
			, Event, {argcurves.keysValuesDo{|key,val| curves[key]=val}}
			, Nil, {}
			, {newKeys.do{|key| curves[key]=(argcurves??{0}) }}
		);
		switch(argdurations.class
			, Event, {
				argdurations.keysValuesDo{|key,val|
					var step=val*resolution;
					var stepSize=step.reciprocal;
					steps[key]=step.round(1.0).asInteger;
					stepSizes[key]=stepSize;
					steps[key]=this.envelopeCalc(curves[key]??{0}, steps[key]);
					durations[key]=val
				}
			}
			, Float, {
				var step=argdurations*resolution;
				var stepSize=step.reciprocal;
				step=step.round(1.0).asInteger;
				newKeys.do{|key|
					steps[key]=step;
					steps[key]=this.envelopeCalc(curves[key]??{0}, step);
					stepSizes[key]=stepSize;
					durations[key]=argdurations
				}
			}
		);
		switch(argdelayTimes.class
			, Event, {argdelayTimes.keysValuesDo{|key,val| delayTimes[key]=val}}
			, Float, {newKeys.do{|key| delayTimes[key]=argdelayTimes}}
			, { newKeys.do{|key| if (delayTimes[key]==nil, {delayTimes[key]=0})} }
		);
		^newKeys
	}
	envelopeCalc {arg curve, steps;
		^switch (curve.class, Float, {
			if (curve!=0.0, {
				Env([0, steps],[1.0], curve).discretize(steps)
			},{
				steps
			})
		}, Symbol, {
			Env([0, steps],[1.0], curve).discretize(steps)
		}, Env, {
			Env(curve.levels*steps, curve.times, curve.curve).discretize(steps)
		}, Array, {
			curve.resamp1(steps)
		}, {steps})
	}
	valuesActionsTransition {arg newEvent, argdurations, argcurves, argdelayTimes, update=true;
		var newKeys;
		//this.stopTransition;//of alleen de newKeys?
		//====================================================================== INIT
		if (update, {this.update});
		//====================================================================== transition keys
		//newEvent[transitionDurationKey]=argdurations??{argdurations=newEvent[transitionDurationKey]??{0}};
		//newEvent[transitionCurveKey]=argcurves??{argcurves=newEvent[transitionCurveKey]??{0.0}};
		//newEvent[transitiondelayTimeKey]=argdelayTimes??{argdelayTimes=newEvent[transitiondelayTimeKey]??{0}};
		//====================================================================== init transition
		newKeys=this.prInitTransition(newEvent, argdurations, argcurves, argdelayTimes);
		//======================================================================
		if (durations.collect{|i| i}.maxItem<waitTime, {
			newEvent.keysValuesDo{|key,val| this.keyValueAction(key, val) };
		},{
			//====================================================================== UPDATE
			//newEvent.keysValuesDo{arg key,value;
			newKeys.do{arg key;
				var value=newEvent[key];
				var startValue, func, cs, rico, start, end, step, stepSize, duration, delayTime, val;
				var env;
				startValue=this[key].value??{value};
				duration=durations[key]??{0};
				if (((value-startValue).abs<0.0001) || (duration<waitTime), {
					this.keyValueAction(key, value);
				},{
					//====================================================================== FORKS
					cs=specs[key];
					step=steps[key];
					stepSize=stepSizes[key];
					func=if (cs==nil
						, {
							rico=(value-startValue)*stepSize;
							{|i|
								val=rico*i+startValue;
								this.keyValueAction(key, val);
							}
						},{
							start=cs.unmap(startValue);
							end=cs.unmap(value);
							rico=(end-start)*stepSize;
							if (cs.step==0.0, {cs=cs.warp});
							{|i|
								val=cs.map(rico*i+start);
								this.keyValueAction(key, val);
							}
					});
					//========================================
					delayTime=(delayTimes[key]??{0.0});
					//step=getal;
					//step=Env([0,n],[1.0],curve).discretize(n);
					//step=Env([0,n],[1.0],curve).discretize(n);
					routines[key]={
						delayTime.wait;
						step.do{arg i;
							func.value(i);
							waitTime.wait;
						};
						this.keyValueAction(key, value);
					}.fork;
					//========================================
				});
			};
		})
	}
}
/*
+ Event {//newEvent, argdurations, argcurves, argdelayTimes, update=true;
	valuesActionsTransition { arg newEvent, durations, curves, delayTimes, specs, actions;
		this.asEventJT(specs, actions).valuesActionsTransition(newEvent, durations, curves, delayTimes, false);
	}
}
*/