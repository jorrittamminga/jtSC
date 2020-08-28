/*
- als je het aantal presets verandert, verander dan automatisch de azimuth slider!
- als je presets verandert en dan gaat morphen (via .mapToMIDI) dan gaat het mis....
- het aantal dimensions gaat plots omhoog waardoor de calculatefunc verandert!!!!
- laat ook de presetnaam (en dus de preset) van de slaves (en de master) meelopen
- morphen met ezmultislider gaat nog mis bij azimuth2D
TODO:
- maak een optie om een lijst van curves die je in de envelopes kunt stoppen
- maak een optie om een lijst van times (per parameter!) die je in de envelopes kunt stoppen
- een segwarp kan natuurlijk ook in de azimuth fader (dan geldt het voor alle parameters)
- vervang blendAt???? (hoewel, het lijkt toch wel een snelle methode)
- bij 'add' in PresetSystem doet PresetMorph twee maal een update/indices, vanwege functions[\store] (aangeroepen door this.store) en functions[\update]. add bevat zowel store als update.... kan dit handiger???
- GUI maken om presets te selecteren en indices te formuleren?
-
p=PresetMorph(presetsystem, indices)
indices of the indices of the filelist in PresetSystem
[0,1,2] = 1D
[[0,1,2]] = 2D circle (center of the circle is the mean of the presets
[[0,1,2,3],4] = 2D pyramid, center of the circle is 4
[[0,1,2,3],[4]] = 3D pyramid
[[0,1,2,3],[4,5,6,7]] = 3D cube
*/
PresetMorph {
	var x, y, z, point, <array, <means, <controlSpecs, <guis, preset, <indices, <entries;
	var <azimuth, <rho, <elevation, <dimensions, <size, <actions, <offset;
	var calculateFunction, <parent, bounds, method, <views, <viewKeys, <>keysThatCanMorph;
	var <keysNoCS, <presets, <initIndices, <keysNoMorph;
	var <>guiType, type, <hasGUI, <presetNr, <functions, <guiFlag;
	var <defaultControlSpecs, <defaultKeysNoCS;
	var <>isMorphing;

	*line1D{arg preset, indices, method='clipAt', views, guiType='presets';
		//var indices=(0..(preset.fileNamesWithoutExtensions.size-1));
		^super.new.init(preset, indices, method, views, guiType, 'oneD');
	}
	*circle1D{arg preset, indices, method='wrapAt', views, guiType='presets';
		//var indices=(0..(preset.fileNamesWithoutExtensions.size-1));
		^super.new.init(preset, indices, method, views, guiType, 'oneD');
	}
	*circle2D{arg preset, method='wrapAt', views, guiType='presets';
		var indices=[(0..(preset.fileNamesWithoutExtensions.size-1))];
		^super.new.init(preset, indices, method, views, guiType, 'twoD');
	}
	*triangle2D{arg preset, method='clipAt', views, guiType='presets';
		var indices=[(0..(preset.fileNamesWithoutExtensions.size-1))];
		^super.new.init(preset, indices, method, views, guiType, 'twoD');
	}
	*polar1D{arg preset, method='wrapAt', views, guiType='azimuth';
		var indices=(0..(preset.fileNamesWithoutExtensions.size-1));
		^super.new.init(preset, indices, method, views, guiType, 'oneD');
	}
	*polar2D{arg preset, method='wrapAt', views, guiType='azimuth';
		var indices=[(0..(preset.fileNamesWithoutExtensions.size-1))];
		^super.new.init(preset, indices, method, views, guiType, 'twoD');
	}
	/*
	*oneD {arg preset, method='wrapAt', views, guiType='presets';
	var indices=(0..(preset.fileNamesWithoutExtensions.size-1));
	^super.new.init(preset, indices, method, views, guiType, 'oneD');
	}

	*twoD {arg preset, method='wrapAt', views, guiType='presets';
	var indices=[(0..(preset.fileNamesWithoutExtensions.size-1))];
	^super.new.init(preset, indices, method, views, guiType, 'twoD');
	}
	*/
	*new {arg preset, indices, method='wrapAt', keysThatCanMorph, guiType='azimuth', type='oneD';
		^super.new.init(preset, indices, method, keysThatCanMorph, guiType, type)
	}

	init {arg argpreset, argindices, argmethod, argkeysThatCanMorph, argguiType, argtype;
		azimuth= -1.0;
		rho=1.0;
		elevation=0.0;
		method=argmethod;
		guiType=argguiType;//\azimuth;

		initIndices=(argindices==nil);

		type=argtype;
		hasGUI=false;
		guiFlag=false;
		presetNr=0;
		functions=();
		preset=argpreset;
		preset.hasMorph=true;
		preset.presetMorph=this;

		keysThatCanMorph=argkeysThatCanMorph;//??{preset.views.keys.asArray};
		//================================================= getSettings and controlspecs
		this.getSettings;

		//================================================= getControlSpecs
		this.indices_(argindices);


		preset.functions[\store]=preset.functions[\store].addFunc({|index|
			if( PathName(preset.localpath).entries.size==preset.presets.size, {
				this.indices_( indices, index );
			},{

			});
		});
		preset.slaves.do{|preset|
			preset.functions[\store]=preset.functions[\store].addFunc({|p|
				this.indices_( indices );
			});
		};

		preset.functions[\index]=preset.functions[\index].addFunc({|p|
			this.index;
		});
		preset.functions[\restore]=preset.functions[\restore].addFunc({|p|
			this.index;
		});
		//		if (type=='oneD', {

		preset.functions[\update]=preset.functions[\update].addFunc({|p|
			this.indices_( indices );
		});

		preset.functions[\path]=preset.functions[\path].addFunc({|p|
			this.indices_(indices);
		});
		//		});

		functions[\script]=if (preset.canScript, {
			{arg index; preset.restoreScript(index)}
		},{
			{}
		});
		//this.gui;
		isMorphing=true;
	}
	/*
	extraControlSpecs {
	views.keys.asArray.difference(controlSpecs.keys.asArray).do{|key|
	var view=views[key];
	NumberBox.superclass

	ListView.superclass
	}
	}
	*/

	getSettings {
		var noMorph=[];
		controlSpecs=();
		//--------------------------------------------- vervangen!
		if (keysThatCanMorph==nil, {views=preset.views},{
			views=();
			keysThatCanMorph.asArray.do{|key| views[key]=preset.views[key]};
		});
		//--------------------------------------------- vervangen!
		//preset.views.keysValuesDo{|key,view| var controlSpec;
		views.keysValuesDo{|key,view| var controlSpec;
			if (view.isKindOf(EZGui), {
				controlSpec=view.controlSpec;
				if (controlSpec.step<0.000001, {controlSpec=controlSpec.warp});
				controlSpecs[key]=controlSpec;
		},{nil})};
		controlSpecs=[controlSpecs];
		//controlSpecs=[preset.controlSpecs];
		//actions=[preset.actions];

		//--------------------------------------------- vervangen!
		//actions=[preset.views.collect(_.action)];
		actions=[views.collect(_.action)];
		//--------------------------------------------- vervangen!
		//views=[preset.views];
		views=[views];

		//this.extraControlSpecs;
		if (preset.slaves.size>0, {
			preset.slaves.do{|ps|
				controlSpecs=controlSpecs.add(ps.controlSpecs);
				//actions=actions.add(ps.actions);
				actions=actions.add(
					//ps.actions
					ps.views.collect(_.action)
				);
				views=views.add(ps.views);
			};
		});
		keysNoCS=views.collect{|view,i|
			view.keys.asArray.difference(controlSpecs[i].keys.asArray)
		};
		keysNoMorph={List[]}!keysNoCS.size;//keysNoCS.deepCopy;

		keysNoCS.do{|array, i|
			array.do{|key,j|
				if (views[i][key].class==TextField, {
					noMorph=noMorph.add([i,key]);
				},{
				});
			};
		};

		noMorph.do{|x,i|
			keysNoCS[x[0]].remove(x[1]);
			keysNoMorph[x[0]].add(x[1]);
		};


		defaultControlSpecs=controlSpecs;
		defaultKeysNoCS=keysNoCS;

	}

	indices_ {arg argindices, index;
		var extraDimension=0, pr;

		controlSpecs=defaultControlSpecs.deepCopy;
		keysNoCS=defaultKeysNoCS.deepCopy;

		indices=if (initIndices, {
			dimensions=nil;
			(0..(PathName(preset.localpath).entries.size-1));
		},{
			argindices??{(0..(PathName(preset.localpath).entries.size-1))};
		});
		presets=[preset.presets];
		preset.slaves.do{|ps|
			var pr=ps.presets, y;
			pr=pr.collect{|p| if (p!=nil, {y=p; p},{y}) };
			presets=presets.add(pr);
		};
		if (dimensions==nil, {
			dimensions=indices.rank;
			if (dimensions==1, {indices=[indices]});
			//if (indices.size>1, {dimensions=3});
		});
		size=indices.collect{|i| i.size};
		offset=indices.collect{|i| i.minItem};

		//--------------------------------------------------------------
		//remove keys for equal values, these keys don't need morphing
		presets.do{arg presets,index;
			var keysArrays=();
			indices.unbubble.do{|i|
				presets[i].keysValuesDo{arg key,val;
					if (keysArrays[key]==nil, {
						keysArrays[key]=[ val ]
					},{
						keysArrays[key]=keysArrays[key].add(val)
					});
				};
			};
			keysArrays.keysValuesDo{|key,array|
				if (array.asSet.size==1, {//no morph!
					controlSpecs[index].removeAt(key);
					if (keysNoCS[index].includesEqual(key), {
						keysNoCS[index].remove(key)
					});
				},{

				});
			}
		};
		//--------------------------------------------------------------

		this.calculate(true);
		if (hasGUI, {
			if (guiType=='presets', {
				guis[\azimuth].controlSpec=
				ControlSpec(0.0, size[0]-(method=='clipAt').binaryValue);
			})
		});
	}
	update {
		this.getSettings;
		if (indices!=nil, {
			this.indices_(indices)
		})
	}
	index {
		if (hasGUI, {
			{guis[\azimuth].value_(preset.index)}.defer
		});
	}
	/*
	polar1D {arg argazimuth;
	azimuth=argazimuth;
	calculateFunction.value(azimuth, rho, elevation);
	}
	polar2D {arg argazimuth, argrho;
	azimuth=argazimuth;
	rho=argrho;
	calculateFunction.value(azimuth, rho, elevation);
	}
	polar3D {arg argazimuth, argrho, argelevation;
	azimuth=argazimuth;
	rho=argrho;
	elevation=argelevation;
	calculateFunction.value(azimuth, rho, elevation);
	}
	*/
	azimuth_ {arg value;
		azimuth=value;
		calculateFunction.value(azimuth, rho, elevation)
	}
	rho_ {arg value;
		rho=value;
		calculateFunction.value(azimuth, rho, elevation)
	}
	elevation_ {arg value;
		elevation=value;
		calculateFunction.value(azimuth, rho, elevation)
	}

	xyToPolar {arg x,y;
		var p=Point(x*2-1, 1-(y*2)).rotate(0.5pi);
		var theta=p.theta/pi;
		azimuth=theta;
		if (guiType=='presets', {
			azimuth=azimuth.linlin(-1.0, 1.0, 0.0
				, guis[\azimuth].controlSpec.maxval
			);
		});
		this.rho_(p.rho);
	}

	polarToXY {
		var x,y, point;
		point=Polar(rho, azimuth*pi * 1.neg + 0.5pi).asPoint;
		guis[\twoD].setXY(point.x*0.5+0.5, point.y*0.5+0.5);
	}

	calculate {arg flag=true;
		var index=0;
		#array, means=controlSpecs.size.collect{|index|
			var array, means;
			#array, means=(dimensions-1).max(1).collect{|d|
				var array=();
				var means=();

				controlSpecs[index].keysValuesDo{|key,cs|
					array[key]=[];
				};
				keysNoCS[index].do{|key|
					array[key]=[];
				};
				indices[d].do{|i|
					var x, value;
					x=presets[index][i];//entries[i].load;
					if (x!=nil, {
						controlSpecs[index].keysValuesDo{|key,cs|
							var value;
							//cs=presets.views[key].controlSpec;
							value=x[key]??{cs.minval};
							value=cs.unmap(value);
							array[key]=array[key].add(value);
						};
						keysNoCS[index].do{|key|
							var value;
							value=x[key]??{0};
							array[key]=array[key].add(value);
						};
					});
				};
				controlSpecs[index].keysValuesDo{|key,cs|
					means[key]=array[key].mean
				};

				keysNoCS[index].do{|key|
					means[key]=array[key].mean
				};

				array=array.collect{|ar|
					//|ar, key|
					//var times={1.0}!(ar.size);
					//en deze times kunnen dus ook anders!
					Env(ar.add(ar[0]), {1.0}!(ar.size)
						//, curve[key]
					)
				};
				[array, means]
			}.flop;

			if (flag, {
				//preset.masters.do{|ez| ez.calculate(false, dimensions)};
				//preset.slaves.do{|ez| ez.calculate(false, dimensions)};
			});

			if (dimensions==3, {
				if (indices[1].size==0, {
					means[0]=array[1].deepCopy.collect{|v| v[0]};
					dimensions=2;
				})
			});
			[array, means]
		}.flop;
		calculateFunction=switch(dimensions, 1, {
			var sizes=size[0], func={}, offsets=offset[0];
			if (method=='clipAt', {sizes=size[0]-1});

			func={arg az=0.0;
				if (guiType=='azimuth', {
					az=az.linlin(-1.0, 1.0, 0.0, sizes);
				});
				//------------------------------------------------------ NEW start
				{preset.index_((az+offsets).round(1.0).asInteger, false)}.defer;
				//------------------------------------------------------ NEW end
				if (guiFlag, {
					{preset.guis[\presetList].value_(az+offsets)}.defer;
				});
				controlSpecs.size.do{|index|
					controlSpecs[index].keysValuesDo{|key, cs|
						var value=cs.map(
							//array[index][0][key].blendAt(az, method)
							array[index][0][key].at(az)
						);
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						});
					};
					keysNoCS[index].do{|key|
						var value=
						//array[index][0][key].blendAt(az, method)
						array[index][0][key].at(az)
						;
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						})
					};
					/*
					keysNoMorph[index].do{|key|
					{views[index][key]}.defer;
					}
					*/
				}
			};
			/*
			if (guiType=='azimuth', {
			func={arg azimith=0.0;
			func.value(azimuth.linlin(-1.0, 1.0, 0.0, sizes))
			}
			});
			*/
			func;
		}, 2, {
			var sizes=size[0], offsets=offset[0], az, value, func={};
			if (method=='clipAt', {sizes=size[0]-1});

			func={arg az=0.0, rho=1.0;
				var tmpPresetNr, flag=false;

				if (guiType=='azimuth', {
					az=az.linlin(-1.0, 1.0, 0.0, sizes);
				});
				tmpPresetNr=indices.flat.blendAt(az);
				flag=(presetNr.floor!=tmpPresetNr.floor);
				presetNr=tmpPresetNr;
				if (flag, {
					functions[\script].value(presetNr)
				});
				//------------------------------------------------------ NEW start
				{preset.index_((az+offsets).round(1.0).asInteger, false)}.defer;
				//------------------------------------------------------ NEW end
				if (guiFlag, {
					{preset.guis[\presetList].value_(az+offsets)}.defer;
				});
				controlSpecs.size.do{|index|
					controlSpecs[index].keysValuesDo{|key, cs|
						value=cs.map(
							[
								means[index][0][key],
								//array[index][0][key].blendAt(az, method)
								array[index][0][key].at(az)
							].blendAt(rho)
						);
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						});
					};
					keysNoCS[index].do{|key|
						value=
						[
							means[index][0][key],
							//array[index][0][key].blendAt(az, method)
							array[index][0][key].at(az)
						].blendAt(rho);
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						});
					};
					/*
					keysNoMorph[index].do{|key|
					{views[index][key]}.defer;
					}
					*/
				};
			};
			/*
			if (guiType=='azimuth', {
			func={arg azimith=0.0;
			func.value(azimuth.linlin(-1.0, 1.0, 0.0, sizes))
			}
			});
			*/
			func;
		}, 3, {
			var sizes=size, cs, value;
			if (method=='clipAt', {sizes=size-1});
			{arg azimuth=0.0, rho=1.0, elevation=0.0;
				var az0, az1;
				az0=azimuth.linlin(-1.0, 1.0, 0.0, sizes[0]);
				az1=azimuth.linlin(-1.0, 1.0, 0.0, sizes[1]);
				//az=az.linlin(-1.0, 1.0, 0.0, sizes);
				//views.keysValuesDo{|key, cs|
				controlSpecs.size.do{|index|
					controlSpecs[index].keysValuesDo{|key, cs|
						value=cs.map(
							[
								[means[index][0][key],
									//array[index][0][key].blendAt(az0, method)
									array[index][0][key].at(az0)
								].blendAt(rho),
								[means[index][1][key],
									//array[index][1][key].blendAt(az1, method)
									array[index][1][key].at(az1)
								].blendAt(rho)
							].blendAt(elevation)
						);
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						});
					};
					keysNoCS.do{|key|
						value=
						[
							[means[index][0][key],
								//array[index][0][key].blendAt(az0, method)
								array[index][0][key].at(az0)
							].blendAt(rho),
							[means[index][1][key],
								//array[index][1][key].blendAt(az1, method)
								array[index][1][key].at(az1)
							].blendAt(rho)
						].blendAt(elevation);
						actions[index][key].value(value);
						if (guiFlag, {
							{views[index][key].value_(value)}.defer;
						})
					};
				}
			}
		});
	}

	start {
		isMorphing=true;
		guis[\azimuth].action={|ez| this.azimuth_(ez.value)};
	}
	stop {
		isMorphing=false;
		guis[\azimuth].action=nil
	}

	gui {arg argparent, argbounds;
		var boundsSlider, boundsKnob, compositeView;
		hasGUI=true;
		guiFlag=true;
		parent=argparent;
		bounds=argbounds??{350@20};
		boundsSlider=(bounds.x-bounds.y-4)@bounds.y;
		boundsKnob=bounds.y@bounds.y;
		if (parent==nil, {
			parent=Window("preset morpher", Rect(0,0,bounds.x, bounds.x+(4*bounds.y))).front;
			parent.addFlowLayout; parent.alwaysOnTop_(true)
		},{
			parent=CompositeView(parent, (bounds.x+8)@(
				if (dimensions<=1, {bounds.y+8}, {bounds.x+8})
			));
			parent.addFlowLayout(4@4, 0@0);
			parent.background_(Color.grey);
		});
		guis=();
		if (guiType==\azimuth, {
			guis[\azimuth]=EZSlider(parent, boundsSlider, \azimuth, ControlSpec(-1.0, 1.0)
				, {|ez|
					this.azimuth_(ez.value);
					if (guiFlag, {
						if (guis[\twoD].class==Slider2D, {
							this.polarToXY})
					});
			}, -1.0);
		},{
			guis[\azimuth]=EZSlider(parent, boundsSlider, \preset
				, ControlSpec(0.0, size[0]-(method=='clipAt').binaryValue), {|ez|
					this.azimuth_(ez.value)
			}, 0.0);
		});
		if (dimensions>1, {
			guis[\rho]=EZSlider(parent, boundsSlider, \rho, ControlSpec(0.0, 1.0), {|ez|
				this.rho_(ez.value);
				if (guiFlag, {this.polarToXY});
			}, 1.0);
			guis[\elevation]=EZSlider(parent, boundsSlider, \elevation, ControlSpec(0.0, 1.0)
				, {|ez|
					this.elevation_(ez.value)
			}, 0.0);
			guis[\twoD]=Slider2D(parent
				, (bounds.x-bounds.y-12)@(bounds.x-bounds.y-12))
			.action_{|sl|
				this.xyToPolar(sl.x, sl.y);
				/*
				var p=Point(sl.x*2-1, 1-(sl.y*2)).rotate(0.5pi);
				var theta=p.theta/pi;
				azimuth=theta;
				if (guiType=='presets', {
				azimuth=azimuth.linlin(-1.0, 1.0, 0.0,guis[\azimuth].controlSpec.maxval);
				});
				this.rho_(p.rho);
				*/
				if (guiFlag, {
					{
						guis[\azimuth].value_(azimuth);
						guis[\rho].value_(rho);
					}.defer;
				})
			};
			guis[\elevationV]=Slider(parent, (bounds.y)@(bounds.x-bounds.y-12)).action_{|sl|
				this.elevation_(sl.value);
				if (guiFlag, {
					{guis[\elevation].value_(sl.value)}.defer;
				})
			};
		});
		guis[\guiFlag]=Button(parent, boundsKnob)
		.states_([ [\gui],[\gui, Color.black, Color.green]]).action_{|b|
			guiFlag=(b.value==1);
		}.value_(preset.guiFlag.binaryValue);
		/*
		guis[\update]=Button(parent, (bounds.x-50)@bounds.y).states_([ ["update"] ]).action_{this.update};
		*/
		parent.rebounds;
	}

	makeGUI {arg argparent, argbounds;
		^this.gui(argparent, argbounds;)
	}
}