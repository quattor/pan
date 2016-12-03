#
# test of operators
#
# @expect="/nlist[@name='profile']/list[@name='t']/*[1]='true' and /nlist[@name='profile']/list[@name='t']/*[2]='true' and /nlist[@name='profile']/list[@name='t']/*[3]='true' and /nlist[@name='profile']/list[@name='t']/*[4]='true' and /nlist[@name='profile']/list[@name='t']/*[5]='true' and /nlist[@name='profile']/list[@name='t']/*[6]='true' and /nlist[@name='profile']/list[@name='t']/*[7]='true' and /nlist[@name='profile']/list[@name='t']/*[8]='true' and /nlist[@name='profile']/list[@name='t']/*[9]='true' and /nlist[@name='profile']/list[@name='f']/*[1]='false' and /nlist[@name='profile']/list[@name='f']/*[2]='false' and /nlist[@name='profile']/list[@name='f']/*[3]='false' and /nlist[@name='profile']/list[@name='f']/*[4]='false' and /nlist[@name='profile']/list[@name='f']/*[5]='false' and /nlist[@name='profile']/list[@name='f']/*[6]='false' and /nlist[@name='profile']/list[@name='f']/*[7]='false' and /nlist[@name='profile']/list[@name='f']/*[8]='false' and /nlist[@name='profile']/list[@name='f']/*[9]='false'"
# @format=pan
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
"/t/9" = true == true;
"/t/10" = true != false;

"/f/0" = 0 != 0.0;
"/f/1" = "foo" != "foo";
"/f/2" = "foo" == "bar";
"/f/3" = "foo" == "foo\x00";
"/f/4" = "abc" >= "abcd";
"/f/5" = "abc" >= "abd";
"/f/6" = "abc" <= "ab";
"/f/7" = "abc" >= "x";
"/f/8" = "9" <= "10";
"/f/9" = true == false;
"/f/10" = true != true;
