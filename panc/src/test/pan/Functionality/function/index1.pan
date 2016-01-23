#
# test of index() builtin
#
# @expect="/nlist[@name='profile']/long[@name='s1']=3 and /nlist[@name='profile']/long[@name='s2']=-1 and /nlist[@name='profile']/long[@name='s3']=8"
# @format=pan
#

object template index1;

# in string
"/s1" = index("foo", "abcfoodefoobar");
"/s2" = index("f0o", "abcfoodefoobar");
"/s3" = index("foo", "abcfoodefoobar", 4);
