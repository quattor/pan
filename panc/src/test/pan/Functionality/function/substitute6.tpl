#
# @expect=org.quattor.pan.exceptions.EvaluationException
# @format=pan
#
object template substitute6;

variable V1 = 'ok1';

'/result' = {
  # v2 = 'ok2';
  substitute('${V1}-${v2}');
};
