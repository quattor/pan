#
# test of index() builtin
#
# @expect="/profile/s1=3 and /profile/s2=-1 and /profile/s3=8"
#

object template index1;

# in string
"/s1" = index("foo", "abcfoodefoobar");
"/s2" = index("f0o", "abcfoodefoobar");
"/s3" = index("foo", "abcfoodefoobar", 4);
