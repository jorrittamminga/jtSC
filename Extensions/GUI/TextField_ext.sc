+ TextField {

	value {
		^this.string.interpret;
	}

	value_ { arg val;
		this.string_( val.asCompileString );
	}

	valueAction_ { arg val;
		this.string_( val.asCompileString  );
		this.doAction;
	}

}
