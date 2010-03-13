#
# @expect=org.quattor.pan.exceptions.EvaluationException
#
object template exists1;

# If there is an invalid path term used within an 
# exists function call, it should throw an exception
# rather than returning false.
#
# Note: this behavior is different than the c/c++
# version of the pan compiler.
#

variable X = hash();
'/result' = exists(X['/illegal/term']);
