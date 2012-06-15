#
# It should not be possible to reference a non-object
# template via a value() call.
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-sf-3529737;
'/result' = value('aa:/aa');
