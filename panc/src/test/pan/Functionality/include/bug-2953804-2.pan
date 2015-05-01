#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-2953804-2;
function f = SELF;
include {f()};
