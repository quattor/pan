#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template join4;

'/x1' = list(list("1"), "2", "3");
'/x2' = join("-", value('/x1'));
