# Test should fail, wrong replacement field
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template format11;

variable HASH = dict("entry1", 1, "entry2", 2);
variable STR = format("%d", HASH);