#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*the first argument for error\(\) needs to be a string.*"
#

object template function18;

function foo = {
  if (ARGC == 0) {
    error(1);
  }
};

'/x' = foo();
