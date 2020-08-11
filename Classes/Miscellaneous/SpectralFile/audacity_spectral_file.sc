SpectralFileA {
	var <pairs, <freqdb, <path;

	*new {arg path;
		^super.new.init(path);
	}

	init {arg argpath;
		var file;
		path=argpath;

		file=File(path, "r");
		pairs=file.readAllString;
		file.close;

		pairs=pairs.replace("\r","], [");
		pairs=pairs.replace("\t",", ");

		pairs=pairs.copyRange(29, pairs.size-4);
		pairs=("["++pairs++"]").interpret;
		freqdb=pairs.flop;
	}


}

/*
x=SpectralFileA("/Users/jorrittamminga/Dropbox/Current/_dopqelgänger/SC_new/spectrum_outro.txt");
x.pairs
(
f=File("/Users/jorrittamminga/Dropbox/Current/_dopqelgänger/SC_new/spectrum_outro.txt", "r");
x=f.readAllString;
f.close;
x=x.replace("\r","], [");
x=x.replace("\t",", ");
x=x.copyRange(29, x.size-4);
x=("["++x++"]").interpret;
//x.pairs;
//x.freqdb
)
x.last
*/