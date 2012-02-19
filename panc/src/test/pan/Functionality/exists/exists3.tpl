#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template exists3;

# If ones tries to dereference a non-resource, the exists 
# function should just return false and not raise an exception.

variable X = undef;
'/result' = exists(X[0]);
