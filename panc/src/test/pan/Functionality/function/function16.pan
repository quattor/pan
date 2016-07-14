# error() also allows formatting
# @expect=org.quattor.pan.exceptions.EvaluationException ".*user-initiated error: wrong number \(2\) of elements in ARGC.*"
#

object template function16;

function foo = {
  if (ARGC != 1) {
    error("wrong number (%d) of elements in ARGC", ARGC);
  };
  "ok";
};

'/x' = foo(1.15, 1.1);
