#
# @expect="/nlist[@name='profile']/string[@name='result']='ok'"
# @format=pan
#
object template lowercase2;

'/result' = to_lowercase('ok');

