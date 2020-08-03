AnalyzerSystemJT : PluginJT {

	classvar <defaultsettings, <defaultfftsizes, <defaulthopsizes, <defaultcontrolSpecs
	, <defaultInputs, <makeUGens;

	var <osc;
	var gater, <analyzer;
	var <fftSizes, <hasOnsets, <numberOfOutputs, totalNumberOfOutputs;
	var <cmdNames;
	var <descriptors, <descriptorsWithoutOnsets;
	var <normalizers, <normalized;
	var <minInput;
	var <inputs, <hpzInputs;
	var <inputsFlag, metadataSpecs, sendreplyFlag;
	var <outBusT, <outBusFFT, <outBusFFTperDescriptor, <outBusperDescriptor;
	var outFFTFlag, outFFTs, outFFTperDescriptor;
	var outFlag, <outFFTperDescriptor;
	var arguments, index;

}