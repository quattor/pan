#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template bug-trac-192-2;

'/a/b' = 'OK';
'/result' = !path_exists('/a/b/c'); 

