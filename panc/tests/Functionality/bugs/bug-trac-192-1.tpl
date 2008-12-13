#
# @expect="/profile/result='true'"
#

object template bug-trac-192-1;

'/a/b' = 'OK';
'/result' = !exists('/a/b/c'); 

