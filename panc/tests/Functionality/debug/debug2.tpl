#
# if debugging is not active, the argument should not be evaluated
#
# @expect="/profile/result='true'"
#
object template debug2;

variable X = 1;
variable Y = debug(X/0);

'/result' = !is_defined(Y);
