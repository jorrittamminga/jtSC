PresetKMeans {
	var <presetsystem, <presets, <>k, <kMeans, <keys, <centroids, <controlSpecs, <assignments;
	var <clusters;
	var <>gui;

	*new {arg presetsystem, k=3;
		^super.new.init(presetsystem, k)
	}

	init {arg argpresetsystem, argk=3;
		presetsystem=argpresetsystem;
		k=argk??{3};
		this.makeKMeans;
	}

	makeKMeans {
		this.preparePresets;
		this.fillKMeans;
	}

	preparePresets {
		keys=presetsystem.controlSpecs.keys.asArray.sort;
		controlSpecs=();
		keys.collect{|key| controlSpecs[key]=presetsystem.controlSpecs[key]};
		presets=presetsystem.presets.size.collect{|index|
			keys.collect{|key|
				var cs=controlSpecs[key];
				var value;
				value=presetsystem.presets[index][key]??cs.minval;
				cs.unmap(value)
			}.flat;
		};
	}

	fillKMeans {
		kMeans=KMeans.new(k);
		presets.do{|preset| kMeans.add(preset)};
		//kMeans.update;
		centroids=kMeans.centroids.collect{|centroid|
			var e=();
			centroid.do{|val,i| e[keys[i]]=controlSpecs[keys[i]].map(val)};
			e
		};
		clusters={[]}!k;
		assignments=kMeans.assignments;
		assignments.do{|cluster, i|
			clusters[cluster]=clusters[cluster].add(i);
		};
	}

	makeGUI {arg parent, bounds=350@20;
		gui=PresetKMeansGUI.new(this, parent, bounds)
	}
}


PresetKMeansGUI : GUIJT {
	var names, indices, <c, height;
	*new {arg presetKMeans, parent, bounds=350@20;
		^super.new.init(presetKMeans, parent, bounds);
	}

	refill {
		c.removeAll;
		c.decorator.reset;
		classJT.k.do{|i|
			ListView(c, ((bounds.x/classJT.k).floor)@(bounds.y-height-8)).items_(
				classJT.clusters[i].collect{|j|
					classJT.presetsystem.fileNamesWithoutExtensions[j]
					.split($_).copyToEnd(1).join
				}
			).action_{|l|
				classJT.presetsystem.restore(classJT.clusters[i][l.value]);
			}
		};
	}

	init {arg presetKMeans, argparent, argbounds;
		classJT=presetKMeans;
		parent=argparent;
		bounds=argbounds;
		height=bounds.y;
		bounds.y=bounds.y*6;
		this.initAll;//parentMargin, parentGap, windowMargin, windowGap;
		views[\update]=Button(parent, bounds.x*0.1@height).states_([[\update]]).action_{
			classJT.makeKMeans;
			this.refill
		};

		views[\k]=PopUpMenu(parent, (bounds.x*0.1)@height).items_(
			(1..classJT.presets.size)
		).value_(classJT.k-1).action_{|p|
			classJT.k=p.value+1;
			classJT.fillKMeans;
			views[\centroids].items_( (0..(classJT.k-1)) );
			this.refill;
		};
		views[\centroids]=PopUpMenu(parent, (bounds.x-parent.decorator.left)@height)
		.items_( (0..(classJT.k-1))   ).action_{|pop|
			classJT.centroids[pop.value].keysValuesDo{arg key, value;
				classJT.presetsystem.views[key].valueAction_(value)
			};
			//this.refill;
		};
		c=CompositeView(parent, bounds.x@(bounds.y-height-8));//bounds.y*4
		c.addFlowLayout(0@0, 0@0);
		c.background_(Color.yellow);
		this.refill;
		parent.decorator.nextLine;
	}
}