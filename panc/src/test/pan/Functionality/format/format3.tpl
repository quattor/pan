#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template format3;

variable STR = format('%X', 15);

'/result' = if ('F' == STR) 'OK';