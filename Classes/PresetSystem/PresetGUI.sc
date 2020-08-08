/*
TODO:
- gui button moet kleiner, en passen op één regel (houd hier dus rekening mee!)
- verbeter guiStore en guiRestore (nu veeeeel teveel if's)
- maak een PresetSystemGUI class en roep deze aan met .gui (of .makeGUI)
- overrule tijdelijk canFocus_(false) naar true als je dubbelklikt op filename interface én time
- zet alle gui-dingen in een defer (zijn het minst belangrijk), zoals functions[\index]
- verschillende soorten gui's
ook met een popupmenu ipv ListView, scheelt weer ruimte
guiType 0: basic
guiType 1: interpolate
guiType 2: morph
guiType 3: interpolate + script
- maak ook een GUI zonder ListView maar alleen maar met een EZNumber met presetnummer
- houd rekening met preLoad (load of restore)
*/
+ PresetSystem {
	initgui {
		var wins;//, parent;
		wins=views.collect{|v|
			if (v.isKindOf(EZGui), {
				parent=v.view.parent;
				if (parent.class==CompositeView, {
					parent
				},{
					parent.findWindow
				})
			},{
				parent=v.parent;
				if (parent.class==CompositeView, {
					parent
				},{
					v.parent.findWindow
				})
			})
		};
		parent=wins[views.keys.asArray.sort[0]];
		windows=();
	}

	//al die if statements kunnen eruit, wordt al door guiType bepaald
	guiFunctions {arg guiType=1;
		functions[\restore]=functions[\restore].addFunc({
			if (newValues[timeKey]!=nil, {
				{guis[timeKey].value_(newValues[timeKey])}.defer;
			});
			if (newValues[curveKey]!=nil, {
				{guis[curveKey].value_(
					interpolationCurves.indexOfEqual(newValues[curveKey])
				)}.defer;
			});
		});
		functions[\delete]=functions[\delete].addFunc({|ps|
			if (guis[\fileName]!=nil, {
				{guis[\fileName].stringColor_(Color.grey)}.defer
			})
		});
		functions[\store]=functions[\store].addFunc({|ps|
			if (guis[\fileName]!=nil, {
				{guis[\fileName].stringColor_(Color.black)}.defer
			})
		});
		functions[\removeSubFolder]=functions[\removeSubFolder].addFunc({|ps|
			if (guis[\fileName]!=nil, {
				{guis[\fileName].stringColor_(Color.grey)}.defer
			})
		});
		functions[\addSubFolder]=functions[\addSubFolder].addFunc({|ps|
			if (guis[\fileName]!=nil, {
				{guis[\fileName].stringColor_(Color.black)}.defer
			})
		});



		functions[\update]=functions[\update].addFunc({|ps|

			if (guis[\presetList]!=nil, {
				{
					this.removePresetNumbers;
					//guis[\presetList].items_(fileNamesWithoutExtensions);
					guis[\presetList].items_(fileNamesWithoutNumbers);
					//fileNamesWithoutExtensionsWithoutNumbers
					guis[\presetList].value_(index);
					//if (type==\master, {gui[\name].string_(blabla)})
				}.defer
			});

			functions[\index].value;
		});
		functions[\index]=functions[\index].addFunc({|ps|

			if (guis[\presetList]!=nil, {

				if (guis[\presetList].value!=index, {{
					guis[\presetList].value_(index)
				}.defer
				})

			});

			/*
			if (windows[\scriptEditorGUI]!=nil, {
			guis[\ScriptEditor].string_
			});
			*/

		});
		functions[\interpolate]=functions[\interpolate].addFunc({|ps|
			if (guis[\interpolate]!=nil, {
				{
					guis[\interpolate].value_(interpolate)
				}.defer
			})
		});
		if (guiType>0, {
			if ((type==\master)||(type==\subfolder), {
				functions[\index]=functions[\index].addFunc({
					guis.name.string_(
						fileNameWithoutExtension.split($_).copyToEnd(1).join);
					slaves.do{|ps|
						{
							ps.guis[\fileName].string_(fileNameWithoutExtension);
							ps.guis[\fileName].stringColor_(
								if (File.exists(ps.fullPath), {Color.black},{Color.grey})
							)
						}.defer
					};
				})
			})
		});
	}

	guiStore {
		if (canScript, {
			this.storeI
		},{
			if (canInterpolate, {
				this.storeI
			},{
				this.store;
			})
		});
	}

	guiRestore {arg value;
		if (canScript, {
			this.restoreS(value)
		},{
			if (canInterpolate, {
				if (interpolate==1, {
					this.restoreI(value)
				},{
					this.restore(value)
				})
			},{
				this.restore(value);
			})
		});
	}

	guiMovePresets{arg source, target;
		this.move(source, target);
	}

	removePresetNumbers {
		fileNamesWithoutNumbers=fileNamesWithoutExtensions.deepCopy;
		fileNamesWithoutNumbers=fileNamesWithoutNumbers.collect{|name|
			name.split($_).copyToEnd(1).join($_)
		};
	}

	gui {arg argparent, bounds=350@28, guiType=2, includeSlaves=true, argguiflag=true;
		var listBounds=0@0, textBounds=4, knobBounds=0@0, cv, cvs, font
		, argbounds=bounds.copy;
		var numberOfKnobs;
		var extraX, masterFlag=((type==\master)||(type==\subfolder)).binaryValue;

		hasGUI=true;

		if (argparent==nil, {this.initgui});
		parent=argparent??{parent};
		parent=parent??{
			var w=Window.new(this.folderName).front;
			w.addFlowLayout; w.alwaysOnTop_(true); w};
		this.removePresetNumbers;

		//bounds.x=bounds.x-8;
		//bounds.y=bounds.y-8;


		//=================================== CALCULATE DIMENSIONS
		//interpolate eznumber=2 knobs, textview=3 knobs
		numberOfKnobs=4+(masterFlag*2);//-, s, r, gui, (i), (script)
		numberOfKnobs=numberOfKnobs+textBounds+(masterFlag*textBounds);
		numberOfKnobs=numberOfKnobs+(canInterpolate.binaryValue*4);// was *3
		numberOfKnobs=numberOfKnobs+(canScript.binaryValue*masterFlag);

		knobBounds=(bounds.x/numberOfKnobs-2).floor@bounds.y;
		extraX=argguiflag.not.binaryValue*knobBounds.x;
		font=Font("Helvetica", knobBounds.y*0.75);
		if (guiType==1, {bounds.y=bounds.y*5});

		cv=CompositeView(parent,
			(bounds.x)//+8
			@(bounds.y));
		//cv.addFlowLayout(4@4, 4@4);
		cv.addFlowLayout(0@0, 4@0);
		cv.background_(Color.grey(0.8));

		if ( ((type==\master)||(type==\subfolder)) && (guiType==1), {
			cvs=2.collect{|i|
				var c=CompositeView(cv, (bounds.x/2-8).floor@(bounds.y-8));
				c.addFlowLayout(0@0, 4@4);
				//c.background_(Color.grey(0.8));
				c
			};
		},{
			cvs=[cv, cv];//beetje lompe oplossing....
		});
		if (guiType==1, {
			listBounds=(bounds.x/2-8)@(bounds.y-8);
		});
		if (guiType==2, {
			listBounds=((bounds.x/3).floor-8)@(bounds.y-8);
		});
		guis=();
		if ((type==\master)||(type==\subfolder), {
			if (guiType==1, {
				guis[\presetList]=ListView(cvs[0], (listBounds.x+extraX)@listBounds.y)
				.items_(fileNamesWithoutNumbers)
				.action_{|list|
					if (list.selection.size==1, {
						//this.index_(list.value);//dubbelopperdepop als er ook een guiRestore volgt!
						this.guiRestore(list.value);
					});
				}.canFocus_(false).font_(font);

				guis[\presetList].selectionMode=\contiguous;
				guis[\presetList].mouseUpAction_({|v|
					var list, source, target;
					list=v.selection;
					if (list.size>1, {
						if (v.value==list.first, {list=list.reverse});
						source=list.first;
						target=list.last;
						this.guiMovePresets(source, target)
					});
				});
				guis[\storeAll]=Button(cvs[1], knobBounds).states_([ ["S"] ]).action_{
					this.storeAll;
				}.canFocus_(false).font_(font);
			});
			if (guiType==2, {
				guis[\presetList]=PopUpMenu(cvs[1]
					, (knobBounds.x*textBounds+extraX)@bounds.y)
				.items_(fileNamesWithoutNumbers)
				.action_{|list|
					//this.index_(list.value);
					this.guiRestore(list.value);
				}.canFocus_(false).font_(font);
			});
			if (guiType==0, {
				guis[\presetList]=EZNumber(cvs[1], 70@bounds.y, "nr:"
					, ControlSpec(0, fileNamesWithoutExtensions.size-1, 0, 1), {|ez|
						//this.index_(ez.value);
						this.guiRestore(ez.value);
				}, 0, false, 20).font_(font);
			});
			guis[\addBefore]=Button(cvs[1], knobBounds).states_([ ["±"] ]).action_{
				this.addBefore;
			}.canFocus_(false).font_(font);
			guis[\add]=Button(cvs[1], knobBounds).states_([ ["+"] ]).action_{
				this.add;
			}.canFocus_(false).font_(font);
		});
		guis[\delete]=Button(cvs[1], knobBounds).states_([ ["-"] ]).action_{
			this.delete;
		}.canFocus_(false).font_(font);
		guis[\store]=Button(cvs[1], knobBounds).states_([ ["s"] ]).action_{
			this.guiStore;
		}.canFocus_(false).font_(font);
		guis[\restore]=Button(cvs[1], knobBounds).states_([ ["r"] ]).action_{
			this.guiRestore;
			if (guis[\name]!=nil, {
				guis[\name].string_(
					fileNamesWithoutExtensions[index].split($_).copyToEnd(1).join);
			})
		}.canFocus_(false).font_(font);
		/*
		guis[\current]=Button(cvs[1], knobBounds).states_([ ["c"] ]).action_{
		this.restoreCurrent
		}.canFocus_(false).font_(font);
		*/
		if (canInterpolate, {
			guis[\interpolate]=Button(cvs[1], knobBounds)
			.states_([ ["I"],["I",Color.black, Color.green] ]).value_(interpolate)
			.action_{|b|
				this.interpolate_(b.value);
				slaves.do{|ps|
					if (ps.guis[\interpolate]!=nil, {
						{ps.guis[\interpolate].value_(b.value)}.defer
					});
				};
			}.canFocus_(false).font_(font);
			guis[timeKey]=NumberBox(cvs[1], (knobBounds.x*2)@knobBounds.y)
			.canFocus_(false).action_{|b|
				this.time_(b.value.abs);
				if (b.value< -0.01, {b.value_(0)});
			}.value_(time).font_(font);

			interpolationCurves=(-10..10)++[\sin];

			guis[curveKey]=PopUpMenu(cvs[1], (knobBounds.x*2)@knobBounds.y)
			.items_(interpolationCurves)
			.canFocus_(false).font_(font).action_{|p|
				this.interpolationCurve_(interpolationCurves[p.value]);
			}.value_(interpolationCurves.indexOfEqual(interpolationCurve));
			guis[timeKey].decimals=1;
			guis[timeKey].clipLo=0.0;
			guis[\interpolationTime].mouseDownAction={|b|
				b.canFocus_(true)
			};
			guis[\interpolationTime].keyDownAction={arg a,b,c,d,e;
				if (e==36, {a.canFocus_(false)});
			};
		});

		if (canScript, {
			guis[\addScript]=Button(cvs[1], (knobBounds.x)@knobBounds.y)
			.states_([ ["script"],["script",Color.black,Color.yellow ]]).action_{|b|
				if (b.value==1, {
					//this.addScript
					this.scriptEditorGUI
				},{
					if (guis[\ScriptEditorWindow]!=nil, {
						guis[\ScriptEditorWindow].close;
						guis[\ScriptEditorWindow]=nil;
					})
				});
			}.canFocus_(false).font_(Font(Font.defaultSerifFace, knobBounds.x/3));
		});

		if (guiType>0, {
			if (type==\slave, {

				guis[\fileName]=StaticText(cvs[1]
					, cvs[1].decorator.bounds.width-cvs[1].decorator.left
					//(knobBounds.x*textBounds+extraX)
					@knobBounds.y)
				.string_(fileNamesWithoutNumbers[index])
				.stringColor_(Color.black).font_(font);
			},{
				if (guiType==1, {
					guis[\prev]=Button(cvs[1], (2*knobBounds.x)@knobBounds.y)
					.states_([ ["prev"] ]).action_{this.prev}.canFocus_(false).font_(font);
					guis[\next]=Button(cvs[1], (2*knobBounds.x)@knobBounds.y)
					.states_([ ["next"] ]).action_{this.next}.canFocus_(false).font_(font);
				});
				guis[\name]=TextField(cvs[1]
					,
					if (guiType==1, {
						(cvs[1].bounds.width-(cvs[1].decorator.margin.x))@knobBounds.y
					},{
						(knobBounds.x*textBounds)@knobBounds.y
					})
				)
				.action_{|t|
					if (fileNameWithoutExtension.split($_).copyToEnd(1).join!=t.string, {
						this.renumber(t.string);
						t.enabled_(false);
						t.canFocus_(false);
						t.enabled_(true);
					})
				}.string_(fileNameWithoutExtension.split($_).copyToEnd(1).join)
				.canFocus_(false).font_(font);
				guis[\name].mouseDownAction={arg b;
					b.enabled_(true);
					b.canFocus_(true);
				};
			});
		});

		if (argguiflag, {
			guis[\guiFlag]=Button(cvs[1],
				//(cvs[1].decorator.bounds.width-cvs[1].decorator.maxRight-8).abs
				knobBounds.x
				@knobBounds.y)
			.states_([ ["gui"],["gui",Color.black,Color.green] ]).action_{|b|
				guiFlag=(b.value==1);
				slaves.do{|p|
					p.guiFlag=(b.value==1);
					{p.guis[\guiFlag].value_(b.value)}.defer;
				};
			}.canFocus_(false).value_(this.guiFlag.binaryValue)
			.font_(Font(Font.defaultSerifFace, knobBounds.y/2));
		});

		this.guiFunctions(guiType);

		if (includeSlaves, {
			slaves.do{|ps|
				if (ps.hasGUI.not, {
					ps.gui(nil, argbounds, 2)
				},{

				})
			};
		});
	}

	makeGUI {arg argparent, bounds=350@20, guiType=2, includeSlaves=true, argguiflag=true;
		^this.gui(argparent, bounds, guiType, includeSlaves, argguiflag)
	}

}