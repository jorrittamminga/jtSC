FadeJT {
	*kr {arg attackTime=1.0, releaseTime=1.0, gate=1.0, doneAction=2, dBzero=80;
		^((Env.new([0, (dBzero + (1.0+dBzero.neg.dbamp).ampdb).squared, 0], [attackTime, releaseTime], 0.0, 1).kr(doneAction, gate).sqrt-dBzero).dbamp - (dBzero.neg.dbamp))
	}
}