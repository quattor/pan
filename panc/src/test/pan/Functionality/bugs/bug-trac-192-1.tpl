#
# @expect="/nlist[@name='profile']/boolean[@name='result']='true'"
# @format=pan
#

object template bug-trac-192-1;

'/a/b' = 'OK';
'/result' = !exists('/a/b/c'); 

