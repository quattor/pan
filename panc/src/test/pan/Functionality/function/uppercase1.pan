#
# @expect="/nlist[@name='profile']/string[@name='result']='OK'"
# @format=pan
#
object template uppercase1;

'/result' = to_uppercase('ok');

