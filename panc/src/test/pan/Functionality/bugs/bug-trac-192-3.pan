#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template bug-trac-192-3;

'/a/b' = undef;
'/result' = !exists('/a/b/c'); 
'/a/b' = 'OK';

