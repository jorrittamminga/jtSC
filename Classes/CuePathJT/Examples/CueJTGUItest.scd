c=CueJTMaster(thisProcess.nowExecutingPath.dirname++"/cues/");
c.analyzeStructure

c.makeGui

PathName(c.cueNames[0]).allFolders

(
var structure=(), cue=0;
c.cueNames.do{|cueName|
	var folders=PathName(cueName).allFolders;
	var event=structure;
	folders.do{|folderName, depth|
		if (event[folderName.asSymbol]==nil, {
			event[folderName.asSymbol]=()
		});
		if (depth==(folders.size-1), {
			event[folderName.asSymbol]=cue
		});
		event=event[folderName.asSymbol];
	};
	cue=cue+1;
};
structure
)

( '0003_Interlude': 9, '0002_PartII': ( '0001_Section2d': 8, '0000_Section1c': 7 ), '0001_PartI': ( '0001_Section2b': 6, '0000_Section1a': 5 ), '0004_PartIII': ( '0000_Section1q': 10 ),
  '0000_Intro': ( '0000_Section1': ( '0002_reverb': ( '0000_test1': 2, '0001_test2': 3 ), '0001_stopSample': 1, '0000_startSample': 0 ), '0001_Section2': 4 ) )



(
var path=thisProcess.nowExecutingPath.dirname++"/cues/";
var objects=();
var values=[], names=[], functions=[], functionLists=[];
var name="", cue=0;
PathName(path).deepFiles.do{arg pathname;
	var fullPath, fileName, pathOnly, folders, out;
	var key, func;
	out=pathname.fullPath.load;
	pathname=PathName(pathname.fullPath.replace(path, "/"));
	fullPath=pathname.fullPath;
	pathOnly=pathname.pathOnly;
	fileName=pathname.fileNameWithoutExtension;
	folders=pathname.allFolders;
	if (pathOnly!=name, {
		names=names.add(pathOnly);
		name=pathOnly;
		values=values.add( () );
		functions=functions.add( () );
	},{

	});
	cue=values.size-1;
	key=fileName.asSymbol;
	values[cue][key]=out;
	//actions[cue][objectKey]=values;
	//func=this.getFunc(objects[objectName], values);
	//of this.putFunc(cueName, cue, objectName, values);
	//cueFunc[cue]=cueFunc[cue].addFunc(func);
};
//x=cueNames
values.postln;
functions;//actions is misschien een betere term...
names;
)

functionLists[0].value(enviroment)

//========================================================================
//========================================================================
c=CueJT(thisProcess.nowExecutingPath.dirname++"/cues/");
c.cues.deepDo(49, {arg event;
	//if (event["000Master"]!=nil, {
	event.postln
	//})
})
//-----------------------------------------
c.cues["0003_Interlude"]
c.cues.keys.asArray.collect{|i| i.class}
//-----------------------------------------
(
f={arg event, key, object;
	if (event.class==Event, {
		[key, event[key], event["000Master"], event.keys.asArray.collect{|k| [k, key, k.class, k==key]}].postln;
		if (event[key.asString]!=nil, {
			"joehoe".postln;
		},{
			event.do{arg event; f.value(event, key, object) }
		})
	})
};
f.value(c.cues, "000Master")
)
//-----------------------------------------
c.cues

PathName(thisProcess.nowExecutingPath.dirname++"/cues/").entries

a=[1, 2,3,4,5,6,7];
f={arg a,b,c,d; [a,b,c,d]};
f.value(*a)


(
var path=thisProcess.nowExecutingPath.dirname++"/cues/";
var cueEvent, index=0, indices=[index];

var parent, bounds=350@240;
var f=();
var views=(), cMain, cListViews;
var deepFolders, deepFoldersFlat, folders, deepFoldersAsEvent;
var numberOfColumns;
var width;

p=PathName(path);
cueEvent=p.deepCollectEvent;

deepFolders=p.deepFolders(false);
deepFoldersFlat=deepFolders.flat;
deepFoldersAsEvent=p.deepFoldersAsEvent;

parent=Window("cues", Rect(400,400,400,400)).front.alwaysOnTop_(true);
parent.addFlowLayout(4@4, 0@0);
cMain=CompositeView(parent, bounds); cMain.addFlowLayout(0@0, 0@0); cMain.background_(Color.rand);
views[\prevB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\PREV] ]);
views[\nextB]=Button(cMain, (bounds.x*0.5).floor.asInteger@20).states_([ [\NEXT] ]);
StaticText(cMain, (bounds.x*0.2).floor.asInteger@20).string_("upcoming: ");
views[\upcoming]=StaticText(cMain, (bounds.x*0.8).floor.asInteger@20).string_("next cue");
cListViews=CompositeView(cMain, bounds.x@(bounds.y-40)); cListViews.addFlowLayout(0@0, 0@0); cListViews.background_(Color.rand);

