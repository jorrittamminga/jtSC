/*
var sl, zz;
w=Window.new.front;
w.addFlowLayout;
w.onClose_{t.stop};
w.alwaysOnTop_(true);
sl=Slider2D.new(w, 350@350);
zz=Slider.new(w, 20@350);
*/
~func[\spiralMotion]={
	arg pars=(radiusX:1, radiusY:1, z: 0, rotate:0.0, speed:1.0, shiftX:0.0, shiftY:0.0,
		direction: 1.0),
	totalDur=15.0, resolution=0.05,
	action={arg x,y,z, slider2D, sliderZ;
		{
		slider2D.setXY(x, y);
		sliderZ.value_(z);
	}.defer;
	}
	, task;

	var initPars=(radiusX:1, radiusY:1, z: 0, rotate:0.0, speed:1.0, shiftX:0.0, shiftY:0.0,direction: 1.0);
	if ((task.class==Routine) || (task.class==Task), {
		task.stop
	});

	task={
		var env=List[], p=pars.deepCopy;
		var x=0, y=0, z=0, sin, cos;
		var time, phase, rotateSin, rotateCos;
		pars.keysValuesDo{|key,val|
			if (val.class==Env, {
				pars[key].times=pars[key].times*totalDur;
				env.add(key)})
		};
		(totalDur*resolution.reciprocal+1).do{|t|
			time=t*resolution;
			//------------------------------------
			env.do{|key| p[key]=pars[key].at(time)};
			//------------------------------------
			phase=(2pi*time*p[\speed]*p[\direction]);
			rotateCos=p[\rotate].cos;
			rotateSin=p[\rotate].sin;
			sin=phase.sin*p[\radiusX];
			cos=phase.cos*p[\radiusY];
			x = (rotateCos*sin) - (rotateSin*cos);// + shiftX;
			y = (rotateSin*sin) + (rotateCos*cos);// + shiftY;
			x=x+p[\shiftX];
			y=y+p[\shiftY];
			z=p[\z];
			//------------------------------------
			x=x*0.5+0.5;
			y=y*0.5+0.5;
			//------------------------------------
			action.value(x,y,z);
			//------------------------------------
			resolution.wait;
		};
	}.fork;
};
/*
~func[\spiralMotion].value((
		radiusX:Env.new([1.0, 0.0],[1.0]),
		radiusY:Env.new([1.0, 0.0],[1.0]),
		z: Env.new([0.0, 1.0],[1.0]),
		rotate:0.0,
		speed:Env.new([0.1, 1.0],[1.0],\exp),
		shiftX:0.0,
		shiftY:0.0,
		direction: -1.0
	),
	15.0, 0.05,
	{arg x,y,z;		{
		sl.setXY(x, y);
		zz.value_(z);
	}.defer;
	}
);
*/