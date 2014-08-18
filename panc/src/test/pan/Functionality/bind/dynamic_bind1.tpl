#
# @expect="/nlist[@name='profile']/nlist[@name='result']/string[@name='ok']='OK'"
# @format=pan
#
object template dynamic_bind1;

variable X = 'ok';

bind '/result/${X}' = string;

'/result/ok' = 'OK';

