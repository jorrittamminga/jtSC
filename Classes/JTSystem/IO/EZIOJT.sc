InEZJT : IOJT {
	var <in;

	*new {arg inBus, target, label, addAction=\addBefore, parent, path;
		^super.new.init(inBus, target, label, addAction, parent, path);
	}

	init {arg arginBus, argtarget, arglabel, argaddAction, argparent, argpath;
		var func;
		path=argpath??{Platform.recordingsDir++"/"};
		this.isThreaded;
		func={
			in=InJT(arginBus, argtarget, arglabel, argaddAction);
			in.addPlugin(\Meter);

			[\EQ, \Compressor].do{|plugin|
				in.labels.do{|label|
					in[label].addPlugin(plugin);
					in[label].plugins[plugin].bypass_(true)
				};
			};
			in.addPlugin(\Recorder, [path]);
			in.addPlugin(\Player, [path]);
			in.plugins[\Player].addMonitor(0, 2);
			{in.makeGUI(argparent)}.defer
		};

		if (threaded, {func.value}, {{func.value}.fork})
	}
}

OutEZJT : IOJT {
	var <out;

	*new {arg inBus, target, label, addAction=\addBefore, parent, path;
		^super.new.init(inBus, target, label, addAction, parent, path);
	}

	init {arg arginBus, argtarget, arglabel, argaddAction, argparent, argpath;
		var func;
		path=argpath??{Platform.recordingsDir++"/"};
		this.isThreaded;
		func={
			out=OutJT(arginBus, argtarget, arglabel, argaddAction);
			out.addPlugin(\Meter);
			out.addPlugin(\MasterFader);
			out.addPlugin(\EQ);
			out.addPlugin(\Compressor, [\CompanderC, (thresh: -10, slopeAbove: 0.5
				, clampTime:0.01, relaxTime:0.1, limiter:true, sanitize:true)]);
			if (arginBus.asArray.flat.size>2, {out.addPlugin(\Splay)});

			[\EQ, \Compressor].do{|plugin|
				out.plugins[plugin].bypass_(true)
			};

			{out.makeGUI(argparent)}.defer;
		};

		if (threaded, {func.value}, {{func.value}.fork})
	}
}
/*
Server.killAll
(
s.waitForBoot{
i=InJT([0,1,2],s);
i.addPlugin(\Meter);
i.makeGUI
}
)
InEZJT
OutEZJT
OutJT
(
var w=Window.new.front;
w.addFlowLayout;
s.waitForBoot{
i=InEZJT([0,1,2],s, parent:w);
o=OutEZJT([0,1,2,3],s, parent:w);
}
)
o.path
*/
