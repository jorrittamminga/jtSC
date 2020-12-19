/*
Button zeker bij twee states niet laten interpoleren!
*/
+ Event {
	free {
		this.stopTransition
	}
	close {
		this.free
	}
	stopTransition {
		this[\routinesJT].do{|r| r.stop}
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
	valuesActionsTransition {arg event, durations, curves, delayTimes, specs, actions, resolution=10, nrt=false;
		var newEvent=event;
		var waitTime=resolution;
		var steps=(), stepSizes=();
		//====================================================================== INIT
		var newKeys=newEvent.keys.asArray;
		var keyz=this.keys.asArray, routines;
		//durations[\common], curves[\common], delayTimes[\common]
		resolution=resolution??{10};
		nrt=nrt??{false};
		waitTime=resolution.reciprocal;

		[\routinesJT].do{|key| keyz.remove(key)};
		newKeys=newKeys.sect(keyz);
		this[\routinesJT]=this[\routinesJT]??{()};
		newKeys.do{|key|
			this[\routinesJT][key].stop;
		};
		#durations, curves, delayTimes=[durations, curves, delayTimes].collect{|key|
			if (key.class.superclass==SimpleNumber, {key.asFloat},{key});
		};
		switch(curves.class
			, Event, {
				newKeys.do{|key| curves[key]=curves[key]??{curves[\common]??{0}}  }
			}
			, {
				var curve=curves;
				curves=();
				newKeys.do{|key|
					curves[key]=curve??{0}
				}
			}
		);
		switch(durations.class
			, Event, {
				newKeys.do{|key| durations[key]=durations[key]??{durations[\common]}};
				durations.keysValuesDo{|key,val|
					var step=val*resolution;
					var stepSize=step.reciprocal;
					steps[key]=step.round(1.0).asInteger;
					stepSizes[key]=stepSize;
					steps[key]=this.envelopeCalc(curves[key]??{0}, steps[key]);
				}
			}
			, Float, {
				var dur=durations;
				var step=dur*resolution;
				var stepSize=step.reciprocal;
				durations=();
				step=step.round(1.0).asInteger;
				newKeys.do{|key|
					//steps[key]=step;
					steps[key]=this.envelopeCalc(curves[key]??{0}, step);
					stepSizes[key]=stepSize;
					durations[key]=dur
				}
			}
		);
		switch(delayTimes.class
			, Event, {
				//delayTimes.keysValuesDo{|key,val| delayTimes[key]=val}
				newKeys.do{|key| delayTimes[key]=delayTimes[key]??{delayTimes[\common]??{0}}  }
			}
			//, Float, {newKeys.do{|key| delayTimes][key]=delayTimes}
			, { var dt=delayTimes; delayTimes=(); newKeys.do{|key| delayTimes[key]=dt??{0}  } }
		);
		actions=actions??{()};
		specs=specs??{()};
		newKeys.do{|key|
			if (this[key].respondsTo(\states), {
				durations[key]=0;
			});
			actions[key]=actions[key]??{
				if (this[key].respondsTo(\action), {
					if (nrt, {
						{arg val;
							this[key].action.value(val);
						}
					},{
						{arg val;
							this[key].action.value(val);
							{this[key].value_(val)}.defer;
						}
					})
				},{
					{arg val; this[key]=val}
				})
			};
			specs[key]=specs[key]??{
				if (this[key].respondsTo(\action), {
					this[key].controlSpec;
				},{
					nil
				});
			};
		};
		//======================================================================
		if (durations.collect{|i| i}.maxItem<waitTime, {
			newEvent.keysValuesDo{|key,val|
				//this.keyValueAction(key, val)
				actions[key].value(val)
			};
		},{
			//====================================================================== UPDATE
			newKeys.do{arg key;
				var value=newEvent[key];
				var startValue, func, cs, rico, start, end, step, stepSize, duration, delayTime, val;
				var env, action;
				startValue=this[key].value??{value};
				duration=durations[key]??{durations[\common]??{0}};
				if ((value==startValue) || (duration<waitTime), {
					//if (((value-startValue).abs<0.0001) || (duration<waitTime), {
					//hier ook nog een nrt versie maken!
					actions[key].value(value)
				},{
					//====================================================================== FORKS
					cs=specs[key];
					step=steps[key]??{steps[\common]};
					stepSize=stepSizes[key]??{stepSizes[\common]};
					action=actions[key];
					func=if (cs==nil
						, {
							rico=(value-startValue)*stepSize;
							{|i|
								val=rico*i+startValue;
								//this.keyValueAction(key, val);
								action.value(val)
							}
						},{
							start=cs.unmap(startValue);
							end=cs.unmap(value);
							rico=(end-start)*stepSize;
							if (cs.step==0.0, {cs=cs.warp});
							{|i|
								val=cs.map(rico*i+start);
								//this.keyValueAction(key, val);
								action.value(val)
							}
					});
					//========================================
					delayTime=delayTimes[key]??{0.0};
					if (nrt, {
						this[\routinesJT][key]={arg time;
							func.value((time-delayTime).clip(0, duration+delayTime)*resolution)
						};
					},{
						this[\routinesJT][key]={
							delayTime.wait;
							step.do{arg i;
								func.value(i);
								waitTime.wait;
							};
							actions[key].value(value)
						}.fork;
					});
					//========================================
				});
			};
		})
	}
}