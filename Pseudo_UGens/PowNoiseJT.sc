PowNoiseJT {

*ar{arg pow=1, coef=0.0;

		var clip, drive;
		clip=pow.max(1.0).pow(-0.1);
		drive=coef.lincurve(0.0, 0.995, 1, 15, 16);

		^OnePole.ar( (WhiteNoise.ar(1.0).pow(pow)*pow.abs.pow(0.414)*0.1).clip2( clip ) * clip.reciprocal * drive, coef)
	}


}