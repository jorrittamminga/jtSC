/*
- ERROR bij store of add presetsblender
maak het toch maar een subclass van PresetsCollectionJT Node, kan dit???
*/
PresetsBlenderJT
{
	var <funcs, <controlSpec, <>value=0, <>depth=1.0;
	var <presets, preset, <array, <method, <blendType, <blendFunc;
	var <saveInPresetFlag, presetsCollectionFlag;
	var <gui, <>objects;

	classvar <addActions;

	*initClass {
		addActions = (
			blendType: (0: \normal, 1: \depth, normal: \normal, depth: \depth )
			, method: (0: \clipAt, 1: \wrapAt, clipAt: \clipAt, wrapAt: \wrapAt)
		);
	}
	*new {arg presetsCollection, method='clipAt', blendType='normal', saveInPresetFlag=true;
		^super.new.init(presetsCollection, method, blendType, saveInPresetFlag)
	}
	init {arg argpresetsCollection, argmethod, argblendType, argsaveInPresetFlag;
		objects=();
		if (argpresetsCollection.class!=PresetsCollectionJT, {
			argpresetsCollection=PresetsCollectionJT(argpresetsCollection);
			argpresetsCollection.makePresetArray;
			presetsCollectionFlag=true;
		},{
			presetsCollectionFlag=false
		});
		presets=argpresetsCollection;
		saveInPresetFlag=argsaveInPresetFlag;
		if (presets.value!=nil, {
			argmethod=presets.value[\method]??{argmethod};
			argblendType=presets.value[\blendType]??{argblendType};
		},{
			argmethod
		});
		//argmethod=presets.value[\method]??{argmethod};
		//argblendType=presets.value[\blendType]??{argblendType};
		argmethod=addActions[\method][argmethod];
		argblendType=addActions[\blendType][argblendType];
		this.prInit(argmethod, argblendType);
		funcs=();
		presets.funcs[\store]=presets.funcs[\store].addFunc({arg i;
			this.prInit;
		});
		presets.funcs[\restore]=presets.funcs[\restore].addFunc({arg i;
			this.prInit;
		});
		presets.presetsJT.funcs[\store]=presets.presetsJT.funcs[\store].addFunc({arg i;
			this.prInit;
		});
	}
	method_{arg methode; method=methode; this.prInit(method,blendType) }
	blendType_{arg blendtype; blendType=blendtype; this.prInit(method,blendType) }
	saveInPresetFlag_ {arg flag=true;
		saveInPresetFlag=flag;
		if (flag, {

		},{

		});
	}
	prInit {arg methode, blendTyp, specs, actions;
		var presetColTmp;
		method=methode??{method};
		blendType=blendTyp??{blendType};
		if (presets.presetsCollection.rank<2, {
			controlSpec=controlSpec??{[0,1].asSpec};
			value=value??{0};
			if (controlSpec.size>0, {controlSpec=controlSpec[0]});
			value=controlSpec.unmap(value.asArray)[0];
			controlSpec=ControlSpec(0, presets.presetsCollection.size-('clipAt': 1, 'wrapAt':0)[method]).warp;
			value=controlSpec.map(value);
		},{
			controlSpec=controlSpec??{[0,1].asSpec};
			value=value??{0};

			value=if (controlSpec.size>0, {
				controlSpec.collect{|cs,i| cs.map(value.asArray.wrapAt(i))};
			},{
				controlSpec.unmap(value.asArray);
			});
			//value=value.reshapeLike(presets.presetsCollection);
			controlSpec=presets.presetsCollection.shape.collect{|i|
				ControlSpec(0, i-('clipAt': 1, 'wrapAt':0)[method]).warp;
			};
			value=value??{Array.fill(controlSpec.size, {0})};
			value=value.asArray.lace(controlSpec.size);
			value=controlSpec.collect{|cs,i| cs.map(value[i])};
		});
		if (presets.presetsCollection.includesEqual(nil), {
			array=[];
		},{
			array=EventsArrayJT.fill(presets.presetsCollection, presets.presetsJT.object, method, specs, actions);//, method
		});
		//blendFunc={arg index; array.blendAtIndex(index)}
		blendFunc=if (presets.presetsCollection.rank<2, {
			if (presets.presetsCollection.size==1, {
				{}
			},{
				if (blendType=='depth', {
					//preset=array.blendAtIndexDepth(blendIndex, blendDepth, \doMapValue);
					{arg index, d; value=index; depth=d; array.blendAtIndexDepth(index, depth) }
				},{
					//preset=array.blendAtIndex(blendIndex, \doMapValue);
					{arg index; value=index;  array.blendAtIndex(index)}
				})
			})
		},{
			if (blendType=='depth', {
				//preset=array.blendAtIndicesDepth(blendIndex, blendDepth, \doMapValue);
				{arg index, d;
					value=index;
					depth=d;
					array.blendAtIndicesDepth(index, depth)
				}
			},{
				//preset=array.blendAtIndices(blendIndex, blendDepth, \doMapValue);
				{arg index; value=index; array.blendAtIndices(index)}
			})
		});
		blendFunc.value(value, depth);
		if (gui!=nil, {
			{gui.makeBlender}.defer
		});
	}
	makeGui {arg parent, bounds=350@20;
		//gui=1.0;
		if (gui==nil, {
			gui=1.0;
			{
				if (presetsCollectionFlag, {presets.makeGui(parent, bounds)});
				gui=PresetsBlenderGUIJT(this, parent, bounds);
				//if (cueJT!=nil, {cueJT.makeGui(gui.parent)});
			}.defer;
		})
	}
}
PresetsBlenderGUIJT {
	var <presetsBlender, <parent, <bounds, <views, <cv, <font, rank;
	var index=0, depth=1;

	*new {arg presetsBlender, parent, bounds=350@20;
		^super.new.init(presetsBlender, parent, bounds)
	}
	makeBlender {
		var boundz, val, cs;
		boundz=bounds.x@(bounds.y/(presetsBlender.controlSpec.size.max(1)));
		font=Font("Monaco", bounds.y/3*0.75);
		rank=presetsBlender.presets.presetsCollection.rank;
		cv.removeAll;
		cv.decorator.reset;

		val=presetsBlender.value??{0};
		cs=presetsBlender.controlSpec;

		presetsBlender.presets.object[\value]=if (rank<2, {
			EZMultiSlider(cv, bounds, \in, [0.0, 1.0], {|ez|
				presetsBlender.blendFunc.value(cs.map(ez.value.asArray[0]), depth)
			}
			, cs.unmap(val.asArray)
			, false, cv.bounds.width*0.05).decimals_(8).font_(font);
		},{
			EZMultiSlider(cv, bounds, \in, [0.0, 1.0], {|ez|
				presetsBlender.blendFunc.value(ez.value.asArray.collect{|val,i| cs[i].map(val)}, depth)
			}
			, val.asArray.collect{|v,i| cs[i].unmap(v)}
			, false, cv.bounds.width*0.05).decimals_(8).font_(font);
		});
		presetsBlender.presets.object[\value].sliderView.indexIsHorizontal = false;
		presetsBlender.presets.object[\value].sliderView.isFilled=true;
		views[\blender]=presetsBlender.presets.object[\value];
		presetsBlender.objects[\blender]=views[\blender];
		if (presetsBlender.blendType=='depth', {
			views[\depth]=EZSlider(cv, bounds.x@(bounds.y/3), \depth, ControlSpec(0, 1.0), {|ez|
				depth=ez.value;
				presetsBlender.blendFunc.value(presetsBlender.value, ez.value)
			}, 1.0).font_(font);
			presetsBlender.objects[\depth]=views[\depth];
		});
	}
	init {arg argpresetsBlender, argparent, argbounds;
		var c;
		presetsBlender=argpresetsBlender;

		parent=argparent;
		views=();
		c=CompositeView(parent, argbounds); c.addFlowLayout(0@0,0@0);
		font=Font("Monaco", argbounds.y*0.75);
		presetsBlender.presets.object[\method]=PopUpMenu(c, (argbounds.x*0.5)@argbounds.y)
		.items_([\clipAt, \wrapAt]).action_{arg i;
			presetsBlender.method_([\clipAt, \wrapAt][i.value]);
		}.value_(
			if (presetsBlender.presets.value==nil, {0},{
				presetsBlender.presets.value[\method]??{0}
			})
		).font_(font);
		presetsBlender.presets.object[\blendType]=PopUpMenu(c, (argbounds.x*0.5)@argbounds.y)
		.items_([\normal, \depth]).action_{|i|
			presetsBlender.blendType_([\normal, \depth][i.value]);
		}
		.value_(
			if (presetsBlender.presets.value==nil, {0},{
				presetsBlender.presets.value[\blendType]??{0}
			})
		).font_(font);
		cv=CompositeView(parent, argbounds.x@(4*argbounds.y)); cv.addFlowLayout(0@0, 0@0);
		bounds=argbounds.x@(argbounds.y*3);
		presetsBlender.objects[\presets]=presetsBlender.presets.gui.views[\presets];
		this.makeBlender
	}
}