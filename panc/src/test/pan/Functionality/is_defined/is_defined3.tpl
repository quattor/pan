#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template is_defined3;

variable X = 1;
'/result' = is_defined(X);
