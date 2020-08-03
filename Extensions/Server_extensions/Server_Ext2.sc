+ Server {
	scope { arg numChannels, index = 0, bufsize = 4096, zoom = (1), rate = \audio, alwaysOnTop=true;
		numChannels = numChannels ?? { if (index == 0) { options.numOutputBusChannels } { 2 } };

		if(scopeWindow.isNil) {
			scopeWindow = Stethoscope(this, numChannels, index, bufsize, zoom, rate, nil,
				this.options.numBuffers);
			// prevent buffer conflicts by using reserved bufnum
			scopeWindow.window.onClose = scopeWindow.window.onClose.addFunc({
				scopeWindow = nil });
		} {
			scopeWindow.setProperties(numChannels, index, bufsize, zoom, rate);
			scopeWindow.run;
			scopeWindow.window.front;
		};
		scopeWindow.window.alwaysOnTop_(alwaysOnTop);
		^scopeWindow
	}

	freqscope {arg alwaysOnTop=true;
		var fs;
		fs=FreqScope.new(server: this);
		fs.window.alwaysOnTop_(alwaysOnTop);
		^fs
	}


	meter { |numIns, numOuts, alwaysOnTop=true|
		var meterr;
		var window, newWindow=true;
		Window.allWindows.do({|i|
			if (i.name.contains(this.name.asString ++ " levels (dBFS)"), {
				newWindow=false; window=i});
		});
		if (newWindow, {
			if( GUI.id == \swing and: { \JSCPeakMeter.asClass.notNil }, {
				window=\JSCPeakMeter.asClass.meterServer( this );
			}, {
				meterr=ServerMeter(this, numIns, numOuts);
				meterr.window.alwaysOnTop_(alwaysOnTop);
				window=meterr
			});
		},{
			window
		});
		^window
	}

	syncJT {|condition, bundles, latency|
		if ((thisProcess.mainThread.state>3), {this.sync});
	}
}