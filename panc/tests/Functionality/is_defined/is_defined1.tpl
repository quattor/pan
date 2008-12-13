#
# @expect="/profile/result='false'"
#
object template is_defined1;

# This should not throw an exception.  This is a 
# change from earlier versions of the pan compiler.
'/result' = is_defined(X);