CueJTGUI {
	var parent, bounds;
	var cMain, <>views, cListViews, numberOfColumns, c;
	var <>cueJT, <>methode;
	*new {arg cueSystem, parent, bounds;
		^super.new.init(cueSystem, parent, bounds)
	}
	*newAdd {arg cueSystem, parent, bounds;
		^super.new.initAdd(cueSystem, parent, bounds)
	}
	prMakeViews {}
	makeCompositeViewMain {
		var c=CompositeView(parent, bounds.x@(3*bounds.y));
		c.addFlowLayout(0@0, 0@0);
		c.background_(Color.green);
		^c
	}
	makeValue {
		if (views[\time]<=0.0, {
			if (views[\extra].string.interpret==nil, {
				if (views[\methode].value==0, {
					cueJT.value_(views[\value].value);
				},{
					views[\methode].value=0;
					cueJT.value_(views[\value].value);
				})
			},{
				cueJT.value
			})
		});
	}
	postInit {}
	init {arg cueSystem, argparent, argbounds;
		var boundsB;
		c=();
		views=();
		cueJT=cueSystem;
		bounds=argbounds??{350@240};
		boundsB=bounds.y;
		parent=argparent??{
			var w=Window("cues", Rect(400,400,400,400)).front.alwaysOnTop_(true);
			w.addFlowLayout(4@4, 0@0);
			w
		};
		c[\main]=this.makeCompositeViewMain;
		Button(c[\main], 20@20).states_([ [""],["X",Color.black,Color.green] ]).value_(1);//activate
		Button(c[\main], 20@20).states_([ [\s] ]).action_{
			//this.makeValue;
			cueJT.store;
		};
		Button(c[\main], 20@20).states_([ [\r] ]).action_{
			cueJT.restore;
		};
		views[\cueName]=StaticText(c[\main], 200@20).string_("").align_(\right).stringColor_(Color.red);
		Button(c[\main], boundsB).states_([ ["+"] ]).action_{cueJT.add};
		Button(c[\main], boundsB).states_([ ["-"] ]).action_{cueJT.delete};
		Button(c[\main], boundsB).states_([ ["o"] ]).action_{ cueJT.open };
		Button(c[\main], boundsB).states_([ ["c"] ]).action_{ cueJT.close };
		//-------------------------------------------------------------------------------- specific GUI for object=CueJTCollection
		this.postInit;
		//----------------------------------------------------------
		//
		//----------------------------------------------------------
		cueJT.func[\cueID]=cueJT.func[\cueID].addFunc({
			{views[\cueName].string_(
				PathName(cueJT.pathRelative).allFolders.collect{|folder| folder.split($_).copyToEnd(1).join($_)}.join($/)
			).stringColor_(Color.black)}.defer
		});
	}
	initAdd {arg cueSystem, argparent, argbounds;
		//c=();
		//views=();
		cueJT=cueSystem;
		bounds=argbounds??{350@240};
	}
}
CuePresetJTGUI : CueJTGUI {
	restoreView {arg x, restoreOnly=true;
		var action={};
		action=if (cueJT.value.class==Event, {
			if (restoreOnly, {
				cueJT.value[\performMsg][\restore, cueJT.value[\performMsg][1]];
			});
			{arg val; cueJT.value[\performMsg][1]=val}
		},{
			if (cueJT.value.size==0, {
				if (restoreOnly, {
					{arg val;
						cueJT.value=val;
					}
				},{
					cueJT.value=[methode, cueJT.value.deepCopy, cueJT.value[2]??{0} ];
					{arg val; cueJT.value[1]=val;}
				});
			},{
				if (restoreOnly, {
					cueJT.value=cueJT.value[1]??{0};
					{arg val; cueJT.value=val}
				},{
					//cueJT.value=[methode, cueJT.value.deepCopy, cueJT.value[2]??{0} ];
					{arg val; cueJT.value[1]=val;}
				});
			});
		});
		action=action.addFunc({arg val;
			cueJT.object.restore(val)
		});

		views[\value]=PopUpMenu(c[\methode], x@20).items_(
			cueJT.object.names
		).action_{arg p;
			action.value(p.value);
		}.value_(
			cueJT.object.index
		);
	}
	restoreIView {arg x;
		var action;
		action=
		if (cueJT.value.class==Event, {
			if (cueJT.value[\performMsg][2].class==Event, {
				//(amp:1.0, ditendat:10, etc)
			},{
				{arg val; cueJT.value[\performMsg][2]=val}
			})
		},{
			if (cueJT.value.size==0, {
				cueJT.value=[\restoreI, cueJT.value.deepCopy, 0];
			});
			{arg val; cueJT.value[2]=val}
		});
		this.restoreView(x-(3.5*bounds.y).ceil, false);
		Button(c[\methode], bounds.y@bounds.y).states_([ ["i"],["I",Color.black,Color.green] ]).value_(cueJT.transitionFlag.binaryValue).action_{|b|
			cueJT.transitionFlag_(b.value>0)
		};
		views[\time]=EZNumber(c[\methode], (2.5*bounds.y).floor@bounds.y, "", ControlSpec(0.0, 60.0, 2.0), {|ez|
			action.value(ez.value);
		}
		, this.getTransitionTime(cueJT.value)
		, false, 0, gap:0@0, margin:0@0).round2_(0.001);
	}
	getTransitionTime {arg val;
		^if (val.class==Event, {
			if (val[\performMsg][2].class==Event, {0},{
				val[\performMsg][2]
			})
		},{
			if (val.size==0, {
				//cueJT.value=[\restoreI, cueJT.value.deepCopy, 0.0];
				0.0;
			},{
				val[2]
			});
		});
	}
	postInit {
		var x=(bounds.x/4).floor, xRest=bounds.x-x;
		//-------------------------------------------------------------------------------- specific GUI for object=PresetJT
		c[\specific]=CompositeView(c[\main], bounds);
		c[\specific].addFlowLayout(0@0, 0@0);
		c[\specific].background_(Color.yellow);
		views[\methode]=PopUpMenu(c[\specific], x@bounds.y).items_(cueJT.classMethods[cueJT.object.class.asSymbol]).action_{arg popupmenu;
			var methodeString;
			cueJT.methode=popupmenu.items[popupmenu.value];
			if (cueJT.methode!=methode, {
				c[\methode].removeAll; c[\methode].decorator.reset;
				methodeString=cueJT.methode.asString;
				methodeString=(methodeString++\View).asSymbol;
				this.performMsg([methodeString, xRest]);
			});
			methode=cueJT.methode;
		};
		c[\methode]=CompositeView(c[\specific], (bounds.x-x)@bounds.y); c[\methode].addFlowLayout(0@0, 0@0); c[\methode].background_(Color.red);
		views[\methode].valueAction_(views[\methode].items.indexOfEqual(cueJT.methode));

		views[\extra]=TextField(c[\main], bounds).string_("");//args and extra

		cueJT.func[\methode]=cueJT.func[\methode].addFunc({arg meth;
			{views[\methode].valueAction_(views[\methode].items.indexOfEqual(meth))}.defer;
		});
		cueJT.func[\index]=cueJT.func[\index].addFunc({arg val;
			{views[\value].value_(val)}.defer;
		});
		cueJT.func[\value]=cueJT.func[\value].addFunc({arg val;
			var time=0;
			{if (views[\time]!=nil, {
				time=this.getTransitionTime(val);
				views[\time].value_(time)
			})}.defer;
		});
	}
}
CueJTCollectionGUI : CueJTGUI {
	var <cueJTs;
	postInit {
		cueJTs=cueJT;
		cueJT=cueJT.cue;
		views[\id]=PopUpMenu(c[\main], 100@20).items_( cueJTs.cues.collect{|i| i.object.class.asString} ).action_{|p|
			cueJT=cueJTs[p.value]
		}
	}
}