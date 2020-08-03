GVerbAz{
*ar{arg numChans = 4, in, roomsize = 10, revtime = 3, damping = 0.5, inputbw =  0.5, spread = 15,
			drylevel = 1, earlyreflevel = 0.7, taillevel = 0.5, maxroomsize = 300, mul = 1,
			add = 0, spread2 = 1, level = 1, width = 2, center = 0.0, orientation = 0.5, levelComp = true;

		^SplayAz.ar(numChans, GVerb.ar(in, roomsize, revtime, damping, inputbw, spread,
			drylevel, earlyreflevel, taillevel, maxroomsize, mul,
			add), spread2, level, width, center, orientation, levelComp)
	}

}