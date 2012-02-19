#
# @expect="/nlist[@name='profile']/string[@name='result']='ok'"
# @format=pan
#
object template lowercase1;

'/result' = to_lowercase('OK');

