#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#

object template bug-trac-161-1;

type type_x = string with is_x();
function is_x = true;

bind '/result' = type_x;

'/result' = 'OK';