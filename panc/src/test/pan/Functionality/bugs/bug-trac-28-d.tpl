#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-28-d;

'/result' = {
  x = list();
  x[0];
};
