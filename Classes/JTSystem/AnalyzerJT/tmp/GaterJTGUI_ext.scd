/*
+ GaterJTGUI {

	makeEZGUI {arg argparent, argbounds=350@20, label, controlSpec, action, value,
		initAction=false, labelWidth, numberWidth,unitWidth=0, labelHeight
		,  layout=\horz, gap, margin, type, equalLength=true, fontName;
		var font;
		var boundz=argbounds.copy;
		font=Font(fontName??{Font.defaultMonoFace}, boundz.y*0.6);//Font.defaultMonoFace
		labelWidth=labelWidth??{boundz.y*0.6 * 8;};
		numberWidth=numberWidth??{boundz.x*0.15};
		controlSpec=controlSpec??{controlSpecs[label]};
		argparent=argparent??{parent};
		if ((action==nil) && (value!=nil), {
			action= {|ez|
				value=ez.value;
				synth.set(label, ez.value);
			}
		});
		if ((type==nil) && (value!=nil), {
			type=if (value!=nil, {
				switch(value.asArray.size, 1, {EZSlider}, 2, {
					EZRanger}, {
					boundz.y=boundz.x*0.8;
					EZMultiSlider
				})
			},{EZSlider})
		});
		if ((type==EZRanger) && (equalLength), {
			labelWidth=labelWidth-numberWidth-4;
		});
		if ((type==EZMultiSlider) && ((boundz.x/boundz.y)>8)
			, {boundz.y=boundz.x*0.25});
		^type.new(argparent, boundz, label, controlSpec, action
			, value, false, labelWidth, numberWidth).font_(font);
	}

	makeWindow {arg name, argbounds, left=400, top=400, margin=4@4, gap=4@4, marginAdd=4;
		var w;
		name=name??{id};
		name=name??{""};
		w=Window(name??{id}, Rect(left,top
			,argbounds.x+((margin.x+marginAdd)*2)
			,argbounds.y+((margin.y+marginAdd)*2)
		)).front;
		window=w;
		w.addFlowLayout(margin, gap);
		w.alwaysOnTop_(true);
		hasWindow=true;
		^w
	}

	makeCompositeView {arg argparent, argbounds, margin=4@4, gap=4@4
		, background=Color.grey(0.5);
		var parent;
		parent=CompositeView(argparent
			, (argbounds.x+(2*margin.x))@
			20000
			//(argbounds.x+(2*margin.x))
		);
		parent.addFlowLayout(margin, gap);
		parent.background_(background);
		^parent
	}

	resizeParent {//arg parent;
		var decoratorBounds, pparent, heightBefore, heightAfter;
		parent.decorator.nextLine;
		heightBefore=parent.bounds.height;
		decoratorBounds=parent.bounds;
		decoratorBounds.height=parent.decorator.top;
		heightAfter=parent.decorator.top;
		parent.bounds_(decoratorBounds);

		if (parent.class!=Window, {
			parent.parent.decorator.shift(0, heightAfter-heightBefore);
			parent.parent.decorator.nextLine;
		});
	}
}
*/