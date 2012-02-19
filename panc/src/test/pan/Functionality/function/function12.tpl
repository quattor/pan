#
# test to_double function
#
# @expect="/profile/d1=45.6 and /profile/d2=123.0 and /profile/d3=45.6 and /profile/d4=1.0"
# @format=xmldb
#

object template function12;

"/d1" = to_double("4.56e+1");
"/d2" = to_double(123);
"/d3" = to_double(4.56e+1);
"/d4" = to_double(true);
