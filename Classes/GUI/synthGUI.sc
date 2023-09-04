/*
- zorg dat je ook je eigen volgorde van sliders kunt maken (ipv alfabetisch)
- als bounds.x<bounds.y dan is de layout \vert
- maak het ook mogelijk om EZKnobs te kiezen
- maak ook een versie van makeGui die args, actions en controlspecs teruggeeft zodat je hier zelf een gui mee kunt bouwen
*/
SynthGUIJT : GUIJT {
	var canFocus, <p, <cs, <synth, <server, <buttons, onClose, <waitTime
	, <returnOnSwitch, <synths, <excludeKeys, <nodeID, <nrt, <rebounds, <>action;

	*new {arg synth, parent, bounds, onClose=true, canFocus=true
		, labelWidth
		, gap, margin, returnOnSwitch=false, waitTime=0, excludeKeys, server, nrt=false, rebounds=true, action, scroll=false;
		^super.new.init(synth, parent, bounds, onClose, canFocus, labelWidth
			, gap, margin, returnOnSwitch, waitTime, excludeKeys, server, nrt=false, rebounds=true, action, scroll);
	}

	init {arg argsynth, argparent, argbounds, argonClose, argcanFocus
		, arglabelWidth, arggap, argmargin, argreturnOnSwitch, argwaitTime, argexcludeKeys, argserver, argnrt
		, argrebounds, argaction, argscroll;
		nrt=argnrt;
		rebounds=argrebounds;
		scroll=argscroll;
		action=argaction;
		switch(argsynth.class, Synth, {
			synth=argsynth;
			synths=[synth];
			name=synth.defName.asString ++ ": " ++ synth.nodeID.asString;
		}, Array, {
			synth=argsynth;
			synths=synth;
			synth=synth[0];
			name=synth.defName.asString ++ ": " ++ synth.nodeID.asString;
		}, Integer, {
			nodeID=argsynth;
			name=nodeID.asString;
		});
		parent=argparent;
		bounds=argbounds;
		onClose=argonClose;
		canFocus=argcanFocus;
		labelWidth=arglabelWidth;
		gap=arggap;
		margin=argmargin;
		waitTime=argwaitTime??{0};
		excludeKeys=argexcludeKeys;
		returnOnSwitch=argreturnOnSwitch;
		freeOnClose=false;
		if (returnOnSwitch.not, {buttons=()});
		this.initVars;
		if (threaded, {
			this.initSettings(argserver)
		},{
			{this.initSettings(argserver)}.fork
		});
	}

	initSettings {arg argserver;
		var but, oscresponder, cond, defName;
		if (synth==nil, {
			cond=Condition.new;
			server=argserver??{Server.default};
			oscresponder=OSCFunc({arg ...args;
				var msg=args[0];
				defName=msg[msg.indexOf(1000)+2];
				cond.unhang;
			}, '/g_queryTree.reply').oneShot;
			server.sendMsg(\g_queryTree, 0);
			cond.hang;
			synth=Synth.basicNew(defName, server, nodeID)
		},{
			server=synth.server;
		});
		p=synth.getAll;
		server.sync;
		cs=synth.specs??{()};
		excludeKeys.do{|key| cs.removeAt(key)};
		{
			var onName=if (hasWindow, {\on},{name});

			this.initGUI;
			if (onClose, {parent.onClose=parent.onClose.addFunc({synths.do(_.free)})});
			if (labelWidth==\auto, {
				labelWidth=(cs.keys.collect{|key|
					key.asString.size}.maxItem*font.size*0.6).ceil;
			});
			but=Button(parent, bounds.x@bounds.y)
			.states_([ [onName],[onName, Color.black, Color.green]]).action_{|b|
				if (p[\gate]!=nil, {
					if (b.value==1, {synths.do(_.run(true))});
					synths.do{|syn| syn.set(\gate, b.value)};
				},{
					synths.do(_.run(b.value.asBoolean));
				});
			}.canFocus_(canFocus).value_(p[\gate]??{1});
			if (returnOnSwitch, {views[\onSwitch]=but},{buttons[\onSwitch]=but});
			cs.sortedKeysValuesDo{|name,cs|
				var butFlag=name.asString.copyRange(0,1)=="t_";
				var action;
				cs=cs.asSpec;//this is a new feature!
				views[name]=if (butFlag, {
					action={synths.do{|syn| syn.set(name, 1)}};
					Button(parent, bounds).states_([ [name] ]).action_{
						synths.do{|syn| syn.set(name, 1)};
					}.canFocus_(canFocus).value_(p[name])
				},{
					action={|ez|
						p[name]=ez.value;
						synths.do{|syn| syn.set(name,ez.value)}
					};
					if (nrt, { action={|ez| [\n_set, synths[0].nodeID, name, ez.value] } });
					this.makeEZGUI(bounds.copy, name, cs, action
						, p[name],false, labelWidth, equalLength:false);
				});
			};
			if (rebounds==true, {
				parent.rebounds;
				if (hasWindow, {
					window.rebounds});
			});
			action.value;
		}.defer;
		if (waitTime>0, {
			while({views.keys.size<(returnOnSwitch.binaryValue+cs.keys.size)}
				,{
					waitTime.wait;
			});
		});
	}
}



+Synth {

	makeGui {arg parent, bounds=350@20, onClose=true, canFocus=true, labelWidth, gap, margin, returnOnSwitch=false, waitTime=0, excludeKeys, nrt=false, rebounds=true, action, scroll=false;
		^SynthGUIJT(this, parent, bounds, onClose, canFocus, labelWidth
			, gap, margin, returnOnSwitch, waitTime, excludeKeys,nrt, rebounds, action: action, scroll:scroll)
	}
}

+Array {
	makeGui {arg parent, bounds=350@20, onClose=true, canFocus=true, labelWidth, gap, margin, returnOnSwitch=false, waitTime=0, excludeKeys, nrt=false, rebounds=true, action, scroll=false;

		^SynthGUIJT(this, parent, bounds, onClose, canFocus, labelWidth
			, gap, margin, returnOnSwitch, waitTime, excludeKeys, nrt:nrt, rebounds:rebounds, action: action, scroll:scroll)
	}
}

+ Integer {
	makeGui {arg parent, bounds=350@20, onClose=true, canFocus=true, labelWidth, gap, margin, returnOnSwitch=false, waitTime=0, excludeKeys, server, nrt=false, rebounds=true, action, scroll=false;
		^SynthGUIJT(this, parent, bounds, onClose, canFocus, labelWidth
			, gap, margin, returnOnSwitch, waitTime, excludeKeys, server, nrt, rebounds, action, scroll:scroll)
	}
}