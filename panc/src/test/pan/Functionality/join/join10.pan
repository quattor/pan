#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template join10;

'/x2' = join(',', dict("key1", 1, "key2", 2));
