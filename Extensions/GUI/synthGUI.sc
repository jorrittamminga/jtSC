/*
zorg dat default window in het midden van het scherm komt te staan, maar dat je top e.d. wel kunt instellen
bij size>2 ? MultiSlider? Meerdere EZSlider opgestapeld met gap=0? etc
maak ook een stop/pauze/play knop
wat als er geen specs zijn? dan maak je default sliders met een soort default spec
*/

+Synth {

	makeGUI {arg parent, bounds=350@20, gap=4@4, margin=4@4, setColors=[Color.black, Color.white, nil, Color.black, Color.white, Color.white, Color.red(1.5)], canFocus=false
		, onClose=false
		, canInterpolate=false, willHang=false, unitWidth=0, returnParent=false
		, showButtons=true, returnButtons=false
		, waitTime=0.0001
		;//colors, fontName="Helvetica", ... , labelWidth=60, numberWidth=45, unitWidth=0, labelHeight=20, layout=\horz, gap, margin, round, setColors, canFocus + (left, top)

		var font=Font("Helvetica", bounds.y*0.75), guis=(), func;
		var p, cs, s, noParent=false, c;
		var title=this.defName.asString ++ ": " ++ this.nodeID.asString;
		var width, height, screenBounds, onCloseF, buttons=();
		var compositeView;

		s=this.server;
		cs=this.specs;

		width=margin.x*2+bounds.x;
		height=(bounds.y+gap.y*(cs.size+2))+(margin.y*2);

		{
			p=this.getAll; s.sync;
			{
				screenBounds=Window.screenBounds;
				if (parent==nil, {
					noParent=true;
					//parent=Window(this.defName.asString, Rect(0,0,bounds.x+8, (bounds.y+4*cs.size)+8));
					parent=Window(title, Rect((screenBounds.width*0.5-(width*0.5)), ((screenBounds.height*0.5)-(0.5*height)), margin.x*2+width, height)
						, scroll: true).front;
					parent.addFlowLayout(margin, gap);
					parent.alwaysOnTop_(true);
				});

				if (onClose, {
					parent.onClose;
					onCloseF=parent.onClose.addFunc({this.free});
					parent.onClose=onCloseF;
					parent.onClose;
					//parent.onClose_{this.free};
				});

				compositeView=CompositeView(parent, width@20);
				compositeView.addFlowLayout(margin, gap);
				//compositeView.background_(Color.yellow);
				/*
				StaticText(parent, bounds).string_(title)
				.font_(Font("Helvetica", bounds.y*0.75, true))
				.stringColor_(Color.white).background_(Color.grey(0.2));
				*/

				if (showButtons, {
					buttons[\updateButton]=Button(compositeView, ((bounds.x*0.8).floor-(margin.x))@bounds.y).states_([[title, Color.white, Color.grey(0.2)]])
					.canFocus_(false).font_(Font("Helvetica", bounds.y*0.75, true))
					.action_{
						//{this.getAll(true).asKeyValuePairs.collect{|i,j| if (j.even, {i.asSymbol},{i})}}.fork
						{
							var all;
							all=this.getAll(true);
							server.sync;
							all.keysValuesDo{|key,val|
								guis[key].value=val;
							}
						}.fork(AppClock)
					}
					//.stringColor_(Color.white).background_(Color.grey(0.2))
					;
					//guis[\getAll]=
					/*
					Button(parent, ((bounds.x*0.5)-(gap.x+margin.x))@bounds.y).states_([["get all", Color.white, Color.grey(0.2)]])
					.canFocus_(false).font_(Font("Helvetica", bounds.y*0.75, true))
					.action_{
					{
					var all;
					all=this.getAll(true);
					server.sync;
					all.keysValuesDo{|key,val|
					guis[key].value=val;
					}
					}.fork(AppClock)
					}*/
					//.stringColor_(Color.white).background_(Color.grey(0.2))
					//				parent.view.decorator.nextLine;
					buttons[\playButton]=Button(compositeView
						, (bounds.x*0.2).floor@bounds.y)
					.states_([ [\on],[\on, Color.black, Color.green]]).action_{|b|
						if (p[\gate]!=nil, {
							if (b.value==1, {this.run(true)});
							this.set(\gate, b.value);
						},{
							this.run(b.value.asBoolean);
						});
					}.canFocus_(false).value_(1);
					if (returnButtons, {
						buttons.keysValuesDo{|key,val| guis[key]=val};
					});
				});

				cs.sortedKeysValuesDo{|name,cs|
					var slider=EZSlider, func, button=false;
					var tmpBounds=bounds;
					var labelWidth=bounds.y*4, numberWidth=bounds.y*3;

					if (canInterpolate, {slider=EZSliderI});

					func={|ez|
						p[name]=ez.value;
						this.set(name,ez.value)
					};

					if (p[name].size>1, {
						if (p[name].size>2, {
							bounds=(bounds.x)@(bounds.y*4);
							slider=EZMultiSlider;
						},{
							slider=EZRanger;
							if (canInterpolate, {slider=EZRangerI});
						})
						//slider=EZMultiSlider
					});
					button=name.asString.copyRange(0,1)=="t_";
					if (button, {
						guis[name]=Button(compositeView, bounds)
						.states_([ [name] ]).action_{
							this.set(name, 1)
						}.canFocus_(false).value_(p[name])
					},{
						//if (cs.step<0.000000001, {cs=cs.warp});
						guis[name]=slider.new(compositeView, bounds, name, cs, func
							, p[name], false
							, labelWidth//bounds.x/4 labelWidth
							, numberWidth//bounds.x/7 numberWidth
							, unitWidth
							//, gap: gap, margin: margin
						)
						//.round2_(0.0001)
						.font_(font)
						.setColors(*setColors).canFocus_(canFocus);
						bounds=tmpBounds;
						guis[name].round2_( if (cs.step<0.00000001, {0.0001}, {cs.step}) );
					});
				};
				//compositeView.decorator.nextLine;
				compositeView.rebounds;
				if (noParent, {
					parent.rebounds
				});
			}.defer
		}.fork;
		if (returnParent, {
			guis[\parent]=parent;//the key 'parent' doesn't seem to work
			//guis[\parentGUI]=parent;
		});
		if (willHang, {
			while({guis.keys.size<(returnButtons.binaryValue*2+cs.keys.size)}
				,{
					//1.wait;
					waitTime.wait;
			});//hang
			if (returnParent, {
				guis[\parent]=parent;//the key 'parent' doesn't seem to work
				//guis[\parentGUI]=parent;
			});

			/*
			c = Condition.new;
			[cs, cs.keys, cs.keys.size, guis.keys, guis.keys.size];
			if (guis.keys.size==cs.keys.size, {c.unhang});
			c.hang;
			*/
		});
		^guis
	}
}