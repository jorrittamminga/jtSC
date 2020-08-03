+ CompositeView {
	rebounds {//arg nextLine=true;
		var decoratorBounds, pparent, heightBefore, heightAfter;
		var widthBefore, widthAfter;
		if (this.decorator!=nil, {
			this.decorator.nextLine;
			this.bounds_(Rect(this.bounds.left, this.bounds.top
				, this.decorator.left.max(this.decorator.maxRight+this.decorator.margin.x)
				, this.decorator.top.max(this.decorator.maxHeight)+this.decorator.margin.y
			));

			if (this.parent.decorator!=nil, {

				if (this.parent.decorator.owner==nil, {
					this.parent.decorator.owner=[
						this.bounds.height
					];
				},{
					this.parent.decorator.owner=this.parent.decorator.owner.add(
						this.bounds.height
					)
				});

				//this.parent.decorator.maxHeight=this.parent.decorator.owner.maxItem;
				//"this parent shift".postln;
				this.parent.decorator.shift(
					(this.bounds.width-(
						this.parent.decorator.left-
						this.parent.decorator.gap.x) + this.bounds.left)
					,0//c.bounds.width-w.view.decorator.left//or 0
					//, this.decorator.margin.y
				);
			})

		});
	}

	findWindow {
		^this.parents.last.findWindow
	}
}