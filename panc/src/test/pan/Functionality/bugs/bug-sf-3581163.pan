# 
# This code raised an exception that wasn't caught.  Ensure
# that a correct pan exception is raised.
#
# @expect=org.quattor.pan.exceptions.EvaluationException
#

object template bug-sf-3581163;

'/result' = replace('BAD', '$OK', 'BAD');
