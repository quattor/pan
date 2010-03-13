#
# @expect="/profile/result='OK'"
#
object template format2;

variable STR = format('%x', 15);

'/result' = if ('f' == STR) 'OK';