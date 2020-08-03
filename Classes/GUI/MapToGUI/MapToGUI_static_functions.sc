+ MapToGUI {

	getAction {
		functionList=guiObject.action;
		guiFunctionList={|val| guiObject.value_(val);};
		actionIndex=nil;
	}

	makeFuncStatic {arg argString, argFuncString, index, argStringOut, argFuncStringOut;
		if (mul!=nil, {
			if (add==nil, {add=0},{
		})});
		this.getAction;
		//----------------------------------------------
		string=argString??{"{arg guiObject, functionList, guiFunctionList, value;"};//
		funcString=argFuncString??{"{arg val, number;"};
		//funcString=funcString++	"var guiVal;";
		//----------------------------------------------
		stringOut=argStringOut??{"{arg ; var val;"};
		funcStringOut=argFuncStringOut??{"{arg view;"};
		//---------------------------------------------- speedlim
		if (timeThreshold>0, {
			string=string++"var time=Main.elapsedTime;";
			funcString=funcString++format(
				"if (Main.elaspedTime-time>%, { time=Main.elapsedTime;",timeThreshold)});
		//---------------------------------------------- glitch
		if (glitch>0, {
			string=string++"var previnval=0;";
			funcString=funcString++format("if ((val-previnval)<=%, { previnval=val;"
				, glitch)
		});
		//---------------------------------------------- variables
		if (controlSpecIn!=nil, {
			string=string++format("var controlSpecIn=%; "
				, controlSpecIn.asCompileStringJT++".warp");
			stringOut=stringOut++format("var controlSpecIn=%; "
				, controlSpecIn.asCompileStringJT++".warp");
		});
		if (controlSpecOut!=nil, {
			string=string++format("var controlSpecOut=%; "
				, controlSpecOut.asCompileStringJT++".warp");
			stringOut=stringOut++format("var controlSpecOut=%; "
				, controlSpecOut.asCompileStringJT++".warp");

		});

		funcStringOut=funcStringOut++"val=view.value";
		//------------------------------------------ make funcString
		//onderstaande kan netter en efficienter!
		if ((mode==\continuous)||(mode==\continuousdefer), {
			if (roundIn>0.00001, {
				funcString=funcString++"val=val.round("++roundIn++");"});
			if (controlSpecOut==nil, {
				if (controlSpecIn==nil, {
					if (mul==nil, {
						//funcString=funcString++""
					},{
						funcString=funcString++if (add==0, {
							funcStringOut=funcStringOut++format("val=val* %;"
								, mul.reciprocal);
							format("val=val* %;", mul);
						},{
							if (add>=0, {
								funcStringOut=funcStringOut++format("val=(val-%)* %;"
									, add, mul.reciprocal);
								format("val=val* %+%;", mul, add);
							},{
								funcStringOut=funcStringOut++format("val=(val+%)* %;"
									, add, mul.reciprocal);
								format("val=val* %-%;", mul, add.neg);
							})
						});
					});
				},{
					if (mul==nil, {
						funcStringOut=funcStringOut++"val=controlSpecIn.map(val)";
						funcString=funcString++format("val=controlSpecIn.unmap(val);");
					},{
						funcString=funcString++if (add==0, {
							funcStringOut=funcStringOut
							++format("val=controlSpecIn.map(val)*%;",mul.reciprocal);
							format("val=controlSpecIn.unmap(val)*%;",mul)
						},{
							if (add>=0, {
								funcStringOut=funcStringOut
								++format("val=controlSpecIn.map((val-%)*%);" ,add
									, mul.reciprocal);
								format("val=controlSpecIn.unmap(val)*%+%;",mul,add)
							},{
								funcStringOut=funcStringOut
								++format("val=controlSpecIn.map((val+%)*%);" ,add.neg
									, mul.reciprocal);
								format("val=controlSpecIn.unmap(val)*%-%;",mul,add.neg)
							})
						})
					});
				});
			},{
				if (controlSpecIn==nil, {
					if (mul==nil, {
						funcStringOut=funcStringOut++"val=controlSpecOut.unmap(val);";
						funcString=funcString++
						format("val=controlSpecOut.map(val);");
					},{
						funcString=funcString++if (add==0, {
							funcStringOut=funcStringOut
							++format("val=controlSpecOut.unmap(val); val=val* %;"
								,mul.reciprocal);
							format("val=val* %; val=controlSpecOut.map(val);",mul);
							//format("val=guiObject.controlSpec.map(val*%);", mul);
						},{
							if (add>=0, {
								funcStringOut=funcStringOut
								++format("val=controlSpecOut.unmap(val); val=(val-%) * %;"
									, add, mul.reciprocal);
								format("val=val* %+%; val=controlSpecOut.map(val);"
									,mul,add);
							},{
								funcStringOut=funcStringOut
								++format("val=controlSpecOut.unmap(val); val=(val+%) * %;"
									, add.neg, mul.reciprocal);
								format("val=val* %-%; val=controlSpecOut.map(val);"
									,mul,add.neg);
							});
						})
					})
				},{
					//if (mul==nil, {
					funcStringOut=funcStringOut
					++"val=controlSpecOut.unmap(val); val=controlSpecIn.map(val);";
					funcString=funcString++
					format("val=controlSpecIn.unmap(val); val=controlSpecOut.map(val);")
					//})
					/*
					,{
					funcString=funcString++if (add==0, {
					funcStringOut=funcStringOut++format("val=controlSpecIn.unmap(val)*%; val=controlSpecOut.map(val);", mul);
					format("val=controlSpecIn.unmap(val)*%; val=controlSpecOut.map(val);", mul)
					},{
					if (add>=0, {
					format("val=controlSpecIn.unmap(val)*%+%; val=controlSpecOut.map(val);"
					, mul, add)
					},{
					format("val=controlSpecIn.unmap(val)*%-%; val=controlSpecOut.map(val);"
					, mul, add.neg)

					})
					})
					})
					*/
				});
			});
			if (roundOut>0.00001, {funcString=funcString
				++"val=val.round("++roundOut++");"});
		});
		//		funcString=funcString++"guiVal=val;";
		/*
		if ((mode==\trigger)||(mode==\toggle), {
		string=string++format("var tr=%; ", triggerThreshold)
		});
		*/

		if(size>1, {
			funcString=funcString++format("value.put(%,val); val=value; "
				, index);
		});

		funcString=funcString++switch(mode
			, \continuous, {
				"functionList.value(val); "
				++ if (guiRun, {"{ guiFunctionList.value(val) }.defer;"},{""});
				//++ if (guiRun, {"{ guiObject.value_(val.postln) }.defer;"},{""});
			}

			, \continuousdefer, {
				"{functionList.value(val); " ++ if (guiRun
					, {"guiFunctionList.value(val);"},{""}) ++ "}.defer"
			}

			, \trigger, {
				format("if (val>%, {val=1.0; functionList.value(val);", triggerThreshold)
				++ if (guiRun, { "{ guiFunctionList.value(val); }.defer;"},{""})++"});"
			}

			, \toggle, {
				string=string++"var prevval=guiObject.value;";
				format("if (val>%, {val=%; {prevval=guiObject.value; val=prevval+val\\%(%); functionList.value(val);", triggerThreshold, step, maxval+1)
				++ if (guiRun, {"guiFunctionList.value(val);"},{""}) ++
				" }.defer; });"
			}

			, \gate, {format("val=%??{val}; functionList.value(val);", forceValue)++
				if (guiRun, {"{guiFunctionList.value(val)}.defer;"},{""})
			}

			, \plusminus, {
				if ((step==nil)||(step==0), {
					step=1;
				});
				guiFunctionList={|val| guiObject.valueAction_(val)};
				format("if (number==%, { { guiFunctionList.value(guiObject.value-%) }.defer},{{ guiFunctionList.value(guiObject.value+%) }.defer });", num[0], step, step)
			}
		);

		if (timeThreshold>0, {funcString=funcString++"})"});
		if (glitch>0, {funcString=funcString++"})"});
		funcString=funcString++"}";
		funcStringOut=funcStringOut++"}";
		funcString=string++funcString++"}";
		funcStringOut=stringOut++funcString++"}";

		funcString.postln;
		^funcString.interpret.value(guiObject, functionList, guiFunctionList, value);
	}
}
