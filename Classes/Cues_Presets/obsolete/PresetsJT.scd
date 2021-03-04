PresetsJT : PresetsFileJT {
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
			pathNameTo=PathName(pathNameFrom.pathOnly++(pathNameFrom.fileNameWithoutExtension.split($_)[0]++"_"++filename++"."++(pathNameFrom.extension)));
			File.rename(pathNameFrom.fullPath, pathNameTo.fullPath);
			entries[index]=pathNameTo;
			fileNames[index]=entries[index].fileNameWithoutExtension;
			fileNamesWithoutNumbers[index]=fileNames[index].split($_).copyToEnd(1).join($_);
			funcs[\basename].value(old, new);
		});
	}

	addToCueList {arg cueList;
		//pathName=basename;
		//cueList.addCue(this);
	}

	makeGui {arg parent, bounds=350@20;
		{gui=PresetsGUIJT(this, parent, bounds)}.defer;
	}
}
//----------------------------------------------------------------------------- GUIs
PresetsGUIJT {
	var <presets;
	var <views, <parent, <bounds;
	*new {arg presets, parent, bounds;
		^super.newCopyArgs(presets).init(parent, bounds)
	}
	init {arg argparent, argbounds;
		var boundsName=(argbounds.x/3).floor@argbounds.y;
		var boundsButton=(boundsName.x/7).floor@argbounds.y;
		var c;
		views=();
		c=CompositeView(argparent, argbounds);
		c.addFlowLayout(0@0, 0@0);
		views[\addBefore]=Button(c, boundsButton).states_([ ["±"] ])
		.action_{ presets.add("new"++presets.entries.size, \addBefore, presets.entries[presets.index]) };
		views[\addAfter]=Button(c, boundsButton).states_([ ["+"] ])
		.action_{ presets.add("new"++presets.entries.size, \addAfter, presets.entries[presets.index]) };
		views[\delete]=Button(c, boundsButton).states_([ ["-"] ]).action_{ presets.delete };
		views[\store]=Button(c, boundsButton).states_([ ["s"] ]).action_{ presets.store };
		views[\restore]=Button(c, boundsButton).states_([ ["r"] ]).action_{ presets.restore };
		views[\basename]=TextField(c, boundsName)
		.string_(presets.fileNamesWithoutNumbers[presets.index]??{presets.basename})
		.action_{arg str; presets.basename_(str.string); };
		views[\prev]=Button(c, boundsButton).states_([ ["<"] ]).action_{ presets.prev };
		views[\presets]=PopUpMenu(c, boundsName)
		.items_(if (presets.array.size>0, {presets.fileNamesWithoutNumbers},{["(empty)"]}))
		.action_{|p|
			presets.restore(p.value);
			{views[\basename].string_(presets.fileNamesWithoutNumbers[p.value])}.defer
		};
		views[\next]=Button(c, boundsButton).states_([ [">"] ]).action_{ presets.next };
		//------------------------------------------------------------------------------- FUNCTIONS
		//combi update en index is dubbelopperdepop!
		[\update, \basename, \index].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					views[\basename].string_( presets.fileNamesWithoutNumbers[presets.index] );
					views[\presets].items_( presets.fileNamesWithoutNumbers ).value_(presets.index);
				}.defer
			});
		};
	}
}