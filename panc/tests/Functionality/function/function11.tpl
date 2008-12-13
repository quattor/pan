#
# test of to_long function
#
# @expect="/profile/l1=291 and /profile/l2=291 and /profile/l3=46 and /profile/l4=1"
#

object template function11;

"/l1" = to_long("0x123");
"/l2" = to_long(0x123);
"/l3" = to_long(4.56e+1);
"/l4" = to_long(true);
