Mapper

IEnvGen.kr(env) is by far de efficienste mapper (heb ik getest!)

Env.xyc;

[val1, val2]

SynthDef(\Mapper1D, {
	var in1=In.kr(inBus1).lag(lagU1, lagD1);
	var outs;
	outs=IEnvGen.kr(Env([value1, value2, value3],[].normalizeSum, ), in1);
}).add;

[
	[val1, val2],
	[val3, val4]
]

SynthDef(\Mapper2D, {
	var in1=In.kr(inBus1).lag(lagU1, lagD1);
	var in2=In.kr(inBus1).lag(lagU1, lagD1);
	var outs1, outs2, outs;
	outs1=IEnvGen.kr(Env([value1, value2, value3],[].normalizeSum, ), in1);
	outs2=IEnvGen.kr(Env([value4, value5, value6],[].normalizeSum, ), in1);
	outs=LinXFade2.kr(outs1, outs2, in2*2-1.0);
}).add;


[
	[val1, val2]
	[val3, val4]
]

SynthDef(\Mapper3D, {
	var in1=In.kr(inBus1).lag(lagU1, lagD1);
	var in2=In.kr(inBus1).lag(lagU1, lagD1);
	var in3=In.kr(inBus1).lag(lagU1, lagD1);
	var outs1, outs2, outs3, outs;
	outs1=IEnvGen.kr(Env([value1, value2, value3],[].normalizeSum, ), in1);
	outs2=IEnvGen.kr(Env([value4, value5, value6],[].normalizeSum, ), in1);
	outs2=IEnvGen.kr(Env([value4, value5, value6],[].normalizeSum, ), in1);
	outs=LinXFade2.kr(outs1, outs2, in2*2-1.0);
}).add;

PresetMorph