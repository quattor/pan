#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template append10;

variable n = 'BAD';

'/x' = append(n, 1);

