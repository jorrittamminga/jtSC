/*
TODO:
- ???*newInterpolate() + initInterpolate???
*/
+ PresetSystem
{
	addInterpolation {arg time, curve, interpolate=1, resolution=10, argslaves;
		this.initInterpolate(time, curve, interpolate, resolution);
		if ((argslaves==false).not, {
			if (slaves.size>0, {
				argslaves=argslaves??{(0..(slaves.size-1))};
				//if (argslaves==nil, {
				slaves.do{|ps|
					ps.initInterpolate(time, curve, interpolate, resolution);
				}
				//})
			})
		});
		masters.do{|ps| ps.addInterpolation(time, curve, interpolate, resolution)};
		^this
	}
	removeInterpolation {
		this.stopAll;
		canInterpolate=false;
	}
	initInterpolate {arg argtime, argcurve, argInterpolate=1, argresolution=10;
		canInterpolate=true;
		interpolate=argInterpolate;
		timeKey=\interpolationTime;
		curveKey=\interpolationCurve;
		//extraKey=\extraParameters;
		routines=();
		routineFunctions=(main:());//(main:[]);
		time=argtime??{newValues[timeKey]??{0.0}};//time[\main]=1.0;
		interpolationCurve=argcurve??{newValues[curveKey]??{0}};
		this.resolution_(argresolution);
		extra=(\removeFromInterpolation:[]
			, \removeOnceFromInterpolation:[]
		);
		nextAction={
			this.restoreI;
		};
		prevAction={
			this.restoreI;
		};
		views.keysValuesDo{|key,view|
			if([Button, TextField].includes(view.class), {
				this.removeFromInterpolation(key)
			});
		};
		functions[\path]={this.restoreI(0)};

		parents.do{|parent|
			parent.onClose=parent.onClose.addFunc({
				routines.do{|r| r.stop}
			})
		};
	}
	interpolate_ {arg i;
		interpolate=i;
		if (interpolate==0, {
			this.stop;
			this.restore;
		});
		slaves.do{|ps| ps.interpolate_(interpolate)};
		masters.do{|ps| ps.interpolate_(interpolate)};
		functions[\interpolate].value;
	}
	removeFromInterpolation {arg keys;
		keys.asArray.do{|key|
			extra[\removeFromInterpolation]=extra[\removeFromInterpolation].add(key);
		};
		extra[\removeFromInterpolation]=extra[\removeFromInterpolation].asSet.asArray;
	}
	removeOnceFromInterpolation {arg keys;
		keys.asArray.do{|key|
			extra[\removeOnceFromInterpolation]=
			extra[\removeOnceFromInterpolation].add(key);
		};
		extra[\removeOnceFromInterpolation]=
		extra[\removeOnceFromInterpolation].asSet.asArray;
	}
	addToInterpolation{arg keys;
		keys.asArray.do{|key|
			extra[\removeFromInterpolation].remove(key);
		};
	}
	resolution_ {arg r;
		resolution=r??{resolution};
		waitTime=resolution.reciprocal;
		steps=time*resolution;//steps[\main]=
		stepSize=steps.reciprocal;//stepSize[\main]=
	}
	storeI {arg i, t, curve;
		var file;
		this.index_(i);
		file=File(fullPath, "w");
		this.getValues;
		values[timeKey]=t??{time};
		values[curveKey]=curve??{interpolationCurve??{newValues[curveKey]??{0}}};

		//if (extraParameters!=nil, {values[extraKey]=extraParameters});
		file.write(values.asCompileString);
		file.close;
		if (preLoad, {presets[index]=values});
		masters.do{|ps| ps.storeI(i, t, curve)};
		functions[\store].value;
	}
	/*
	storeAllI {arg i, name;
	this.store(i, name);
	slaves.do{|presetsystem| presetsystem.store(i, name) };
	}
	*/
	//betere naam verzinnen voor interpolateOnce
	interpolateOnce {arg key, time, curve, toValue, fromValue;
		var steps, stepSize;
		toValue=toValue??{presets[index][key]};
		steps=time*resolution;//steps[\main]=
		stepSize=steps.reciprocal;
		routineFunctions[key]=();//dit kan mooier he!!!
		fromValue=fromValue??{views[key].value};
		if ( (fromValue-toValue).abs>0.0000001, {
			this.makeInterpolationFunction(key
				, fromValue, toValue, stepSize, curve, key);
			this.interpolateForks(steps, key, curve);
			this.removeOnceFromInterpolation(key);
		});
	}
	restoreActionKeyValue{arg key, value;
		//actions[key].value(value);//
		views[key].action.value(value);
		if (guiFlag, {
			{views[key].value_(value)}.defer;
		});
	}
	//=doInterpolateMain of prInterpolate (private)
	doInterpolate {arg t, curve, getValues=true;
		var keys, routineKeys;
		//----------------------------------------------- stop all routines
		//if (routines!=nil, {
		routineKeys=routines.keys;
		routineKeys.removeAll(extra[\removeOnceFromInterpolation]);
		routineKeys.do{|key|
			routines[key].stop};
		//});
		//----------------------------------------------- restore function
		keys=views.keys;//not very efficient.... do this in the init function, not everytime
		time=if (t==nil, {
			if (newValues[timeKey]==nil, {
				time
			},{
				newValues[timeKey]
		})}, {t});

		interpolationCurve=curve??{newValues[curveKey]??{0}};
		//-------------------------------------------------- INTERPOLATION
		if (time>0, {
			//temporaraly turn off morph if there is any
			this.time_(time);
			if (getValues, {this.getValues});
			//------------------------------------------------- NO INTERPOLATION
			keys.removeAll(extra[\removeFromInterpolation]);
			keys.removeAll(extra[\removeOnceFromInterpolation]);
			extra[\removeFromInterpolation].do{|key|
				views[key].action.value(newValues[key]);
			};
			if (guiFlag, {
				extra[\removeFromInterpolation].do{|key|
					{views[key].value_(newValues[key])}.defer
				};
			});
			routineFunctions[\main]=();
			keys.do{|key|
				var fromValue=values[key]//??{defaultValues[key]}
				, toValue=newValues[key]//??{defaultValues[key]}
				;
				//var flag=(toValue-fromValue).asArray.sum.abs>0.000001;
				var flag;

				flag=(toValue.asArray-fromValue.asArray).flat.abs.sum>0.000001;

				if (flag, {
					this.makeInterpolationFunction(key, values[key], newValues[key]
						, stepSize
						//, curve//specific curve per key, default=0.0?
					);
				},{
					views[key].action.value(newValues[key]);
					if (guiFlag, {
						{views[key].value_(newValues[key])}.defer
					})
				});
			};
			//---------------------------------------------------- stop morphing
			this.interpolateForks(steps, \main, interpolationCurve);

			if (hasMorph==true, {
				routines[\PresetMorphBypass]={
					//routines[\PresetMorphBypass].stop;
					presetMorph.stop;
					time.wait;
					presetMorph.start;
				}.fork;
			});

			//slaves.do{|presetsystem| presetsystem.restoreI(index, getValues:getValues) };
		},{
			restoreAction.value(this);
			//slaves.do{|presetsystem| presetsystem.restore(index) };
		});
		/*
		slaves.do{|presetsystem|
		if (presetsystem.canInterpolate, {
		presetsystem.restoreI(index, getValues:getValues)
		},{
		presetsystem.restore(index)
		})
		};
		*/
		/*
		slaves.do{|presetsystem|
		if (presetsystem.type==\subfolder, {
		if (File.exists(presetsystem.localpath), {
		presetsystem.restore(fromMaster:true)
		});
		},{
		if (presetsystem.canInterpolate, {
		presetsystem.restoreI(index, getValues:getValues, fromMaster:true)
		},{
		presetsystem.restore(fromMaster:true)//(index) beter???
		})
		})
		};
		*/
		slaves.do{|presetsystem|
			if (presetsystem.type==\subfolder, {
				if (File.exists(presetsystem.localpath), {

					//hier even op de interpolationtime van preset index 0 checken!

					if (presetsystem.canInterpolate, {
						presetsystem.restoreI(getValues:getValues, fromMaster:true)
					},{
						presetsystem.restore(fromMaster:true)//(index) beter???
					})
				});
			},{
				if (presetsystem.canInterpolate, {
					presetsystem.restoreI(index, getValues:getValues, fromMaster:true)
				},{
					presetsystem.restore(fromMaster:true)//(index) beter???
				})
			})
		};

		//----------------------------------------------- end of restore
		functions[\restore].value;
		extra[\removeOnceFromInterpolation]=[];
	}
	loadI {arg i, t, curve, getValues=true;
		var file, extra;
		this.index_(i);
		if (File.exists(fullPath), {
			file=File(fullPath, "r");
			newValues=file.readAllString.interpret;
			file.close;
			extra=views.keys.difference(newValues.keys);
			extra.do{|key| newValues[key]=views[key].value};
			this.checkValues;
			this.doInterpolate(t, curve, getValues);
		});
	}
	restoreI {arg i, t, curve, getValues=true, fromMaster=false;
		var file, extra;
		if (interpolate==0, {
			this.restore(i, fromMaster)
		},{
			if (fromMaster.not, {
				this.index_(i);
			});
			if (presets[index]!=nil, {
				newValues=presets[index];
				/*
				extra=views.keys.difference(newValues.keys);
				extra.do{|key|
				{views[key].value}.defer;
				newValues[key]=views[key].value
				};
				*/
				this.doInterpolate(t, curve, getValues);
			});
			masters.do{|ps| ps.restoreI(i,t,curve,getValues)};
		});
	}

	stop {
		//doe hier iets met excludeOnceParameter
		routines.do(_.stop);
		masters.do{|ps| ps.stop};
		//---------------------------------------------------- stop morphing
		if (hasMorph==true, {
			presetMorph.start;
		});
	}

	stopAll {
		this.stop;
		slaves.do{|sl| sl.stop};
	}

	interpolateForks {arg steps, key=\main, curve;
		var env;
		routines[key].stop;//routines.do(_.stop);
		routines[key]=if (
			//(curve!=nil)||(curve!=0.0), {
			(curve!=nil)&&(curve!=0.0), {
				//curve=newValues[curveKey];
				env=Env.new([0, steps], [steps], curve);
				{
					(steps+1).do{|i|
						i=env.at(i);
						routineFunctions[key].do{|func|
							func.value(i);
						};
						waitTime.wait;
					}
				}.fork
			},{
				{
					(steps+1).do{|i|
						//i=env.at(i);
						routineFunctions[key].do{|func|
							func.value(i);
						};
						waitTime.wait;
					}
				}.fork
		});
	}
	//
	time_{arg t, key=\main;
		time=t??{time};//time[key]
		time=time.abs;
		steps=time*resolution;//time[key]
		stepSize=steps.reciprocal;//stepSize[key]
	}

	interpolationCurve_ {arg curve, key=\main;
		interpolationCurve=curve??{0};
		values[curveKey]=interpolationCurve;
	}

	makeInterpolationFunction {arg key, fromValue, toValue, stepSize, curve
		, routineKey=\main;
		var flag, func;
		var start, end, rico, out, gui=views[key]
		//, action=actions[key]
		, action=views[key].action
		//, cs=controlSpecs[key]
		, cs=if (controlSpecs[key]!=nil, {views[key].controlSpec},{nil})
		, env;

		func=if (cs==nil
			//controlSpecs[key]==nil//
			, {
				//			if ((curve==nil)||(curve==0), {
				rico=(toValue-fromValue)*stepSize;
				{|i| rico*i+fromValue }
				//			},{
				//				env=Env([fromValue,toValue],[stepSize.reciprocal], curve);
				//				{|i| env.at(i) }
				//			});
			},{
				start=cs.unmap(fromValue);
				end=cs.unmap(toValue);
				//			if ((curve==nil)||(curve==0), {
				rico=(end-start)*stepSize;
				{|i| cs.map(rico*i+start)}
				//			},{
				//				env=Env([start,end],[stepSize.reciprocal], curve);
				//				{|i| cs.map(env.at(i)) }
				//			});
		});

		routineFunctions[routineKey][key]={arg i;
			//out=cs.map((rico*i+start));
			out=func.value(i);
			//this.restoreActionViewValue(view,out);
			if (guiFlag, {
				{gui.value_(out)}.defer;
			});
			action.value(out);
		}
	}
	/**/
}