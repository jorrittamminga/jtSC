(
s.waitForBoot{
	c=Buffer.read(s, "/Users/jorrittamminga/Music/Samples/flestik1.aif"); s.sync;
	b=BouncerJT(c)
}
)

b.buffer_(Buffer.read(s, "/Users/jorrittamminga/Music/Samples/flestik14.aif", bufnum:1));
b.buffer_(c);

b.set(\numChannels, 2)


x=b.bounceMirror((bounces:24, deltaTime: [0.05, 1.05], rate: 2.0, amp: [[1.0, 0.01],[1],\exp], az: [[-1.0, 1],[1]], dur: -1, startPos: 0, rt:[[1.0,0],[1]], fadeOut:0.0))
x=b.bounceMirror((bounces:24, deltaTime: [0.1, 0.95], rate: [[1.9,2.0],[1],\exp], amp: [[0.01, 1],[1],\exp], az: [[-1.0, 1],[1]], dur: -1, startPos: 0, rt:[[1.0,0],[1]], fadeOut:0.0))

x=b.bounce((bounces:24, deltaTime: [0.05, 1.05], rate: 2.0, amp: [[1.0, 0.01],[1],\exp], az: [[-1.0, 1],[1]], dur: -1, startPos: 0, rt:[[1.0,0],[1]], fadeOut:0.0))
x=b.bounce((bounces:24, deltaTime: [Array.geom(24, 0.05, 1.05).last, 1.05.reciprocal], rate: 2.0, amp: [[0.01, 1.0],[1],\exp], az: [[1.0, -1],[1]], dur: -1, startPos: 0, rt:[[0.0,1],[1]], fadeOut:0.0))


x=b.bounce((bounces:24, deltaTime: [0.05, 1.05], rate: 2.0, amp: [[1.0, 0.01],[1],\exp], az: [[-0.5, 0.5],[1]], dur: -1, startPos: 0, rt:[[1.0,0],[1]], fadeOut:0.0))

x=b.bounce((bounces:32, deltaTime: [0.3, 0.85], rate: [[2.0,2],[1.0],\exp], amp: [[1.0, 0.002],[1],\exp], az: [[-1.0, 1],[1]], dur: -1, startPos: 0, rt:[[1.0,0],[1]], fadeOut:0.0))

x=b.bounce((bounces:32, deltaTime: [0.3, 0.89], rate: 1, amp: [[0.05, 1],[1],\exp], az: [[-1.0, 1],[1]], dur: -1, startPos: 0, rt:0, fadeOut:0.0))
x=b.bounce((bounces:32, deltaTime: [Array.geom(32, 0.3, 0.89).last, 0.89.reciprocal], rate: 1, amp: [[1, 0.05],[1],\exp], az: [[1.0, -1],[1]], dur: -1, startPos: 0, rt:0, fadeOut:0.0))



b.buffer_(Buffer.read(s, "/Users/jorrittamminga/Music/Samples/malespeaker.aiff", bufnum:2));
x=b.bounce((bounces:24, deltaTime: [0.05, 1.05], rate: 1.0, amp: [[1.0, 1.0],[1],\exp], az: [[-1.0, 1],[1]], dur: [[0.01,0.5],[1], \exp], startPos: [[0.0,1.0],[1]], rt:1, fadeOut:0))
b.numChannels_(2);

.asAzimuthArray

0.4999.azToBusAndPan2(2,0)

0.1.neg.fold(-0.5, 0.5)*2



a=( 'bounces': 24, 'az': [ [ -1.0, 1 ], [ 1 ] ], 'deltaTime': [ 0.05, 1.05 ], 'fadeOut': 0.0,
  'dur': -1, 'amp': [ [ 1.0, 0.01 ], [ 1 ], exp ], 'rt': [ [ 1.0, 0 ], [ 1 ] ], 'rate': 2.0, 'startPos': 0 )


[ [ -1.0, 1 ], [ 1 ] ].rank