#
# type incompatibilities
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template operator5;

"/ok" = - 7;
"/bad" = - "foo";
