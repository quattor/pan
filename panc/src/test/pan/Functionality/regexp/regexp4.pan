#
# too few args should give a nice error message
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template regexp4;

"/bug" = match("12323");
