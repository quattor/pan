#
# @expect="/profile/result='false'"
#
object template is_defined4;

variable X = undef;
'/result' = is_defined(X);