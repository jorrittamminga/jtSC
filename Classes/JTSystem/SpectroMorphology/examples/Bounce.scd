
~bus[\Bounce]=Bus.audio(s, ~noc);
~bus[\BounceVerb]=Bus.audio(s, ~noc);//zou ook mono kunnen zijn!
s.sync;
~synth[\BounceMixer]=Synth.head(s, \SubMixer, [\inBus, ~bus[\Bounce], \inBusWet, ~bus[\BounceVerb], \outBus, ~bus[\Out], \wetBus, ~bus[\GVerb]]);
s.sync;

{
	~func[\bounceGUI].value(\Bounce, ~buf[\Rec]);
~synth[\BounceMixer].makeGUI(~win[\Bounce]);
}.defer;//moet dit een functie zijn?
