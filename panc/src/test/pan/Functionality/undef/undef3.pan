#
# undef cannot be used against the root resource
#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*root element cannot be replaced by element of type undef.*"
#

object template undef3;
"/" = undef;
