#
# test of index() builtin
#
# @expect="/profile/s1=3 and /profile/s2=-1 and /profile/s3=8 and /profile/l1=2 and /profile/l2=-1 and /profile/n1='ab' and /profile/n2=''"
#

object template index1;

# in string
"/s1" = index("foo", "abcfoodefoobar");
"/s2" = index("f0o", "abcfoodefoobar");
"/s3" = index("foo", "abcfoodefoobar", 4);

# in list of strings
"/l1" = index("foo", list("Foo", "FOO", "foo", "bar"));
"/l2" = index("foo", list("Foo", "FOO", "foo", "bar"), 3);

# in nlist of long
"/n0" = nlist("a", 1, "b", 2, "ab", 12, "ba", 21);
"/n1" = index(12, value("/n0"));
"/n2" = index(12, value("/n0"), 1);
