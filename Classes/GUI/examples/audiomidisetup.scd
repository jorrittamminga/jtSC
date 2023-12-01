m=MIDISetupWindowJT.new
{AudioSetupWindowJT.new; s.boot}.fork

(
var c=Condition.new;
{
	AudioSetupWindowJT.new(action: {c.unhang});
	c.hang;
	s.boot;
}.fork
)

m.midiOut

MIDIEndPoint("IAC Driver", "Bus 1").

nil.asArray

Recorder

ServerOptions.inDevices
ServerOptions.outDevices

s.options.inDevices

InJT


