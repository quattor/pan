#
# function returning an error
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template function2;

function foo = {
  if (ARGC != 1) {
    error("wrong ARGC: " + to_string(ARGC));
  };
  "ok";
};

"/x1" = foo(1);

"/x2" = foo(1, 2);

"/x3" = foo(1, 2, 3);
