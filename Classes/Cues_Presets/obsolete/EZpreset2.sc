/*
addUpdater gaat af en toe niet goed!!!!
can be more efficient.....
only update when something has changed?

path in init geeft de naam van de folder aan waar alle presets staan. naam van de preset (zoals dat bij restore en store het geval is) is de naam van de file/preset in die folder

scheidt de gui movements van de gui funcs (dus de gui movements in een grote defer, de gui funcs daar los van)
*/
GuiPreset {
	var <path, file, <>guis, <>preset, <routine, currentPreset, cs, <>resolution, interpolationArrays, updateFunc, noValue;
	var <>arrayKeys, newKeys, allKeysFromPreset;
	var guisThatNeverInterpolate;
	var routineMonitor;

	*new {arg guis, path, routineMonitor;
		^super.new.init(guis, path, routineMonitor)
	}

	init {arg argguis, argpath,argroutineMonitor;
		path=argpath??{"./preset"};
		guis=argguis;
		preset=();
		cs=();
		interpolationArrays=();
		resolution=10;
		routine=Routine;
		updateFunc=();
		guisThatNeverInterpolate=List[];
		arrayKeys=List[];
		noValue=();
		allKeysFromPreset=List[];

		routineMonitor=argroutineMonitor;

		guis.keysValuesDo({|key,val|
			if (val.isKindOf(EZGui), {cs[key]=val.controlSpec},{cs[key]=ControlSpec(0,1) });
			if (val.class.asString.contains("Text"), {
				guisThatNeverInterpolate.add(key);
				noValue[key]=true;
			},{noValue[key]=false});

			updateFunc[key]={|i|
				preset[key]=i.value
			};

			if (val.class.asString.contains("utton"), {
				if (val.states.size<3, {guisThatNeverInterpolate.add(key)});
			});
		});

		if( File.exists(path).not, {
			preset=();
			guis.keys.do({|key|
				var value;
				if (noValue[key],{
					value=guis[key].value;
					if (value==nil, {value==""});
					},{
						value=guis[key].value;
						if (value==nil, {value=cs[key].minval; });
						if (value.size>0, {value=value.collect({|i| if (i==nil, {cs[key].minval},{i})});   });
				});
				preset[key]=value;
			});
			this.store(path);
			this.restore(path, 0);
			},{
				file=File(path,"r");
				preset=file.readAllString.interpret;
				file.close;
				/*
				allKeysFromPreset=preset.keys;
				guis.keys.difference(allKeysFromPreset).do({|key|
				preset[key.asSymbol]=guis[key].value;
				});
				if (	allKeysFromPreset.size>0, {
				file=File(path,"w");
				file.write(preset.asCompileString);
				file.close;
				});
				*/
				this.restore(path, 0);

		});

		guis.keys.do({|key|
			var remover={
				if (arrayKeys.includes(key), {arrayKeys.remove(key); this.addUpdater(key)});
			};
			this.addUpdater(key);
			guis[key].mouseDownAction_(remover);
		});

	}

	stop {
		if (routine.isPlaying, {
			routine.stop;
			arrayKeys.do({|key| this.addUpdater(key); });
			arrayKeys=List[];
		});
	}

	addUpdater {arg key;
		guis[key].addAction(updateFunc[key]);
	}

	removeUpdater {arg key;
		guis[key].removeAction(updateFunc[key]);
	}

	restore {arg argpath, time=0.0;
		var file;
		var source=(), target, directKeys=List[], keys, numberOfSteps=(time*resolution).round(1.0).asInteger, waitTime=resolution.reciprocal;
		var env;
		routine.stop;

		//source=preset;
		path=argpath??{path};

		if( File.exists(path).not, {
			this.store(path)
			},{
				file=File(path,"r");
				preset=file.readAllString.interpret;
				file.close;
		});
		if (preset!=nil, {
			target=preset.deepCopy;

			target.keys.do({|key|
				if (guis[key]==nil, {target.removeAt(key)},{
					if (noValue[key], {
						//target[key]=guis[key].string;
						},{
							case
							{(guis[key].value.size==0)&&(target[key].size>0)}{target[key]=target[key][0]}
							{(guis[key].value.size>0)&&(target[key].size==0)}{target[key]=target[key].dup(guis[key].value.size)};
					})
				});
			});

			preset=target;//DIT MOET NOG WAT BETER!

			if (time<=0.0, {
				target.keysValuesDo({|key,val|
					if (guis[key]!=nil, {
						this.removeUpdater(key);
						{guis[key].valueAction_(val); }.defer;
						this.addUpdater(key);
					});
				});
				},{
					arrayKeys=List[];
					//				env=Env([0.0, 1.0],[1.0],[\sine]).discretize(numberOfSteps).as(Array);
					env=Env([0.0, 1.0],[1.0],[0]).discretize(numberOfSteps).as(Array);
					guis.keys.do({|key|
						source[key]=guis[key].value
					});
					keys=source.keys.sect(target.keys);
					keys.do({|key|
						var flag=true;
						if (noValue[key].not, {flag=(source[key].asArray-target[key].asArray).abs.sum<0.0000001});
						if (flag || (guisThatNeverInterpolate.includes(key)), {directKeys.add(key)},{arrayKeys.add(key)})
					});
					directKeys.do({|key|
						this.removeUpdater(key);
						{guis[key].valueAction_(target[key]); }.defer;
						this.addUpdater(key);
					});
					arrayKeys.do({|key|
						var cSpec=cs[key],start,end;
						interpolationArrays[key]=if (source[key].size>0, {
							start=source[key].asArray; end=target[key].asArray;
							start.collect({|v1,i| var v2=end[i];
								cSpec.map(env.range(cSpec.unmap(v1), cSpec.unmap(v2)) );
							}).flop
							},{
								cSpec.map(env.range(cSpec.unmap(source[key]), cSpec.unmap(target[key])) );
						});
					});
					if (arrayKeys.size>0, {
						routine={
							arrayKeys.do({|key| this.removeUpdater(key); });
							if (routineMonitor!=nil, {{routineMonitor.value_(1);}.defer});
							numberOfSteps.do({|i|
								arrayKeys.do({|key|
									var val=interpolationArrays[key][i];
									{guis[key].valueAction_(val)}.defer;
								});
								waitTime.wait;
							});
							if (routineMonitor!=nil, {{routineMonitor.value_(0)}.defer;});
							arrayKeys.do({|key| this.addUpdater(key); });
							arrayKeys=List[];
						}.fork;
					});
			});

			},{
				this.store(path);
		});
	}


	store {arg argpath;
		path=argpath??{path};
		file=File(path,"w");
		file.write(preset.asCompileString);
		file.close;
	}

}
