+String {
	interpretSafeJT {
		var firstChar = this[0];
		// Quick check for Symbol (begins with '/')
		if (firstChar == $/) {
			^this.asSymbol
		};
		// Quick check for possible number (starts with digit or minus)
		if (firstChar.isDecDigit or: { firstChar == $- }) {
			var dotCount = 0;
			var eCount = 0;
			var isNumeric = true;
			var hasSeenE = false;

			// Single pass through the string to check if it's a valid number
			this.do { |char, i|
				if (i > 0 or: { char != $- }) { // Skip checking the first char if it's minus
					if (char == $.) {
						// Decimal point is not valid after 'e'
						if (hasSeenE) {
							isNumeric = false;
							^this
						};
						dotCount = dotCount + 1;
					} {
						if (char == $e or: { char == $E }) {
							hasSeenE = true;
							eCount = eCount + 1;
						} {
							if (char == $- or: { char == $+ }) {
								// Minus or plus only valid right after 'e'
								if (i > 0 and: { (this[i-1] == $e) or: { this[i-1] == $E } }) {
									// Valid
								} {
									isNumeric = false;
									^this
								};
							} {
								if (char.isDecDigit.not) {
									isNumeric = false;
									// Break early once we know it's not a number
									^this
								};
							};
						};
					};
				};
			};

			// If it's a valid number
			if (isNumeric) {
				// Check for too many decimal points or 'e's
				if ((dotCount > 1) or: { eCount > 1 }) {
					^this
				};

				// For scientific notation or any float, use asFloat
				if ((dotCount > 0) or: { eCount > 0 }) {
					^this.asFloat
				} {
					^this.asInteger
				};
			};
		};
		// Default case - return the string itself
		^this
	}
}