#
# @expect="/profile/result='OK'"
#
object template format3;

variable STR = format('%X', 15);

'/result' = if ('F' == STR) 'OK';