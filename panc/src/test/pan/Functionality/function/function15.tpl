#
# make sure that to_long() understands the different bases
#
# @expect="/profile/x1=12 and /profile/x2=10 and /profile/x3=18 and /profile/y1=12 and /profile/y2=10 and /profile/y3=18"
#

object template function15;

"/x1" = to_long("12");		# 12 (decimal)
"/x2" = to_long("012");		# 10 (octal)
"/x3" = to_long("0x12");	# 18 (hexadecimal)

"/y1" = 12;
"/y2" = 012;
"/y3" = 0x12;
