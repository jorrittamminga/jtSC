/*
client side version of Sanitize
inf, NaN, denormal, isNaN
isNumber
*/
+Array {
	sanitizeJT {
		^this.collect{|val|
			if (
				(val.isNaN) || (val==inf)// || (val.isNumber.not)
			)
			{
				0.0
			} {
				val
			}
		};//can this be more effecient?
	}
}