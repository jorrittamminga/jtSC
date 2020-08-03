Server.killAll

(
s.waitForBoot{
i=InJT([0], [s]);
i.addPlugin(\Meter);
	i[0].addPlugin(\EQ, [\threeBand]);
i.makeGUI;
}
)

i[0].plugins[\EQ].args


(
t=Server.internalNew("live",makeWindow:false);
u=Server.internalNew("synth",makeWindow:false);
//s.waitForBoot{x=InJT2([0,1],s,[\mic1,\mic2])}
u.waitForBoot{t.waitForBoot{s.waitForBoot{x=InJT([[0],[2,4,5,6],7,8,9],[s, [t, u], t,[s,u],u],[\a,\b,\c,\d,\e,\f,\g,\h,\i,\j])}}}
)
x.addPlugin(\Meter);
x.plugins[\Meter].makeGUI;
w=Window.new.front; w.alwaysOnTop_(true); w.addFlowLayout; c=8.collect{|i| var c=CompositeView(w, 40@300); c.addFlowLayout; c.background_(Color.black); c};
w.onClose_({arg what; what.findWindow.postln});
//x.plugins[\Meter].makeGUI(c);
x[\a].mute_(false)

x[\a].addPlugin(\Compressor, [\SoftKneeCompressor]);
x[\a].plugins[\Compressor].makeGUI.addPresetSystem(folderName:"/Compressor/a/");
x[\a].addPlugin(\EQ, [\HP]);
x[\a].plugins[\EQ].makeGUI(showScope:false)


x.addPlugin(\Recorder);
x.plugins[\Recorder].startRecording
x.plugins[\Recorder].stopRecording

x.free

s.queryAllNodes
t.queryAllNodes
u.queryAllNodes

(
t=Server.internalNew("live",makeWindow:false);
u=Server.internalNew("synth",makeWindow:false);
u.waitForBoot{t.waitForBoot{s.waitForBoot{
	a={ {PinkNoise.ar(0.1)}!5 }.play;
	b={ Out.ar(0, PinkNoise.ar(0.1)) }.play(t);
//	c={ Out.ar(4, PinkNoise.ar(0.1)) }.play(u);
	y=OutJT2(
	[ [0,1,2,4] ], [ [s,t,u] ]);
	y.postln;
}}}
)
y.addPlugin(\Meter);
y.plugins[\Meter].makeGUI;
y[0].mute_(true);
y[0].mute_(false);
y.mute_(true);
y.mute=false;
y.amp_(0.1)
y.db_(0)
(
y.free;
a.free;
b.free;
c.free;
)

s.waitForBoot{r=RecorderJT2([0,2]+s.options.numOutputBusChannels,s)};
r.makeGUI(nil, 200@20);
r.gui.window.close
r.startRecording
r.pauseRecording(true)
r.pauseRecording(false)
r.stopRecording
r.free
//----------------------------------------------------------------------
(
x.optimizeSynthAndBusForMeter;
m=MeterJT2(x.busForMeter, x.synthForMeter);
)

m.makeGUI
m.free
x.free
x.busForMeter
m.inBus.collect{|b| m.busIndexPerServer.flat.indexOfEqual(b)}
m.busIndexPerServer.flat.collect{|b| m.inBus.flat.indexOfEqual(b)}
m.inBusFlat
m.inBus

x={Out.ar(100, SoundIn.ar([0,2]))}.play
m=MeterJT2([100,101].collect{|i| i.asBus('audio')}, x)
m.makeGUI
m.free

m.gui.close

x.busPerFlatIndex
x[\a].bus
x.bus
m.makeGUI
m.inBus
m.busIndexPerServer
m.inBusFlat
m.cmdName
m.numberOfMeters
m.numberOfMetersPerServer


u.waitForBoot{t.waitForBoot{s.waitForBoot{m=MeterJT2([10,[16,13],18],[[s,t],u,[s,t,u]])}}}
m.busIndexPerServer
m.inBusFlat

