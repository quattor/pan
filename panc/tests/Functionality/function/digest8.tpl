#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template digest8;

'/result' = {
  a = 'BAD';
  digest(a, 'msg');
};
