+ StaticText {
	stringFit_ {arg text, margin=0@0;
		this.string_(text).font_(
			Font.monospace(
				text.fontSize(
					(this.bounds.width-margin.x)@(this.bounds.height-margin.y)
				)
			)
		)
	}
}

+ EZGui {
	labelFit {arg margin=0@0;
		this.labelView.stringFit_(this.labelView.string, margin)
	}
}

+ Button {
	stringFit {arg margin=4@4;
		this.font_(
			Font.monospace(this.states.collect{|but| but[0].asString.size}.maxItem
				.fontSize(
					(this.bounds.width-margin.x)@(this.bounds.height-margin.y)

				)
			)
		)
	}
}