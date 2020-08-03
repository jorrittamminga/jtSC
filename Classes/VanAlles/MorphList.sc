/*
x=EventMorph.new([(freq:100),(freq:200,amp:[0.1,0.4]),(amp:[0.2,0.3])], 7);
x=EventMorph.new([(freq:100),(freq:200,amp:[0.1,0.4]),(amp:[0.2,0.3])], 7);
x.ati(0.5)

x=EventMorph.new([(freq:100),(freq:200,amp:0.1),(amp:0.2)]);//1-D
e=EventMorph.new([[(freq:100),(freq:220),(freq:330)],[(freq:440),(freq:880),(freq:1100)],[(freq:440),(freq:880),(freq:1100)]]);//2-D

[ (), () ]//1-D, 2 presets
[ [(),()], [(),()] ]//2-D, 4 presets
[ [ [(),()], [(),()] ], [ [(),()], [(),()] ] ]//3-D, 8 presets

e.at([1,1])
x.at(0.05)
x.ati(1.05)
x.atn(0.05)
x.atin(0.05)
x.put([(freq:600,amp:0.05),(freq:200,amp:0.1,rate:5.0),(amp:0.2)]);

*/
EventMorph {
	var <list, <keys, <value, <size, <n, <controlSpecs, <rank, <shape;

	*new{arg list=[(freq:100),(freq:200)], n=8, controlSpecs=(), shape=\sine;
		^super.new.init(list,controlSpecs,n,shape)
		}

	init{arg arglist, argcontrolSpecs, argn, argshape;
		var tmpList=List[], hasEvent=false;

		n=argn;
		controlSpecs=argcontrolSpecs;
		shape=argshape;
		list=arglist;

		this.put(arglist, argcontrolSpecs, argn, argshape);

	}

	//read{}
	//write{}

	put {arg arglist, argcontrolSpecs, argn, argshape;
		var tmpList=List[], hasEvent=false;

		n=argn??{n};
		controlSpecs=argcontrolSpecs??{controlSpecs};
		shape=argshape??{shape};
		list=arglist;

		//hier nog aparte check voor de controlSpecs! hoeft niet per se in die interpolatecs nog eens te gebeuren
		/*
				if (controlSpec==nil, {
			controlSpec=ControlSpec.specs[key];
			if (controlSpec==nil, {controlSpec=ControlSpec(min, max)});
		});
		*/

		value=();

		if (list.flatten(list.rank)[0].class!=Event, {
			list=list.deepCollect(list.rank-1,{|i| var event=(); i.clump(2).do({|i| event[i[0]]=i[1]}); event;})//array-to-event
		});
		keys=list.flat.collect({|j| j.keys.asArray}).flat.asSet.asArray;

		rank=list.rank;
		size=list.size;

		keys.do({|key|
			var tmp;
			value[key]=list.deepCollect(list.rank, {|i| i[key] });
			value[key]=value[key].deepCollect(rank-1, {|i| i.resampnil});

			rank.do({|j|
				value[key]=value[key].deepCollect(j, {|i|
					if (i.flat.minItem.class.superclass==SimpleNumber, {
						i.interpolatecs(n, controlSpec:controlSpecs[key]);
					},{
						i.resamp0(i.size*n)
					})
				});
			});
			value[key];
		});
	}

//at is nog wat 'vies' en inefficient....
	at {arg index;
		var event=();
		index=index.asArray.copyRange(0, rank-1)*n;
		keys.do({|key|
			var tmp=value[key].deepCopy;
			index.do({|ind|
				tmp=tmp.clipAt((ind+0.5).floor);
			});
			event[key]=tmp
		});
		^event
	}


	ati {arg index;
		var event=();
		index=index.asArray.copyRange(0, rank-1)*n;
		keys.do({|key|
			event[key]=
			2.collect({|k|
				var tmp=value[key].deepCopy;
				index.do({|ind|
					tmp=tmp.clipAt(ind.floor+k);
				});
			tmp
			}).blendAt(index.frac.mean);
		});
		^event
	}


	atn {arg index;//normalized index i.e. 0..1
		var event=();
		index=index.asArray.clip(0,1);
		keys.do({|key|
			var tmp=value[key].deepCopy;
			index.do({|ind|
				tmp=tmp.clipAt((ind*tmp.size).floor)
			});
			event[key]=tmp;
		});
		^event
	}

	atin {arg index;//normalized index i.e. 0..1 and interpolation
		var event=();
		index=index.asArray.clip(0,1);
		(index*n).frac.mean;
		 //was index.size.collect
		keys.do({|key|
			event[key]=
			2.collect({|k|
				var tmp=value[key].deepCopy;
				index.do({|ind|
					tmp=tmp.clipAt((ind*tmp.size).floor+k)
				});
			tmp
			}).blendAt((index*n).frac.mean);
		});
		^event
	}


	atOSC {arg index;
		^this.at(index).asKeyValuePairs
	}

	atiOSC {arg index;
		^this.ati(index).asKeyValuePairs
	}

	atnOSC {arg index;
		^this.atn(index).asKeyValuePairs
	}

	atinOSC {arg index;
		^this.atn(index).asKeyValuePairs
	}

}

MorphEvent1D : EventMorph {}