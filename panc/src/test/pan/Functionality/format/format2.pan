#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template format2;

variable STR = format('%x', 15);

'/result' = if ('f' == STR) 'OK';