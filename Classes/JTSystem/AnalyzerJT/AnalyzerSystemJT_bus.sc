+ AnalyzerSystemJT {

	initBusses {
		var index, min;
		outBusperDescriptor=();
		arguments=();
		if (hasOnsets && outFlag, {
			outBusT=Bus.control(server, 1);//trigger outBus, for onsets
			server.sync;
			arguments[\outBusT]=outBusT;
		});
		if (outFlag, {
			outBus=Bus.control(server, totalNumberOfOutputs);
			server.sync;
			arguments[\outBus]=outBus;
		});
		if (outFFTFlag, {
			min=outFFTs.minItem;
			outBusFFT=Bus.control(server, outFFTs.size);
			server.sync;
			outBusFFTperDescriptor=();
			descriptors.do{|key|
				if (outFFTperDescriptor[key]==nil, {
					outFFTperDescriptor[key]=min;
				});
				outBusFFTperDescriptor[key]=(outFFTperDescriptor[key]??{min})+outBusFFT.index;
			};
			arguments[\outBusFFT]=outBusFFT;
		});
		index=if (outBus==nil, {0},{outBus.index});
		descriptorsWithoutOnsets.do{|key|
			var noc=numberOfOutputs[key]??{1};
			outBusperDescriptor[key]=index.asBus('control', noc, server);
			index=index+noc;
		};
	}
}