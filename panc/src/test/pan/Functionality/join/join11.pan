#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template join11;

'/x' = join(",", "a", "b", 1);
