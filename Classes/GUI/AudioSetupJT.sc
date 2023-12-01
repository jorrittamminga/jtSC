AudioSetupWindowJT {
	var inDevices, outDevices, inDevice, outDevice, window, views, <>server, <>dict, <path;

	*new {arg server, inDevice, outDevice, path, fontSize=12, action;
		^super.new.init(server, inDevice, outDevice, path, fontSize, action)
	}
	init {arg argserver, arginDevice, argoutDevice, argpath, fontSize, action;
		var file, cond, width, height;
		dict=();
		server=argserver??{Server.default};

		inDevices=ServerOptions.inDevices;
		outDevices=ServerOptions.outDevices;

		path=argpath??{"~/Desktop/".standardizePath};
		path=path++"audiosetup.scd";
		dict[\inDevice]=arginDevice;
		dict[\outDevice]=argoutDevice;

		if (File.exists(path), {
			file=path.load;
			file.keysValuesDo{|key,val| dict[key]=val};
		},{
			this.write;
		});

		{
			width=((26*fontSize+8+4));
			height=(fontSize*4.5+(3*4)+8);
			window=Window("Audio setup", Rect(
				(Window.screenBounds.width-width)/2
				,(Window.screenBounds.height-height)/2
				,width,400), false, true).front;
			window.addFlowLayout;
			window.userCanClose_(false);
			window.alwaysOnTop_(true);

			StaticText(window, (fontSize*6)@(fontSize*1.5)).string_("inDevice:").align_(\right).font_(Font(Font.defaultMonoFace, fontSize));
			PopUpMenu(window, (fontSize*20)@(fontSize*1.5)).items_(inDevices).action_{|p|
				dict[\inDevice]=inDevices[p.value];
			}.font_(Font(Font.defaultMonoFace, fontSize)).value_(inDevices.indexOfEqual(dict[\inDevice]));

			StaticText(window, (fontSize*6)@(fontSize*1.5)).string_("outDevice:").align_(\right).font_(Font(Font.defaultMonoFace, fontSize));
			PopUpMenu(window, (fontSize*20)@(fontSize*1.5)).items_(outDevices).action_{|p|
				dict[\outDevice]=outDevices[p.value];
			}.font_(Font(Font.defaultMonoFace, fontSize)).value_(outDevices.indexOfEqual(dict[\outDevice]));

			Button(window, (fontSize*13)@(fontSize*1.5)).action_{|b|
				server.options.inDevice=nil;
				server.options.outDevice=nil;
				{window.close}.defer;
				action.value
			}.states_([ ["x cancel"] ]).font_(Font(Font.defaultMonoFace, fontSize));

			Button(window, (fontSize*13)@(fontSize*1.5)).action_{|b|
				server.options.inDevice=dict[\inDevice];
				server.options.outDevice=dict[\outDevice];
				this.write;
				{window.close}.defer;
				action.value
			}.states_([ ["√ Apply",Color.black,Color.green] ]).font_(Font(Font.defaultMonoFace, fontSize));

			window.rebounds
		}.defer;
		if ( (action==nil) && (thisProcess.mainThread.state>3)) {
			cond=Condition.new;
			action={cond.unhang};
			cond.hang;
		};
	}
	write {
		var file;
		file=File(path, "w");
		file.write( dict.asCompileString );
		file.close;
	}
}