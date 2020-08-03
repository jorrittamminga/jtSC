+ Window {

	reboundsTo {arg width=100, height=100;
			this.bounds_(
				Rect(this.bounds.left, this.bounds.top, width, height)
		);
	}

	rebounds {arg nextLine=true;
		var decoratorBounds, pparent, heightBefore, heightAfter;
		if (this.view.decorator!=nil, {
			/*
			if (this.view.decorator.owner.size>0, {
			this.view.decorator.maxHeight=this.view.decorator.owner.maxItem.postln;
			});
			*/
			if (nextLine, {this.view.decorator.nextLine});
			this.bounds_(
				Rect(this.bounds.left, this.bounds.top
					, this.view.decorator.maxRight+this.view.decorator.margin.x
					, this.view.decorator.top));
		});
	}


	reboundsCompositieView {


	}

	front2 {arg margin=4, gap=4;
		this.front;
		this.addFlowLayout;//(margin,gap);
		this.alwaysOnTop_(true);
	}

}


+ QWindow {

	front2 {arg margin=4, gap=4;
		this.front;
		this.addFlowLayout;//(margin,gap);
		this.alwaysOnTop_(true);
	}

}



/*
+ SCWindow {

front2 {arg margin=4, gap=4;
this.front;
this.addFlowLayout;//(margin,gap);
this.alwaysOnTop_(true);
}

}
*/