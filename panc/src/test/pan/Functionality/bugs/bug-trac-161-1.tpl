#
# @expect="/profile/result='OK'"
#

object template bug-trac-161-1;

type type_x = string with is_x();
function is_x = true;

bind '/result' = type_x;

'/result' = 'OK';