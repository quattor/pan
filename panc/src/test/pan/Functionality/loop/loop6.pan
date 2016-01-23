#
# test of infinite recursion
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template loop6;

function f = {
  f();
};

"/bug" = f();
