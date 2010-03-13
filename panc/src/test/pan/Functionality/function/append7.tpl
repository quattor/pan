#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template append7;

variable n = null;

'/x' = append(list(), n);
