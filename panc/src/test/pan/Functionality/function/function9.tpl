#
# test to_boolean function
#
# @expect="/profile/t1='true' and /profile/t1='true' and /profile/t3='true' and /profile/t4='true' and /profile/f1='false' and /profile/f2='false' and /profile/f3='false' and /profile/f4='false'"
#

object template function9;

"/t1" = to_boolean("hello");
"/t2" = to_boolean(123);
"/t3" = to_boolean(4.56);
"/t4" = to_boolean(true);

"/f1" = to_boolean("");
"/f2" = to_boolean(0);
"/f3" = to_boolean(0.0);
"/f4" = to_boolean(false);
