
# 
# This should generate a controlled exception, but 
# was generating a null pointer exception. 
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template object-lookup1;

'/result' = value('missing-object:/root');
