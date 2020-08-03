+ Object {
	asCompileStringJT {
		var class, string, args;
		//thiz=e.controlSpec;
		class=this.class;
		string=class.asString;
		string=string++"(";
		args=this.storeArgs;
		args.do{|i| string=string++i.asCompileString++","};
		string.removeAt(string.size-1);
		string=string++")";
		^string
	}

}