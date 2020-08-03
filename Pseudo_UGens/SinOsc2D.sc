SinOsc2D {

	*kr{ arg freq=1, rotate=0.0, mulX=1.0, mulY=1.0, addX=0.0, addY=0.0;

		var x, y, cos, sin, rotateCos, rotateSin;
		cos=SinOsc.kr(freq, 0, mulY, addY);
		sin=SinOsc.kr(freq, 0.5pi, mulX, addX);
		rotateCos=rotate.cos;
		rotateSin=rotate.sin;
		x = (rotateCos*sin) - (rotateSin*cos);// + shiftX;
		y = (rotateSin*sin) + (rotateCos*cos);// + shiftY;
		^[x,y]
		}
}

//freq=440.0, phase=0.0, mul=1.0, add=0.0
//SinOsc