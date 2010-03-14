#
# @expect="/profile/result='OK'"
#
object template default20;

type t = string = 'OK';

bind '/result' = t;
