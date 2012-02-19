#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template default20;

type t = string = 'OK';

bind '/result' = t;
