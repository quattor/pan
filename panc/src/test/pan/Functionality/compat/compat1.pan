#
# a resource can't replace a property (and vice-versa)
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template compat1;

"/x" = 1;
"/x" = undef;
"/x" = list(1, 2);

"/y" = 1;
"/y" = list(1, 2);
