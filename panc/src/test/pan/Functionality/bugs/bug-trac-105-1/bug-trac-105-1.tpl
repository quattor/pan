#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-105-1;

variable PATH="a//b";
include {PATH};
