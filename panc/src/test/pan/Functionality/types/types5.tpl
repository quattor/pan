#
# test of link type
#
# @expect="/nlist[@name='profile']/nlist[@name='foo']/long[@name='bar'] and /nlist[@name='profile']/string[@name='link']"
# @format=pan
#

object template types5;

bind "/link" = long*;

"/foo/bar" = 123;

"/link" = "/foo/bar";
