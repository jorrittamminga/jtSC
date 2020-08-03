+ Collection {
	
	deepDoo{arg function={|i| i.postln}; 
		this.do({|that|
			if (that.class==this.class, {that.deepDoo(function)},{that.postln})
			})
		}

	includesCollection { arg that;
		var flags; 
		flags=this.collect({|i| i==that});  
		^flags.includes(true)
			
		}

}
