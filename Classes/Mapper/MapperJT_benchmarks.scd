//benchmark tests
c=\freq.asSpec;
w=c.warp;
b=MapperControlSpecJT(20, 20000, \exp);

//equally fast
{1000000.do{var val=1.0.rand; b.warp.map(val.clip(0,1.0))}}.bench//fast
{1000000.do{var val=1.0.rand; c.warp.map(val.clip(0,1.0))}}.bench//fast
{1000000.do{var val=1.0.rand; w.map(val.clip(0,1.0))}}.bench//fast

//a bit slower
{1000000.do{var val=1.0.rand; b.map(val)}}.bench//slow
{1000000.do{var val=1.0.rand; c.mapFast(val)}}.bench//slow

//slowest
{1000000.do{var val=1.0.rand; c.map(val)}}.bench//slowest



{1000000.do{var val=2.0.rand; c.map(val)}}.bench//slow
{1000000.do{var val=2.0.rand; c.warp.map(val.clip(0.0, 1.0)).round(0)}}.bench//fast