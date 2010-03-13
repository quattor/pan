#
# type incompatibilities
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template operator4;

variable i = 7; 

"/ok" = length("foo") < i;
"/bad" = "foo" < i;
