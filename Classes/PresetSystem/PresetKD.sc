PresetKD {
	var <presetsystem, <presets, <presetsWithIndex, <kdtree, <>n;
	var <>gui, <labels;

	*new {arg presetsystem;
		^super.new.init(presetsystem)
	}

	init {arg argpresetsystem;
		presetsystem=argpresetsystem;
		this.makeKDTree;
		n=1;
	}

	preparePresets {
		var keys=presetsystem.controlSpecs.keys.asArray.sort;
		presets=presetsystem.presets.size.collect{|index|
			keys.collect{|key|
				var cs=presetsystem.controlSpecs[key];
				var value;
				value=presetsystem.presets[index][key]??cs.minval;
				cs.unmap(value)
			}.flat;
		};
		presetsWithIndex=presets.collect{arg preset, index;
			preset++index
		};
	}

	makeKDTree {
		this.preparePresets;
		kdtree=KDTree(presetsWithIndex, lastIsLabel:true);//
	}


	nearestToPreset {arg index= -1, k=1;
		//kNearest { |point, k, nearestSoFar, bestDist=inf, incExact=true|
		var preset, out;
		n=k;
		index=index??{presetsystem.index};
		preset=if (index<0, {
			presetsystem.normalizePreset(-1)
		},{
			presets[index]
		});
		labels=if (k>1, {
			out=kdtree.kNearestSort(preset, k);
			out.collect(_.label);
		},{
			out=kdtree.nearest(preset)[0].label.asArray//, incExact:false
		});
		^labels
	}

	makeGUI {arg parent, bounds=350@20;
		gui=PresetKDGUI.new(this, parent, bounds)
	}
}


PresetKDGUI : GUIJT {
	var names, indices;
	*new {arg presetKD, parent, bounds=350@20;
		^super.new.init(presetKD, parent, bounds);
	}

	init {arg presetKD, argparent, argbounds;
		classJT=presetKD;
		parent=argparent;
		bounds=argbounds;
		this.initAll;//parentMargin, parentGap, windowMargin, windowGap;
		views[\update]=Button(parent, bounds.x*0.1@bounds.y).states_([[\update]]).action_{
			classJT.makeKDTree;
			views[\k].controlSpec=ControlSpec(1, classJT.presetsystem.presets.size, 0, 1);
		};
		views[\nearestC]=Button(parent, bounds.x*0.2@bounds.y).states_([[\nearestC]]).action_{
			classJT.nearestToPreset(-1, classJT.n);
			views[\presets].items_( classJT.labels.collect{|i|
				classJT.presetsystem.fileNamesWithoutExtensions[i].split($_).copyToEnd(1).join
			});
			views[\presets].valueAction_(0);
		};
		views[\nearestP]=Button(parent, bounds.x*0.2@bounds.y).states_([[\nearestP]]).action_{
			classJT.nearestToPreset(classJT.presetsystem.index, classJT.n);

			views[\presets].items_( classJT.labels.collect{|i|
				classJT.presetsystem.fileNamesWithoutExtensions[i].split($_).copyToEnd(1).join
			});
			views[\presets].valueAction_(0);
		};
		views[\k]=EZNumber(parent, (bounds.x*0.1)@bounds.y, nil
			, ControlSpec(1, classJT.presetsystem.presets.size, 0, 1), {|ez|
				classJT.n=ez.value}, classJT.n, false, 0);
		views[\presets]=PopUpMenu(parent, (bounds.x-parent.decorator.left)@bounds.y)
		.items_([0]).action_{|pop|
			classJT.presetsystem.restore(classJT.labels[pop.value]);
			//classJT.presetsystem.index_(classJT.labels[pop.value]);
			//classJT.presetsystem.doRestore
		};
		parent.decorator.nextLine;
	}

}
