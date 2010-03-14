#
# @expect="/profile/result='true'"
#

object template bug-trac-192-4;

'/a/b' = undef;
'/result' = !path_exists('/a/b/c'); 
'/a/b' = 'OK';

