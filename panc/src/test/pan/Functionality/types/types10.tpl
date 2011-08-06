#
# test of link type (wrong target type)
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template types10;

bind "/link" = double*;

"/foo/bar" = 123;

"/link" = "/foo/bar";
