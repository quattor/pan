#
# if debugging is not active, the argument should not be evaluated
#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#
object template debug2;

variable X = 1;
variable Y = debug(X/0);

'/result' = !is_defined(Y);
