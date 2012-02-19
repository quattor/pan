#
# test of link type
#
# @expect="/profile/foo/bar and /profile/link"
# @format=xmldb
#

object template types5;

bind "/link" = long*;

"/foo/bar" = 123;

"/link" = "/foo/bar";
