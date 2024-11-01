+ FlowLayout {
	nextLine {
		if (owner!=nil, {
			maxHeight=maxHeight.max(owner.maxItem);
			//top=top.max(owner.maxItem)
		});
		left = bounds.left + margin.x;
		top = top + maxHeight + gap.y;
		maxHeight = 0;
		owner = [0];
	}

	/*
		nextLine {
		left = bounds.left + margin.x;
		top = top + maxHeight + gap.y;
		maxHeight = 0;
	}
	*/
}