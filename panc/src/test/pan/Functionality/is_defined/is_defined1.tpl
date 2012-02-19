#
# @expect="/nlist[@name='profile']/boolean[@name='result']='false'"
# @format=pan
#
object template is_defined1;

# This should not throw an exception.  This is a 
# change from earlier versions of the pan compiler.
'/result' = is_defined(X);