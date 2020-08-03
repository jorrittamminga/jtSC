Server.killAll
(
s.waitForBoot{
	var voices=128;
	x={arg revtime=5, amp=0.1; NHHall.ar(In.ar(0, 2), revtime.lag(1.0), 1.0)*amp.lag(1.0)}.play;
	//b=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/lacrimaeneuten.aiff");
	b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/VoetstappenOostkappele.aif", 0, -1, 0);
	s.sync;

	//c=Constellation([\voices, voices, \buffer, b], x).play

	c=Constellation([\voices, voices, \buffer, b], x).play

}
)

c.set(\rate, {exprand(0.25, 4.0)}!c.voices)
c.set(\rate, {exprand(0.5, 8.0)}!c.voices)
c.get(\amp)
c.get(\boost)
c.voices
c.setI( [\rate, {exprand(0.99, 1.01)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 0.2501)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 4.0)}!c.voices, 4.0] )
c.setI( [\rate, {exprand(0.25, 0.5)}!c.voices, 4.0] )

c.setI( [\rate, {(1..16).choose*0.125+0.001.rand2}!c.voices, 4.0] )

c.setI( [\rate, ({(1..8).choose*0.125+0.001.rand2}!c.voices).neg, 4.0] )
c.setI( [\az, {rrand(-0.5,0.5)}!c.voices, 4.0] )


c.setI( [\rate, {exprand(0.25, 1.0)}!c.voices, {exprand(1.0, 6.0)}!c.voices, 0, {rrand(0, 5.0)}!c.voices] )



b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/VoetstappenOostkappele.aif", 0, -1, 0);


(
s.waitForBoot{
	var voices=64;
	x={arg revtime=5, amp=0.1; NHHall.ar(In.ar(0, 2), revtime.lag(1.0), 1.0)*amp.lag(1.0)}.play;
b=Buffer.readChannel(s, "/Users/jorrittamminga/Music/Samples/VoetstappenOostkappele.aif", 0, -1, 0);
	s.sync;
	//c=Constellation([\voices, voices, \buffer, b], x).play

	c=Constellation2([\voices, voices, \buffer, b.bufnum], x).play

}
)