//soundIn
u.waitForBoot{t.waitForBoot{s.waitForBoot{m=MeterJT2([10,[16,13],18],[s,t,u])}}}
m.busIndexPerServer
m.inBusFlat

EZSlider(nil, 20@350, \amp, \amp.asSpec, layout: 'vert')

x.free;

[s, [s, t], t,[s,t],s].rank
[[s, t], t,[s,t],s].rank
[1,2,3].rank

x=[1,[1,2],2,[3,4,5]];

x.deepCollect(x.maxRank, {|i| i*2})

maxRank



x.busPerServer
s.queryAllNodes
x.server
x.servers
x.inBus
x.bus
x.target
x.group
x.synth
x.label
x.inBusFlat
x.busPerServer
x.busIndexPerServer
x.busPerFlatIndex
x.synthPerFlatIndex[8]
x.singles
x[\h].synth
x[4].bus
Group(s, \addBefore)
u.queryAllNodes


InJT
inBus=[[0,1,2,3],[4,5,6],7,8,9];
target=[s, [s, t], u,[s,t],s]
//makeLabel {}
//convertBus {arg makeBus=true}
s: Bus.audio(s, 9) (namelijk [0,1,2,3]++[4,5,6]++8++9)
t: Bus.audio(t, 4) (namelijk [4,5,6]++8)
u: Bus.audio(u, 1) (namelijk 7)
bus=[Bus.audio(s,9), Bus.audio(t,4), Bus.audio(u,1)];
//_____linkLabelToBus
//_____linkLabelToServer
//inBus=inBus.flat;
//linkLabelToSynth


inBus=[[0,1]]
target=[[s,t]]
bus=[Bus.audio(s,2), Bus.audio(t,2)];

inBus=[0,1]
target=[s,t]
bus=[Bus.audio(s,1), Bus.audio(t,1)];


OutJT
outBus=[0,1,2,3,4];
target=s;
bus=Bus.audio(s, 5);

outBus=[[0,1,2,3,4]];
target=[[s,t]];
bus=[Bus.audio(s, 5), Bus.audio(t,5)]

i=[0,1];
t=s;

{[]}!1


inBus: [ 0, 1, 2, 3 ]
i: 0
serverIndex: [ 0 ]
indexFlat 4
[ [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], -1, -1, -1, -1, -1, -1 ]
[ [ Bus(audio, 0, 1, localhost) ], [ Bus(audio, 1, 1, localhost) ], [ Bus(audio, 2, 1, localhost) ], [ Bus(audio, 3, 1, localhost) ], -1, -1, -1, -1, -1, -1 ]
inBus: [ 4, 5, 6 ]
i: 1
serverIndex: [ 1, 2 ]
indexFlat 7
[ [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], [ Synth('InJT0' : 1001) ], [ Synth('InJT1' : 1001), Synth('InJT2' : 1001) ], [ Synth('InJT1' : 1001), Synth('InJT2' : 1001) ], [ Synth('InJT1' : 1001), Synth('InJT2' : 1001) ], -1, -1, -1 ]
[ [ Bus(audio, 0, 1, localhost) ], [ Bus(audio, 1, 1, localhost) ], [ Bus(audio, 2, 1, localhost) ], [ Bus(audio, 3, 1, localhost) ], [ Bus(audio, 4, 1, live), Bus(audio, 4, 1, synth) ], [ Bus(audio, 5, 1, live), Bus(audio, 5, 1, synth) ], [ Bus(audio, 6, 1, live), Bus(audio, 6, 1, synth) ], -1, -1, -1 ]
inBus: 7
i: 2
serverIndex: [ 1 ]




0.asArray.includes(1)



{1000000.do{ 3.do{|i| i*2+3+1 }}}.bench
{x=[1,2,3]; 1000000.do{ 3.do{|i| x[i] }}}.bench


f={arg x; x+1}
z={arg x; x*10};

f=f.addFunc(z);
f.value(1)
f.removeFunc(z)
f.value(1)

