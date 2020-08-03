f={arg ...v; v[1]*1.0}; {100000.do{f.value(*Array.rand(2,0.0, 1.0))}}.bench
f={arg v0,v1; v1*1.0}; {100000.do{f.value(*Array.rand(2,0.0, 1.0))}}.bench


f={arg ...v; v.copyToEnd(1)*1.0}; {10000.do{f.value(*Array.rand(10,0.0, 1.0))}}.bench
f={arg v1,v2,v3,v4,v5,v6,v7,v8,v9,v10; [v2,v3,v4,v5,v6,v7,v8,v9,v10]*1.0}; {10000.do{f.value(*Array.rand(10,0.0, 1.0))}}.bench


f={arg v1,v2,v3,v4,v5,v6,v7,v8,v9,v10; [v1,v2,v3,v4,v5,v6,v7,v8,v9,v10]*1.0}; {10000.do{f.value(*Array.rand(10,0.0, 1.0))}}.bench
f={arg ...v; v*1.0}; {10000.do{f.value(*Array.rand(10,0.0, 1.0))}}.bench




f=FreqSpec(20,20000,\exp); {100000.do{f.map(1.0.rand)}}.bench
f=FreqSpec(20,20000,\exp).warp; {100000.do{f.map(1.0.rand)}}.bench


f={arg val; (val*10).round(0.001);}; {10000.do{f.value(1.0.rand)}}.bench

f={arg val; val=val*10; val.round(0.001);}; {10000.do{f.value(1.0.rand)}}.bench


f={arg val; val*0.1+1}; {10000.do{f.value(1.0.rand)}}.bench
f="{arg val; val*0.1+1};".interpret; {10000.do{f.value(1.0.rand)}}.bench
f={arg val; val*0.1}; g={arg val; f.value(val)+1 };   {10000.do{g.value(1.0.rand)}}.bench

g.value(0.1)


f={arg val; val.squared}; {10000.do{f.value(1.0.rand)}}.bench
f={arg val; val.squared.round(0.1)}; {10000.do{f.value(1.0.rand)}}.bench



f={arg val; val*0.01}; {10000.do{f.value(1.0.rand)}}.bench

f={arg val; val=val*0.01; val+10}; {10000.do{f.value(1.0.rand)}}.bench
f={arg val; val=val*0.01}; f=f.addFunc{arg val; val+10}; {10000.do{f.value(1.0.rand)}}.bench
FunctionList

f={arg ...msg; msg.postln}
f.value(12,3,45,234655,543)
f.value(*[12,3,45,234655,543])

f={arg val; val*0.01}; {10000.do{f.value(1.0.rand)}}.bench
f={arg ...val; val[0]*0.01}; {10000.do{f.value(1.0.rand)}}.bench
f={arg ...val; val[0]*0.01}; {10000.do{f.value(1.0.rand)}}.bench


f={arg val; val*0.01}; {10000.do{f.value(1.0.rand)}}.bench
i=0; f={arg val; if (i==0, {val*0.01}, {val*0.1})}; {10000.do{f.value(1.0.rand)}}.bench
f={arg val, i=0; if (i==0, {val*0.01}, {val*0.1})}; {10000.do{f.value(1.0.rand)}}.bench
g={arg in; in*0.01}; f={arg val, i=0; g.value(val)}; {10000.do{f.value(1.0.rand)}}.bench

.perform


{10000.do{128.rand*0.01234321} }.bench
f={arg val; val*0.01234321}; {10000.do{f.value(128.rand)} }.bench


{10000.do{128.rand/127} }.bench

c=ControlSpec(0, 128); w=c.warp; {10000.do{c.map(128.rand)} }.bench
c=ControlSpec(0, 128); w=c.warp; {10000.do{w.map(128.rand)} }.bench
c=ControlSpec(0, 128); w=c.warp; {10000.do{1.0.rand*2+0} }.bench

{10000.do{w.map(128.rand).linlin(0,127,0.0, 1.0)} }.bench

c=ControlSpec(10, 100.0, 0); r=c.maxval-c.minval; [c.map(0.5), 0.5*r+10]

1.asFloat==1.0


c=ControlSpec(1, 128, \exp, 0.1); w=c.warp; {10000.do{w.map(1.0.rand)} }.bench
c=ControlSpec(1, 128, 0, 0.1); w=c.warp; {10000.do{w.map(1.0.rand)} }.bench
c=ControlSpec(1, 128, 0, 0.1); w=c.warp; {10000.do{(128.rand*0.001).round(0.1) } }.bench


c=ControlSpec(1, 123.5432564, 0, 0.1).map(0.5)

{10000.do{128.rand*0.001}}.bench
{10000.do{(128.rand*0.001).round(0.001)}}.bench



x=127; m=1/127; m=m*100.0; x*m



(
var cs1=ControlSpec(0.0, 127.0);//unmap
var cs2=ControlSpec(12.0, 21.0);//map
var mul1, add1, mul2, add2;
var mul, add;
var val=127, val1, val2;//midi input

//unmap, scale back to 0..1.0
add1=cs1.minval.neg;
mul1=(cs1.maxval-cs1.minval).reciprocal;
val1=(val+add1)*mul1;

//map
add2=cs2.minval;
mul2=(cs2.maxval-cs2.minval);
//val2=(val1)*mul2+add2

add=add2+(add1*mul1);
mul=mul1*mul2;
val*mul+add

)



x="test";
x=x++("poep");
x

OSCFunc