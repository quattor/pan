#
# a value should be returned even if debugging isn't active;
# if not active, the value should be the empty string
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template debug1;

variable X = debug('BAD');

'/result' = !is_defined(X);
