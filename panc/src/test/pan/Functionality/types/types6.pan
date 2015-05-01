#
# test of link type (path does not exist)
#
# @expect=org.quattor.pan.exceptions.ValidationException
#

object template types6;

bind "/link" = element*;

"/foo/baz" = 123;

"/link" = "/foo/bar";
