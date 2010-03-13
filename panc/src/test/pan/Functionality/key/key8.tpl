#
# incorrect use of first/next
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template key8;

"/bug" = first(list(), x, y[x]);
