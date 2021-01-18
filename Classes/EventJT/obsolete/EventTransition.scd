/*
implement also other transition types (instead of only a to b.....)
curve can be a number (where 0 is linear), an envelope or an array filled with a 'shape'

x=(a: 123, b:432);
x.transitionTo_((a:432, b:436), 1, \sin, actions: {|val,key| [key,val].postln})
x.transitionTo_((a:100, b:50), 2.0, \sin)
x.transitionTo_((a:200, b:150))
x.transitionTo_((a:200, b:150), 3.0, 0.0)

a=(a: {|val,key| [key,val, "hallo"].postln}, b: {|val,key| [key,val].postln})
x=(a: 123, b:432);
x.transitionTo_((a:432, b:436), 1, actions: a)
x.transitionTo_((a:4322, b:1436))
a[\a]={|val,key| [key,val, "yo"].postln}
x.transitionTo_((a:1, b:2), 3.5)

{100.do{|i| i.sqrt+1}}.bench
{x=(0..99); 100.do{|i| x[i]+1}}.bench
{x=(0..99); x.do{|i| i+1}}.bench

*/
EventTransition : EventManagerJT {
	//var <specs, <actions, <event, <keys;

	var <delayTimes, <curves, <durations;
	var <steps, <stepSizes, <waitTime, routines, <allKeys;
	var <resolution=10;

	*new {arg event, specs, actions;
		^super.new.init(event, specs, actions)
	}
	init {arg argevent, argspecs, argactions;
		event=argevent;
		allKeys=event.keys;
		specs=argspecs??{()};
		actions=();
		if (argactions!=nil, {this.actions_(argactions)});
		//==========================================
		delayTimes=();
		curves=();
		specs=();
		durations=();
		routines=();
		stepSizes=();
		steps=();
		//==========================================
		this.resolution_(10);
		event.know=this;//misuse of the class variable 'know'.....
	}
	free {
		this.stop
	}
	close {
		this.free
	}
	stop {
		routines.do{|r| r.stop}
	}
	resolution_ {arg r;
		resolution=r??{resolution};
		waitTime=resolution.reciprocal;
	}
	actions_ {arg action, key;
		if (key!=nil, {
			actions[key]=action;
		},{
			switch(action.class, Function, {
				allKeys.do{arg key; actions[key]=action}
			}, Event, {
				action.keysValuesDo{|key,action| actions[key]=action}
			},{

			})
		})
	}
	keyValueAction {arg key, value;
		event[key]=value;
		actions[key].value(value, key);
	}
	prInit {arg newEvent, argdurations, argcurves, argdelayTimes;
		var newKeys=newEvent.keys;
		allKeys.addAll(newKeys);
		#argdurations, argcurves, argdelayTimes=[argdurations, argcurves, argdelayTimes].collect{|key|
			if (key.class.superclass==SimpleNumber, {key.asFloat},{key});
		};
		switch(argcurves.class
			, Event, {argcurves.keysValuesDo{|key,val| curves[key]=val}}
			, Nil, {}
			, {newEvent.keysDo{|key| curves[key]=(argcurves??{0}) }}
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
				newEvent.keysDo{|key|
					steps[key]=step;
					steps[key]=this.envelopeCalc(curves[key]??{0}, step);
					stepSizes[key]=stepSize;
					durations[key]=argdurations
				}
			}
		);
		switch(argdelayTimes.class
			, Event, {argdelayTimes.keysValuesDo{|key,val| delayTimes[key]=val}}
			, Float, {newEvent.keysDo{|key| delayTimes[key]=argdelayTimes}}
			, { newEvent.keysDo{|key| if (delayTimes[key]==nil, {delayTimes[key]=0})} }
		);
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
	transitionTo_ {arg newEvent, argdurations, argcurves, argdelayTimes;
		//====================================================================== INIT
		this.prInit(newEvent, argdurations, argcurves, argdelayTimes);
		//======================================================================
		if (durations.collect{|i| i}.maxItem<waitTime, {
			newEvent.keysValuesDo{|key,val| this.keyValueAction(key, val) };
		},{
			//====================================================================== UPDATE
			newEvent.keysValuesDo{arg key,value;
				var startValue, func, cs, rico, start, end, step, stepSize, duration, delayTime, val;
				var env;
				startValue=event[key]??{value};
				duration=durations[key]??{0};
				if (((value-startValue).abs<0.0001) || (duration<waitTime), {
					this.keyValueAction(key, value);
					//event[key]=value;
					//actions[key].value(value, key);
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
						delayTime.wait;//
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

+ Event {
	transitionTo_ { arg newEvent, durations, curves, delayTimes, specs, actions;
		^if (this.know.class==EventTransition, {//beter: this.parent.class!
			this.know.transitionTo_(newEvent, durations, curves, delayTimes);
		},{
			EventTransition(this, specs, actions).transitionTo_(newEvent, durations, curves, delayTimes);
		})
	}
}