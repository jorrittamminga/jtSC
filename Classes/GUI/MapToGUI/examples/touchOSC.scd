(
s.waitForBoot{
	var n=NetAddr("192.168.178.234", 9000);
	w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
	b=Button(w, 300@200).states_([ [\on],[\ON, Color.black,Color.red]]).action_{|b| b.value};
	l=Button(w, 300@20).states_([["poep"]]).action_{arg l; "yo".post;l.postln};
	b.mapToOSC('/1/multitoggle1/1/1', netAddr: n);
	f=EZSlider(w, 350@20, \freq, \freq.asSpec, {|ez| ez.value.postln});
	f.mapToOSC('/1/fader1'
		, netAddr: n
	);
	l.mapToOSC('/1/label2', netAddr: n);
}
)

OSCFunc.trace

(
s.waitForBoot{
	var n=NetAddr("192.168.178.234", 9000);
	w=Window.new.front; w.addFlowLayout; w.alwaysOnTop_(true);
	f=EZSlider2D(w, 350@350, \test, ControlSpec(-1.0, 1.0), {|ez| ez.value.postln});
	f.mapToOSC('/1/multixy1/1');

}
)

f.action
EZSlider2D

l.states=[ ["test"] ]; l.action=1;

n=NetAddr("169.254.91.60", 9000);
n.sendMsg('/1/label2', "poepen")

/*
tutorial TouchOSC, iphone and macbook
https://www.youtube.com/watch?v=ZVTbRNu2BI0

1. in TouchOSC (on iphone/ipad):
connections: OSC: Enabled

2. in TouchOSC (on iphone/ipad):
connections: OSC: Host:
(macbooks ip adres which can be found in system preferences/Netwerk, in my case 192.168.178.24)
Port (outgoing): 57120 (in this case the langport of SuperCollider)
NetAddr.langPort

3. boot the server in SuperCollider
s.boot

4. test it with:
OSCFunc.trace

5. Cmd period to stop the trace
*/

(
//path masterfaders
var w, guis, o;
var receiver=NetAddr("192.168.178.12", 57120);//TouchOSC: vul IP adres in (b.v. 192.168.178.14), staat in system preferences-netwerk
var sender=NetAddr("192.168.178.13", 9000);//TouchOSC: Local IP adres, port(incoming)


w=Window("OSC", Rect(500,500,500,500)).front; w.alwaysOnTop_(true); w.addFlowLayout;

5.do{|i|
	var c=CompositeView(w, 80@350), d, fader, button, pan;
	c.addFlowLayout(0@0, 0@0);
	d=CompositeView(c, 40@350); d.addFlowLayout(0@0, 0@0);
	d.decorator.shift(0, 200);
	Button(d, 40@40).states_([ [\bang] ]);
	button=Button(d, 40@40).states_([ [\mute, Color.white, Color.black],[\mute,Color.black,Color.grey] ]);
	d=CompositeView(c, 40@350); d.addFlowLayout(0@0, 0@0);
	OSCFunc({|a,b,c,d|
		//		[a,b,c,d].postln;
		{fader.value_(fader.controlSpec.map(a[1]))}.defer;
	}, ['/1/fader1','/1/fader3','/1/fader4','/1/fader5','/1/fader6'][i], );
	OSCFunc({|a,b,c,d|
		//		[a,b,c,d].postln;
		{button.value_(a[1].postln)}.defer;
	}, ['/1/toggle1','/1/toggle2','/1/toggle3','/1/toggle4','/1/toggle5'][i]);
	pan=EZKnob(d, 40@80, \pan, \amp.asSpec, {|ez| }, 0, false);
	fader=EZSlider(d, 40@250, \amp, \amp.asSpec, {|ez|
		sender.sendMsg('/1/fader1', ez.controlSpec.unmap(ez.value));
	}, 0, layout: 'vert');
	OSCFunc({|a,b,c,d|
		//		[a,b,c,d].postln;
		{pan.value_(pan.controlSpec.map(a[1]))}.defer;
	}, ['/1/rotary3','/1/rotary4','/1/rotary5','/1/rotary6','/1/rotary7'][i]);
};

sender.sendMsg('/1/label', "test");

)