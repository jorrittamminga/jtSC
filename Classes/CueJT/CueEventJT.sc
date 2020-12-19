CueEventJT : CueJT {
	var <transitionFlag;
	/*
		*initClass {
		classesMethods = (
			PresetJT: [\restore, \restoreI],
			PresetJTCollection: [\restore, \restoreI],
			PresetJTCollectionBlender: [\restore, \restoreI]
		);
	}
	*/
	*new {arg object, name, enviroment;
		^super.basicNew(object, name, enviroment)
	}
	prInit {
		transitionFlag=true;
		methode=classMethods[object.class.asSymbol][0];
		this.addToObjectFuncs;
		func[\store]=func[\store].addFunc({
			this.object.store;
		});
	}
	makeFunc {arg val;
		var obj, out;
		obj=this.getObject(val);
		val=val??{value.deepCopy};

		out=switch(val.class, Event, {
			var f, restEvent=val.deepCopy;
			var performMsg=val[\performMsg]??{[]};
			var preAction=val[\preAction];
			var postAction=val[\postAction];
			var methode=performMsg[0];
			if (methode==\restoreI, {});
			f={arg e;
				{
					preAction.value(e);
					if ((methode==\restoreI) && (transitionFlag==false), {
						obj.performMsg([\restore, performMsg[1]]);
					},{
						obj.performMsg(val);
					});
					postAction.value(e);
				}.fork;
				this.value_(val);
				func[\methode].value(methode);
				func[\index].value(performMsg[1]);
				func[\value]=val;
			};
			f;
		}, Array, {
			var f;
			var methode=val[0];
			f={
				if ((methode==\restoreI) && (transitionFlag==false), {
					obj.performMsg([\restore, val[1]]);
				},{
					obj.performMsg(val);
				});
				this.value_(val);
				func[\methode].value(val[0]);
				func[\index].value(val[1]);
				func[\value].value(val);
			};
			f
		}, Function, {
			val
		}, String, {
			var f;
			var methode=classMethods[obj.class.asSymbol][0]??\value;
			f={
				obj.perform(methode, val);
				this.value_(val);
				func[\methode].value(methode);
				func[\value].value(val);
			};
			f;
		}, {
			var f;
			var methode=classMethods[obj.class.asSymbol][0]??\value;
			f={
				obj.perform(methode, val);
				this.value_(val);
				func[\methode].value(methode);
				func[\value].value(val);
				func[\index].value(val);
			};
			f
		});
		^out
	}
	indexUpdate {arg object, index, funcKey, args;
		var flag=false, objectClass=object.class;
		switch(funcKey, \add, {
			var i; i=args[0];
			if (index>=i, {
				flag=true;
				index=index+1;
			});
		}, \removeAt, {
			var i; i=args[0];
			if (index>i, {
				flag=true;
				index=index-1;
			},{if (index==i, {
				this.delete
			})})
		}, \name, {
			var i=args[0], prevIndex=args[3], prevName=args[4], newName=args[5];
			if (index.class==String, {
				if (index==prevName, {
					index=newName;
				})
			},{
				if (index==i, {
					flag=true;
					index=prevIndex;
				}, {if (index==prevIndex, {
					flag=true;
					index=i;
				})});
			});
		});
		^index
	}
	addToObjectFuncs {
		[\add, \removeAt, \name].do{|funcKey|
			var tmpCueID=cueID;
			object.func[funcKey]=object.func[funcKey].addFunc({arg args;
				paths.do{|p, i|
					var flag=false, obj;
					path=p;
					cueID=i;
					value=values[cueID];
					switch(value.class, Event, {
						var index=value[\index]??{0}, newIndex;
						newIndex=indexUpdate(obj, index, funcKey, args);
						if (newIndex!=index, {flag=true; value[\index]=newIndex});
					}, Array, {
						var index, newIndex;
						index=value[1];
						newIndex=indexUpdate(obj, index, funcKey, args);
						if (newIndex!=index, {flag=true; value[1]=newIndex});
					}, String, {
						var index=value, newIndex;
						newIndex=indexUpdate(obj, index, funcKey, args);
						if (newIndex!=index, {flag=true; value=newIndex});
					}, {
						var index=value, newIndex;
						newIndex=indexUpdate(obj, index, funcKey, args);
						if (newIndex!=index, {flag=true; value=newIndex});
					});
					if (flag, {this.store});
				};
				cueID=tmpCueID;
			});
			if (gui!=nil, {
				{
					gui.views[\value].items_(object.names)
					//.value_();
				}.defer;
			});
		};
	}
	transitionFlag_ {arg flag=true;
		var tmpFuncList;
		if (flag!=transitionFlag, {
			transitionFlag=flag;
			if (flag==false, {
				object.restore(object.index)
			});
		});
	}
	makeGui {arg parent, bounds=350@20;
		{gui=CuePresetJTGUI(this, parent, bounds)}.defer
	}
}