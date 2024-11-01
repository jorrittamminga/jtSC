ClusterJTGUI : GUIJT {
	var clusterJT, c, butBounds, bufferPath;
	var tmpFuncs, tmpViews;

	*new {arg clusterJT, parent, bounds, margin=4@4, gap=4@4;
		^super.new.init(clusterJT, parent, bounds, margin, gap);
	}

	init {arg argclusterJT, argparent, argbounds, argmargin, arggap;
		var keys, tmpBounds;

		classJT=argclusterJT; clusterJT=classJT;
		parent=argparent;
		bounds=argbounds;
		margin=argmargin;
		gap=arggap;

		c=();
		tmpFuncs=clusterJT.funcsEvent.copy;
		tmpBounds=bounds.copy;
		bounds.x=bounds.x*2+gap.x+(2*margin.x);

		this.initAll;

		bufferPath=clusterJT.argsEvent[\buffer].path;

		bounds=tmpBounds.copy;
		butBounds=(bounds.x/4-gap.x).floor@bounds.y;
		//--------------------------------------------- MASTER BUTTONS
		c[\master]=CompositeView(parent, (parent.bounds.width-(2*margin.x))
			@(2*margin.y+bounds.y));
		c[\master].addFlowLayout(margin, gap);
		views[\play]=Button(c[\master], butBounds).states_([ [\play] ]).action_{
			clusterJT.play(views[\renderNew].value==1, normalize: clusterJT.normalize)
		};
		views[\renderNew]=Button(c[\master], butBounds).states_([ [\newScore]
			, [\newScore, Color.black, Color.green]]).value_(1);
		views[\normalize]=Button(c[\master], butBounds).states_([ [\normalize]
			, [\normalize, Color.black, Color.green]])
		.action_{|b| clusterJT.normalize=(b.value==1)}.valueAction_(
			//clusterJT.normalize.binaryValue
			1
		);
		views[\type]=PopUpMenu(c[\master], butBounds).items_([\render,\realtime]).action_{|p|
			clusterJT.type_(p.items[p.value].asSymbol)
		};
		views[\read]=Button(c[\master], butBounds).states_([[\read]]).action_{
			Dialog.openPanel({|p|
				clusterJT.read(p);
				bufferPath=p.dirname;
			}, path: bufferPath)
		};
		//--------------------------------------------- ARGS
		tmpViews=();
		c[\sliders]=CompositeView(parent, bounds.x@
			(bounds.y+gap.y*clusterJT.controlSpecs.size+(2*margin.y)));
		c[\sliders].addFlowLayout(margin, gap);
		clusterJT.controlSpecs.sortedKeysValuesDo{|key, cs|
			var d=CompositeView(c[\sliders], bounds);
			d.addFlowLayout(0@0, 0@0);
			d.background_(Color.rand);
			if (clusterJT.argsEvent[key].class!=Function, {
				viewsPreset[key]=EZGuiJT(d, bounds, key, cs, {|ez|
					clusterJT.argsEvent[key]=ez.value
				}
				, clusterJT.argsEvent[key]??cs.default
				, false, 100, 60
				, equalLength: false);
			},{
				views[key]=StaticText(d, bounds).string_(key)
			});
		};
		tmpViews=viewsPreset.copy;
		//--------------------------------------------- FUNCS
		c[\funcs]=CompositeView(parent, bounds.x@
			(bounds.y+gap.y*clusterJT.controlSpecs.size+(2*margin.y)));
		c[\funcs].addFlowLayout(margin, gap);
		clusterJT.controlSpecs.sortedKeysValuesDo{|key, cs|
			var kkey=(\_++key++\func).asSymbol;
			viewsPreset[kkey]=
			TextField(c[\funcs], bounds).string_(
				clusterJT.funcsEvent[key].asCompileString
			).action_{arg t;
				var d, tmpValue=clusterJT.argsEvent[key];
				t=if (t.class==TextField, {t.string},{t});//kijk, dit is slim!
				if ((t.interpret==nil)||(t==""), {
					clusterJT.funcsEvent.removeAt(key);
					clusterJT.argsEvent[key]=clusterJT.argsEvent[key].asArray[0];
					tmpValue=clusterJT.argsEvent[key].asArray[0];
				},{
					clusterJT.funcsEvent[key]=t.interpret;
					clusterJT.argsEvent[key]=
					if (clusterJT.argsEvent[key].value.size==2, {
						clusterJT.argsEvent[key].value
					},{
						{clusterJT.argsEvent[key].asArray[0]}!2
					});
					tmpValue=clusterJT.argsEvent[key];
				});
				if (viewsPreset[key].value.size!=tmpValue.size, {
					d=viewsPreset[key].view.parent;
					viewsPreset[key].remove;//alleen als er verandering is
					d.decorator.reset;
					viewsPreset[key]=EZGuiJT(d, bounds, key, cs, {|ez|
						clusterJT.argsEvent[key]=ez.value
					}
					, tmpValue
					, false, 100, 60, equalLength: false);
				},{


				});
				//tmpFuncs=viewsPreset[(key++\func).asSymbol].string.interpret;
			};
		};
		window.onClose=window.onClose.addFunc({clusterJT.close});
		parent.rebounds;
		window.rebounds;
	}
}