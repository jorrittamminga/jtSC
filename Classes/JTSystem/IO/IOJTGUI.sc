/*
maak ook buttons voor 'master' plugins zoals EQ en Compressor, ook als er geen masterfader is
*/
IOJTGUI : GUIJT {
	var <in;
	*new {arg in, parent, bounds, margin, gap, parentMargin=4@4, parentGap=4@2, meterHeight;
		^super.new.init(in, parent, bounds, margin, gap, parentMargin, parentGap, meterHeight);
	}
	close {

	}
	init {arg argin, argparent, argbounds, argmargin, arggap, argparentMargin
		, argparentGap, argmeterHeight;
		var meterViews, channelbounds, meterbounds, channelFont, recplaybounds;
		var masterFaderbounds, masterFaderX=0;
		var c, heightBefore;
		classJT=argin;
		in=argin;
		parent=argparent;
		bounds=argbounds;
		margin=argmargin;
		gap=arggap;
		channelbounds=bounds.copy;
		masterFaderbounds=bounds.copy;

		if (parent==nil, {
			hasWindow=true;
			parent=Window("", Rect(0,0,400,400)); parent.addFlowLayout; parent.alwaysOnTop_(true); parent.front});

		channelbounds.x=channelbounds.x-(margin.x*2);
		channelbounds.y=channelbounds.y-(margin.y*2);
		masterFaderbounds.x=masterFaderbounds.x-(margin.x*2);
		masterFaderbounds.y=masterFaderbounds.y-(margin.y*2);
		parentMargin=argparentMargin??{4@4};
		parentGap=argparentGap??{4@2};
		/*
		if (in.plugins==nil, {
		in.addPlugin(\Meter);
		});
		*/
		if (in.plugins==nil, {
				in.addPlugin(\Meter);
		},{
			if (in.plugins.keys.includes(\Meter).not, {
				in.addPlugin(\Meter);
			});
		});
		if (in.plugins!=nil, {
			//if (in.plugins.keys.includes(\Meter).not, {in.addPlugin(\Meter);});
			if (in.plugins[\MasterFader]!=nil, {
				masterFaderX=1//bounds.x+gap.x+(2*margin.x)
			})
		});
		bounds=
		//((in.labels.size+masterFaderX*bounds.x)+ (in.labels.size+masterFaderX-1*parentGap.x))
		(
			(in.labels.size+masterFaderX*bounds.x)
			+ (in.labels.size+masterFaderX-1*parentGap.x)
			//+ (2*parentMargin.x)
		)
		@
		//(bounds.y*24);
		400;
		//bounds=300@(bounds.y*20);

		if (parent.class==Window, {
			heightBefore=parent.view.decorator.maxHeight;
		},{
			heightBefore=parent.decorator.maxHeight;
		});

		this.initAll(parentMargin, parentGap);

		channelFont=Font(Font.defaultMonoFace, channelbounds.y*0.6);
		masterFaderbounds=masterFaderbounds.x @ ((gap.y+channelbounds.y)*2
			+ (masterFaderbounds.x*6));
		meterbounds=channelbounds.x@(argmeterHeight??{channelbounds.x*6});
		recplaybounds=(((bounds.x-(2*margin.x)-gap.x)/2).floor
			- 0//deze kan ws weg!
		)@channelbounds.y;

		in.labels.do{|label,i|
			var c=CompositeView(parent,
				//(channelbounds.x)//+(2*margin.x)
				(channelbounds.x+(2*margin.x))
				@
				//(channelbounds.y)
				( (channelbounds.y+gap.y) * (1+1) + (2*margin.y) + (meterbounds.y+gap.y) )
			);//+2*margin.y
			var meterC, labelSize=label.asString.size, labelFont=Font(Font.defaultMonoFace
				, (channelbounds.x/labelSize*1.666).min(channelFont.size));
			views[label]=();
			c.addFlowLayout(margin, gap);
			//c.background_(Color.rand);
			c.canFocus_(false);
			//c.background_(Color.red);

			StaticText(c, channelbounds).string_(label)
			.font_(labelFont)
			.align_(\center)
			//.background_(Color.green)
			;
			if (in.plugins[\Meter]!=nil, {
				meterC=CompositeView(c, meterbounds);
				meterC.addFlowLayout(0@0, 0@0);
				meterViews=meterViews.add(meterC);
			});
			views[label][\mute]=Button(c, channelbounds)
			.states_([ [\m], [\M, Color.black, Color.yellow]]).action_{|b|
				in[label].mute_(b.value>0)
			}.font_(channelFont).canFocus_(false);
			if (in[label].plugins!=nil, {
				in[label].plugins.sortedKeysValuesDo{arg key, plugin;
					var letter=plugin.class.asString[0];
					var window, gui;
					var bypassFunc={arg flag;
						if (flag, {
							{views[label][key].states_([ [letter, Color.grey] ])}.defer;
						},{
							{views[label][key].states_([ [letter, Color.black
								, Color.green(1.5)] ])}.defer;
						});
					};
					views[label][key]=Button(c, channelbounds).states_([ [letter] ])
					.action_{
						if (plugin.gui==nil, {
							gui=plugin.makeGUI;
							if (key==\Compressor, {
								gui.window.userCanClose_(true);
							},{
								gui.window.userCanClose_(false);
							})

							//gui.window.userCanClose_(false);
						},{
							plugin.gui.window.front;
						});
					}.font_(channelFont).canFocus_(false);
					if (key!=\Player, {
						bypassFunc.value(plugin.bypass);
						plugin.bypassFunc=plugin.bypassFunc.addFunc(bypassFunc);
					});
					plugin
				};
			});
			//c.decorator.nextLine;
			c.rebounds;
		};

		if (in.plugins[\Meter]!=nil,{
			in.plugins[\Meter].makeGUI(meterViews, meterbounds, in.labels, 0@0, 0@0);
		});

		[\Player, \Recorder].do{|key|
			if (in.plugins[key]!=nil, {
				var letter=key.asString[0];
				views[key]=Button(parent, recplaybounds).states_([[letter]]).action_{
					if (in.plugins[key].gui==nil, {
						in.plugins[key].makeGUI(userCanClose:false)
					},{
						in.plugins[key].gui.front
					});
				}.font_(Font(Font.defaultMonoFace, recplaybounds.y*0.8)).canFocus_(false);
			});
		};

		if (in.plugins[\MasterFader]!=nil, {
			c=CompositeView(parent, (2*margin.x)+masterFaderbounds.x
				@(2*margin.y+masterFaderbounds.y));
			c.addFlowLayout(margin, gap);
			//c.background_(Color.rand);
			views[\MasterFader]=EZSlider(c, masterFaderbounds.x@
				(channelbounds.y+gap.y+meterbounds.y)
				, \amp, \db.asSpec, {|ez|
					in.db_(ez.value)}, in.db, labelHeight:channelbounds.y
				, layout: \vert).font_(channelFont);
			views[\MasterFader].labelView.align_(\center);
			views[\MasterFader].sliderView.canFocus_(false);
			views[\MasterFader].numberView.canFocus_(false);

			views[\MasterMute]=Button(c, channelbounds)
			.states_([ [\m], [\M, Color.black, Color.yellow]]).action_{|b|
				in.mute_(b.value>0)
			}.font_(channelFont).canFocus_(false);

			in.plugins.sortedKeysValuesDo{arg key, plugin;
				if ([\MasterFader, \Meter].includesEqual(key).not, {
					var letter=plugin.class.asString[0];
					var window, gui;
					var bypassFunc;
					if (key==\Splay, {
						views[key]=Button(c, channelbounds).states_([
							["Splay", Color.black, Color.green(1.5)],
							["Splay", Color.grey] ])
						.action_{|b|
							plugin.bypass_(b.value>0);
						}.font_(channelFont).canFocus_(false)
						.value_(plugin.bypass.binaryValue);
					},{
						bypassFunc={arg flag;
							if (flag, {
								{views[key].states_([ [letter, Color.grey] ])}.defer;
							},{
								{views[key].states_([ [letter, Color.black
									, Color.green(1.5)] ])}.defer;
							});
						};

						views[key]=Button(c, channelbounds).states_([ [letter] ])
						.action_{
							if (plugin.gui==nil, {
								gui=plugin.makeGUI;
								if (key==\Compressor, {
									gui.window.userCanClose_(true);
								},{
									gui.window.userCanClose_(false);
								})
							},{
								plugin.gui.window.front;
							});
						}.font_(channelFont).canFocus_(false);
						bypassFunc.value(plugin.bypass);
						plugin.bypassFunc=plugin.bypassFunc.addFunc(bypassFunc);
						plugin
					})
				});
			};
			c.rebounds;
		});

		/*
		in.plugins.keysValuesDo{|pluginName, plugin|
		switch(pluginName,
		\Meter, {"meter!"},
		\Player, {"player"},
		\Recorder, {"recorder!"},
		\MasterFader, {"masterfader!"},
		{plugin}
		);
		};
		*/

		/*
		if ((in.plugins[\Recorder]!=nil) && (in.plugins[\Player]!=nil), {
		in.plugins[\Recorder].stopRecordingFunc=
		in.plugins[\Recorder].stopRecordingFunc.addFunc({arg r;
		in.plugins[\Player].path_(in.plugins[\Recorder].path, true);
		});
		});
		*/
		parent.rebounds;

		if (hasWindow, {
			window.rebounds;
		});

		if (parentAtInit.class!=nil, {
			if (parentAtInit.class==Window, {
				parentAtInit.view.decorator.reset;
				parentAtInit.view.decorator.maxHeight=heightBefore+parent.bounds.height;
				parentAtInit.view.decorator.nextLine;
			},{

			});
		});

		if (parent.onClose==nil, {parent.onClose={}});

		parent.onClose=parent.onClose.addFunc({
			in.free;
			this.close;
		});
	}
}