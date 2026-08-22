// based on SensorData by (c) 2006, Marije Baalman
SensorDataSM {
	var <data, <>ltlen, <>stlen;

	*new{ |ltl,stl|
		^super.new.init(ltl,stl);
	}

	init{ |ltl,stl|
		ltlen = ltl ? 50;
		stlen = stl ? 10;
		data = Signal.new( ltlen );
		^this;
	}

	addValue{ |newval|
		data = data.addFirst(newval);
		if ( data.size > ltlen,
			{
				data.pop( data.size - ltlen );
		});
	}

	shortMean{
		var ldata;
		ldata = data.copyRange( 0, stlen - 1 );
		^ldata.meanF;
	}

	longIntegral{
		var ldata = data.copyRange( 0, ltlen -1 );
		^ldata.integral;
	}

	shortIntegral{
		var ldata = data.copyRange( 0, stlen -1 );
		^ldata.integral;
	}
	fluctuation{
		^( this.shortStdDev / this.longStdDev );
	}

	longStdDev{
		var ldata;
		ldata = data.copyRange( 0, ltlen - 1 );
		^ldata.stdDev;
	}

	longMean{
		var ldata;
		ldata = data.copyRange( 0, ltlen - 1 );
		^ldata.meanF;
	}

	shortStdDev{
		var ldata;
		ldata = data.copyRange( 0, stlen - 1 );
		^ldata.stdDev;
	}
}

SensorDataEMA {
	var <ema, <alpha, <rate;
	var <lagTime;

	*new { |alpha=0.12, rate=60|
		^super.new.init(alpha, rate);
	}
	init { |a, r|
		rate=r;
		ema = 0;
		this.alpha_(a);
	}
	addValue { |newval|
		^ema = (alpha * newval) + ((1 - alpha) * ema);
	}
	//--------------------------------------------------- compatibility with Synth, SensorData and Lag
	set { |...pairs|
		pairs.pairsDo { |key, value|
			switch (key)
			{\alpha}     { this.alpha_(value) }
			{\lagTime}   { this.lagTime_(value) }
			{\rate}      { this.rate_(value) }
			{\stlen}     { this.stlen_(value) }
			{\maFactor1} { this.maFactor1_(value) }
			{\maFactor}  { this.maFactor_(value) }
			{\maTime1}   { this.lagTime_(value) }
			{\maTime}    { this.lagTime_(value) }
		}
	}
	lagTime_ {arg t;
		lagTime = t.max(0);
		if (lagTime > 0) {
			alpha = 1.0 - ((-1.0) / (lagTime * rate)).exp;
		} {
			alpha = 1.0;
		}
	}
	rate_ {arg r;
		rate=r;
		this.lagTime_(lagTime);
	}
	stlen_ {arg val;
		this.alpha_(2.0 / (val.max(1.0) + 1.0))
	}
	maFactor_ {arg m;
		this.alpha_(1.0-m)
	}
	maFactor1_ {arg m;
		this.alpha_(1.0-m)
	}
	movingAverage {
		^ema
	}
	movingAverage1 {
		^ema
	}
	shortMean {
		^ema
	}

	stlen {
		^(2.0 / alpha) - 1.0
	}
	alpha_ {arg a;
		alpha=a;
		lagTime = -1.0 / (rate * (1.0 - alpha).log);
	}
	maFactor1 {
		^(1.0-alpha)
	}
	maFactor {
		^(1.0-alpha)
	}
}