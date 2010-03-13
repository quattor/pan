#
# variable subscripts must be longs or strings
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template variable8;

"/bad" = {
 a = 1.0;
 x[0] = 1;
 x[a] = 2;
};
