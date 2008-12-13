#
# different property (data) types can't be mixed
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template compat2;

"/x" = 1;
"/x" = undef;
"/x" = "foo";

"/y" = 1;
"/y" = "foo";
