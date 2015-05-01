#
# test of index() builtin
#
# @expect="/nlist[@name='profile']/long[@name='l1']=2 and /nlist[@name='profile']/long[@name='l2']=-1"
# @format=pan
#

object template index2;

# in list of strings
"/l1" = index("foo", list("Foo", "FOO", "foo", "bar"));
"/l2" = index("foo", list("Foo", "FOO", "foo", "bar"), 3);
