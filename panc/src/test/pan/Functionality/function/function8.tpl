#
# test to_string function
#
# @expect="/nlist[@name='profile']/string[@name='s1']='hello' and /nlist[@name='profile']/string[@name='s2']='123' and /nlist[@name='profile']/string[@name='s3']='4.56' and /nlist[@name='profile']/string[@name='s4']='true'"
# @format=pan
#

object template function8;

"/s1" = to_string("hello");
"/s2" = to_string(123);
"/s3" = to_string(4.56);
"/s4" = to_string(true);
