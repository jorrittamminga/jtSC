EZLevelIndicator {
	var <levelIndicator, <staticText, <>synth, <font;
	var dBLow;

	*new { arg parent, bounds, bus, target, addAction=\addAfter,
		label, controlSpec, action, initVal,
		initAction=false, labelWidth=30, numberWidth=45,
		unitWidth=0, labelHeight=20,  layout=\horz, gap=1@1, margin, updateFreq=20, dBLow=80.neg
		, showLabel=false, showValue=false
		;

		^super.new.init(parent, bounds, bus, target, addAction, label, controlSpec, action,
			initVal, initAction, labelWidth, numberWidth,
			unitWidth, labelHeight, layout, gap, margin, updateFreq, dBLow
			, showLabel, showValue

		)
	}

	init { arg parent, bounds, bus, target, addAction, label, argControlSpec, argAction, initVal,
		initAction, labelWidth, argNumberWidth,argUnitWidth,
		labelHeight, argLayout, argGap, argMargin, updateFreq, dBLow
		, showLabel=false, showValue=false
		;

		var c, d, boundsLevelIndicator, boundsStaticText, server, oscCmd, oscFunc;
		//var showLabel=false, showValue=false;
		//labelWidth=30;

		if (bus.class==Bus, {bus=bus.indices});
		if (target==nil, {target=Server.default; server=Server.default},{server=target.server});

		bus=bus.asArray;

		if (label==nil, {
			label=bus

		});

		bounds.x=bounds.x.floor;

		if (bounds.x<bounds.y, {
			labelWidth=bounds.x;
			boundsStaticText=[bounds.x, bounds.x*2/3];

			if (boundsStaticText[1]>labelHeight, {boundsStaticText[1]=labelHeight});

			font=Font("Helvetica", boundsStaticText[1]*0.8);
			boundsLevelIndicator=[(bounds.x), bounds.y-(showLabel.binaryValue*boundsStaticText[1])
				-(showValue.binaryValue*boundsStaticText[1])];
			d=CompositeView(parent, (bounds.x*bus.size+(argGap.x))@(bounds.y));
		},{
			boundsStaticText=[bounds.y*3/2, bounds.y];
			if (boundsStaticText[1]>labelHeight, {boundsStaticText[1]=labelHeight});
			font=Font("Helvetica", boundsStaticText[1]*0.8);
			boundsLevelIndicator=[bounds.x, (bounds.y)];
			d=CompositeView(parent, (bounds.x+(argGap.x))@(bounds.y*bus.size));
		});

		d.addFlowLayout(0@0, 0@0);
		d.background_(Color.black);
		levelIndicator={0}!bus.size; staticText={0}!bus.size;

		{|i|
			var c;
			c=CompositeView(d, (bounds.x)@(bounds.y));
			c.addFlowLayout(argGap, 0@0); c.background_(Color.black);

			if (showLabel, {
				StaticText(c,labelWidth@boundsStaticText[1])
				.font_(font).string_(label.wrapAt(i).asString).stringColor_(Color.white)
				.background_(Color.blue)
				;
			});


			levelIndicator[i]=LevelIndicator(c
				, (bounds.x)@(boundsLevelIndicator[1])).warning_(0.9).critical_(1.0)
			.drawsPeak_(true);
			if (showValue, {
				staticText[i]=StaticText(c, boundsStaticText.asPoint)
				.font_(font).string_("-80").stringColor_(Color.white)
				.background_(Color.blue)
				;
			});
		}!bus.size;

		oscCmd=("/"++server.name ++ bus.asString ++ Main.elapsedTime.asString);

		synth=SynthDef(\OSCmeter, {arg updateFreq=20;
			var in=In.ar(bus);
			SendPeakRMS.kr(in, updateFreq, 3, oscCmd.asString)
		}).play(target, [\updateFreq, updateFreq], addAction);

		oscFunc=OSCFunc({|msg|
			{
				try {
					var channelCount = msg.size - 3 / 2;
					channelCount.do {|channel|
						var baseIndex = 3 + (2*channel);
						var peakLevel = msg.at(baseIndex);
						var rmsValue  = msg.at(baseIndex + 1);
						var meter = levelIndicator.at(channel);
						var string = if (showValue, {staticText.at(channel)});
						var rms;
						if (meter.isClosed.not) {
							meter.peakLevel = peakLevel.ampdb.linlin(dBLow, 0, 0, 1, \min);
							rms=rmsValue.ampdb;
							meter.value = rms.linlin(dBLow, 0, 0, 1);
							if (showValue, {
								string.string_(rms.round(1.0).asString);
							});
						}
					}
				} { |error|
					if(error.isKindOf(PrimitiveFailedError).not) { error.throw }
				};
			}.defer;
		}, oscCmd.asSymbol, server.addr).fix;

		parent.onClose=(parent.onClose.addFunc({ synth.free; oscFunc.remove; }));
	}
}