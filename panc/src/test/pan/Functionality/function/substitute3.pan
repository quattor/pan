#
# @expect=org.quattor.pan.exceptions.EvaluationException
# @format=pan
#
object template substitute3;

'/result' = substitute('${v1}-${==}', dict('v1', 'ok1'));

