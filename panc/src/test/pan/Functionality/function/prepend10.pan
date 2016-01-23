#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template prepend10;

variable n = 'BAD';

'/x' = prepend(n, 1);

