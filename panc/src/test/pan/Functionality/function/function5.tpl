#
# tests of splice on strings
#
# @expect="/nlist[@name='profile']/string[@name='s1']='12abcde' and /nlist[@name='profile']/string[@name='s2']='abcde12' and /nlist[@name='profile']/string[@name='s3']='ab12cde' and /nlist[@name='profile']/string[@name='s4']='bcde' and /nlist[@name='profile']/string[@name='s5']='abcd' and /nlist[@name='profile']/string[@name='s6']='abce' and /nlist[@name='profile']/string[@name='s7']='abXXXe'"
# @format=pan
#

object template function5;

# prepend
"/s1" = splice("abcde", 0, 0, "12");

# append
"/s2" = splice("abcde", 5, 0, "12");

# insert before c
"/s3" = splice("abcde", 2, 0, "12");

# remove first
"/s4" = splice("abcde", 0, 1);

# remove last
"/s5" = splice("abcde", -1, 1);

# remove last but one
"/s6" = splice("abcde", -2, 1);

# replace cd by XXX
"/s7" = splice("abcde", 2, 2, "XXX");
