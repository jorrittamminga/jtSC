(
var n=4, k=0;
var viewsP=(), w, v;
var bufWrJT, chunkerJT, analyzerJT, gaterJT, bufnumSlider=[];

w=Window("Chunks", Rect(0,0,630,20)).front;
w.addFlowLayout;
w.alwaysOnTop_(true);

s.waitForBoot{
	i=AutoIn(0);

	bufWrJT=BufWrJT(i.bus[0], i.synth[0], 20, false, false, true, false);
	bufWrJT.makeGUI;
	chunkerJT=ChunkerJT(bufWrJT, \copy);
	chunkerJT.makeGUI(bufWrJT.gui.compositeView);
	bufWrJT.startRecording;
	analyzerJT=AnalyzerJT(i.bus[0], i.synth[0], [\specflatness, \loudness]);
	gaterJT=GaterJT(analyzerJT, functions:{arg msg;
		if (msg[3]==1, {
			chunkerJT.startChunk
		},{
			chunkerJT.endChunk({
				y[k%n].set(\bufnum, chunkerJT.bufArray.last);
				y[k%n].run(true);
				y[k%n].set(\gate, 1, \bufnum, chunkerJT.bufArray.last);
				bufnumSlider.do{|slider| {
					slider.controlSpec.maxval= chunkerJT.bufArray.size-1}.defer};
				k=k+1;
				//bufnum.do(_.controlSpec.maxItem=k)
			});
		});
	});
	analyzerJT.makeGUI(freeOnClose:true, gaterJT:gaterJT);
	gaterJT.makeGUI(freeOnClose:true, analyzerJT:analyzerJT);
	while({gaterJT.gui.views[\holdTime]==nil},{0.001.wait});
	{
		gaterJT.gui.views[\holdTime].action=gaterJT.gui.views[\holdTime].action.addFunc{|ez|
			chunkerJT.latencyPost=(ez.value-0.05).max(0);
			{chunkerJT.gui.views[\latencyPost].value_(chunkerJT.latencyPost)}.defer;
		};
		viewsP=();
		gaterJT.gui.views.keysValuesDo{|key,view|
			if (key.asString.contains("threshold_"), {viewsP[key]=view})};
		[\minTime, \holdTime].do{|key| viewsP[key]=gaterJT.gui.views[key]};
		gaterJT.gui.addPresetSystem(viewsP
			, thisProcess.nowExecutingPath.dirname++"/presets/", "gater", 2);

	}.defer;

	SynthDef(\ChunkPlayer, {arg bufnum, startPos=0, endPos=1.0, outBus=0, az=0.0, amp=1.0, fadeIn=0.01, fadeOut=0.01, gate=1, rate=1, lagTime=0.1;
		var env=EnvGen.kr(Env.asr(fadeIn, 1, fadeOut), gate, doneAction:1), out;
		var trigger;//=TDuty.ar((endPos-startPos)*BufDur.kr(bufnum), Changed.kr(startPos+endPos+bufnum));
		//trigger=Impulse.kr(((endPos-startPos).abs*BufDur.kr(bufnum)).max(lagTime*2).reciprocal);

		trigger=TDuty.kr(
			((endPos-startPos).abs*BufDur.kr(bufnum)).max(lagTime*2)
			, Changed.kr(gate+bufnum)//+Changed.ar(K2A.ar(bufnum))
		);

		rate=((endPos-startPos)>0)*2-1*rate;

		out=PlayBufCF2.ar(1, bufnum, rate, trigger, startPos*BufFrames.kr(bufnum), 1, lagTime);
		Out.ar(outBus, PanAz.ar(2, out*amp*env, az))
	}, metadata: (specs: (startPos:ControlSpec(0, 1.0), endPos: ControlSpec(0.0, 1.0)
		, amp: \amp.asSpec, lagTime: ControlSpec(0.01, 1.0, \exp))
	)).add;
	s.sync;
	y=n.collect{
		Synth(\ChunkPlayer, [\bufnum, 0, \startPos, 0, \endPos, 1]).register
	};
	y.do{|synth|
		var guis;
		guis=synth.makeGUI(w, onClose:true, willHang:true);
		bufnumSlider=bufnumSlider.add(EZSlider(w, 350@20, \bufnum, ControlSpec(0,0,0,1), {|ez|
			synth.set(\bufnum, chunkerJT.bufArray[ez.value]);
		}));
	};
	y.do(_.set(\gate, 0));
	{
		w.rebounds
	}.defer;
};
)