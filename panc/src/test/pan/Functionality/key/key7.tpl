#
# incorrect use of first/next
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template key7;

"/bug" = first(1, x, y);
