PresetsCollectionJT : PresetsJT {
	var <>indices;
	var <presetsCollection, <hasBlender;

	*new {arg presets, indices;
		^super.basicNew(presets, indices)
	}
	initObject {
		presetsJT=object;
		indices=pathName??{[0]};
		presetsJT.funcs[\basename]=presetsJT.funcs[\basename].addFunc({arg old, new;
			var updateFlag=false;
			array.do{|preset,i|
				var ind=preset[\indices].deepCopy.flat, inds, flag=false;
				inds=ind.flat.indicesOfEqual(old.asSymbol);
				if (inds.size>0, {
					flag=true;
					inds.do{|i| ind[i]=new.asSymbol};
					preset[\indices]=ind.reshapeLike(preset[\indices]);
				});
				if (flag, {updateFlag=true; this.put(i, preset)});
			};
			//if (updateFlag, {"update!".postln; this.update});
		});
		presetsJT.funcs[\add]=presetsJT.funcs[\add].addFunc({arg startIndex;
			var updateFlag=false;
			array.do{|preset,i|
				var ind=preset[\indices].deepCopy.flat, inds, flag=false;
				preset[\indices]=preset[\indices].deepCollect(0x7FFFFFFF, {|i| if (i>=startIndex, {flag=true; i+1},{i})});
				if (flag, {updateFlag=true; this.put(i, preset)});
			};
			if (updateFlag, {
				array[index];
				//this.update
			});
		});
		presetsJT.funcs[\store]=presetsJT.funcs[\store].addFunc({arg i;
			actionArray[index].value
		});
	}
	initGetAction{
		object=();
		getAction={ object.collect(_.value) };//default getAction
	}
	makePresetArray {
		presetsCollection=indices.deepCollect(0x7FFFFFFF, {|i| presetsJT.array.clipAt(i)??{presetsJT.array[0]} });
	}
	convertToIndices {arg in;
		^in.deepCollect(0x7FFFFFFF, {|i| if ((i.class==Symbol) || (i.class==String), {
			presetsJT.keys.indexOfEqual(i.asString)??{0}
		},{
			i.clip(0, presetsJT.array.size-1)
		})
		});
	}
	initSetAction {
		action={arg val=();
			var ind=val[\indices]??{indices};
			ind=this.convertToIndices(ind);
			{
				indices=ind;
				this.makePresetArray;
				object.keysValuesDo{|key,obj|
					//obj.action.value(val[key]);
					{obj.value=(val[key])}.defer;
				}
			}
		}
	}
	//initEntriesAction;
	initPathName {
		basename=\empty;
		//presetsJT.folderID=presetsJT.folderID??{0};
		//presetsJT.folderID=presetsJT.folderID+1;
		pathName=(presetsJT.directory.fullPath++("Collections/")).asPathName;//++numberOfFolders
		directory=pathName;
		if (File.exists(directory.fullPath).not, {File.mkdir(directory.fullPath)});
		this.update;
	}
	addBlender {

	}
	makeGui {arg parent, bounds=350@20;
		if (gui==nil, {
			{gui=PresetsCollectionGUIJT(this, parent, bounds);}.defer
			//if (cueJT!=nil, {cueJT.makeGui(gui.parent)});
		});
	}
}

PresetsCollectionGUIJT : PresetsGUIJT {
	preInit {
		presets.object[\indices]=TextField(parent, bounds).value_(presets.indices).action_{arg textField;
			presets.indices=presets.convertToIndices(textField.value);
			presets.makePresetArray
		};
		[\add].do{|key|
			presets.funcs[key]=presets.funcs[key].addFunc({
				{
					{presets.object[\indices].value=presets.object[\indices].value}.defer;
					//presets.object[\indices].string_(presets.object[\indices].value);
					//views[\basename].string_( presets.fileNamesWithoutNumbers[presets.index] );
					//views[\presets].items_( presets.fileNamesWithoutNumbers ).value_(presets.index);
				}.defer
			});
		}
	}
}