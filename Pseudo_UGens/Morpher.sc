
ConvolutionMorph {

	*ar {
		arg in1, in2, morph=0.0, framesize=4096, smear=24, smooth=0.9, smearCurve=0.0, smoothCurve= -4.0, boost=2.0, tr=50, slopeAbove=0.5;

		var fft1, fft2, fft3, fft4, fft5, fft6, fft7, fft8, out, ws=framesize;
		var freqPeak, ampPeak;
		var smear1, smear2, smooth1, smooth2, morph1, morph2, morphtotal;
		var env=EnvGen.kr(Env.dadsr(framesize/SampleRate.ir, framesize/SampleRate.ir, 1.0, 1.0, 1.0), 1);

		morph=morph.lag(0.1);

		smear1=morph.lincurve(0.0, 0.5, smear, 0, smearCurve);
		smear2=morph.lincurve(0.5, 1.0, 0, smear, smearCurve.neg);

		smooth1=morph.lincurve(0.0, 0.5, smooth, 0, smoothCurve.neg);
		smooth2=morph.lincurve(0.5, 1.0, 0, smooth, smoothCurve);

		boost=morph.fold(0.0, 0.5).linexp(0.0, 0.5, 1.0, boost);

		fft1=FFT(LocalBuf(ws), in1);
		fft2=FFT(LocalBuf(ws), in2);
		fft3=FFT(LocalBuf(ws), in1);//PV_Copy
		fft4=FFT(LocalBuf(ws), in2);//PV_Copy
		fft5=FFT(LocalBuf(ws), in1);//PV_Copy
		fft6=FFT(LocalBuf(ws), in2);//PV_Copy
		fft7=FFT(LocalBuf(ws), in1);//PV_Copy
		fft8=FFT(LocalBuf(ws), in2);//PV_Copy

		smear1=morph.linlin(1/3, 0.5, smear, 0);
		smear2=morph.linlin(0.5, 2/3, 0, smear);

		fft3=PV_MagSmear(fft3, smear2);
		fft4=PV_MagSmear(fft4, smear1);

		fft3=PV_MagSmear(fft3, smooth2);
		fft4=PV_MagSmear(fft4, smooth1);

		fft5=PV_MagMul(fft5, fft4);//hier weer de wortel oid van!
		fft6=PV_MagMul(fft6, fft3);//hier weer de wortel oid van!

//		fft5=PV_MagClip(fft5, tr);
//		fft6=PV_MagClip(fft6, tr);

		morph1=morph.linlin(0.0, 1/3, 0.0, 1.0);
		morph2=morph.linlin(2/3, 1.0, 1.0, 0.0);
		morphtotal=morph.linlin(1/3, 2/3, 0.0, 1.0);

		fft7=PV_Morph(fft1, fft5, morph1);
		fft8=PV_Morph(fft2, fft6, morph2);
		fft7=PV_Morph(fft7, fft8, morphtotal);

//		fft7=PV_Compander(fft7, tr, 1.0, slopeAbove);

//		#freqPeak, ampPeak=FFTPeak.kr(fft7, 20.0, 19000.0);
//		freqPeak.lag3(3.0).poll(fft7);

		out=IFFT(fft7);

		//out=BPeakEQ.ar(out, freqPeak.lag3(2.0), 1.0, morph.fold2(0.5).linlin(0.0,0.5, 0.0, -24.0));

		^out*boost*env
	}
}

/*
PV_Morpher : PV_ChainUGen {

*new {
arg fft1, fft2, morph=0.0, smear=24, smearCurve=0.0;

var fft3, fft4, fft5, fft6, fft7, fft8, out;
var smear1, smear2, morph1, morph2, morphtotal;
//var env=EnvGen.kr(Env.dadsr(framesize/SampleRate.ir, framesize/SampleRate.ir, 1.0, 1.0, 1.0), 1);

smear1=morph.lincurve(0.0, 0.5, smear, 0, smearCurve);
smear2=morph.lincurve(0.5, 1.0, 0, smear, smearCurve.neg);

fft3=PV_Copy(fft1);
fft4=PV_Copy(fft2);
fft5=PV_Copy(fft1);
fft6=PV_Copy(fft2);
fft7=PV_Copy(fft1);
fft8=PV_Copy(fft2);

smear1=morph.linlin(1/3, 0.5, smear, 0);
smear2=morph.linlin(0.5, 2/3, 0, smear);
fft3=PV_MagSmear(fft3, smear2);
fft4=PV_MagSmear(fft4, smear1);

fft5=PV_MagMul(fft5, fft4);
fft6=PV_MagMul(fft6, fft3);

morph1=morph.linlin(0.0, 1/3, 0.0, 1.0);
morph2=morph.linlin(2/3, 1.0, 1.0, 0.0);
morphtotal=morph.linlin(1/3, 2/3, 0.0, 1.0);

fft7=PV_Morph(fft1, fft5, morph1);
fft8=PV_Morph(fft2, fft6, morph2);
fft7=PV_Morph(fft7, fft8, morphtotal);

//^fft7
^this.multiNew('control', fft7)
}
}

*/

