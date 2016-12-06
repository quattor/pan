#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid format specification or mismatched types in format\(\):.*"
#
object template format6;

variable STR = format('%x', 'BAD');
