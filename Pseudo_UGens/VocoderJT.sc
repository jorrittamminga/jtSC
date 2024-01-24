VocoderJT {
	*ar { 	arg car, mod, num, low=100, high=5000, bw=1.0, at=0.01, rt=0.1, mul=1.0;

		var cFreqs, rq, amps, out;
		cFreqs=[low.cpsmidi, high.cpsmidi].resamp1(num);
		rq=((cFreqs[1]-cFreqs[0]).midiratio-1.0)*bw;
		cFreqs=cFreqs.midicps;
		amps=Amplitude.kr(Resonz.ar(mod, cFreqs, rq), at, rt);
		out=Resonz.ar(car, cFreqs, rq, amps).sum*rq.reciprocal.sqrt;

		^out

		/*
		out = Mix.arFill(( num + 1 ), { arg i;

			ratio = (( high / low)**num.reciprocal );

			cf =  ( ratio**i) * low;

			filtmod = BPF.ar( mod, cf, q);

			tracker = Amplitude.kr(filtmod);

			filtcar = BPF.ar( car, cf, q);


			( outscal * ( filtcar * tracker ));
		});

		hf = HPF.ar(HPF.ar( mod, hpf), hpf);


		^out + ( hpfscal * hf )
		*/

	}

}