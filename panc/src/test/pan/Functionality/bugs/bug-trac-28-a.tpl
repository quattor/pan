#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-28-a;

variable X = nlist();
'/result' = X['key'];
