#
# @expect="/profile/result/x='OK'"
#

object template bug-trac-161-4;

function f = true;

bind '/result' = string{} with f();
'/result/x' = 'OK';
