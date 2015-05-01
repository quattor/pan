#
# @expect=org.quattor.pan.exceptions.EvaluationException
# @format=pan
#
object template substitute4;

'/result' = substitute('${v1}-${v2}', 'BAD');

