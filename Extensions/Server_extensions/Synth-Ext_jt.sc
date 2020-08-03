+ SynthDesc {
	controlNamesAndSizes {
		var key=\test, event=();
		this.controls.do({|i,j|
			var name=i.name;
			if (name.asString!="?", {
				event[name.asSymbol]=1;
				key=name.asSymbol;
				},{
					event[key]=event[key]+1;
			})
		});
		^event
	}
}

+Synth {

	getx {arg key, action;
		var defName=this.defName, defNameSymbol=defName.asSymbol, desc;
		var keysAndSizes=SynthDescLib.global.at(defNameSymbol).controlNamesAndSizes;
		var count=keysAndSizes[key];

		if (keysAndSizes[key]<2, {
			this.get(key, action);
			},{
				this.getn(key, count, action);
		})
	}

	controlNames {
		var defName=this.defName, defNameSymbol=defName.asSymbol;
		^SynthDescLib.global.at(defNameSymbol).controlNames
	}


	getAll {arg specsFlag=false;
		var defName=this.defName, defNameSymbol=defName.asSymbol, out=()
		, server=this.server, desc;
		var keysAndSizes=SynthDescLib.global.at(defNameSymbol).controlNamesAndSizes
		, controlNames=SynthDescLib.global.at(defNameSymbol).controlNames;
		if (specsFlag, {
			controlNames=this.specs.keys;
		});

		controlNames.do{|key|
			if (keysAndSizes[key]<2, {
				this.get(key, {|val| out[key]=val}); server.sync;
				},{
					this.getn(key, keysAndSizes[key], {|val| out[key]=val}); server.sync;
			})
		};
		^out
	} // make an event or array with all keys and values


	specs {
		var defName=this.defName, defNameSymbol=defName.asSymbol, out=(), server=this.server;
		var keysAndSizes=SynthDescLib.global.at(defNameSymbol).controlNamesAndSizes
		, controlNames=SynthDescLib.global.at(defNameSymbol).controlNames;
		var desc=SynthDescLib.global.at(defNameSymbol);
		var specs=();

		if (desc.class==SynthDesc, {
			if (desc.metadata!=nil, {
				specs=desc.metadata.specs.deepCopy
			})
		});
		^specs
	}

}