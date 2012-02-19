#
# make sure protected resources don't get in the way 
# of legal operations
#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template protected1;

'/' = nlist('a', 1);
'/result' = 'OK';
