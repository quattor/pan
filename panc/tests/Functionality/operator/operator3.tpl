#
# type incompatibilities
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template operator3;

variable i = 1;

"/ok" = to_string(i) + "foo";
"/bad" = i + "foo";
