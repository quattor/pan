#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-28-b;

variable X = list();
'/result' = X[0];
