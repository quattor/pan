#
# undef will trigger an error in most opertions
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template undef2;

"/x" = 1;
"/y" = value("/x") + 2;
"/x" = undef;
"/y" = value("/x") + 2;

