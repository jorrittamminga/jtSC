+ Array {

	set {arg ...msg;
		this.do{|synth| synth.set(*msg)}
	}

}