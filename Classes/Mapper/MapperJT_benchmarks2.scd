//benchmark tests
//mapFast {arg value;^warp.map(value.clip(0.0, 1.0))}
c=\freq.asSpec;
{1000000.do{var val=1.0.rand; c.warp.map(val.clip(0,1.0))}}.bench//fast
{1000000.do{var val=1.0.rand; c.mapFast(val)}}.bench//slower!