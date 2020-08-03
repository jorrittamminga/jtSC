+ Env {

	resampForBlend {arg newSize;
		var factor = this.levels.size - 1 / (newSize - 1).max(1);
		var prevIndex= -1, new=true;
		var levels=this.levels.deepCopy
		, times=Array.fill(newSize-1, 0)
		, curves=this.curves.resamp0(newSize);

		levels=Array.fill(newSize, { |i|
			var index=(i * factor).round(1.0).asInteger;
			new=(index!=prevIndex);
			prevIndex=index;

			if ((i>0) && (index>0), {
				if (new, {
					times.put(i-1, this.times[index-1]);
					//curves.put(i-1, env.curves[index-1]);
				});
			});
			levels.at(index);
		});
		this.levels=levels;
		this.times=times;
		this.curves=curves;
	}
	/*
	blend2 { arg argAnotherEnv, argBlendFrac=0.5;
	var max1=levels.size.max(argAnotherEnv.levels.size);
	var max2=times.size.max(argAnotherEnv.times.size);
	var newCurves=curves.blend(argAnotherEnv.curves, argBlendFrac);

	newCurves;//dit moet natuurlijk beter!

	^this.class.new(
	levels.resamp1(max1).blend(argAnotherEnv.levels.resamp1(max1), argBlendFrac),
	times.resamp1(max2).blend(argAnotherEnv.times.resamp1(max2), argBlendFrac),
	newCurves,
	releaseNode,
	loopNode
	)
	}
	*/
	discretize2 {arg resolution=10, rescale=false;//number of values per second
		var array=this.discretize(this.times.asArray.sum*resolution).as(Array);
		//if (array.last!=this.levels.last, {array=array.add(this.levels.last)});
		array[0]=this.levels.first;
		array[array.size-1]=this.levels.last;
		if (rescale, {array=array.linlin(array.minItem, array.maxItem, this.levels.minItem, this.levels.maxItem)});
		^array

	}

	pairs {arg resolution=10, rescale=false, offset=0;
		var totalTime=this.times.asArray.sum, steps=(resolution*totalTime).ceil, stepSize=totalTime/steps;
		var times=(0, stepSize..totalTime), levels;
		levels=times.collect({|time| this[time]});
		if (rescale, {levels=levels.linlin(levels.minItem, levels.maxItem, this.levels.minItem, this.levels.maxItem)});
		^[times+offset, levels].flop
	}

	asTimesLevels {arg resolution=10, rescale=false, offset=0;
		^this.pairs(resolution, rescale, offset).flop

	}

	mean {arg n;
		n=n??{this.times.asArray.sum*750};
		^this.discretize(n).as(Array).mean
	}

	threshold {arg threshold=0.5;
		var levels, times=List[], low, high;
		var flag=true;
		var startTimes=[0]++this.times.integrate;
		this.times.collect{|time,i|
			var min,max, curve=this.curves.asArray.wrapAt(i);
			var last=(if (times.size>0, {times.last},{0}));
			min=this.levels[i];
			max=this.levels[i+1];
			time=ControlSpec(this.levels[i], this.levels[i+1]
				, this.curves.asArray.wrapAt(i)).unmap(threshold)*this.times[i];
			if (min>max, {#min,max=[min,max].swap(0,1)});
			if ( ((time-this.times[i]).abs>0.0000001) && (time>0), {
				times.add(startTimes[i]+time);
			});
		};
		low=times.deepCopy; high=times.deepCopy;
		if (this.levels.first<threshold, {low=[0]++low}, {high=[0]++high});
		if (this.levels.last<threshold, {low=low++[this.times.sum]}, {high=high++[this.times.sum]});
		low=low.asSet.asArray.sort;
		high=high.asSet.asArray.sort;
		if (low.size%2==1, {low.removeAt(low.size-1)});
		if (high.size%2==1, {high.removeAt(high.size-1)});
		^[low.clumps([2]), high.clumps([2])];
	}

	//---------------------------------------------- PITCH ENVELOPES
	//for 'pitch' envelopes with freqs as levels
	nextDuration {
		^this.prCalculateDuration(0)
	}

	prevDuration {
		^this.prCalculateDuration(-1)
	}

	prCalculateDuration {arg i=0;
		var t=0;
		var env=this.copy;
		env.levels=env.levels.reciprocal;
		t=env[0];
		while({t<env.duration},{t=t+env[t]});
		^(i*env.levels.last+t)
	}


}