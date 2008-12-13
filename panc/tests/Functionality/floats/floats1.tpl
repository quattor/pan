#
# sample file to test floating point numbers
#
# @expect="count(/profile/*)=4"
#

object template floats1;

"/x1" = 3.14159265359;
"/x2" = -0.0001;		# this is unary minus
"/x3" = 123e45 + 123e-45;
"/x4" = 1.2E+3;
