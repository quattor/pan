#
# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid format specification or mismatched types in format\(\):.*"
#
object template format5;

variable STR = format('%q');
