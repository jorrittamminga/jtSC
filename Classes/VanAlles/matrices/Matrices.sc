Matrices : Array {

	*new{arg index;
		^(Platform.userExtensionDir++"/JT/matrices/matrix"
			++index.asString++".scd").load
	}

	*at{arg index;
		^(Platform.userExtensionDir++"/JT/matrices/matrix"
			++index.asString++".scd").load

	}

}