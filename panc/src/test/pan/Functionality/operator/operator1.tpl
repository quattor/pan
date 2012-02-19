#
# test of operators
#
# @expect="/profile/t[1]='true' and /profile/t[2]='true' and /profile/t[3]='true' and /profile/t[4]='true' and /profile/t[5]='true' and /profile/t[6]='true' and /profile/t[7]='true' and /profile/t[8]='true' and /profile/t[9]='true' and /profile/f[1]='false' and /profile/f[2]='false' and /profile/f[3]='false' and /profile/f[4]='false' and /profile/f[5]='false' and /profile/f[6]='false' and /profile/f[7]='false' and /profile/f[8]='false' and /profile/f[9]='false'"
# @format=xmldb
#

object template operator1;

"/t/0" = 0 == 0.0;
"/t/1" = "foo" == "foo";
"/t/2" = "foo" != "bar";
"/t/3" = "foo" != "foo\x00";
"/t/4" = "abc" < "abcd";
"/t/5" = "abc" < "abd";
"/t/6" = "abc" > "ab";
"/t/7" = "abc" < "x";
"/t/8" = "9" > "10";

"/f/0" = 0 != 0.0;
"/f/1" = "foo" != "foo";
"/f/2" = "foo" == "bar";
"/f/3" = "foo" == "foo\x00";
"/f/4" = "abc" >= "abcd";
"/f/5" = "abc" >= "abd";
"/f/6" = "abc" <= "ab";
"/f/7" = "abc" >= "x";
"/f/8" = "9" <= "10";
