(
s.waitForBoot{
	b=Buffer.read(s, "/Users/jorrit/Dropbox/Samples/malespeaker.aiff"); s.sync;
	x={arg buf=0, endPos= -1; LoopBufJT.ar(1, buf, 1, 1, 0, endPos)!2}.play(args: [\buf, b.bufnum]);
}
)

b=Buffer.read(s, "/Users/jorrit/Dropbox/Samples/sax_laag_flag_1.aiff", bufnum:b.bufnum);

b=Buffer.read(s, "/Users/jorrit/Dropbox/Samples/malespeaker.aiff", bufnum:b.bufnum);

x.set(\endPos, 1000000)