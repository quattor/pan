#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template exists2;

# Valid, but non-existent terms should not produce 
# an exception even when the wrong term is used for
# a particular resource.

variable X = nlist();
'/result' = exists(X[0]);
