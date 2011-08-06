#
# test of index() builtin
#
# @expect="/profile/l1=2 and /profile/l2=-1"
#

object template index2;

# in list of strings
"/l1" = index("foo", list("Foo", "FOO", "foo", "bar"));
"/l2" = index("foo", list("Foo", "FOO", "foo", "bar"), 3);
