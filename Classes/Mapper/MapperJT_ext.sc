+ControlSpec {
	asMapperJT {arg name, default, lag, smoothSize;
		^Mapper2JT(this, name, default, lag, smoothSize)
	}
}

+Synth {
	mapJT { arg ... args;
		var mapInOut=args.clump(2).flop;
		var mapMsg;
		mapMsg=mapInOut.deepCopy;
		mapMsg[0]=mapMsg[0].collect{|mapper| mapper.name};
		mapMsg[1]=mapMsg[1].collect{|mapper| mapper.bus??{mapper.makeBus(this.server)}};
		mapMsg=mapMsg.flop.flat;
		this.map(*mapMsg);
		if ((mapInOut[0].size==1) && (mapInOut[1].size==1)) {
			mapInOut=mapInOut.collect(_.unbubble)
		};
		^MapperInOutJT(mapInOut[0], mapInOut[1]).makeMapperSynthJT(this, \addBefore, mapMsg)
	}
}

+ Array {
	makeMapperSynthJT {arg target, addAction, mapMsg;


	}
}