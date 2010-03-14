#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template bug-trac-108-2;

# This should give a detailed error containing
# partial path information.
'/alpha/beta' = 'OK';
'/alpha/beta/gamma' = 'BAD';
