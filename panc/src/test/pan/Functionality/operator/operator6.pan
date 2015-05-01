#
# type incompatibilities
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template operator6;

"/ok" = length(list(7)) == 1;
"/bad" = list(7) == 1;
