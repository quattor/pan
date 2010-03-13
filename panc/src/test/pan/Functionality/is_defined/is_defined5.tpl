#
# @expect="/profile/result='false'"
#
object template is_defined5;

variable X = null;
'/result' = is_defined(X);