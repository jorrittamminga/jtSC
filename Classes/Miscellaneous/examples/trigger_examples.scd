(
x=GateJT(0.5, {"below threshold".postln}, {"above threshold".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.post; " ".post;
		x.set(value);
		0.1.wait;
	}
}.fork
)


(
x=TriggerJT(0.5, {"trigger below threshold\n".postln}, {"trigger above threshold\n".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.postln;
		x.set(value);
		0.1.wait;
	}
}.fork
)


(
x=GateTimedJT(0.5, 1.0, {"below threshold\n".postln}, {"above threshold\n".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.postln;
		x.set(value);
		0.1.wait;
	}
}.fork
)

(
x=TriggerTimedJT(0.5, 1.0, {"below threshold\n".postln}, {"above threshold\n".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.postln;
		x.set(value);
		0.1.wait;
	}
}.fork
)

(
x=SchmittJT(0.5, 0.6, {"below threshold".postln}, {"above threshold".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.post; " ".post;
		x.set(value);
		0.1.wait;
	}
}.fork
)


(
x=SchmittTriggerJT(0.5, 0.6, {"trigger below threshold\n".postln}, {"trigger above threshold\n".postln});
{
	var value;
	inf.do{
		value=rrand(0.0, 1.0);
		value.postln;
		x.set(value);
		0.1.wait;
	}
}.fork
)