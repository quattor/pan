#
# external paths forbidden in assignments
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template path3;

"//foo/bar" = 123;