numberOfColumns=3;
f[\getAllListViews]={arg event;
	var e=event.deepCopy, folders=[], flag=(e.class==Event);
	var j=0;
	while({e.class==Event}, {
		folders=folders.add(e.keys.asArray.sort);
		i=indices[j]??{indices=indices.add(0); 0};
		e=e[e.keys.asArray.sort[i]];
		j=j+1;
	});
	folders
};
f[\allFolders]={arg i;
	index=i;
	deepFolders[index].collect{|p|
		p=PathName("/"++(p.fullPath.replace(path, "")));
		p.allFolders.asArray;
	};
};
f[\changeName]={arg oldName, newName, event;
	[oldName, newName, event].postln
};
f[\addBefore]={};
f[\addAfter]={};
f[\remove]={};
f[\addFolderBefore]={};
f[\addFolderAfter]={};
f[\removeFolder]={};

f[\makeListViews]={arg folders;
	var numberOfColums=folders.size;
	var event=deepFoldersAsEvent;
	cListViews.removeAll;
	cListViews.decorator.reset;
	width=(bounds.x/numberOfColums).floor.asInteger;
	folders.collect{|folders, i|
		var views=(), folderNames, currentEvent;
		var buttonWidth=(width/6).floor.asInteger;
		var c=CompositeView(cListViews, width@cListViews.decorator.bounds.height);
		folderNames=folders.collect{|i|
			i.asString.split($_).copyToEnd(1).join($_)
		};
		c.addFlowLayout(0@0,0@0);
		c.background_(Color.rand);
		views[\listView]=ListView(c, width@(cListViews.decorator.bounds.height-40)).items_(folderNames).canFocus_(false).action_{arg l;
			indices[i]=l.value;
			indices=indices.copyRange(0, i);
			f[\makeListViews].value(folders=f[\getAllListViews].value(deepFoldersAsEvent))
		}.value_(indices[i]);

		currentEvent=event.postln;

		event=event[folders[indices[i]]];

		views[\textField]=TextField(c, width@20).string_(folderNames[indices[i]]).action_{arg t;
			f[\changeName].value(folderNames[indices[i]], t.string, currentEvent)
		};
		Button(c, buttonWidth@20).states_([ ["+v"] ]).canFocus_(false).action_{f[\addAfter].value};//addAfter
		Button(c, buttonWidth@20).states_([ ["+^"] ]).canFocus_(false).action_{f[\addBefore].value};//addBefore
		Button(c, buttonWidth@20).states_([ ["-"] ]).canFocus_(false).action_{f[\remove].value};//remove
		Button(c, buttonWidth@20).states_([ ["+<"] ]).canFocus_(false).action_{f[\addFolderBefore].value};//addFolderBefore
		Button(c, buttonWidth@20).states_([ ["+>"] ]).canFocus_(false).action_{f[\addFolderAfter].value};//addFolderAfter
		Button(c, buttonWidth@20).states_([ ["--"] ]).canFocus_(false).action_{f[\removeFolder].value};//removeFolder
		views[\compositeView]=c;
		views
	};
};
f[\makeListViews].value(folders=f[\getAllListViews].value(deepFoldersAsEvent, 0))

)

x=PathName("/Users/jorrit/Dropbox/GitHub/jtSC/Classes/CuePathJT/Examples/cues/").deepFoldersAsEvent.postcs;"";

x=PathName("/Users/jorrit/Dropbox/GitHub/jtSC/Classes/CuePathJT/Examples/cues/").deepCollectEvent.postcs;"";

( '0003_Interlude': ( "000Master": ( 'value': 0 ) ), '0002_PartII': ( '0001_Section2d': ( "000Master": ( 'value': 0 ) ), '0000_Section1c': ( "000Master": ( 'value': 0 ) ) ), '0001_PartI': ( '0001_Section2b': ( "000Master": ( 'value': 0 ) ), '0000_Section1a': ( "000Master": ( 'value': 0 ) ) ), '0004_PartIII': ( '0000_Section1q': ( "000Master": ( 'value': 0 ) ) ),
'0000_Intro': ( '0000_Section1': ( '0002_reverb': ( '0000_test1': ( "000Master": ( 'value': 20 ) ), '0001_test2': ( "000Master": ( 'value': 21 ) ) ), '0001_stopSample': ( "tester": ( 'value': 1111 ), "000Master": ( 'value': 1 ) ), '0000_startSample': ( "000Master": ( 'value': 0 ), "tester": ( 'value': 14321 ) ) ), '0001_Section2': ( "000Master": ( 'value': 20 ) ) ) )

f=nil.addFunc({arg x; x=x+1});
f.value(1)