+ Dictionary {
	changeKey {arg oldKey, newKey;
		this[newKey]=this[oldKey];
		this.removeAt(oldKey);
	}
}
