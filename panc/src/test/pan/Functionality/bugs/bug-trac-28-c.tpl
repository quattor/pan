#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-28-c;

'/result' = {
  x = nlist();
  x['key'];
};
