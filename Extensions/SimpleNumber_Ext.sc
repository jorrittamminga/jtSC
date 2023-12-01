+SimpleNumber {

	fontSize {arg bounds=100@20, factor=0.608;
		^if (this*factor>(bounds.x/bounds.y), {bounds.x/(this*factor)},{bounds.y})
	}
	//after Vassilakis, 2001 & 2005 http://www.acousticslab.org/learnmoresra/moremodel.html
	//value between 0.0 and 0.090387257747353
	roughness {arg item2=440, weight1=1.0, weight2=1.0, normalize=true;
		var freqMin=this.min(item2), freqMax=item2.max(this);
		var ampMin=weight1.min(weight2), ampMax=weight2.max(weight1);
		var x=(ampMin*ampMax);
		var y=(2*ampMin)/(ampMin+ampMax);
		var b1 = 3.5,  b2 = 5.75,  s1 = 0.0207,  s2 = 18.96,  s = 0.24/(s1*freqMin + s2);
		var z= (b1.neg*s*(freqMax-freqMin)).exp - (b2.neg*s*(freqMax-freqMin)).exp;
		var factor=if (normalize, {11.063506349481},{1.0});

		^x.pow(0.1)*0.5*(y.pow(3.11))*z*factor
	}

	//inverse formule of roughess. returns [freqMin, freqMax] in relation to this (=centerfreq)
	bwr {arg roughness=0.24, normalized=true;
		/*
		var dev=this.copy, attempt=0, attempts=10, diff=10000, r;
		while({(diff>0.01)&&(attempt<attempts)},{

		diff=(roughness-this.roughness(this+dev));
		attempt=attempt+1
		});

		^dev
		*/
		/*
		//var freqMin=this.min(item2), freqMax=item2.max(this);
		var b1 = 3.5,  b2 = 5.75,  s1 = 0.0207,  s2 = 18.96,  s = 0.24/(s1*this + s2);
		//var z= (b1.neg*s*(freqMax-freqMin)).exp - (b2.neg*s*(freqMax-freqMin)).exp;
		//var factor=if (normalize, {11.063506349481},{1.0});

		[b1.neg*s, b2.neg*s].postln;

		^((roughness*2.0).log / ((b1.neg*s) - (b2.neg*s)) )

		//z*factor
		*/
	}

	nextInList { |list|  // collection is sorted
		var index, out;
		out=list.performNearestInList(this);
		index=list.indexOf(out);
		if (out<this, {
			out=list.clipAt(index+1)
		});
		^out
	}
	prevInList { |list|  // collection is sorted
		var index, out;
		out=list.performNearestInList(this);
		index=list.indexOf(out);
		if (out>this, {
			out=list.clipAt(index-1)
		});
		^out
	}
	/*
	noise {arg i=0, prev, bits=5, w=1.0, maxBits=16, rand={1.0.rand};
	var that, out;
	var pot=bits.collect({|k| 2.pow(k)}), sum=pot.sum;
	prev=prev??{0!bits};
	pott=pot.pow(w);
	sum=pott.sum;
	out=rand.size.collect({|j|
	if (i%pot[j]==0, {rand[j]=1.0.rand*(pot[j].pow(w))});
	});
	[(rand.sum).linlin(0, sum, 0.0, 1.0), rand]
	}
	*/
	/*
	asTimeString {


	}
	*/
	asBeats {arg beats=4, division=4, subdivision=4, resolution=128, beatsOffset=0;
		var bar=1, beat=1, sub=1, rest=1, frac;
		bar=(this/beats).floor.asInteger+1;
		beat=(this%beats).floor.asInteger+1;
		frac=(this.frac*subdivision);
		sub=(frac).asInteger+1;
		rest=(((frac+1-sub))*resolution).asInteger+1;
		^[bar, beat, sub, rest]
		//^(bar.asString++" " ++ beat ++ " " ++ sub ++ " "++ rest)
	}
	asBeatsString {arg beats=4, division=4, subdivision=4, resolution=128, beatsOffset=0;
		var b=this.asBeats(beats, division, subdivision, resolution, beatsOffset);
		/*
		var bar=1, beat=1, sub=1, rest=1, frac;
		bar=(this/beats).floor.asInteger+1;
		beat=(this%beats).floor.asInteger+1;
		frac=(this.frac*subdivision);
		sub=(frac).asInteger+1;
		rest=(((frac+1-sub))*resolution).asInteger+1;
		*/
		^(b[0].asString++" " ++ b[1] ++ " " ++ b[2] ++ " "++ b[3])
	}
	subfactorial {
		^(this.factorial/1.exp+0.5).floor.asInteger
	}

	decimals {
		^this.asString.split($.)[1].size
	}

	thetaToAz {
		^(this.neg+0.5pi).wrap(-pi,pi)/pi
	}

	aztoTheta {
		^(0.5-this).wrap(-1.0,1.0)*pi
	}

	thetaToAzNoWrap {
		^(this.neg+0.5pi)/pi
	}

	aztoThetaNoWrap {
		^(0.5-this)*pi
	}

	//Array.geom(size, start, grow)
	geomToDuration {arg start=1.0, grow=1.0;
		^if (grow==1, {this*start},{
			((grow.pow(this)-1/(grow-1))*start);
		})
	}

	geomLast {arg start=1.0, grow=1.1;
		^(grow.pow(this-1)*start)
	}

	geomSizeToDuration{arg start=1.0, grow=1.1; ^this.geomToDuration(start, grow)}

	geomEndToSize {arg start=1.0, grow=1.0, max=1000, minValue=0.0001;
		var out, geom;
		out=(((this*(grow-1))/start+1).log/grow.log);
		if (out.asInteger<0, {out=max});
		if (grow<1.0, {
			geom=(minValue/start).log/grow.log;
			if (out>geom, {out=geom});
		});
		^out
	}
	geomDurToSize {arg start=1.0, grow=0.9, max=1000, minValue=0.001;
		var size=0, ar=[start];
		while({(ar.sum<this)&&(size<max)&&(ar.minItem>minValue)},{
			size=size+1;
			ar=Array.geom(size, start, grow);
		});
		^(size-1)
	}
	geomEndToGrow {arg start=1.1, size=16, maxIterations=12000;
		var deltaTime=this, factor;
		var i=0, grow=1.0, dur=0.0, factor2;

		factor=Array.geom(size, start, grow).sum/deltaTime;
		if (factor>1, {factor=factor.reciprocal});

		factor2=factor;

		while ({ ((deltaTime-dur).abs>0)&&(i<maxIterations)},{
			if (dur>deltaTime, {grow=grow*factor2;},{grow=grow*(1/factor2);});
			dur=Array.geom(size, start, grow).sum;//.integrate.last;
			i=i+1;
			factor2=factor.pow(1/i)
		});
		^grow
	}

	geomDurStartEndToSize {arg start=0.1, end=0.2;
		var n=(this/([start,end].mean)).asInteger, sum=0;
		while({sum<this},{
			n=n+1;
			sum=Env([start, end],[1],\exp).discretize2(n).sum
		});
		^n
	}

	geomDurStartEndToSizeGrow {arg start=0.1, end=0.2;
		var n=this.geomDurStartEndToSize(start, end);
		var grow=this.geomEndToGrow(start, n, 120000);
		^[n,grow]
	}

	growToCurve{
		^(this.log*16)
	}

	curveToGrow{
		^((this/16).exp)
	}

	geomSumArgs {arg start=0.1, grow=0.9, adjust=\start, max=1000, minValue=0.0001;
		var size=this.geomEndToSize(start, grow, max, minValue).round(1.0).asInteger;
		switch(adjust, \grow,
			{grow=this.geomEndToGrow(start, size)}
			, \start,
			{start=(this/Array.geom(size, start, grow).sum)*start}
		);
		^[size.asInteger, start, grow]
	}

	nearestPrime{
		var next=this.nextPrime, prev=this.prevPrime;
		^if ((this-prev)<(next-this), {prev},{next})
	}

	cpsbin {arg bufferSize=2048, sr;
		var sampleRate;
		sampleRate=sr??{Server.default.sampleRate?44100};
		^((this/(sampleRate*0.5)*(bufferSize)).clip(0,bufferSize-1))
	}

	bincps {arg bufferSize=2048;
		var sampleRate=Server.default.sampleRate?44100;
		^(((sampleRate*0.5/bufferSize)*this).clip(0, sampleRate*0.5))
	}

	binDelaymaxdelay{arg hop=0.5;
		var sampleRate=Server.default.sampleRate?44100;
		^(this*(512-12)/sampleRate*hop)
	}

	keyToDegree2 {arg mode=[0,2,4,5,7,9,11], stepsPerOctave=12;
		var steps=0;
		//		while( {this<(mode.minItem-(steps*stepsPerOctave)) },{steps=steps+1});
		//		^this.keyToDegree(mode-(steps*stepsPerOctave))-(steps*mode.size);


		while({(steps*stepsPerOctave+this)<mode.minItem},{steps=steps+1});
		while({(steps*stepsPerOctave+this)>mode.maxItem},{steps=steps-1});
		^mode.indexOf(this+(steps*stepsPerOctave))-(steps*mode.size)


	}

	chooseRangeMod {arg min=60, max=72, modulo=12;
		var list;
		if (max-min<modulo, {max=min+modulo});
		list=(min..max);
		^list[(list%modulo).indicesOfEqual(this%modulo).choose]
	}

	azpan2 {
		^(this.fold2(0.5)*2)
	}

	pan2az {
		^(this.wrap2(2.0)*0.5)
	}

	azToIndex {arg numChannels=2;
		^(this+numChannels.reciprocal*(numChannels/2))%numChannels
	}

	azToBusAndPan2 {arg numChannels=2, outBus=0;
		var factor, out;
		if (numChannels>2, {
			factor=(this+numChannels.reciprocal*(numChannels*0.5))%numChannels;
			out=[outBus+factor.asInteger, factor.frac.linlin(0.0, 1.0, -1.0, 1.0)];
		},{
			out=[outBus, (this*2).fold2(1.0)]
		});
		^out
	}
	azToBussesAndPan2 {arg numChannels=2, outBus=0;
		var factor=(this+numChannels.reciprocal*(numChannels*0.5))%numChannels;
		var out;
		out=outBus+factor.asInteger;
		^[out, (out+1).wrap(outBus, outBus+numChannels-1), factor.frac.linlin(0.0, 1.0, -1.0, 1.0)]
	}
	azToBussesAndAmps {arg numChannels=2, totalNumChannels=4, outBus=0, compensate=1.0, orientation;
		var az= this;
		var factor, amps, outBusses, az2;
		factor=(az+totalNumChannels.reciprocal*(totalNumChannels/2))%totalNumChannels;
		az2=factor.frac.linlin(0, 1.0, numChannels.reciprocal.neg, numChannels.reciprocal);
		amps=az2.azToAmps2(numChannels, compensate, orientation);
		outBusses=(0..(totalNumChannels-1)).rotate(factor.asInteger.neg);
		outBusses=outBusses.copyRange(0, numChannels-2)++outBusses.last;
		^[outBusses+outBus, amps]
	}
	azToBussesAndAmps2 {arg numChannels=4, outBus=0, compensate=1.0, orientation;
		var az= this;
		var factor, amps, outBusses, az2, rotation, rotationAmps;

		//rotation=(numChannels*this).asInteger.neg;
		rotation=(numChannels*this*0.5).round(1.0).asInteger.neg;
		//rotationAmps=(rotation*0.5).abs.round(1.0).asInteger*(if (rotation>0.0, {1},{-1}));

		rotationAmps=rotation;


		amps=az.azToAmps(numChannels);
		^[(0..numChannels-1).rotate(rotation).copyRange(0,1)+outBus, amps.rotate(rotationAmps).copyRange(0,1)]
	}
	azToAmps2 {arg numChannels=4, compensate=1.0, orientation;
		^((this*pi-numChannels.asAzimuthArray(orientation)).abs/pi*(0.5*pi)).wrap2(0.5pi).abs.cos*((numChannels*0.5).pow(-0.5*compensate))
	}

	azToAmps {arg numChannels=2, width=0.0, compensate=false, orientation;
		var out;
		out=(1-(( ((this-(numChannels.asAzimuthArray/pi)).abs).wrap(-1.0, 1.0).abs*(numChannels*0.5))*(1-width))
		).clip(0.0, 1.0).normalizeSum.sqrt;
		^out
	}

	asAzimuthArray {arg orientation;
		orientation=orientation??{0.5+(0.5*this.odd.binaryValue)};
		^((0..(this-1))-orientation).wrap2((this/2))/this*2*pi
	}

	harmonicChord {arg partials=[1,2,3,4], round=1.0, distort=1.0, range=[16,136];
		var indexLow, indexHigh, chord, removeFundamental=false;
		if (partials.size==0, {partials=(1..partials)});

		//hier nog inbouwen dat wanneer de '1' ontbreekt dat deze tijdelijk wordt toegevoegd en daarna weer wordt verwijderd

		if (partials.asInteger.includes(1).not, {"nee".postln; removeFundamental=true; partials=[1]++partials});
		chord=(this.midicps*partials).cpsmidi.distortchord(distort, round);
		if (removeFundamental, {chord.removeAt(0)});
		indexLow=chord.indexInBetween(range[0]).round(1.0).asInteger;
		indexHigh=chord.indexInBetween(range[1]).round(1.0).asInteger;
		^chord.copyRange(indexLow,indexHigh)
	}

	splitter {arg splitPoints=[60];
		^this.asArray.splitter(splitPoints)
	}

	split {arg splitPoints=[60];
		^this.asArray.split(splitPoints)
	}

	findIndex {arg ranges=[[0,30],[24,49],[45,66]], prev=0;
		var that=this.clip(ranges.deepCopy.flat.minItem+1, ranges.deepCopy.flat.maxItem-1);
		prev=prev.clip(0, ranges.size-1);
		while(
			{((that>ranges[prev].minItem)&&(that<ranges[prev].maxItem)).not},{
				if (that<=ranges[prev].minItem, {prev=prev-1}, {prev=prev+1});
				prev
		});
		^prev
	}

	reverseCompensation {arg rate=1, duration=1.0;
		var position, time;
		position = rate.abs*duration + this;
		time=duration.neg;
		^[position, time]//startPos in seconds the buffer, offset starttime
	}
	/*
	-10.findIndex

	[[0,30],[24,49],[45,66]].flat.minItem
	*/

	binaryIterations {arg iterations=1;
		var that=this.copy;
		iterations.do{
			that=that.asArray++(that+1%2);
			that=that.flatten;
		};
		^that
	}



	boermanIterations {arg iterations=2, ratio=0.61803398874989, variant=0;
		var x=this.copy;

		ratio=[ratio, 1-ratio];
		x=x*ratio;

		if (variant==1, {
			(iterations-1).do{|depth|
				var index=0;
				var bins;
				depth=depth+1;
				bins=(depth%2).binaryIterations(depth);
				x=x.deepCollect(depth+1, {|item,i,r|
					var ratios=ratio.copy;
					if (bins[index]==1, {
						ratios=ratios.reverse
					});
					index=index+1;
					item*ratios
				});
				//x.postln;
			};
		},{
			(iterations-1).do{|depth|
				var index=0;
				var bins;
				ratio=ratio.reverse;
				depth=depth+1;
				bins=(depth%2).binaryIterations(depth);
				x=x.deepCollect(depth+1, {|item,i,r|
					var ratios=ratio.copy;
					//if (bins[index]==1, {ratios=ratios.reverse});
					index=index+1;
					item*ratios
				});
				//x.postln;
			};

		});
		^x
	}


	erb {//equivalent rectangular bandwidth => critical band
		^(24.7 * ((4.37*this) / 1000 + 1))
	}

	ierb {
		^((1000*this - 24700) / 107.939)
	}

	cpsmel {
		^(this/700+1).log10*2595
	}


	coefcps {arg server=Server.default;
		var sr=server.sampleRate??{44100};
		^( log(this)*sr/2pi.neg )
	}

	cpscoef {arg server=Server.default;
		var sr=server.sampleRate??{44100};
		^(exp(-2pi * (this / sr)))
	}

}