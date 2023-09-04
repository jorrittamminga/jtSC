i=IControlsView(nil);
i.at(0, 0)
i.bread[0][1]

x.put(row, col, val)

9.size



(
var factor=0.608, factor2=0.608;
var bounds=200@40, string="12345678901234";//, size=if (string.size*factor>(bounds.x/bounds.y), {bounds.x/(string.size*factor)},{bounds.y});
w=Window.new.front;
w.addFlowLayout;
w.alwaysOnTop_(true);
StaticText(w, bounds).string_(string).background_(Color.white).font_(Font.monospace(string.fontSize(bounds)))
)

.mapToMIDI()

	e[\func][\midimonitor]={arg nr, ch, name, fader, knob, buttonTop, buttonBottom, extra, decentName;
		var width=10, height;
		var nam, fad, kno, butTop, butBottom;
		var cv=e[\cv][ch][nr], cvL, cvR, states;
		var fontSize=20;
		e[\viewsMIDImonitor][name]=();
		width=cv.bounds.width;
		height=18;//width*0.3;
		//nam=StaticText(cv, width@height).string_(name).font_(Font("Monaco", height)).background_(Color.black).stringColor_(Color.white);
		fontSize=(height/name.asString.size.pow(0.25)).min(height);

		if (e[\win][name]!=nil, {
			//var neme=decentName??{name};
			nam=Button(cv, width@height).states_([[name,Color.white,Color.black],[name,Color.yellow,Color.black]])
			.font_(Font("Monaco", fontSize)).action_{|b|
				if (b.value==1, {
					e[\win][name].visible_(true);
					e[\win][name].front;
				},{
					e[\win][name].visible_(false);
				})
			}.canFocus_(false);
		},{
			nam=StaticText(cv, width@height).string_(name).font_(Font("Monaco", fontSize)).background_(Color.black).stringColor_(Color.white).align_(\center);
		});
		e[\viewsMIDImonitor][name][\main]=nam;
		width=(width/2).floor;
		height=cv.bounds.height-height;
		cvL=CompositeView(cv, width@height);
		cvL.addFlowLayout(0@0,0@0);
		//cvL.background_(Color.rand);
		cvR=CompositeView(cv, width@height);
		cvR.addFlowLayout(0@0,0@0);
		//cvR.background_(Color.rand);
		if (knob!=nil, {
			kno=EZNumber(cvR, width@35, knob.labelView.string, knob.controlSpec, nil, knob.value, false, width, width, 0, 10, \line2, 0@0, 0@0);
			kno.numberView.font_(Font("Monaco", fontSize*1.25));
			e[\viewsMIDImonitor][name][\knob]=kno;
		},{
			cvR.decorator.shift(0, 35);
		});
		if (fader!=nil, {
			fad=EZNumber(cvR, width@35, fader.labelView.string, fader.controlSpec, nil, fader.value, false, width, width, 0, 10, \line2, 0@0, 0@0);
			fad.numberView.font_(Font("Monaco", fontSize*1.25));
			e[\viewsMIDImonitor][name][\fader]=fad;
		});
		//, 0, false, labelHeight:10, layout: \horz, gap:0@0, margin: 0@0
		[buttonTop, buttonBottom].do{|but,i|
			if (but!=nil, {
				if (but.class==Button, {
					states=but.states;
				},{
					states=[[0],[1,Color.black,Color.yellow]];
				});
				if (i==0, {
					butTop=Button(cvL, width@(height*0.5)).states_(states).value_(but.value);
					e[\viewsMIDImonitor][name][\butTop]=butTop;
					if (extra!=nil, {
						but.action=but.action.addFunc({
							{butTop.states_(buttonTop.states)}.defer
						});
					});
				},{
					butBottom=Button(cvL, width@(height*0.5)).states_(states).value_(but.value);
					e[\viewsMIDImonitor][name][\butBottom]=butBottom;
					if (extra!=nil, {
						but.action=but.action.addFunc({
							{butTop.states_(buttonTop.states)}.defer
						});
					});
				});
			},{
				cvL.decorator.shift(0, 35);
			});
		};

		[fader, knob, buttonTop, buttonBottom].do{|view,i|
			var view2=[fad, kno, butTop, butBottom][i];
			if (view!=nil, {
				view.action=view.action.addFunc({|ez|
					{view2.value_(ez.value)}.defer
				})
			})
		};
	};
