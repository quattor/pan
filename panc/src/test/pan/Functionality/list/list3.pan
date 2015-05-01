#
# can't merge list and nlist
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template list3;

"/bug" = merge(list(1, 2, 3), nlist("a", "A", "b", "B"));
