#
# bad usage of return
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template return3;

"/bad" = return(return(1));
