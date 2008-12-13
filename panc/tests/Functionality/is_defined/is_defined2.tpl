#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template is_defined2;

# This should throw an exception because it 
# cannot calculate the value, and X+Y is not
# a simple variable reference.
'/result' = is_defined(X+Y);