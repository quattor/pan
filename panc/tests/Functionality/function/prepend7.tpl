#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template prepend7;

variable n = null;

'/x' = prepend(list(), n);
