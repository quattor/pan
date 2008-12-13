#
# @expect="/profile/result='true'"
#

object template bug-trac-192-2;

'/a/b' = 'OK';
'/result' = !path_exists('/a/b/c'); 

