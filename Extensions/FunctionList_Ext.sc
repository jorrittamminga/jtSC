+ Object {
// FunctionList support
	addFuncFirst { arg ... functions;
		^FunctionList(functions++[this])
	}

}

+ FunctionList {
	addFuncFirst { arg ... functions;
		if(flopped) { Error("cannot add a function to a flopped FunctionList").throw };
		//array = array.addAll(functions)
		array=array.addFirst(functions);
		array=array.flat;
	}
}

+ Function {
	array { ^[this] }
	//interpret { ^this.interpretVal }
}