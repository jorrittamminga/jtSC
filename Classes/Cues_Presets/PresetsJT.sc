PresetsJT : PresetsFileJT {
	var <neuralNet, <blender;
	*new {arg object, pathName;
		var flag=false;
		if (pathName.class==String, {if (pathName.contains($/), {flag=false},{flag=true})}, {
			if (pathName.class==Symbol, {flag=true});
		});
		^if (flag, {
			CuesJT(object, pathName)
		},{
			super.basicNew(object, pathName)
		})
	}
	basename_ {arg filename="newfilename";
		var file, old=basename.copy, new=filename.copy;
		var pathNameFrom, pathNameTo;
		basename=filename;
		if (entries[index]!=nil, {
			pathNameFrom=entries[index];
			pathNameTo=PathName(pathNameFrom.pathOnly++(pathNameFrom.fileNameWithoutExtension.split($_)[0]++"_"
				++filename++"."++(pathNameFrom.extension)));
			File.rename(pathNameFrom.fullPath, pathNameTo.fullPath);
			entries[index]=pathNameTo;
			fileNames[index]=entries[index].fileNameWithoutExtension;
			fileNamesWithoutNumbers[index]=fileNames[index].split($_).copyToEnd(1).join($_);
			funcs[\basename].value(old, new);
		});
	}
	addToCueList {arg cueList, cueName;
		//var cueJT;
		cueName=cueName??{(directory).allFolders.last.asSymbol};
		cueJT=CuesJT(this, cueName);
		if (gui!=nil, {
			cueJT.makeGui(gui.parent, gui.bounds)
		});
		cueJT.addToCueList(cueList);
	}
	addNN{neuralNet=PresetsNNJT(this)}
	addBlender{blender=PresetsBlenderJT(this)}
	makeGui {arg parent, bounds=350@20;
		gui=1.0;
		{
			gui=PresetsGUIJT(this, parent, bounds);
			if (cueJT!=nil, {cueJT.makeGui(gui.parent)});
			if (neuralNet!=nil, {neuralNet.makeGui(parent, bounds)});
			if (blender!=nil, {blender.makeGui(parent, bounds)});
		}.defer;
	}
}
//----------------------------------------------------------------------------- GUIs
PresetsGUIJT {
	var <presets;
	var <views, <parent, <bounds, <font, <fontNames;
	*new {arg presets, parent, bounds;
		^super.newCopyArgs(presets).init(parent, bounds)
	}
	preInit {}
	postInit {}
	init {arg argparent, argbounds;
		var boundsName=(argbounds.x/3).floor@argbounds.y;
		var boundsButton=(boundsName.x/9)@argbounds.y;
		var c;
		views=();
		parent=argparent;
		bounds=argbounds;
		font=Font("Monaco", bounds.y*0.75);
		fontNames=Font("Monaco", bounds.y*0.75);
		this.preInit;
		c=CompositeView(argparent, argbounds);
		c.addFlowLayout(0@0, 0@0);
		views[\addBefore]=Button(c, boundsButton).states_([ ["±"] ])
		.action_{ presets.add("new"++presets.entries.size, \addBefore, presets.entries[presets.index]) }.font_(font);
		views[\addAfter]=Button(c, boundsButton).states_([ ["+"] ])
		.action_{ presets.add("new"++presets.entries.size, \addAfter, presets.entries[presets.index]) }.font_(font);
		views[\delete]=Button(c, boundsButton).states_([ ["-"] ]).action_{ presets.delete }.font_(font);
		views[\store]=Button(c, boundsButton).states_([ ["s"] ]).action_{ presets.store }.font_(font);
		views[\restore]=Button(c, boundsButton).states_([ ["r"] ]).action_{ presets.restore }.font_(font);
		views[\basename]=TextField(c, boundsName)
		.string_(presets.fileNamesWithoutNumbers[presets.index]??{presets.basename})
		.action_{arg str; presets.basename_(str.string); }.font_(fontNames);
		views[\prev]=Button(c, boundsButton).states_([ ["<"] ]).action_{ presets.prev }.font_(font);
		views[\presets]=PopUpMenu(c, boundsName)
		.items_(if (presets.array.size>0, {presets.fileNamesWithoutNumbers},{["(empty)"]}))
		.action_{|p|
			presets.restoreAtIndex(p.value);
			{views[\basename].string_(presets.fileNamesWithoutNumbers[p.value])}.defer
		}.font_(fontNames);
		views[\next]=Button(c, boundsButton).states_([ [">"] ]).action_{ presets.next }.font_(font);
		views[\index]=StaticText(c, (boundsButton.x*2)@(boundsButton.y)).string_("0").align_(\right)
		.font_(font).stringColor_(Color.white).background_(Color.black);
		this.postInit;
		//------------------------------------------------------------------------------- FUNCTIONS
		//combi update en index is dubbelopperdepop!
		[\update, \basename, \index].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					views[\basename].string_( presets.fileNamesWithoutNumbers[presets.index] );
					views[\presets].items_( presets.fileNamesWithoutNumbers ).value_(presets.index);
					views[\index].string_(presets.index);
				}.defer
			});
		};
	}
}