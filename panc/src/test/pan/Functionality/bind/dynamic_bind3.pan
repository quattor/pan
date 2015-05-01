#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template dynamic_bind3;

bind '/result/${XX}' = long;

'/result/bad' = 'BAD';

