#
# @expect="/profile/result[1]='OK'"
# @format=xmldb
#

object template bug-trac-161-3;

function f = true;

bind '/result' = string[] with f();
'/result/0' = 'OK';
