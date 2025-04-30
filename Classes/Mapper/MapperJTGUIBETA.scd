MapperJTGUI {
	var <mapper, <parent, bounds, <label, <>action, numberWidth, labelWidth, margin, gap, decimals, stepCurve;
	var <views, <viewsEditor, <>viewAction;
	var <width, <height, <viewsKeys, <values;

	*new {arg mapper, parent, bounds=350@20, label="", action={}, numberWidth, labelWidth, margin=0@0, gap=2@2, decimals=4, stepCurve=1;
		^super.newCopyArgs(mapper, parent, bounds, label, action, numberWidth, labelWidth, margin, gap, decimals, stepCurve).init
	}
	init {
		var cv, font, maplinMethod=\linlin;
		var curves=[\amp, \cos, \db, \exp, \lin, \sin ];
		var warp;
		var minvalkey, maxvalkey, curvekey, warpkey, stepkey, sizekey, lagkey;
		var minval, maxval;
		var numberOfRows, hasParent=(parent!=nil);

		views=();
		viewsEditor=();
		action=action??{{}};
		viewAction={};
		//-------------------------------------------------
		font=Font(Font.defaultMonoFace, bounds.y*0.6);
		numberOfRows=((parent!=nil)&&(label!=nil)).binaryValue+2+(mapper.smoothSize==nil).not.binaryValue+(mapper.lagAction==nil).not.binaryValue+(mapper.controlSpec.step>0.0000001).binaryValue;
		width=(2*margin.x+bounds.x);
		height=((gap.y+bounds.y)*numberOfRows+(2*margin.y));
		labelWidth=labelWidth??{bounds.x*0.15};
		numberWidth=numberWidth??{bounds.x*0.2};

		minvalkey=(label++"_"++\minval).asSymbol;
		maxvalkey=(label++"_"++\maxval).asSymbol;
		curvekey=(label++"_"++\curve).asSymbol;
		warpkey=(label++"_"++\warp).asSymbol;
		stepkey=(label++"_"++\step).asSymbol;
		sizekey=(label++"_"++\size).asSymbol;
		lagkey=(label++"_"++\lag).asSymbol;

		viewsKeys=(minval: minvalkey, maxval: maxvalkey, curvekey: curvekey, warpkey: warpkey, stepkey: stepkey);
		values=(minval: mapper.controlSpec.minval, maxval: mapper.controlSpec.maxval, curve: mapper.controlSpec.warp.asSpecifier, step: mapper.controlSpec.step);
		values[\warp]=curves.indexOfEqual(mapper.controlSpec.warp.asSpecifier);
		//-------------------------------------------------
		if (parent==nil) {
			parent=Window(label, Rect(400,400,width+8,height+8));
			parent.addFlowLayout(4@4,0@0); parent.alwaysOnTop_(true);
			parent.userCanClose_(false);
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
			views[key2]=EZSlider(parent, bounds, key, mapper.controlSpec.deepCopy, {|ez|
				if ((values[\warp]==3) && (ez.value<=0.0)) {
					{views[key2].value_(0.001)}.defer;
				} {
					if (i==0) {
						mapper.controlSpec.minval_(ez.value);
						//if (changeControlSpec) {this.minval_(controlSpec.minval)}
					}{
						mapper.controlSpec.maxval_(ez.value);
						//if (changeControlSpec) {this.maxval_(controlSpec.maxval)}
					};
					values[key]=ez.value;
					//warp=mapper.controlSpec.warp;
				};
				action.value;
			}, [mapper.controlSpec.minval, mapper.controlSpec.maxval][i], false, labelWidth, numberWidth).font_(font).decimals_(decimals)
		};
		views[curvekey]=EZSlider(parent, (bounds.x*0.75-gap.x).floor@bounds.y, \curve
			, [-10, 10, 0, stepCurve].asSpec, {|ez|
				mapper.controlSpec.warp_(ez.value);
				values[\curve]=ez.value;
				//warp=mapper.controlSpec.warp;//eruit?
				{views[warpkey].value_(nil)}.defer;
				action.value;
				mapper.changemapLinFunc(ez.value);
				//if (changeControlSpec) {this.warp_(controlSpec.warp)}
		}, if (mapper.controlSpec.warp.asSpecifier.isFloat) {mapper.controlSpec.warp.asSpecifier} {0}, false, labelWidth, numberWidth)
		.font_(font).decimals_(decimals);
		views[warpkey]=PopUpMenu(parent, (bounds.x*0.25).floor@bounds.y).items_(curves).action_{|pop|
			if ((pop.value==3)&&( (values[\minval]<=0.0) || (values[\maxval]<=0.0))) {
				{views[warpkey].value_(nil)}.defer;
			} {
				if (pop.value==4) {
					values[\curve]=0;
					{views[curvekey].value_(0)}.defer;
				};
				mapper.controlSpec.warp_(curves[pop.value]);
				mapper.changemapLinFunc(curves[pop.value]);
				values[\warp]=pop.value;
				action.value;
			}
		}.value_(if (mapper.controlSpec.warp.asSpecifier.class==Symbol) {curves.indexOfEqual(mapper.controlSpec.warp.asSpecifier)}).font_(font);

		if (mapper.controlSpec.step>0.00001) {
			views[stepkey]=EZSlider(parent, bounds, \step, [0, 10, 4.0], {|ez|
				mapper.controlSpec.step_(ez.value);
				action.value;
				//if (changeControlSpec) {this.step_(controlSpec.step)}
			}, mapper.controlSpec.step, false, labelWidth, numberWidth).font_(font).decimals_(decimals);
		};
		if (mapper.smoothSize!=nil) {
			views[sizekey]=EZSlider(parent, bounds, \size, [1, 20, \exp, 1], {|ez|
				mapper.smoothArray.size_(ez.value.asInteger)
			}, mapper.smoothArray.size, false, labelWidth, numberWidth).font_(font);
		};
		if (mapper.lagAction!=nil) {
			views[lagkey]=EZRanger(parent, bounds, \lag, [0.0, 2000, 4.0, 1, 0, "ms"].asSpec
				, mapper.lagAction
				, [0, 0], false, labelWidth, numberWidth, 20).font_(font);
		};
		views.do{|view|
			if (view.class.isKindOfClass(EZGui)) {
				view.view.background_(Color.yellow)
			} {
				view.background_(Color.yellow)
			}
		};
	}
}