+ ClusterJT {

	makeInitScoreFunction {
		^{arg p, f;
			var pp=(), score=List[], endTime=1, keys, maxDur, latencyT=0.00001;
			var addActionID, targetID;
			//---------------------------------------------------- inits
			addActionID = if (type==\render, {2},{addActions[addAction]});
			targetID = if (type==\render, {1},{ target.asTarget.nodeID});

			if (type!=\render, {latencyT=latency});

			p=p??{argsEvent.copy};
			f=f??{funcsEvent.copy};

			keys=p.keys;
			[\voices, \sampleRate, \numChannels, \loop, \sync, \sampleFormat
				, \headerFormat, \rate].do{|key|
				keys.remove(key)};

			if (type==\render, {keys.remove(\outBus)});
			keys=([\rate]++(keys.asArray.sort));
			//---------------------------------------------------- make values
			keys.do{|key|
				pp[key]=if (f[key].class==Function, {
					if (p[key].class==Function, {
						{|i| p[key].value(pp, i, p)}!p[\voices]
					},{
						{|i| f[key].value(p[key],pp,i, p)}!p[\voices]
					})
				},{

					if (p[key].class==Function, {
						{p[key].value(p)}!p[\voices]
					},{
						{p[key]}!p[\voices]
					})
				});
			};
			//---------------------------------------------------- ratescaling
			pp[\startFrame]=pp[\startFrame].collect{|startFrame,i|
				if (startFrame<=1.0, {
					startFrame=pp[\buffer][i].duration*startFrame
				});
				startFrame
			};
			pp[\dur]=pp[\dur].collect{|dur,i|
				var buf=pp[\buffer][i];
				if (dur<0, {
					dur=buf.duration*dur.abs;
				});
				dur=dur*(pp[\rate][i].pow(pp[\durRateScale][i]));
				if (p[\loop]==0, {
					dur=dur.min(
						(buf.numFrames-pp[\startFrame][i])/buf.sampleRate/pp[\rate][i]
					);
				});
				dur
			};
			pp[\sustainTime]=pp[\releaseTime].size.collect{|i|
				var sustainTime;
				pp[\releaseTime][i]=pp[\releaseTime][i].min(1.0);
				pp[\attackTime][i]=pp[\attackTime][i].min(1-pp[\releaseTime][i]);
				sustainTime=(1-pp[\attackTime][i]-pp[\releaseTime][i])*pp[\dur][i];
				pp[\releaseTime][i]=pp[\releaseTime][i]*pp[\dur][i];
				pp[\attackTime][i]=pp[\attackTime][i]*pp[\dur][i];
				sustainTime
			};
			pp[\amp]=pp[\rate].collect{|rate,i|
				rate.pow(pp[\ampRateScale][i])
			}*(p[\voices].reciprocal.sqrt);
			pp[\rate]=pp[\rate].collect{|rate,i|
				if (pp[\reverse][i].coin, {
					pp[\startFrame][i]=pp[\buffer][i].numFrames
					-pp[\startFrame][i]-2;//why -2?
					rate.neg
				},{rate})
			};

			//---------------------------------------------------- sync
			maxDur=pp[\dur].maxItem;
			pp[\startTime]=pp[\dur].collect{|dur,i|
				((maxDur-dur)*p[\sync])+(pp[\delayTime][i])
			};

			//---------------------------------------------------- convert buffers
			//only when rendering a score (instead of realtime playback)
			if (type==\render, {
				synthDef.do{|synthdef| score.add([0.0, ['/d_recv', synthdef.asBytes]])};
				pp[\buffer].asArray.asSet.asArray.do{|buf|
					score.add([0.0, [\b_allocRead, buf.bufnum, buf.path]]);
				};
			});
			pp[\buffer]=pp[\buffer].collect{|buffer, i|
				pp[\synthDef][i]=(pp[\synthDef][i].asString++buffer.numChannels).asSymbol;
				buffer.bufnum
			};

			//---------------------------------------------------- make score
			[\durRateScale, \ampRateScale, \sync, \reverse].do{|key| pp.removeAt(key)};

			p[\voices].do{|i|
				var msg=List[], startTime, duration;
				pp.keysValuesDo{|key,val|
					if (key==\startTime, {
						pp[\startTime][i]=pp[\startTime][i].max(0)+latencyT;
						startTime=pp[\startTime][i]
					});
					if (key==\dur, {duration=val[i]});
					if (key==\synthDef, {},{
						msg.add(key); msg.add(val[i] );
					});
				};
				if (startTime+duration>endTime, {endTime=startTime+duration});
				score.add([pp[\startTime][i],
					[\s_new, pp[\synthDef][i], -1
						, addActionID, target.nodeID
				] ++ msg.asOSCArgArray]);
			};
			score.add([endTime+0.01, [\c_set, 0, 0]]);
			score
			//^score
		};
	}
}