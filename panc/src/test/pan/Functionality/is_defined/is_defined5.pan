#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template is_defined5;

variable X = null;
'/result' = is_defined(X);