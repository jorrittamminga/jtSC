/*
Button zeker bij twee states niet laten interpoleren!
*/
+ Event {
	envelopeCalc2 {arg curve, duration, delayTime=0;
		^switch (curve.class, Float, {
			if (curve!=0.0, {
				Env([0, 0, duration],[delayTime, duration], curve)
			},{
				Env([0, 0, duration],[delayTime, duration], 0)
			})
		}, Symbol, {
			Env([0, 0, duration],[delayTime, duration], curve)
		}, Env, {
			//Env(curve.levels*steps, curve.times, curve.curve)
		}, Array, {
			//curve.resamp1(steps)
		}, {
			Env([0, 0, duration],[delayTime, duration], 0)
		})
	}
	initTransition {arg event, durations, curves, delayTimes, specs, actions, resolution=10;
		var newEvent=event;
		var keyz=this.keys.asArray;
		var newKeys=newEvent.keys.asArray.sect(this.keys.asArray);
		var steps=(), stepSizes=();
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
				newKeys.do{|key| curves[key]=curve??{0}}
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
		switch(durations.class
			, Event, {
				newKeys.do{|key| durations[key]=durations[key]??{durations[\common]}};
				//----------------------------------------------------------------------- ROUTINES BASED
				if (resolution>0, {
					durations.keysValuesDo{|key,val|
						var step=val*resolution;
						var stepSize=step.reciprocal;
						steps[key]=step.round(1.0).asInteger;
						stepSizes[key]=stepSize;
						steps[key]=this.envelopeCalc(curves[key]??{0}, steps[key]);
						//----------------------------------------------------------------------- end ROUTINES BASED
					}
				},{
					newKeys.do{|key|
						steps[key]=this.envelopeCalc2(curves[key]??{0}, durations[key], delayTimes[key]);
					}
				})
			}
			, Float, {
				var dur=durations;
				//----------------------------------------------------------------------- ROUTINES BASED
				var step, stepSize;
				durations=();
				newKeys.do{|key| durations[key]=dur };
				//----------------------------------------------------------------------- ROUTINES BASED
				if (resolution>0, {
					step=dur*resolution;
					stepSize=step.reciprocal;
					step=step.round(1.0).asInteger;
					newKeys.do{|key|
						//steps[key]=step;
						steps[key]=this.envelopeCalc(curves[key]??{0}, step);
						stepSizes[key]=stepSize;
					}
				},{
					newKeys.do{|key|
						steps[key]=this.envelopeCalc2(curves[key]??{0}, durations[key], delayTimes[key]);
					}
				});
				//----------------------------------------------------------------------- end ROUTINES BASED
			}
		);
		actions=actions??{()};
		specs=specs??{()};
		newKeys.do{|key|
			if (this[key].respondsTo(\states), {
				durations[key]=0;
			});
			actions[key]=actions[key]??{
				if ((this[key].respondsTo(\action))&&(resolution>0), {
					{arg val;
						this[key].action.value(val);
						{this[key].value_(val)}.defer;
					}
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
		^[newEvent, newKeys, durations, curves, delayTimes, specs, actions, steps, stepSizes]
	}
	valuesTransition {arg event, durations, curves, delayTimes, specs, actions;
		var newEvent, newKeys, envs=(), timeEnv;
		#newEvent, newKeys, durations, curves, delayTimes, specs, actions, timeEnv=this.initTransition(event, durations, curves, delayTimes, specs, actions, 0);
		newEvent.sortedKeysValuesDo{|key, val|
			var startValue=this[key]??{val};
			var curve=0;
			if (specs[key]!=nil, {curve=specs[key].asSpec.warp.asSpecifier});

			if (startValue.size==0, {
				envs[key]=Env([startValue, val],[durations[key]], curve);
			},{
				envs[key]=startValue.collect{|startValue, i|
					Env([startValue, val[i]],[durations[key]], curve);
				}
			})
		};
		^[envs, timeEnv]
	}
	valuesActionsTransition2 {arg event, durations, curves, delayTimes, specs, actions, resolution=10;
		var newEvent, newKeys;
		//====================================================================== ROUTINE INITs
		var waitTime;
		var steps=(), stepSizes=();
		var routines;
		//====================================================================== INIT
		//durations[\common], curves[\common], delayTimes[\common]
		//---------------------------------------------------------- ROUTINE VARS
		resolution=resolution??{10};
		waitTime=resolution.reciprocal;
		[\routinesJT].do{|key| this.keys.asArray.remove(key)};
		this[\routinesJT]=this[\routinesJT]??{()};
		newKeys.do{|key| this[\routinesJT][key].stop;};
		//----------------------------------------------------------
		#newEvent, newKeys, durations, curves, delayTimes, specs, actions, steps, stepSizes=this.initTransition(event, durations, curves, delayTimes, specs, resolution);
		//======================================================================
		if (durations.collect{|i| i}.maxItem<waitTime, {
			newEvent.keysValuesDo{|key,val|
				//this.keyValueAction(key, val)
				actions[key].value(val)
			};
		},{
			//====================================================================== UPDATE
			newKeys=newKeys.sort;
			newKeys.do{arg key;
				var value=newEvent[key];
				var startValue, func, cs, rico, start, end, step, stepSize, duration, delayTime, val;
				var env, action;
				startValue=this[key].value??{value};
				duration=durations[key]??{durations[\common]??{0}};
				if ((value==startValue) || (duration<waitTime), {
					//if (((value-startValue).abs<0.0001) || (duration<waitTime), {
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
					this[\routinesJT][key]={
						delayTime.wait;
						step.do{arg i;
							func.value(i);
							waitTime.wait;
						};
						actions[key].value(value)
					}.fork;
					//========================================
				});
			};
		})
	}
}