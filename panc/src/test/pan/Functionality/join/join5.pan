#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template join5;

'/x1' = list("a", "b");
'/x2' = join("-", value('/x1'), "c");
