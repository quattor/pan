# Test should fail, wrong replacement field
# @expect=org.quattor.pan.exceptions.EvaluationException ".*invalid format specification or mismatched types in format\(\):.*"
#

object template format11;

variable HASH = dict("entry1", 1, "entry2", 2);
variable STR = format("%d", HASH);
