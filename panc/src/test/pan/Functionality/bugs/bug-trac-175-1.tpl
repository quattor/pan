#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-175-1;
variable X = undef;
'/result' = match(X, 'x');
