#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid format specification or mismatched types in format\(\):.*"
#
object template format7;

variable STR = format('%x%x', 1);
