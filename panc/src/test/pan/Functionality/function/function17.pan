# error() formatting works with resources
# @expect=org.quattor.pan.exceptions.EvaluationException ".*user-initiated error: too many arguments: \[ 1.15, 1.1 \].*"
#

object template function17;

function foo = {
  if (ARGC != 1) {
    error("too many arguments: %s", ARGV);
  };
  "ok";
};

'/x' = foo(1.15, 1.1);
