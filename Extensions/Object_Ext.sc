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
	deepCollectKeys { arg depth, function, index = 0, rank = 0, keys; ^function.value(this, index, rank) }
}