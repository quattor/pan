#
# global variable cannot be modified from DML
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template variable10;

variable foo = 1;

# read = ok
"/test1" = foo;

# modify = error
"/test2" = {
  value('/test1');  # This prevents compile-time optimization of this block.
  foo = 2;
  return(3);
};
