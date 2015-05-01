#
# test of infinite while loop
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template loop4;

"/bug" = {
  while(true) {
    true;
  };
};
