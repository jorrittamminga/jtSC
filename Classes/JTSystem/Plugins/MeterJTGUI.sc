/*
*/
MeterJTGUI {
	var <meter, <numberOfMeters, <numberOfMetersPerServer, <>dBLow;
	var <labels, <orderOfMeters, reorderFlag, <window, <parent, <bounds, <guis;
	var <oscGUI, <compositeviews, closeFunc, <layout;

	*new {arg meter, parent, bounds=20@150, labels, margin=4@4, gap=4@4, font, layout=\vert
		, showBus=false, orderOfMeters;
		^super.new.init(meter, parent, bounds, labels, margin, gap, font, layout
			, showBus, orderOfMeters);
	}

	removeOSCFunc {
		oscGUI.do(_.free);
	}

	addOSCFunc {
		var offset=0;
		var n=0;
		oscGUI=meter.servers.collect{arg server, serverID;
			var oscf, guiOffset, numberOfMeters;
			offset=offset+n;
			guiOffset=offset;
			n=numberOfMetersPerServer[serverID];
			numberOfMeters=n;

			OSCFunc({arg msg;
				numberOfMeters.do{arg i;
					var db=msg[i*2+3+1].ampdb;
					var value=db.linlin(dBLow, 0, 0, 1);
					var peakdB=msg[i*2+3].ampdb;
					var peak=peakdB.linlin(dBLow, 0, 0, 1, \min);
					{
						guis[0][guiOffset+i].value_(value);
						guis[0][guiOffset+i].peakLevel_(peak);
						guis[1][guiOffset+i].value_(peakdB);
					}.defer
				}
			}
			, meter.cmdName[serverID]
			, meter.netAddr[serverID]
			);
		};
	}

	addOSCFuncSum {
		var offset;
		var valueSum={{0}!meter.servers.size}!numberOfMeters;
		var peakSum=valueSum.deepCopy;
		var dBSum=valueSum.deepCopy;

		oscGUI=meter.servers.collect{arg server, serverID;
			var func, func1;
			if (serverID==(meter.servers.size-1), {
				func1={arg i; {
					var valuedB, peakdB;
					//valuedB=valueSum[i].sqrt.sum.ampdb;
					valuedB=valueSum[i].sum.ampdb;
					peakdB=peakSum[i].sum.ampdb;
					guis[0][i].value_( valuedB.linlin(dBLow, 0, 0, 1) );//.sum
					guis[0][i].peakLevel_( peakdB.linlin(dBLow, 0, 0, 1, \min) );//sum
					guis[1][i].value_( peakdB );//sum
				}.defer
				}
			});
			func={arg msg;
				numberOfMeters.do{arg i;
					var value=msg[i*2+3+1];
					var peak=msg[i*2+3];
					valueSum[i][serverID]=value;
					peakSum[i][serverID]=peak;
					func1.value(i);
				}
			};
			OSCFunc(func, meter.cmdName[serverID], meter.netAddr[serverID])
		};
	}

	init {arg argmeter, argparent, argbounds, arglabel, margin, gap, argfont
		, arglayout, showBus, argorderOfMeters;
		var c;
		reorderFlag=true;

		meter=argmeter;
		bounds=argbounds;
		parent=argparent??{
			window=Window("meters"
				, (meter.numberOfMeters*(bounds.x+gap.x)+margin.x+margin.x)
				@(bounds.y+margin.y+margin.y+gap.y)
			).front;
			window.addFlowLayout(margin, gap);
			window.alwaysOnTop_(true);
			parent=window;
		};
		orderOfMeters=argorderOfMeters??{reorderFlag=false; (0..meter.numberOfMeters-1)};

		orderOfMeters=meter.inBus.flat.collect{|b|
			meter.busIndexPerServer.flat.indexOfEqual(b)
		};
		//labels=arglabel??{orderOfMeters};
		layout=arglayout;
		dBLow= -80;
		//font=argfont??{this.initFont(argbounds)};
		oscGUI=[];
		guis=[0,1];
		numberOfMeters=meter.numberOfMeters;
		numberOfMetersPerServer=meter.numberOfMetersPerServer;
		guis[0]=Array.newClear(numberOfMeters);
		guis[1]=Array.newClear(numberOfMeters);

		compositeviews=numberOfMeters.collect{|i|
			var c=CompositeView(parent.asArray.wrapAt(i), bounds.x@(bounds.y+bounds.x));
			var heights=[0.9, 0.1].normalizeSum*bounds.y;
			var index=orderOfMeters[i];
			if (showBus, {heights=[0.9, 0.1,0.1].normalizeSum*bounds.y;});
			c.addFlowLayout(0@0, 0@0);
			guis[1][index]=NumberBox(c, bounds.x@(heights[1]))
			.font_(Font(Font.defaultMonoFace, heights[1]*0.5)).canFocus_(false);
			guis[0][index]=LevelIndicator(c, bounds.x@heights[0])
			.warning_(0.9).critical_(1.0).drawsPeak_(true);
			//.numTicks_(9)
			//.numMajorTicks_(3);
			if (showBus, {
				StaticText(c, bounds.x@heights[2]).string_(
					if (meter.inBusFlat[i].class==Bus, {meter.inBusFlat[i].index},{
						meter.inBusFlat[i]
					})
				).font_(Font(Font.defaultMonoFace, heights[2]*0.5));
			});
			if (window==nil, {
				window=[c.findWindow];
			},{
				if (window.asArray.includesEqual(c.findWindow).not, {
					window=window.asArray.add(c.findWindow)
				});
			});
			c
		};

		if ((meter.servers.asArray.size>1), {
			if (meter.sumMeters, {this.addOSCFuncSum},{this.addOSCFunc})
		},{
			this.addOSCFunc;
		});
		closeFunc={arg tv;
			tv=tv.findWindow;
			if (window.class==Window, {
				//weet ik nog niet....
			},{
				window.remove(tv);
			});
			this.close;};
		window.asArray.do{|w| w.onClose_(w.onClose.addFunc(closeFunc))};
	}

	close {
		this.removeOSCFunc;
		window.asArray.do{|w| w.onClose.removeFunc(closeFunc)};
		meter.gui=nil;
		if (window==nil, {
			compositeviews.remove;
		},{
			window.asArray.do(_.close)
		});
	}

	free { this.close }
}