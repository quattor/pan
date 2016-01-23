#
# bad usage of return
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template return2;

"/x" = to_string(return("?"));
