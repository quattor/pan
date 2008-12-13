#
# sample file to test integers
#
# @expect="count(/profile/*)=6"
#

object template integers1;

"/x1" = 1965;		# simple
"/x2" = -5691;		# this is unary minus
"/x3" = 123 + 0123;	# the second one is in octal!
"/x4" = 0xf + 0xff + 0xfff + 0xdead + 0xbeef;
"/x5" = 1234567890 * 987654321;
"/x6" = 2147483647; 	# biggest value allowed
