#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template bug-trac-192-4;

'/a/b' = undef;
'/result' = !path_exists('/a/b/c'); 
'/a/b' = 'OK';