Morph {

	*ar {
		arg in1, in2, morph=0.0, framesize=4096, lagTime=5.0, freqMin=100, freqMax=4000, smear=1, fade= -2, smooth= -1.0, freqDepth=0.0;
		var hop=0.5;
		var smear1=10, smear2=10, smooth1=0.0, smooth2=0, fade1=0.0, fade2=0.0, freqDepth1=0, freqDepth2=0.0;

		var in3, in4, in5, in6, freq, mag;
		var chain1, chain2, chain3, chain4, chain5, chain6, chain7, chain8;
		var value, value2, value3, value4, value5, value6;
		var ws=framesize;
		var ws2=ws/2;
		var bufSize=4096, bufSize1=bufSize-1;
		var freq1,freq2,hasFreq1,hasFreq2;
		var in1FFT, in2FFT;
		var ps1, ps2, ps;
		var fadeTable, smearTable, smoothTable, freqDepthTable, morphTable1, morphTable2;

		fadeTable=LocalBuf.newFrom(Env([0,1],[bufSize],-2.0).discretize(bufSize));
		smearTable=LocalBuf.newFrom(Env([1,0.02*ws],[bufSize],1.0).discretize(bufSize));
		smoothTable=LocalBuf.newFrom(Env([0.0, 0.99],[bufSize], -1.0).discretize(bufSize));
		freqDepthTable=LocalBuf.newFrom(Env([0.0, 1.0],[bufSize], 0.0).discretize(bufSize));
		morphTable2=LocalBuf.newFrom(Env([0,0.5,1],[0.5,0.5],[6,-6]).discretize(bufSize));

		morph=morph.lag(0.1);
		value2=(morph*2).fold(0,1)*bufSize1;
		value3=(morph*2).clip(1,2)-1*bufSize1;
		value4=(1-morph*2-1).clip(0,1)*bufSize1;
		value5=(morph*2).clip(0,1)*bufSize1;
		value6=(1-morph*2).clip(0,1)*bufSize1;
		value=morph*bufSize1;

		fade1=BufRd.kr(1, fadeTable, bufSize1-value4, 1, 2);
		fade2=BufRd.kr(1, fadeTable, bufSize1-value3, 1, 2);
		smear1=BufRd.kr(1, smearTable, value3, 1, 2);
		smear2=BufRd.kr(1, smearTable, value4, 1, 2);
		smooth1=BufRd.kr(1, smoothTable, value3, 1, 2);
		smooth2=BufRd.kr(1, smoothTable, value4, 1, 2);
		freqDepth1=BufRd.kr(1, freqDepthTable, value5, 1, 2);
		freqDepth2=BufRd.kr(1, freqDepthTable, value6, 1, 2);
		morph=BufRd.kr(1, morphTable2, value, 1, 2);

		#freq1,hasFreq1=Pitch.kr(in1, freqMin, freqMin, freqMax);
		#freq2,hasFreq2=Pitch.kr(in2, freqMin, freqMin, freqMax);

		ps1=(freq2-freq1).lag3(lagTime)*freqDepth1;
		ps2=(freq1-freq2).lag3(lagTime)*freqDepth2;

		in5=in1;
		in6=in2;

		in1=FreqShift.ar(in1, ps1);
		in2=FreqShift.ar(in2, ps2);

		in1FFT=in1; //BHiShelf.ar(in1, hfreq, hrs, hdb)*env*boost1;
		in2FFT=in2; //BHiShelf.ar(in2, hfreq, hrs, hdb)*env*boost2;

		chain1=FFT(LocalBuf(ws), in1FFT, hop);
		chain2=FFT(LocalBuf(ws), in2FFT, hop);
		chain3=FFT(LocalBuf(ws), in1, hop);
		chain4=FFT(LocalBuf(ws), in2, hop);
		chain5=FFT(LocalBuf(ws), in1FFT, hop);
		//		chain6=FFT(LocalBuf(ws), in1FFT, hop);
		//		chain7=FFT(LocalBuf(ws), in5, hop);
		//		chain8=FFT(LocalBuf(ws), in6, hop);

		chain1=PV_MagSmooth(chain1, smooth1);
		chain1=PV_MagSmear(chain1, smear1-1);
		//		chain1=PV_MagClip(chain1, 100);//threshLimiter1

		chain2=PV_MagSmooth(chain2, smooth2);
		chain2=PV_MagSmear(chain2, smear2-1);
		//		chain2=PV_MagClip(chain2, 100);//threshLimiter2

		chain5=PV_Copy(chain1, chain5);
		chain1=PV_MagMul(chain1, chain2);//convolution
		chain2=PV_MagMul(chain2, chain5);//convolution

		chain1=PV_Morph(chain3, chain1, fade1);
		chain2=PV_Morph(chain4, chain2, fade2);

		chain5=PV_Morph(chain1, chain2, morph);
		chain5=PV_Compander(chain5, 50, 1, 0.9);
		chain5=PV_MagClip(chain5, 100);

		^IFFT(chain5);
	}
}


