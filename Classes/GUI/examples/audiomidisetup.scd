m=MIDISetupWindowJT.new;//

{AudioSetupWindowJT.new; s.boot}.fork//

//or

(
var c=Condition.new;
{
	AudioSetupWindowJT.new(action: {c.unhang});
	c.hang;
	s.boot;
}.fork
)

/*
m.midiOut
*/