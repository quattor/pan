#
# bad usage of format function
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template format1;

"/x" = format();
