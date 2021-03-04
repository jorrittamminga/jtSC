PresetsBlenderJT {
	var <presets, array, <method, <blendFunc;

	*new {arg presetsCollection;
		^super.new.init(presetsCollection)
	}

	init {arg argpresetsCollection;
		presets=argpresetsCollection;
		this.prInit;
	}

	prInit {arg methode;
		method=methode;
		array=EventsArrayJT.fill(presets.presetsCollection, presets.presetJT.object);//, method
	}

}
