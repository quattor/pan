#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-105-2;

variable X = "a//b";
include {X};
