/*
inBusA kan zijn: 1: Bus, 2: [Bus, Bus, Bus], 3: [0,1,5,6,9,10], 4: [1,3,5,[6,7,8,9]]
*/
ConcatJTSystem {
	var <path, <target, <threaded;
	var <synthDef, <synth, <server, <inBus, <inBusA, <inBusFFT, <id, <outBus;
	var <>pathArray, <>fileNameArray, <>fileName="test";
	var <cmdName, <bufRecording, <bufData, <dataSize;
	var <bufKDTree, <oscF, <isNormalized;
	var <synthPlayer, <synthRecorder, <recTime, <maxRecTime, <counter;
	var <specs, <descriptors, <analyzer;
	var <gui;

	makePath {
		if (path==nil, {path=thisProcess.platform.recordingsDir;});
		if ((PathName(path).fileNameWithoutExtension==nil)
			|| (PathName(path).fileNameWithoutExtension==""), {
			//fileName
		},{
			//fileName=PathName(path).fileNameWithoutExtension
		});
		if (File.exists(path.dirname).not, {path.dirname.mkdir;});
		if (File.exists(path++"NN/").not, {(path++"NN/").mkdir});
	}

	initSettings {arg initFlag=false;
		this.convertinBusA;
		dataSize=inBusA.size+1;
		this.makePath;
		server=target.server;
		threaded=thisProcess.mainThread.state>3;
	}

	makeNormalized {arg analyzer, descriptors, replace=false;
		AnalyzerJTNormalizer(analyzer, descriptors, replace)
	}

	convertinBusA {
		inBusA=switch (inBusA.class, Bus, {
			(inBusA.index..(inBusA.index+inBus.numChannels-1))
		}, Array, {
			inBusA.flat.collect{|bus|
				if (bus.class==Bus, {
					(bus.index..(bus.index+inBus.numChannels-1))
				},{
					bus
				})
			}.flat
		}
		);
	}

	getFileNames {
		fileNameArray=[];
		pathArray=[];
		PathName(path.dirname).entries.do{|p|
			if (p.isFile, {
				fileNameArray=fileNameArray.add(p.fileNameWithoutExtension);
				pathArray=pathArray.add(p.fullPath);
			});
		};
		if (fileNameArray.includesEqual(fileName), {

		});
		fileName=if (path.isFolder, {
			fileNameArray[0];
		},{
			path.fileNameWithoutExtension;
		});
	}
}