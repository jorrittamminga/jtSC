MapperControlSpecJTGUI {
	var <mapper, <parent, bounds, <label, <>action, numberWidth, labelWidth, margin, gap, decimals, stepCurve;
	var <views, <viewsEditor, <>viewAction;
	var <width, <height, <viewsKeys, <values;
	var <controlSpec;

	*new {arg mapper, parent, bounds=350@20, label, action={}, numberWidth, labelWidth, margin=0@0, gap=2@2, decimals=4, stepCurve=1;
		^super.newCopyArgs(mapper, parent, bounds, label//??{mapper.name}
			, action, numberWidth, labelWidth, margin, gap, decimals, stepCurve)
		.init(mapper)
	}
	init {arg argcontrolSpec;
		var cv, font, maplinMethod=\linlin;
		var curves=[\amp, \cos, \db, \exp, \lin, \sin ];
		var warp;
		var minvalkey, maxvalkey, curvekey, warpkey, stepkey, sizekey, lagkey, smoothkey;
		var minval, maxval;
		var numberOfRows, hasParent=(parent!=nil);
		//[ a MapperControlSpecJT(0, 1, 'linear', 0, 0, ""), a TopView, Point( 350, 20 ), , a Function, nil, nil, Point( 0, 0 ), Point( 2, 2 ), 4, 1 ]

		controlSpec=argcontrolSpec;
		views=();
		viewsEditor=();
		action=action??{{}};
		viewAction={};
		//-------------------------------------------------
		font=Font(Font.defaultMonoFace, bounds.y*0.6);
		numberOfRows=((parent!=nil)&&(label!=nil)).binaryValue+3+(mapper.smoothSize==nil).not.binaryValue+(mapper.lag==nil).not.binaryValue+(controlSpec.step>0.0000001).binaryValue;
		width=(2*margin.x+bounds.x);
		height=((gap.y+bounds.y)*numberOfRows+(2*margin.y));

		labelWidth=labelWidth??{bounds.x*0.15};
		numberWidth=numberWidth??{bounds.x*0.2};
		minvalkey=(mapper.name++"_"++\minval).asSymbol;
		maxvalkey=(mapper.name++"_"++\maxval).asSymbol;
		curvekey=(mapper.name++"_"++\curve).asSymbol;
		warpkey=(mapper.name++"_"++\warp).asSymbol;
		stepkey=(mapper.name++"_"++\step).asSymbol;
		sizekey=(mapper.name++"_"++\smoothSize).asSymbol;
		lagkey=(mapper.name++"_"++\lag).asSymbol;
		viewsKeys=(minval: minvalkey, maxval: maxvalkey, curvekey: curvekey, warpkey: warpkey, stepkey: stepkey);
		values=(minval: controlSpec.minval, maxval: controlSpec.maxval, curve: controlSpec.warp.asSpecifier, step: controlSpec.step);
		values[\warp]=curves.indexOfEqual(controlSpec.warp.asSpecifier);
		//-------------------------------------------------
		if (parent==nil) {
			parent=Window(label, Rect(400,400,width+8,height+8));
			parent.addFlowLayout(4@4,0@0); parent.alwaysOnTop_(true);
			parent.userCanClose_(false);
		} {
			if (parent.decorator==nil) {parent.addFlowLayout(0@0,gap)};
		};
		if ((label!=nil)&&hasParent) {
			StaticText(parent, bounds).string_(label).font_(font).align_(\center)
			.background_(Color.black).stringColor_(Color.white)
		};
		//-------------------------------------------------
		[\minval, \maxval].do{|key, i|
			var key2=[minvalkey, maxvalkey][i];
			views[key2]=EZSlider(parent, bounds, key, controlSpec.deepCopy, {|ez|
				if ((values[\warp]==3) && (ez.value<=0.0)) {
					{views[key2].value_(0.001)}.defer;
				} {
					if (i==0) {
						mapper.minval_(ez.value);
						//if (changeControlSpec) {this.minval_(controlSpec.minval)}
					}{
						mapper.maxval_(ez.value);
						//if (changeControlSpec) {this.maxval_(controlSpec.maxval)}
					};
					values[key]=ez.value;
					//warp=controlSpec.warp;
				};
				action.value;
			}, [controlSpec.minval, controlSpec.maxval][i], false, labelWidth, numberWidth).font_(font).decimals_(decimals)
		};
		views[curvekey]=EZSlider(parent, (bounds.x*0.75-gap.x).floor@bounds.y, \curve
			, [-10, 10, 0, stepCurve].asSpec, {|ez|
				mapper.warp_(ez.value);
				values[\curve]=ez.value;
				//warp=controlSpec.warp;//eruit?
				{views[warpkey].value_(nil)}.defer;
				action.value;
				//mapper.changemapLinFunc(ez.value);
				//if (changeControlSpec) {this.warp_(controlSpec.warp)}
		}, if (controlSpec.warp.asSpecifier.isFloat) {controlSpec.warp.asSpecifier} {0}, false, labelWidth, numberWidth)
		.font_(font).decimals_(decimals);
		views[warpkey]=PopUpMenu(parent, (bounds.x*0.25).floor@bounds.y).items_(curves).action_{|pop|
			if ((pop.value==3)&&( (values[\minval]<=0.0) || (values[\maxval]<=0.0))) {
				{views[warpkey].value_(nil)}.defer;
			} {
				if (pop.value==4) {
					values[\curve]=0;
					{views[curvekey].value_(0)}.defer;
				};
				mapper.warp_(curves[pop.value]);
				//mapper.changemapLinFunc(curves[pop.value]);
				values[\warp]=pop.value;
				action.value;
			}
		}.value_(if (controlSpec.warp.asSpecifier.class==Symbol) {curves.indexOfEqual(controlSpec.warp.asSpecifier)}).font_(font);
		if (controlSpec.step>0.00001) {
			views[stepkey]=EZSlider(parent, bounds, \step, [0, 10, 4.0], {|ez|
				mapper.step_(ez.value);
				action.value;
				//if (changeControlSpec) {this.step_(controlSpec.step)}
			}, controlSpec.step, false, labelWidth, numberWidth).font_(font).decimals_(decimals);
		};
		if (mapper.smoothSize!=nil) {
			views[sizekey]=EZSlider(parent, bounds, \size, [1, 20, \exp, 1], {|ez|
				mapper.smoothSize_(ez.value.asInteger)
			}, mapper.smoothSize, false, labelWidth, numberWidth).font_(font);
		};
		if (mapper.lag!=nil) {
			views[lagkey]=EZRanger(parent, bounds, \lag, [0.0, 2000.max(mapper.lag.maxItem*1000), 4.0, 1, 0, "ms"].asSpec
				, {|ez|
					mapper.lag=ez.value*0.001;
				}
				, mapper.lag*1000, false, labelWidth, numberWidth, 20).font_(font);
		};
		views.do{|view,i|
			if (view.isKindOf(EZGui)) {
				view.view.background_(Color.yellow)
			} {
				view.background_(Color.yellow)
			}
		};
		[minvalkey, maxvalkey, curvekey, warpkey, stepkey, sizekey, lagkey].do{|key| if (views[key]!=nil) {viewsEditor[key]=views[key]}};
	}
}