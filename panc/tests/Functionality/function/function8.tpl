#
# test to_string function
#
# @expect="/profile/s1='hello' and /profile/s2='123' and /profile/s3='4.56' and /profile/s4='true'"
#

object template function8;

"/s1" = to_string("hello");
"/s2" = to_string(123);
"/s3" = to_string(4.56);
"/s4" = to_string(true);
