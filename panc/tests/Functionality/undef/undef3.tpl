#
# undef cannot be used against the root resource
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template undef3;
"/" = undef;
