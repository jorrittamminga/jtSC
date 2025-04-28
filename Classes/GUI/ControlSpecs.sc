//check also .editSpec for EZSlider and EZControlSpecEditor
+ ControlSpec {
	gui {arg parent, bounds=350@20, label="", numberWidth, labelWidth, decimals=4, margin=0@0, gap=2@2, stepCurve=1, changeControlSpec=false, smoothArray, lagAction
		//, includeCurve=true, includeStep=true
		;
		var views=(), viewsEditor=(), cv, font, map, maplin, unmap, maplinMethod=\linlin;
		var curves=[\amp, \cos, \db, \exp, \lin, \sin ];
		var width, height;
		var out=(actions: {}), controlSpec, warp;
		var minvalkey, maxvalkey, curvekey, warpkey, stepkey, sizekey, lagkey;
		var minval, maxval;
		var numberOfRows, hasParent=(parent!=nil);

		//-------------------------------------------------
		if (smoothArray!=nil) {out[\smoothArray]=SmoothArrayJT(smoothArray)};

		font=Font(Font.defaultMonoFace, bounds.y*0.6);
		numberOfRows=((parent!=nil)&&(label!=nil)).binaryValue+3+(smoothArray==nil).not.binaryValue+(lagAction==nil).not.binaryValue;

		width=(2*margin.x+bounds.x);
		height=((gap.y+bounds.y)*numberOfRows+(2*margin.y));
		out[\height]=height;
		labelWidth=labelWidth??{bounds.x*0.15};
		numberWidth=numberWidth??{bounds.x*0.2};

		minvalkey=(label++"_"++\minval).asSymbol;
		maxvalkey=(label++"_"++\maxval).asSymbol;
		curvekey=(label++"_"++\curve).asSymbol;
		warpkey=(label++"_"++\warp).asSymbol;
		stepkey=(label++"_"++\step).asSymbol;
		sizekey=(label++"_"++\size).asSymbol;
		lagkey=(label++"_"++\lag).asSymbol;

		out[\viewsKeys]=(minval: minvalkey, maxval: maxvalkey, curvekey: curvekey, warpkey: warpkey, stepkey: stepkey);

		controlSpec=this.deepCopy;
		minval=controlSpec.minval;
		maxval=controlSpec.maxval;
		//actions={controlSpec.postln;};
		/*
		if (changeControlSpec) {out[\actions]={
		this=controlSpec
		}};
		*/
		out[\values]=(minval: controlSpec.minval, maxval: controlSpec.maxval, curve: controlSpec.warp.asSpecifier
			, step: controlSpec.step);
		out[\values][\warp]=curves.indexOfEqual(controlSpec.warp.asSpecifier);

		//-------------------------------------------------
		unmap={arg value; controlSpec.warp.unmap(value).clip(0, 1.0)};
		map={arg value; controlSpec.warp.map(value)};
		maplin={arg value, spec=\unipolar.asSpec;
			value.linlin(spec.minval, spec.maxval, controlSpec.minval, controlSpec.maxval)
		};

		//-------------------------------------------------
		if (parent==nil) {
			parent=Window(label, Rect(400,400,width+8,height+8));
			parent.addFlowLayout(4@4,0@0); parent.alwaysOnTop_(true);
			parent.userCanClose_(false);
			//parent.front;
			//cv=CompositeView(parent, width@height); cv.addFlowLayout(margin, gap);
			//cv.background_(Color.grey);
		} {
			if (parent.decorator==nil) {parent.addFlowLayout(0@0,gap)};
		};

		if ((label!=nil)&&hasParent) {
			StaticText(cv, bounds).string_(label).font_(font).align_(\center)
			.background_(Color.black).stringColor_(Color.white)
		};

		//-------------------------------------------------
		[\minval, \maxval].do{|key, i|
			var key2=[minvalkey, maxvalkey][i];
			views[key2]=EZSlider(parent, bounds, key, this.deepCopy, {|ez|
				if ((out[\values][\warp]==3) && (ez.value<=0.0)) {
					{views[key2].value_(0.001)}.defer;
				} {
					if (i==0) {
						controlSpec.minval_(ez.value);
						//if (changeControlSpec) {this.minval_(controlSpec.minval)}
					}{
						controlSpec.maxval_(ez.value);
						//if (changeControlSpec) {this.maxval_(controlSpec.maxval)}
					};
					out[\values][key]=ez.value;
					out[\warp]=controlSpec.warp;
				};
				out[\actions].value;
			}, [this.minval, this.maxval][i], false, labelWidth, numberWidth).font_(font).decimals_(decimals)
		};
		views[curvekey]=EZSlider(parent, (bounds.x*0.75-gap.x).floor@bounds.y, \curve
			, [-10, 10, 0, stepCurve].asSpec, {|ez|
				controlSpec.warp_(ez.value);
				out[\values][\curve]=ez.value;
				out[\warp]=controlSpec.warp;
				{views[warpkey].value_(nil)}.defer;
				out[\actions].value;
				maplin={arg value, spec=\unipolar.asSpec;
					value.lincurve(spec.minval, spec.maxval, controlSpec.minval, controlSpec.maxval, ez.value);
				};
				//if (changeControlSpec) {this.warp_(controlSpec.warp)}
		}, if (this.warp.asSpecifier.isFloat) {this.warp.asSpecifier} {0}, false, labelWidth, numberWidth)
		.font_(font).decimals_(decimals);
		views[warpkey]=PopUpMenu(parent, (bounds.x*0.25).floor@bounds.y).items_(curves).action_{|pop|
			if ((pop.value==3)&&( (out[\values][\minval]<=0.0) || (out[\values][\maxval]<=0.0))) {
				{views[warpkey].value_(nil)}.defer;
			} {
				if (pop.value==4) {
					out[\values][\curve]=0;
					{views[curvekey].value_(0)}.defer;
				};
				controlSpec.warp_(curves[pop.value]);
				out[\controlSpec]=controlSpec;
				out[\warp]=controlSpec.warp;
				out[\values][\warp]=pop.value;
				out[\actions].value;

				//[\amp, \cos, \db, \exp, \lin, \sin ];
				//maplin={arg value, spec=\unipolar.asSpec;value.linlin(spec.minval, spec.maxval, controlSpec.minval, controlSpec.maxval)};
				maplin=switch(curves[pop.value])
				{\exp} {
					{arg value, spec=\unipolar.asSpec;value.linexp(spec.minval, spec.maxval, controlSpec.minval, controlSpec.maxval)}
				}
				{\lin} {
					{arg value, spec=\unipolar.asSpec;value.linlin(spec.minval, spec.maxval, controlSpec.minval, controlSpec.maxval)}
				}
				{arg value, spec=\unipolar.asSpec; controlSpec.warp.map(spec.unmap(value).clip(0,1.0))}
				//if (changeControlSpec) {this.warp_(controlSpec.warp)}
			}
		}.value_(if (this.warp.asSpecifier.class==Symbol) {curves.indexOfEqual(this.warp.asSpecifier)}).font_(font);
		views[stepkey]=EZSlider(parent, bounds, \step, [0, 10, 4.0], {|ez|
			controlSpec.step_(ez.value);
			out[\warp]=controlSpec.warp;
			out[\actions].value;
			//if (changeControlSpec) {this.step_(controlSpec.step)}
		}, this.step, false, labelWidth, numberWidth).font_(font).decimals_(decimals);
		if (smoothArray!=nil) {
			views[sizekey]=EZSlider(parent, bounds, \size, [1, 20, \exp, 1], {|ez|
				out[\smoothArray].size_(ez.value.asInteger)
			}, smoothArray, false, labelWidth, numberWidth).font_(font);
		};
		if (lagAction!=nil) {
			views[lagkey]=EZRanger(parent, bounds, \lag, [0.0, 2000, 4.0, 1, 0, "ms"].asSpec
				, lagAction
				, [0, 0], false, labelWidth, numberWidth, 20).font_(font);
		};

		views.do{|view|
			if (view.class.isKindOfClass(EZGui)) {
				view.view.background_(Color.yellow)
			} {
				view.background_(Color.yellow)
			}
		};

		//-------------------------------------------------
		out[\controlSpec]=controlSpec;
		out[\spec]=controlSpec;
		out[\warp]=controlSpec.warp;
		out[\views]=views;
		out[\viewsEditor]=viewsEditor;
		out[\parentView]=parent;
		out[\unmap]=unmap;
		out[\map]=map;
		out[\maplin]=maplin;
		//out[\viewAction];
		^out
	}
}