#
# bad usage of return
#
# @expect=org.quattor.pan.exceptions.SyntaxException
#

object template return4;

"/bad" = {
  x[return(1)] = 2;
  return(x);
};