MorphFFT {

	*ar {
		arg in1, in2, morph=0.0, framesize=4096, lagTime=5.0, freqMin=100, freqMax=4000, smear=1, fade= -2, smooth= -1.0, freqDepth=0.0;
		var hop=0.5;
		var smear1=10, smear2=10, smooth1=0.0, smooth2=0, fade1=0.0, fade2=0.0, freqDepth1=0, freqDepth2=0.0;

		var in3, in4, in5, in6, freq, mag;
		var chain1, chain2, chain3, chain4, chain5, chain6, chain7, chain8;
		var value, value2, value3, value4, value5, value6;
		var ws=framesize;
		var ws2=ws/2;
		var bufSize=4096, bufSize1=bufSize-1;
		var freq1,freq2,hasFreq1,hasFreq2;
		var in1FFT, in2FFT;
		var ps1, ps2, ps;
		var fadeTable, smearTable, smoothTable, freqDepthTable, morphTable1, morphTable2;

		fadeTable=LocalBuf.newFrom(Env([0,1],[bufSize],-2.0).discretize(bufSize));
		smearTable=LocalBuf.newFrom(Env([1,0.02*ws],[bufSize],1.0).discretize(bufSize));
		smoothTable=LocalBuf.newFrom(Env([0.0, 0.99],[bufSize], -1.0).discretize(bufSize));
		freqDepthTable=LocalBuf.newFrom(Env([0.0, 1.0],[bufSize], 0.0).discretize(bufSize));
		morphTable2=LocalBuf.newFrom(Env([0,0.5,1],[0.5,0.5],[6,-6]).discretize(bufSize));

		morph=morph.lag(0.1);
		value2=(morph*2).fold(0,1)*bufSize1;
		value3=(morph*2).clip(1,2)-1*bufSize1;
		value4=(1-morph*2-1).clip(0,1)*bufSize1;
		value5=(morph*2).clip(0,1)*bufSize1;
		value6=(1-morph*2).clip(0,1)*bufSize1;
		value=morph*bufSize1;

		fade1=BufRd.kr(1, fadeTable, bufSize1-value4, 1, 2);
		fade2=BufRd.kr(1, fadeTable, bufSize1-value3, 1, 2);
		smear1=BufRd.kr(1, smearTable, value3, 1, 2);
		smear2=BufRd.kr(1, smearTable, value4, 1, 2);
		smooth1=BufRd.kr(1, smoothTable, value3, 1, 2);
		smooth2=BufRd.kr(1, smoothTable, value4, 1, 2);
		freqDepth1=BufRd.kr(1, freqDepthTable, value5, 1, 2);
		freqDepth2=BufRd.kr(1, freqDepthTable, value6, 1, 2);
		morph=BufRd.kr(1, morphTable2, value, 1, 2);

		#freq1,hasFreq1=Pitch.kr(in1, freqMin, freqMin, freqMax);
		#freq2,hasFreq2=Pitch.kr(in2, freqMin, freqMin, freqMax);

		ps1=(freq2-freq1).lag3(lagTime)*freqDepth1;
		ps2=(freq1-freq2).lag3(lagTime)*freqDepth2;

		in5=in1;
		in6=in2;

		in1=FreqShift.ar(in1, ps1);
		in2=FreqShift.ar(in2, ps2);

		in1FFT=in1; //BHiShelf.ar(in1, hfreq, hrs, hdb)*env*boost1;
		in2FFT=in2; //BHiShelf.ar(in2, hfreq, hrs, hdb)*env*boost2;

		chain1=FFT(LocalBuf(ws), in1FFT, hop);
		chain2=FFT(LocalBuf(ws), in2FFT, hop);
		chain3=FFT(LocalBuf(ws), in1, hop);
		chain4=FFT(LocalBuf(ws), in2, hop);
		chain5=FFT(LocalBuf(ws), in1FFT, hop);
		//		chain6=FFT(LocalBuf(ws), in1FFT, hop);
		//		chain7=FFT(LocalBuf(ws), in5, hop);
		//		chain8=FFT(LocalBuf(ws), in6, hop);

		chain1=PV_MagSmooth(chain1, smooth1);
		chain1=PV_MagSmear(chain1, smear1-1);
		//		chain1=PV_MagClip(chain1, 100);//threshLimiter1

		chain2=PV_MagSmooth(chain2, smooth2);
		chain2=PV_MagSmear(chain2, smear2-1);
		//		chain2=PV_MagClip(chain2, 100);//threshLimiter2

		chain5=PV_Copy(chain1, chain5);
		chain1=PV_MagMul(chain1, chain2);//convolution
		chain2=PV_MagMul(chain2, chain5);//convolution

		chain1=PV_Morph(chain3, chain1, fade1);
		chain2=PV_Morph(chain4, chain2, fade2);

		chain5=PV_Morph(chain1, chain2, morph);
		chain5=PV_Compander(chain5, 50, 1, 0.9);
		^PV_MagClip(chain5, 100)
		//^IFFT(chain5);
	}
}