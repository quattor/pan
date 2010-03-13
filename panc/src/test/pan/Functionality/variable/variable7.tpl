#
# variable subscripts must be properties
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template variable7;

"/bad" = {
 x[0] = 1;
 x[list(0)] = 2;
};
