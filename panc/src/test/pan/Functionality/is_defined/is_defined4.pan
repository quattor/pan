#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template is_defined4;

variable X = undef;
'/result' = is_defined(X);