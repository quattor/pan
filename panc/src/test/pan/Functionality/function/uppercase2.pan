#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template uppercase2;

'/result' = to_uppercase('OK');

