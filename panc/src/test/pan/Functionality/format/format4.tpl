#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template format4;

variable STR = format('%o', 15);

'/result' = if ('17' == STR) 'OK';