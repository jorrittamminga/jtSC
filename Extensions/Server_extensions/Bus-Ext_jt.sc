+Bus {

indices {
	^{|i| i+this.index }!this.numChannels

	}

}

+Array {
	asBus { arg rate = \audio,numChannels=1,server;
		^Bus.new(rate,this.sort[0],this.size,server)
	}
}