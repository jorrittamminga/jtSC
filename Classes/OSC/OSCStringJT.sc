/*
o=OSCPlayerJT.new("/Users/jorrit/Desktop/_250404_092707_0.txt").gui
*/
OSCStringJT : String {
	classvar <asciiNumbers;

	*initClass {
		asciiNumbers = [45,46]++(48..57)
	}


}