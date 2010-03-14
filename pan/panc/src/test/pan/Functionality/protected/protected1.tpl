#
# make sure protected resources don't get in the way 
# of legal operations
#
# @expect="/profile/result='OK'"
#

object template protected1;

'/' = nlist('a', 1);
'/result' = 'OK';
