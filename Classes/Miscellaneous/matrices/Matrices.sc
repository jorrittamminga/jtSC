Matrices : Array {
	classvar matrices;

	*initClass {
		matrices=(2..10).collect{|i|
			//(Platform.userExtensionDir++"/jtSC/matrices/matrix"++i.asString++".scd").load
			//(thisProcess.nowExecutingPath.dirname++"/matrix"++i.asString++".scd").load
			//(Matrices.filenameSymbol.asString.dirname++"/matrix"++i.asString++".scd").postln;
			(Matrices.filenameSymbol.asString.dirname++"/matrix"++i.asString++".scd").load
		};
		^matrices
	}

	*new{arg size;
		/*
		^(Platform.userExtensionDir++"/jtSC/matrices/matrix"
			++index.asString++".scd").load
		*/
		^(matrices.clipAt(size-2)).deepCopy
	}

	*at{arg index;
		/*
		^(Platform.userExtensionDir++"/jtSC/matrices/matrix"
			++index.asString++".scd").load
		*/
		^(matrices.clipAt(index-2)).deepCopy
	}

}