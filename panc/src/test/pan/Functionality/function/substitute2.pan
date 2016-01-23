#
# @expect=org.quattor.pan.exceptions.EvaluationException
# @format=pan
#
object template substitute2;

'/result' = substitute('${v1}-${v2}', dict('v1', 'ok1'));

