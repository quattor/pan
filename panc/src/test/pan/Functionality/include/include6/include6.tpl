#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

#
# Should not be able to directly include a structure template.
#
object template include6;

include {'include6_struct'};

'/result' = 'BAD';
