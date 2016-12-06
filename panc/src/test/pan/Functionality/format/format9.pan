# Test should fail, wrong replacement field
# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid format specification or mismatched types in format\(\):.*"
#

object template format9;

variable LIST = list(1, 2, 3);
variable STR = format("%d", LIST);
