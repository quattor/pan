#
# @expect="/profile/result='true'"
#
object template is_defined3;

variable X = 1;
'/result' = is_defined(X);
