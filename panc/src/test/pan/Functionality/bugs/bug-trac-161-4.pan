#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='x']='OK'"
# @format=pan
#

object template bug-trac-161-4;

function f = true;

bind '/result' = string{} with f();
'/result/x' = 'OK';
