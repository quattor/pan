#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template exists6;

# If ones tries to dereference a non-resource, the exists 
# function should just return false and not raise an exception.

variable X = 1;
'/result' = exists(X['a']);
