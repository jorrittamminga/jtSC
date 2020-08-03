+ Class {

	topclass {
		var i=this.superclasses.size;
		^this.superclasses.clipAt(i-2);
	}
}