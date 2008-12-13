#
# we can't delete the root resource
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template delete2;

"/x" = "hi!";
"/" = null;
